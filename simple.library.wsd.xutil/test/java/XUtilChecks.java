package simple.library.wsd.xutil.test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.graphics.Color;
import android.system.Os;
import android.util.Log;

import simple.library.wsd.xutil.XUtilBCD;
import simple.library.wsd.xutil.XUtil几何;
import simple.library.wsd.xutil.XUtil剪贴板;
import simple.library.wsd.xutil.XUtil压缩;
import simple.library.wsd.xutil.XUtil大数;
import simple.library.wsd.xutil.XUtil字节集;
import simple.library.wsd.xutil.XUtil尺寸;
import simple.library.wsd.xutil.XUtil应用;
import simple.library.wsd.xutil.XUtil摘要;
import simple.library.wsd.xutil.XUtil文件;
import simple.library.wsd.xutil.XUtil文本;
import simple.library.wsd.xutil.XUtil日期;
import simple.library.wsd.xutil.XUtil正则;
import simple.library.wsd.xutil.XUtil电话;
import simple.library.wsd.xutil.XUtil缓存;
import simple.library.wsd.xutil.XUtil编码;
import simple.library.wsd.xutil.XUtil网络;
import simple.library.wsd.xutil.XUtil设备;
import simple.library.wsd.xutil.XUtil资源;
import simple.library.wsd.xutil.XUtil身份证;
import simple.library.wsd.xutil.XUtil通话;
import simple.library.wsd.xutil.XUtil配置;
import simple.library.wsd.xutil.XUtil键盘;
import simple.library.wsd.xutil.XUtil随机;
import simple.library.wsd.xutil.XUtil颜色;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 唯一 Simple 测试工程的原生断言；不进入 XUtil 类库交付。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtilChecks {

	private static final StringBuilder REPORT = new StringBuilder();
	private static int passed;
	private static int failed;
	private static volatile boolean complete;

	/**
	 * 测试入口只提供静态函数。
	 */
	private XUtilChecks() {
	}

	/**
	 * 执行界面线程与本地数据断言，返回逐组结果。
	 *
	 * @return 断言报告。
	 */
	@SimpleFunction
	public static String run() {
		REPORT.setLength(0);
		passed = 0;
		failed = 0;
		complete = false;
		group("文本与正则", new Runnable() {
			@Override
			public void run() {
				check(XUtil文本.是否空(null), "空引用");
				check(XUtil文本.是否空白(" \t\n"), "空白");
				equal(XUtil文本.去首尾空白(" 中 文 "), "中 文", "去首尾");
				equal(XUtil文本.去全部空白(" a\tb\nc "), "abc", "去空白");
				equal(XUtil文本.反转("中文abc"), "cba文中", "反转");
				equal(XUtil文本.首字母大写("abc"), "Abc", "首字母大写");
				equal(XUtil文本.首字母小写("Abc"), "abc", "首字母小写");
				check(XUtil文本.忽略大小写比较("ABC", "abc"), "不区分大小写");
				check(XUtil文本.比较版本("1.10", "1.2") > 0, "数字版本比较");
				check(XUtil文本.到整数("非法", 42) == 42, "整数缺省值");
				check(XUtil文本.到长整数("12345678900", 0) == 12345678900L, "长整数");
				check(XUtil文本.到小数("1.25", 0) == 1.25, "小数");
				check(XUtil正则.匹配("[0-9]+", "123"), "完整匹配");
				check(!XUtil正则.匹配("[0-9]+", "a123"), "匹配拒绝");
				check(Arrays.equals(XUtil正则.提取("[0-9]+", "a12b34"), new String[] { "12", "34" }), "提取");
				check(Arrays.equals(XUtil正则.分割("a,b,c", ","), new String[] { "a", "b", "c" }), "分割");
				equal(XUtil正则.替换首次("a1b2", "[0-9]", "X"), "aXb2", "替换首次");
				equal(XUtil正则.替换全部("a1b2", "[0-9]", "X"), "aXbX", "替换全部");
				check(XUtil正则.是否邮箱("test@example.com"), "邮箱");
				check(XUtil正则.是否网址("https://example.com"), "网址");
				check(XUtil正则.是否IP地址("127.0.0.1"), "IP");
				check(XUtil正则.是否手机号码("13800138000"), "手机格式");
				check(XUtil正则.是否中文("中文"), "中文");
			}
		});
		group("编码与摘要", new Runnable() {
			@Override
			public void run() {
				String text = "中文 + / 😀";
				byte[] bytes = XUtil编码.文本到字节集(text);
				equal(XUtil编码.字节集到文本(bytes), text, "UTF-8");
				check(Arrays.equals(XUtil编码.Base64解码(XUtil编码.Base64编码(bytes)), bytes), "Base64");
				equal(XUtil编码.URL解码(XUtil编码.URL编码(text)), text, "URL");
				equal(XUtil编码.HTML解码(XUtil编码.HTML编码("<a>&\"'")), "<a>&\"'", "HTML");
				equal(XUtil摘要.MD5("abc"), "900150983CD24FB0D6963F7D28E17F72", "MD5 向量");
				equal(XUtil摘要.SHA1("abc"), "A9993E364706816ABA3E25717850C26C9CD0D89D", "SHA1 向量");
				equal(XUtil摘要.SHA256("abc"), "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD", "SHA256 向量");
				check(XUtil摘要.SHA512("abc").length() == 128, "SHA512");
				equal(XUtil摘要.HMACSHA256("The quick brown fox jumps over the lazy dog", "key"),
						"F7BC83F430538424B13298E6AA6FB143EF4D59A14946175997479DBC2D1A3CD8", "HMAC 向量");
				check(XUtil摘要.HMACSHA512("abc", "key").length() == 128, "HMAC512");
			}
		});
		group("日期、颜色与尺寸", new Runnable() {
			@Override
			public void run() {
				equal(XUtil日期.格式化(0, "yyyy-MM-dd HH:mm:ss", "UTC"), "1970-01-01 00:00:00", "UTC 起点");
				equal(XUtil日期.格式化(0, "yyyy-MM-dd HH:mm:ss", "Asia/Shanghai"), "1970-01-01 08:00:00", "时区");
				check(XUtil日期.解析("1970-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss", "UTC") == 0, "日期解析");
				check(XUtil日期.是否今天(XUtil日期.取当前时间()), "今天");
				check(XUtil日期.取星期(0) >= 1 && XUtil日期.取星期(0) <= 7, "星期");
				check(!XUtil日期.取友好间隔(System.currentTimeMillis()).isEmpty(), "友好时间");
				check(XUtil颜色.从文本(XUtil颜色.到文本(0xff123456)) == 0xff123456, "ARGB 往返");
				check(XUtil颜色.是否深色(Color.BLACK) && !XUtil颜色.是否深色(Color.WHITE), "深色");
				check(Color.alpha(XUtil颜色.置透明比例(Color.RED, 0.5f)) == 127, "透明比例");
				check(Color.red(XUtil颜色.加深(Color.RED, 0.5f)) < 255, "加深");
				check(Color.red(XUtil颜色.变浅(0xff404040, 0.5f)) > 64, "变浅");
				check(Math.abs(XUtil尺寸.像素到DP(XUtil尺寸.DP到像素(50)) - 50) <= 1, "DP 往返");
				check(Math.abs(XUtil尺寸.像素到SP(XUtil尺寸.SP到像素(16)) - 16) <= 1, "SP 往返");
				check(XUtil尺寸.取屏幕宽度() > 0 && XUtil尺寸.取屏幕高度() > 0 && XUtil尺寸.取密度() > 0, "显示配置");
			}
		});
		group("私有配置与缓存", new Runnable() {
			@Override
			public void run() {
				check(XUtil配置.清空("checks"), "清空配置");
				check(XUtil配置.写文本("checks", "text", "中文"), "写配置文本");
				equal(XUtil配置.读文本("checks", "text", "缺省"), "中文", "读配置文本");
				check(XUtil配置.写整数("checks", "int", 23) && XUtil配置.读整数("checks", "int", 0) == 23, "整数配置");
				check(XUtil配置.写长整数("checks", "long", 12345678900L) && XUtil配置.读长整数("checks", "long", 0) == 12345678900L, "长整数配置");
				check(XUtil配置.写逻辑("checks", "bool", true) && XUtil配置.读逻辑("checks", "bool", false), "逻辑配置");
				check(XUtil配置.写单精度("checks", "float", 1.25f) && XUtil配置.读单精度("checks", "float", 0) == 1.25f, "单精度配置");
				check(XUtil配置.是否存在("checks", "text") && XUtil配置.删除("checks", "text"), "配置存在与删除");
				check(!XUtil配置.是否存在("checks", "text"), "删除已生效");
				XUtil缓存.清空("checks");
				XUtil缓存.写文本("checks", "empty", "", 0);
				equal(XUtil缓存.读文本("checks", "empty", "缺省"), "", "缓存空文本");
				XUtil缓存.写字节集("checks", "bytes", new byte[0], 0);
				check(XUtil缓存.读字节集("checks", "bytes", new byte[] { 1 }).length == 0, "缓存空字节集");
				XUtil缓存.写文本("checks", "Aa", "第一项", 0);
				XUtil缓存.写文本("checks", "BB", "第二项", 0);
				equal(XUtil缓存.读文本("checks", "Aa", "缺省"), "第一项", "常见哈希碰撞第一项");
				equal(XUtil缓存.读文本("checks", "BB", "缺省"), "第二项", "常见哈希碰撞第二项");
				check(XUtil缓存.取数量("checks") >= 4 && XUtil缓存.取字节数("checks") > 0, "缓存容量");
				check(XUtil缓存.删除("checks", "Aa"), "缓存删除");
				equal(XUtil缓存.读文本("checks", "Aa", "缺省"), "缺省", "缓存缺省");
				XUtil缓存.写文本("checks", "ttl", "待过期", 1);
			}
		});
		group("文件、ZIP 与路径边界", new Runnable() {
			@Override
			public void run() {
				try {
					fileChecks();
				} catch (Exception failure) {
					throw new IllegalStateException(failure);
				}
			}
		});
		group("附加模块与字节集", new Runnable() {
			@Override
			public void run() {
				check(XUtil随机.取整数(5, 5) == 5, "相等随机边界");
				for (int i = 0; i < 30; i++) {
					int value = XUtil随机.取整数(-3, 3);
					check(value >= -3 && value < 3, "随机范围");
				}
				check(XUtil随机.取数字文本(8).matches("[0-9]{8}"), "随机数字");
				check(XUtil随机.取字母文本(8).matches("[a-zA-Z]{8}"), "随机字母");
				check(XUtil随机.取大写字母(8).matches("[A-Z]{8}"), "随机大写");
				check(XUtil随机.取小写字母(8).matches("[a-z]{8}"), "随机小写");
				check(XUtil随机.取数字字母(8).matches("[a-zA-Z0-9]{8}"), "随机数字字母");
				equal(XUtil大数.加("0.1", "0.2"), "0.3", "十进制加");
				equal(XUtil大数.减("1.0", "0.3"), "0.7", "十进制减");
				equal(XUtil大数.乘("0.1", "0.2"), "0.02", "十进制乘");
				equal(XUtil大数.除("1", "3", 2), "0.33", "十进制除");
				equal(XUtil大数.舍入("1.235", 2), "1.24", "舍入");
				check(XUtil大数.比较("1.00", "1") == 0, "十进制比较");
				check(!XUtil大数.金额格式化("1234567.89", ",").isEmpty(), "金额格式");
				check(XUtil身份证.校验("11010519491231002X"), "身份证校验向量");
				check(!XUtil身份证.校验("110105194912310021"), "身份证错误校验位");
				equal(XUtil身份证.取生日("11010519491231002X"), "19491231", "生日");
				check(!XUtil身份证.取性别("11010519491231002X").isEmpty(), "性别");
				check(!XUtil身份证.取省份("11010519491231002X").isEmpty(), "省份");
				check(!XUtil身份证.脱敏("11010519491231002X").equals("11010519491231002X"), "脱敏");
				byte[] values = { 1, 2, 3, 2, 3 };
				check(Arrays.equals(XUtil字节集.拼接(new byte[] { 1 }, new byte[] { 2 }), new byte[] { 1, 2 }), "字节集拼接");
				check(XUtil字节集.查找(values, new byte[] { 2, 3 }) == 1, "字节集首次索引");
				check(XUtil字节集.反向查找(values, new byte[] { 2, 3 }) == 3, "字节集末次索引");
				check(Arrays.equals(XUtil字节集.切片(values, 1, 3), new byte[] { 2, 3 }), "字节集切片");
				equal(XUtilBCD.解码(XUtilBCD.编码("00123456")), "00123456", "BCD 前导零");
				check(Math.abs(XUtil几何.距离(0, 0, 3, 4) - 5) < 0.001f, "点距离");
				check(Arrays.equals(XUtil几何.中点(0, 0, 2, 4), new float[] { 1, 2 }), "中点");
				check(Arrays.equals(XUtil几何.插值(0, 0, 4, 8, 0.25f), new float[] { 1, 2 }), "插值");
			}
		});
		group("资源、设备与前台能力", new Runnable() {
			@Override
			public void run() {
				check(!XUtil应用.取包名().isEmpty() && !XUtil应用.取名称().isEmpty(), "应用名称");
				check(!XUtil应用.取版本名().isEmpty() && XUtil应用.取版本号() > 0, "应用版本");
				check(XUtil应用.是否已安装(XUtil应用.取包名()), "查询自身包");
				check(new File(XUtil应用.取安装路径()).isFile(), "安装路径");
				check(!XUtil设备.取品牌().isEmpty() && !XUtil设备.取制造商().isEmpty(), "设备厂商");
				check(!XUtil设备.取型号().isEmpty() && !XUtil设备.取硬件().isEmpty(), "设备型号");
				check(!XUtil设备.取系统版本().isEmpty() && XUtil设备.取系统API() >= 24, "设备系统");
				check(!XUtil设备.取AndroidID().isEmpty(), "应用范围标识");
				int textId = XUtil资源.取标识("xutil_test_message", "string");
				equal(XUtil资源.取文本(textId), "资源读取成功", "本库应用资源");
				check(Arrays.equals(XUtil资源.取文本数组(XUtil资源.取标识("xutil_test_words", "array")), new String[] { "甲", "乙" }), "文本数组资源");
				check(Arrays.equals(XUtil资源.取整数数组(XUtil资源.取标识("xutil_test_numbers", "array")), new int[] { 3, 5 }), "整数数组资源");
				check(XUtil资源.取尺寸像素(XUtil资源.取标识("xutil_test_size", "dimen")) > 0, "尺寸资源");
				equal(XUtil资源.读资产文本("hello.txt"), "资产中文\n第二行\n", "资产原换行");
				XUtil剪贴板.写文本("XUtil 中文剪贴板");
				equal(XUtil剪贴板.读文本(), "XUtil 中文剪贴板", "前台剪贴板");
				XUtil键盘.隐藏();
				XUtil键盘.是否显示();
				check(XUtil电话.取电话类型() >= 0, "电话类型");
				XUtil电话.是否电话设备();
				XUtil电话.SIM是否就绪();
				XUtil电话.取运营商();
				XUtil网络.是否已连接();
				XUtil网络.是否可访问互联网();
				XUtil网络.是否无线网络();
				XUtil网络.是否移动网络();
				XUtil网络.是否以太网();
				XUtil网络.是否VPN();
				XUtil网络.是否按流量计费();
				check(true, "网络能力调用完成");
			}
		});
		group("无效输入及授权边界", new Runnable() {
			@Override
			public void run() {
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil日期.解析("2023-02-29", "yyyy-MM-dd", "UTC");
					}
				}, "非法日期");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil日期.解析("2024-01-01x", "yyyy-MM-dd", "UTC");
					}
				}, "未消费日期尾部");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil日期.格式化(0, "yyyy", "Not/AZone");
					}
				}, "非法时区");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil配置.清空("../outside");
					}
				}, "配置路径逃逸");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil缓存.清空("../outside");
					}
				}, "缓存路径逃逸");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil缓存.写文本("checks", "k", "v", -1);
					}
				}, "负有效期");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil颜色.置透明比例(0, Float.NaN);
					}
				}, "非法比例");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil随机.取整数(3, 1);
					}
				}, "倒置范围");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil大数.除("1", "0", 2);
					}
				}, "除零");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil字节集.切片(new byte[1], 0, 2);
					}
				}, "数组越界");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtilBCD.编码("12X4");
					}
				}, "非数字 BCD");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtilBCD.编码("123");
					}
				}, "奇数 BCD");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtilBCD.解码(new byte[] { (byte) 0xab });
					}
				}, "BCD 半字节");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil文件.读文本("/does-not-exist/xutil");
					}
				}, "缺失文件");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil资源.读资产文本("does-not-exist.txt");
					}
				}, "缺失资产");
				// 不自动拨出电话；只有未授权时验证拒绝分支，授权由独立按钮验证。
				if (!XUtil通话.是否已授权()) {
					try {
						XUtil通话.拨打("5551234");
						check(false, "未授权直接通话必须拒绝");
					} catch (SecurityException expected) {
						check(true, "未授权直接通话拒绝");
					}
				}
			}
		});
		return report();
	}

	/**
	 * 在 Simple 工作线程回调中检查 DNS 结果、缓存过期和界面线程限制。
	 *
	 * @param addresses Simple 直接调用域名函数集得到的地址。
	 */
	@SimpleFunction
	public static void background(final String[] addresses) {
		group("后台域名与有效期", new Runnable() {
			@Override
			public void run() {
				check(addresses != null && addresses.length > 0, "localhost 域名解析");
				reject(new Runnable() {
					@Override
					public void run() {
						XUtil键盘.隐藏();
					}
				}, "后台界面操作拒绝");
				try {
					Thread.sleep(1500);
				} catch (InterruptedException failure) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException(failure);
				}
				equal(XUtil缓存.读文本("checks", "ttl", "已过期"), "已过期", "缓存秒级过期");
			}
		});
		complete = true;
		Log.i("ES4A-XUtil", report());
	}

	/**
	 * 取得可供定时器读取的后台完成状态。
	 *
	 * @return 后台是否结束。
	 */
	@SimpleFunction
	public static boolean isComplete() {
		return complete;
	}

	/**
	 * 记录 Simple 工作线程在调用原生后台检查前发生的错误。
	 *
	 * @param message 错误说明。
	 */
	@SimpleFunction
	public static void failBackground(String message) {
		failed++;
		REPORT.append(message).append('\n');
		complete = true;
		Log.e("ES4A-XUtil", message);
	}

	/**
	 * 返回最新的逐组结果。
	 *
	 * @return 报告文本。
	 */
	@SimpleFunction
	public static synchronized String report() {
		return "通过 " + passed + " 项，失败 " + failed + " 项\n" + REPORT;
	}

	/**
	 * 为每组隔离异常，避免一个失败掩盖后续功能。
	 *
	 * @param name 用例组。
	 * @param action 操作。
	 */
	private static void group(String name, Runnable action) {
		int before = failed;
		try {
			action.run();
		} catch (RuntimeException | AssertionError failure) {
			failed++;
			Log.e("ES4A-XUtil", name, failure);
			REPORT.append(name).append("：").append(failure).append('\n');
		}
		REPORT.append(name).append(failed == before ? "：通过\n" : "：失败\n");
	}

	/**
	 * 记录断言失败并保留可定位的说明。
	 *
	 * @param value 条件。
	 * @param name 断言名称。
	 */
	private static void check(boolean value, String name) {
		if (!value) {
			throw new AssertionError(name);
		}
		passed++;
	}

	/**
	 * 比较文本，不改变大小写等实际返回内容。
	 *
	 * @param actual 实际值。
	 * @param expected 预期值。
	 * @param name 断言名称。
	 */
	private static void equal(String actual, String expected, String name) {
		check(expected.equals(actual), name + "，实际：" + actual);
	}

	/**
	 * 确认非法输入被拒绝。
	 *
	 * @param action 非法调用。
	 * @param name 用例名。
	 */
	private static void reject(Runnable action, String name) {
		try {
			action.run();
		} catch (IllegalArgumentException | IllegalStateException | ArithmeticException expected) {
			check(true, name);
			return;
		}
		check(false, name + "未被拒绝");
	}

	/**
	 * 在应用私有目录验证正常 ZIP、恶意条目、已有文件和链接目标。
	 *
	 * @throws Exception 文件或链接操作失败。
	 */
	private static void fileChecks() throws Exception {
		File root = new File(XUtil文件.取私有目录(), "xutil_checks");
		if (root.exists()) {
			check(XUtil文件.删除(root.getPath()), "清理上次测试");
		}
		check(XUtil文件.创建目录(root.getPath()), "创建目录");
		final String file = new File(root, "source/中文.txt").getPath();
		XUtil文件.写文本(file, "中文\n", false);
		XUtil文件.写文本(file, "附加\n", true);
		equal(XUtil文件.读文本(file), "中文\n附加\n", "文件 UTF-8 追加");
		check(XUtil文件.是否存在(file) && XUtil文件.是否文件(file), "文件存在");
		check(XUtil文件.是否目录(root.getPath()), "目录存在");
		check(XUtil文件.取文件长度(file) == "中文\n附加\n".getBytes(StandardCharsets.UTF_8).length, "文件字节数");
		check(XUtil文件.取文件长度("https://127.0.0.1:1/xutil") == -1, "文件长度不隐式联网");
		check(XUtil文件.创建文件(new File(root, "empty").getPath()), "创建空文件");
		check(XUtil文件.列出(root.getPath()).length == 2, "列出目录");
		check(!XUtil文件.取缓存目录().isEmpty(), "私有缓存目录");
		check(XUtil摘要.文件MD5(file).length() == 32, "文件 MD5");
		String zip = new File(root, "data.zip").getPath();
		check(XUtil压缩.压缩(new File(root, "source").getPath(), zip), "压缩");
		check(XUtil压缩.列出(zip).length > 0, "归档条目");
		String[] files = XUtil压缩.解压(zip, new File(root, "out").getPath(), 1024);
		check(files.length == 1, "解压文件数");
		equal(XUtil文件.读文本(files[0]), "中文\n附加\n", "解压内容");
		for (String entry : new String[] { "../escaped.txt", "/absolute.txt", "..\\escaped.txt", "C:/escaped.txt" }) {
			final String malicious = zip(root, "bad.zip", entry, "x");
			final String target = new File(root, "rejected").getPath();
			reject(new Runnable() {
					@Override
					public void run() {
						XUtil压缩.解压(malicious, target, 1024);
					}
				}, "归档越界 " + entry);
		}
		final String limited = zip(root, "limited.zip", "large.txt", "0123456789");
		reject(new Runnable() {
					@Override
					public void run() {
						XUtil压缩.解压(limited, new File(root, "limit").getPath(), 3);
					}
				}, "解压上限");
		final String overwrite = zip(root, "overwrite.zip", "existing.txt", "overwrite");
		final File target = new File(root, "overwrite");
		XUtil文件.写文本(new File(target, "existing.txt").getPath(), "original", false);
		reject(new Runnable() {
					@Override
					public void run() {
						XUtil压缩.解压(overwrite, target.getPath(), 1024);
					}
				}, "不覆盖已有文件");
		equal(XUtil文件.读文本(new File(target, "existing.txt").getPath()), "original", "已有文件保留");
		File link = new File(root, "linked");
		Os.symlink(target.getPath(), link.getPath());
		try {
			final String linked = link.getPath();
			reject(new Runnable() {
					@Override
					public void run() {
						XUtil压缩.解压(overwrite, linked, 1024);
					}
				}, "拒绝链接目标");
		} finally {
			check(link.delete(), "清理测试链接");
		}
		check(!new File(root, "escaped.txt").exists(), "没有越界写入");
	}

	/**
	 * 构造包含指定名称的 ZIP，供边界用例消费。
	 *
	 * @param root 私有目录。
	 * @param file 归档文件名。
	 * @param entry 条目名。
	 * @param text 条目内容。
	 * @return ZIP 路径。
	 * @throws Exception 写入失败。
	 */
	private static String zip(File root, String file, String entry, String text) throws Exception {
		File destination = new File(root, file);
		try (ZipOutputStream output = new ZipOutputStream(new FileOutputStream(destination))) {
			output.putNextEntry(new ZipEntry(entry));
			output.write(text.getBytes(StandardCharsets.UTF_8));
			output.closeEntry();
		}
		return destination.getPath();
	}
}
