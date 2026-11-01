package simple.library.wsd.xqrcode;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.library.wsd.xqrcode.internal.zxing.Result;
import simple.library.wsd.xqrcode.internal.zxing.ResultPoint;
import org.json.JSONObject;

/**
 * 一次成功识别的快照；图像及坐标均以本次提交的采样后图像为基准。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 识别结果 {
	private String text = "";
	private String format = "";
	private byte[] raw = new byte[0];
	private byte[] image = new byte[0];
	private float[] points = new float[0];
	private String metadata = "{}";
	private int bits;
	private long timestamp;

	/**
	 * 创建空结果；识别成功事件提供包含内容的结果。
	 */
	public 识别结果() {
	}

	/**
	 * 从解码器复制结果及图像，不向调用方暴露引擎类型。
	 *
	 * @param source 解码结果。
	 * @param png 本次识别图像的 PNG 数据，未请求时为空数组。
	 * @return 独立结果快照。
	 */
	public static 识别结果 from(Result source, byte[] png) {
		识别结果 value = new 识别结果();
		value.text = source.getText() == null ? "" : source.getText();
		value.format = source.getBarcodeFormat().name();
		value.raw = source.getRawBytes() == null ? new byte[0] : source.getRawBytes().clone();
		value.bits = source.getNumBits();
		value.timestamp = source.getTimestamp();
		value.image = png.clone();
		ResultPoint[] detected = source.getResultPoints();
		if (detected != null) {
			value.points = new float[detected.length * 2];
			for (int i = 0; i < detected.length; i++) {
				if (detected[i] != null) {
					value.points[i * 2] = detected[i].getX();
					value.points[i * 2 + 1] = detected[i].getY();
				}
			}
		}
		if (source.getResultMetadata() != null) {
			JSONObject json = new JSONObject();
			for (java.util.Map.Entry<?, ?> entry : source.getResultMetadata().entrySet()) {
				try {
					Object item = entry.getValue();
					if (item instanceof java.util.List) {
						org.json.JSONArray array = new org.json.JSONArray();
						for (Object part : (java.util.List<?>) item) {
							array.put(part instanceof byte[] ? android.util.Base64.encodeToString((byte[]) part, android.util.Base64.NO_WRAP) : String.valueOf(part));
						}
						json.put(entry.getKey().toString(), array);
					} else {
						json.put(entry.getKey().toString(), item);
					}
				} catch (org.json.JSONException error) {
					throw new IllegalStateException("无法序列化识别元数据", error);
				}
			}
			value.metadata = json.toString();
		}
		return value;
	}

	/**
	 * 解码后的文本内容。
	 *
	 * @return 解码后的文本内容。
	 */
	@SimpleProperty
	public String 内容() {
		return text;
	}


	/**
	 * ZXing 格式名，例如 QR_CODE、CODE_128。
	 *
	 * @return ZXing 格式名，例如 QR_CODE、CODE_128。
	 */
	@SimpleProperty
	public String 格式() {
		return format;
	}


	/**
	 * 原始编码字节的副本。
	 *
	 * @return 原始编码字节的副本。
	 */
	@SimpleProperty
	public byte[] 原始字节() {
		return raw.clone();
	}


	/**
	 * 原始编码的有效位数。
	 *
	 * @return 原始编码的有效位数。
	 */
	@SimpleProperty
	public int 有效位数() {
		return bits;
	}


	/**
	 * 解码时间戳，单位毫秒。
	 *
	 * @return 解码时间戳，单位毫秒。
	 */
	@SimpleProperty
	public long 时间戳() {
		return timestamp;
	}


	/**
	 * 识别图像的 PNG 数据副本；未请求时为空数组。
	 *
	 * @return 识别图像的 PNG 数据副本；未请求时为空数组。
	 */
	@SimpleProperty
	public byte[] 识别图像() {
		return image.clone();
	}


	/**
	 * 按 x、y 交替排列的识别点；坐标单位为识别图像像素。
	 *
	 * @return 按 x、y 交替排列的识别点；坐标单位为识别图像像素。
	 */
	@SimpleProperty
	public float[] 识别点() {
		return points.clone();
	}


	/**
	 * 元数据 JSON，字节段使用 Base64；没有附加信息时为 {}。
	 *
	 * @return 元数据 JSON，字节段使用 Base64；没有附加信息时为 {}。
	 */
	@SimpleProperty
	public String 附加信息() {
		return metadata;
	}

}
