package simple.library.wsd.xhttp2;

import io.reactivex.Observable;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 有限的内部 Retrofit 方法映射；请求头由本次请求的客户端拦截器应用。 保留 Response 而非仅返回响应文本，以传递错误状态、重复头和二进制内容。
 *
 * @author 树先生 xhwsd@qq.com
 */
interface HttpService {

	/**
	 * 创建 GET 请求的冷响应流，不在界面线程读取响应体。
	 *
	 * @param url 已经解析的请求地址。
	 * @return 创建 GET 请求的冷响应流，不在界面线程读取响应体。
	 */
	@Streaming
	@HTTP(method = "GET")
	Observable<Response<ResponseBody>> get(@Url String url);

	/**
	 * 创建 HEAD 请求的冷响应流，不在界面线程读取响应体。
	 *
	 * @param url 已经解析的请求地址。
	 * @return 创建 HEAD 请求的冷响应流，不在界面线程读取响应体。
	 */
	@Streaming
	@HTTP(method = "HEAD")
	Observable<Response<Void>> head(@Url String url);

	/**
	 * 创建 OPTIONS 请求的冷响应流，不在界面线程读取响应体。
	 *
	 * @param url  已经解析的请求地址。
	 * @param body 响应体或已构建的请求体。
	 * @return 创建 OPTIONS 请求的冷响应流，不在界面线程读取响应体。
	 */
	@Streaming
	@HTTP(
		method = "OPTIONS",
		hasBody = true
	)
	Observable<Response<ResponseBody>> options(@Url String url, @Body RequestBody body);

	/**
	 * 创建 POST 请求的冷响应流，不在界面线程读取响应体。
	 *
	 * @param url  已经解析的请求地址。
	 * @param body 响应体或已构建的请求体。
	 * @return 创建 POST 请求的冷响应流，不在界面线程读取响应体。
	 */
	@Streaming
	@HTTP(
		method = "POST",
		hasBody = true
	)
	Observable<Response<ResponseBody>> post(@Url String url, @Body RequestBody body);

	/**
	 * 创建 PUT 请求的冷响应流，不在界面线程读取响应体。
	 *
	 * @param url  已经解析的请求地址。
	 * @param body 响应体或已构建的请求体。
	 * @return 创建 PUT 请求的冷响应流，不在界面线程读取响应体。
	 */
	@Streaming
	@HTTP(
		method = "PUT",
		hasBody = true
	)
	Observable<Response<ResponseBody>> put(@Url String url, @Body RequestBody body);

	/**
	 * 创建 PATCH 请求的冷响应流，不在界面线程读取响应体。
	 *
	 * @param url  已经解析的请求地址。
	 * @param body 响应体或已构建的请求体。
	 * @return 创建 PATCH 请求的冷响应流，不在界面线程读取响应体。
	 */
	@Streaming
	@HTTP(
		method = "PATCH",
		hasBody = true
	)
	Observable<Response<ResponseBody>> patch(@Url String url, @Body RequestBody body);

	/**
	 * 创建 DELETE 请求的冷响应流，不在界面线程读取响应体。
	 *
	 * @param url  已经解析的请求地址。
	 * @param body 响应体或已构建的请求体。
	 * @return 创建 DELETE 请求的冷响应流，不在界面线程读取响应体。
	 */
	@Streaming
	@HTTP(
		method = "DELETE",
		hasBody = true
	)
	Observable<Response<ResponseBody>> delete(@Url String url, @Body RequestBody body);
}
