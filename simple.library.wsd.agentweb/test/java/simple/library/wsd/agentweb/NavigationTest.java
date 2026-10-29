package simple.library.wsd.agentweb;

import java.lang.reflect.Method;

/**
 * 校验主动加载和网页跳转共用的地址边界，覆盖本地页面及外部协议回归。
 * 此检查只执行纯地址规则，不创建 Android 视图或占用模拟器。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class NavigationTest {

	/**
	 * 静态检查入口，不创建实例。
	 */
	private NavigationTest() {
	}

	/**
	 * 检查本地链接不会再次被当作未知协议，且不会放行外部应用地址。
	 *
	 * @param arguments 未使用的命令行参数。
	 * @throws Exception 编译产物缺少规则方法或检查失败。
	 */
	public static void main(String[] arguments) throws Exception {
		Method rule = 浏览器Impl.class.getDeclaredMethod("isPageUrl", String.class);
		rule.setAccessible(true);
		String[] accepted = {
			"http://127.0.0.1:18764/page", "https://example.com/", "HTTPS://example.com/page?q=1#part",
			"file:///android_asset/agentweb_page2.html", "file:///data/user/0/app/files/page.html",
			"content://provider/document/12", "about:blank", "data:text/plain,hello",
			"blob:https://example.com/1234", "https://example.com/城市"
		};
		String[] rejected = {
			null, "", " ", "agentweb_page2.html", "http:", "https:example.com", "http://",
			"http://example.com/a b", "https://example.com/%ZZ", "javascript:alert(1)",
			"intent://example/#Intent;end", "tel:123", "mailto:test@example.com", "sms:123",
			"weixin://wap/pay", "alipays://test", "custom://page"
		};
		for (String url : accepted) {
			check(rule, url, true);
		}
		for (String url : rejected) {
			check(rule, url, false);
		}
		System.out.println("NavigationTest: " + (accepted.length + rejected.length) + " passed");
	}

	/**
	 * 将实际结果与公开的协议边界比较，失败时保留对应输入。
	 *
	 * @param rule 编译产物中的实际规则。
	 * @param url 待检查地址。
	 * @param expected 预期是否放行。
	 * @throws Exception 方法调用失败或结果不一致。
	 */
	private static void check(Method rule, String url, boolean expected) throws Exception {
		boolean actual = ((Boolean) rule.invoke(null, url)).booleanValue();
		if (actual != expected) {
			throw new AssertionError("地址规则不一致：" + url + ", expected=" + expected);
		}
	}
}
