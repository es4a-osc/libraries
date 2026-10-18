package simple.library.wsd.barcode.internal.zxing.view;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;

/**
 * 将解码器发现的定位点交给扫描框绘制。
 */
public final class ViewfinderResultPointCallback implements ResultPointCallback {
	private final ViewfinderView viewfinderView;

	public ViewfinderResultPointCallback(ViewfinderView viewfinderView) {
		this.viewfinderView = viewfinderView;
	}

	@Override
	public void foundPossibleResultPoint(ResultPoint point) {
		this.viewfinderView.addPossibleResultPoint(point);
	}
}
