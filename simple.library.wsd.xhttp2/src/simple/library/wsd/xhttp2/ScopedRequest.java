package simple.library.wsd.xhttp2;

import com.xuexiang.xhttp2.request.CustomRequest;

import okhttp3.OkHttpClient;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 使用 XHttp2 原生错误、重试及调度链，但不使用其可变的全局 Retrofit/OkHttp 构建器。 每次请求持有独立的 Retrofit
 * 与客户端配置；响应体在工作线程读取完后才交付界面。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ScopedRequest extends CustomRequest {

	/**
	 * 设置本次独立客户端和重试参数，保持响应体在工作线程读取。
	 *
	 * @param client        客户端配置或次级默认值。
	 * @param retries       最大额外重试次数。
	 * @param retryDelay    首次重试等待的毫秒数。
	 * @param retryIncrease 后续每次追加的等待毫秒数。
	 */
	ScopedRequest(OkHttpClient client, int retries, long retryDelay, long retryIncrease) {
		mOkHttpClient = client;
		mRetryCount = retries;
		if (retryDelay > Integer.MAX_VALUE || retryIncrease > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("重试间隔和增量不能超过整数毫秒范围。");
		}
		mRetryDelay = (int) retryDelay;
		mRetryIncreaseDelay = (int) retryIncrease;
		// 工作线程的订阅由所属任务安排；不把 Retrofit 的原始响应体切到界面线程读取。
		mIsSyncRequest = true;
		mIsOnMainThread = false;
	}

	@Override
	public CustomRequest build() {
		if (mRetrofit == null) {
			mRetrofit = new Retrofit.Builder().baseUrl("https://localhost/").client(mOkHttpClient)
					.addConverterFactory(GsonConverterFactory.create())
					.addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
		}
		return this;
	}
}
