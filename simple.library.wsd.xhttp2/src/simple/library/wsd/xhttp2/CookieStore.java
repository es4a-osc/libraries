package simple.library.wsd.xhttp2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * 会话专属 CookieJar，按名称、域和路径区分记录，并执行过期、Secure 和域/路径匹配。
 * 仅在启用持久化时保存有有效期的 Cookie，会话 Cookie 始终只留在内存。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class CookieStore implements CookieJar {

	private final Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();
	private final SharedPreferences preferences;
	private final Gson gson = new Gson();

	CookieStore(SharedPreferences preferences) {
		this.preferences = preferences;
		if (preferences != null) {
			String data = preferences.getString("cookies", "[]");
			StoredCookie[] stored = gson.fromJson(data, StoredCookie[].class);
			if (stored == null) {
				throw new IllegalStateException("Cookie 持久化内容无效。");
			}
			for (StoredCookie item : stored) {
				Cookie cookie = item.cookie();
				if (cookie.expiresAt() > System.currentTimeMillis()) {
					cookies.put(key(cookie), cookie);
				}
			}
		}
	}

	@Override
	public synchronized void saveFromResponse(HttpUrl url, List<Cookie> values) {
		Map<String, Cookie> next = new LinkedHashMap<String, Cookie>(cookies);
		for (Cookie cookie : values) {
			if (cookie.expiresAt() <= System.currentTimeMillis()) {
				next.remove(key(cookie));
			} else {
				next.put(key(cookie), cookie);
			}
		}
		commit(next);
	}

	@Override
	public synchronized List<Cookie> loadForRequest(HttpUrl url) {
		List<Cookie> result = new ArrayList<Cookie>();
		for (Cookie cookie : cookies.values()) {
			if (cookie.expiresAt() > System.currentTimeMillis() && cookie.matches(url)) {
				result.add(cookie);
			}
		}
		// 长路径优先，保留同名但不同路径的 Cookie；不通过名称去重改变服务端语义。
		Collections.sort(result, new Comparator<Cookie>() {
			@Override
			public int compare(Cookie first, Cookie second) {
				return Integer.compare(second.path().length(), first.path().length());
			}
		});
		return result;
	}

	synchronized void put(HttpUrl url, String header) {
		Cookie cookie = Cookie.parse(url, header);
		if (cookie == null) {
			throw new IllegalArgumentException("Set-Cookie 内容或所属地址无效。");
		}
		List<Cookie> one = new ArrayList<Cookie>();
		one.add(cookie);
		saveFromResponse(url, one);
	}

	synchronized boolean remove(String name, String domain, String path) {
		Map<String, Cookie> next = new LinkedHashMap<String, Cookie>(cookies);
		boolean removed = false;
		Iterator<Cookie> iterator = next.values().iterator();
		while (iterator.hasNext()) {
			Cookie cookie = iterator.next();
			if (cookie.name().equals(name) && cookie.domain().equalsIgnoreCase(domain) && cookie.path().equals(path)) {
				iterator.remove();
				removed = true;
			}
		}
		commit(next);
		return removed;
	}

	synchronized void clear() {
		commit(new LinkedHashMap<String, Cookie>());
	}

	/**
	 * 先成功写入持久化，再替换内存，失败不会留下“已保存”的假象。
	 */
	private void commit(Map<String, Cookie> next) {
		if (preferences != null) {
			List<StoredCookie> persistent = new ArrayList<StoredCookie>();
			for (Cookie cookie : next.values()) {
				if (cookie.persistent() && cookie.expiresAt() > System.currentTimeMillis()) {
					persistent.add(new StoredCookie(cookie));
				}
			}
			if (!preferences.edit().putString("cookies", gson.toJson(persistent)).commit()) {
				throw new IllegalStateException("Cookie 持久化写入失败。");
			}
		}
		cookies.clear();
		cookies.putAll(next);
	}

	private static String key(Cookie cookie) {
		return cookie.name() + "\n" + cookie.domain() + "\n" + cookie.path();
	}

	/**
	 * 保留 hostOnly，不能通过 toString/parse 往返将仅当前主机的 Cookie 扩大到子域。
	 */
	private static final class StoredCookie {
		String name;
		String value;
		String domain;
		String path;
		long expires;
		boolean secure;
		boolean httpOnly;
		boolean hostOnly;

		StoredCookie(Cookie cookie) {
			name = cookie.name();
			value = cookie.value();
			domain = cookie.domain();
			path = cookie.path();
			expires = cookie.expiresAt();
			secure = cookie.secure();
			httpOnly = cookie.httpOnly();
			hostOnly = cookie.hostOnly();
		}

		Cookie cookie() {
			Cookie.Builder builder = new Cookie.Builder().name(name).value(value).path(path).expiresAt(expires);
			if (hostOnly) {
				builder.hostOnlyDomain(domain);
			} else {
				builder.domain(domain);
			}
			if (secure) {
				builder.secure();
			}
			if (httpOnly) {
				builder.httpOnly();
			}
			return builder.build();
		}
	}
}
