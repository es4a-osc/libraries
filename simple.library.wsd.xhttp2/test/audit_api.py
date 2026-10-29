"""核对编译后真实注解接口与清单，检查双向遗漏、类型、常量值及静态标记。"""

from pathlib import Path
import json
import re
import subprocess


LIB = Path(__file__).resolve().parent.parent
SDK = LIB.parents[1] / "sdk"
JDK = SDK / "tools/jdk-11.0.32.1/bin"
OUTPUT = LIB / "test/build/verification/api-audit"
TYPES = {"整数型": "int", "长整数型": "long", "单精度小数型": "float", "双精度小数型": "double",
		 "逻辑型": "boolean", "文本型": "java.lang.String", "字节型": "byte", "变体型": "simple.runtime.variants.Variant"}


def java_type(value):
	return java_type(value[:-2]) + "[]" if value.endswith("[]") else TYPES.get(value, value)


def run(arguments):
	result = subprocess.run([str(value) for value in arguments], capture_output=True, text=True, encoding="utf8", errors="replace")
	if result.returncode:
		raise RuntimeError(result.stdout + result.stderr)
	return result.stdout


def main():
	OUTPUT.mkdir(parents=True, exist_ok=True)
	manifest = json.loads((LIB / "library.json").read_text(encoding="utf8"))
	definitions = [item for category in manifest["categories"] for item in category["definitions"]]
	names = [item["type"] for item in definitions]
	classpath = [str(LIB / "classes.jar")] + [item["path"] for item in json.loads(
		(LIB / "build/ide/classpath.json").read_text(encoding="utf8"))["libraries"]]
	source = OUTPUT / "ApiInventory.java"
	source.write_text('''import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleProperty;

/** 只读取编译后公开注解；不构造 Android 组件。 */
public final class ApiInventory {
	public static void main(String[] names) throws Exception {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (String name : names) {
			Class<?> type = Class.forName(name, false, ApiInventory.class.getClassLoader());
			List<Map<String, Object>> members = new ArrayList<Map<String, Object>>();
			for (Method method : type.getDeclaredMethods()) {
				String kind = method.isAnnotationPresent(SimpleProperty.class) ? "properties"
					: method.isAnnotationPresent(SimpleEvent.class) ? "events"
					: method.isAnnotationPresent(SimpleFunction.class) ? "functions" : "";
				if (kind.isEmpty()) { continue; }
				Map<String, Object> item = new LinkedHashMap<String, Object>();
				item.put("name", method.getName());
				item.put("kind", kind);
				item.put("return", method.getReturnType().getCanonicalName());
				List<String> params = new ArrayList<String>();
				for (Class<?> param : method.getParameterTypes()) { params.add(param.getCanonicalName()); }
				item.put("params", params);
				item.put("static", Modifier.isStatic(method.getModifiers()));
				members.add(item);
			}
			for (Field field : type.getDeclaredFields()) {
				if (!field.isAnnotationPresent(SimpleDataElement.class)) { continue; }
				Map<String, Object> item = new LinkedHashMap<String, Object>();
				item.put("name", field.getName());
				item.put("kind", "constants");
				item.put("type", field.getType().getCanonicalName());
				item.put("value", field.get(null));
				members.add(item);
			}
			result.put(name, members);
		}
		System.out.println(new Gson().toJson(result));
	}
}
''', encoding="utf8")
	run([JDK / "javac.exe", "-encoding", "UTF-8", "-source", "11", "-target", "11", "-Xlint:all,-options", "-Werror",
		 "-classpath", ";".join(classpath), "-d", OUTPUT, source])
	actual = json.loads(run([JDK / "java.exe", "-Dfile.encoding=UTF-8", "-cp", ";".join([str(OUTPUT)] + classpath),
							 "ApiInventory", *names]))
	errors = []
	checked = 0
	for definition in definitions:
		members = actual[definition["type"]]
		consumed = set()
		for kind in ("functions", "events", "properties", "constants"):
			seen = set()
			for item in definition.get(kind, []):
				identity = item["name"]
				if identity in seen:
					errors.append(f'{definition["name"]}.{identity}: 清单重复')
				seen.add(identity)
				matches = []
				for index, member in enumerate(members):
					if member["kind"] != kind or member["name"] != identity:
						continue
					if kind == "constants":
						expected = json.loads(item["value"])
						valid = member["type"] == java_type(item["type"]) and member["value"] == expected
					elif kind == "properties":
						value_type = java_type(item["type"])
						valid = ((not member["params"] and member["return"] == value_type)
								 or (item.get("writable", False) and member["params"] == [value_type] and member["return"] == "void"))
					else:
						valid = (member["params"] == [java_type(param["type"]) for param in item.get("params", [])]
								 and member["return"] == java_type(item.get("return", "void"))
								 and member["static"] == item.get("static", False))
					if valid:
						consumed.add(index)
						matches.append(member)
				expected_count = 2 if kind == "properties" and item.get("writable", False) else 1
				if len(matches) != expected_count:
					errors.append(f'{definition["name"]}.{identity}: 期望 {expected_count} 个真实注解成员，得到 {len(matches)}')
				for param in item.get("params", []):
					if not re.search(r"[\u4e00-\u9fff]", param["name"]):
						errors.append(f'{definition["name"]}.{identity}: 参数名未中文化 {param["name"]}')
				checked += 1
		for index, member in enumerate(members):
			if index not in consumed:
				errors.append(f'{definition["name"]}.{member["name"]}: 编译后注解未对应清单 {member["kind"]}')
	report = {"definitions": len(definitions), "manifest_members": checked,
			  "compiled_members": sum(len(value) for value in actual.values()), "errors": errors}
	(OUTPUT / "report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf8")
	print(json.dumps(report, ensure_ascii=False, indent=2))
	if errors:
		raise SystemExit(1)


if __name__ == "__main__":
	main()
