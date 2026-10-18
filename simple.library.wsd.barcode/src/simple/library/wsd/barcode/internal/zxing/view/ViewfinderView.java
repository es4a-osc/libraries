package simple.library.wsd.barcode.internal.zxing.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import simple.library.wsd.barcode.internal.zxing.camera.CameraManager;
import com.google.zxing.ResultPoint;
import java.util.Collection;
import java.util.HashSet;

/**
 * 绘制 ZXing 扫描框、扫描线和定位点。
 */
public final class ViewfinderView extends View {
	private static final int[] SCANNER_ALPHA = new int[]{0, 64, 128, 192, 255, 192, 128, 64};
	private static final long ANIMATION_DELAY = 100L;
	private static final int OPAQUE = 255;
	private final Paint paint = new Paint();
	private Bitmap resultBitmap;
	private final int maskColor = 2131034135;
	private final int resultColor = -16776961;
	private final int frameColor = -16711936;
	private final int laserColor = -65536;
	private final int resultPointColor = -256;
	private int scannerAlpha = 0;
	private Collection<ResultPoint> possibleResultPoints = new HashSet<ResultPoint>(5);
	private Collection<ResultPoint> lastPossibleResultPoints;

	public ViewfinderView(Context context) {
		super(context);
	}

	@Override
	public void onDraw(Canvas canvas) {
		Rect frame = CameraManager.get().getFramingRect();
		if (frame == null) {
			return;
		}

		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// 先绘制扫描区域外的遮罩，扫描框内部留给结果图或扫描指示。
		this.paint.setColor(this.resultBitmap != null ? -16776961 : 2131034135);
		canvas.drawRect(0.0f, 0.0f, width, frame.top, this.paint);
		canvas.drawRect(0.0f, frame.top, frame.left, frame.bottom + 1, this.paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, this.paint);
		canvas.drawRect(0.0f, frame.bottom + 1, width, height, this.paint);
		if (this.resultBitmap != null) {
			this.paint.setAlpha(255);
			canvas.drawBitmap(this.resultBitmap, frame.left, frame.top, this.paint);
		} else {
			this.paint.setColor(-16711936);
			canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, this.paint);
			canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, this.paint);
			canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, this.paint);
			canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, this.paint);
			this.paint.setColor(-65536);
			this.paint.setAlpha(SCANNER_ALPHA[this.scannerAlpha]);
			this.scannerAlpha = (this.scannerAlpha + 1) % SCANNER_ALPHA.length;
			int middle = frame.height() / 2 + frame.top;
			canvas.drawRect(frame.left + frame.width() / 2 - 12, middle - 1, frame.left + frame.width() / 2 + 14, middle + 2, this.paint);
			int middle2 = frame.width() / 2 + frame.left;
			canvas.drawRect(middle2 - 1, frame.top + frame.height() / 2 - 12, middle2 + 2, frame.bottom - frame.height() / 2 + 14, this.paint);

			// 当前和上一轮候选点分开绘制，以透明度和半径表现短暂识别轨迹。
			Collection<ResultPoint> currentPossible = this.possibleResultPoints;
			Collection<ResultPoint> currentLast = this.lastPossibleResultPoints;

			if (currentPossible.isEmpty()) {
				this.lastPossibleResultPoints = null;
			} else {
				this.possibleResultPoints = new HashSet<ResultPoint>(5);
				this.lastPossibleResultPoints = currentPossible;
				this.paint.setAlpha(255);
				this.paint.setColor(-256);

				for (ResultPoint point : currentPossible) {
					canvas.drawCircle((float)frame.left + point.getX(), (float)frame.top + point.getY(), 6.0f, this.paint);
				}
			}

			if (currentLast != null) {
				this.paint.setAlpha(127);
				this.paint.setColor(-256);

				for (ResultPoint point : currentLast) {
					canvas.drawCircle((float)frame.left + point.getX(), (float)frame.top + point.getY(), 3.0f, this.paint);
				}
			}

			this.postInvalidateDelayed(100L, frame.left, frame.top, frame.right, frame.bottom);
		}
	}

	public void drawViewfinder() {
		this.resultBitmap = null;
		this.invalidate();
	}

	public void drawResultBitmap(Bitmap barcode) {
		this.resultBitmap = barcode;
		this.invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		this.possibleResultPoints.add(point);
	}
}
