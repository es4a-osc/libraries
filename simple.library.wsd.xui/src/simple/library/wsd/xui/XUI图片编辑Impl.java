package simple.library.wsd.xui;

import android.graphics.Bitmap;
import android.view.View;

import com.xuexiang.xui.widget.imageview.edit.OnBitmapSaveListener;
import com.xuexiang.xui.widget.imageview.edit.OnPhotoEditorListener;
import com.xuexiang.xui.widget.imageview.edit.PhotoEditor;
import com.xuexiang.xui.widget.imageview.edit.PhotoEditorView;
import com.xuexiang.xui.widget.imageview.edit.PhotoFilter;
import com.xuexiang.xui.widget.imageview.edit.SaveSettings;
import com.xuexiang.xui.widget.imageview.edit.TextStyleBuilder;
import com.xuexiang.xui.widget.imageview.edit.ViewType;

import simple.runtime.components.组件容器;
import simple.runtime.errors.运行错误;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI图片编辑的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI图片编辑Impl extends XUI视图组件基类 implements XUI图片编辑 {

	private int outputFormat;
	private int outputQuality = 100;

	private int editCount;
	private PhotoFilter currentFilter;

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private PhotoEditorView view;

	// XUI 编辑引擎与当前编辑视图一一对应。
	private PhotoEditor editor;

	// 原生保存异步返回；切窗或销毁后不再写出旧请求或通知旧页面。
	private boolean disposed;
	private boolean saving;
	private long saveGeneration;
	private boolean trimTransparentBorder;

	/**
	 * 创建XUI图片编辑。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI图片编辑Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new PhotoEditorView(ComponentSupport.createContext());
		currentFilter = PhotoFilter.NONE;
		view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
			@Override
			public void onViewAttachedToWindow(View target) {
				// 再次进入时仍可编辑，但不会继续上一次离开的保存请求。
			}

			@Override
			public void onViewDetachedFromWindow(View target) {
				saveGeneration++;
				saving = false;
			}
		});

		editor = new PhotoEditor.Builder(view.getContext(), view).build();
		editor.setOnPhotoEditorListener(new OnPhotoEditorListener() {
			@Override
			public void onEditTextChangeListener(View target, String text, int color) {
				editingText = target;
				文字编辑请求(text, color);
			}

			@Override
			public void onAddViewListener(ViewType type, int count) {
				editCount = count;
				编辑对象改变(type.name(), count);
			}

			@Override
			public void onRemoveViewListener(ViewType type, int count) {
				editCount = count;
				if (editingText != null && editingText.getParent() != view) {
					editingText = null;
				}
				编辑对象改变(type.name(), count);
			}

			@Override
			public void onStartViewChangeListener(ViewType type) {
				// 画笔与贴图拖动归编辑器处理，避免外层滚动框在移动时抢走手势。
				if (view.getParent() != null) {
					view.getParent().requestDisallowInterceptTouchEvent(true);
				}
				对象拖动改变(type.name(), true);
			}

			@Override
			public void onStopViewChangeListener(ViewType type) {
				if (view.getParent() != null) {
					view.getParent().requestDisallowInterceptTouchEvent(false);
				}
				对象拖动改变(type.name(), false);
			}
		});

		return view;
	}

	@Override
	public void 置图片(String image) {
		view.getSource().setImageDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置画笔模式(boolean enabled) {
		editor.setBrushDrawingMode(enabled);
	}

	@Override
	public void 置画笔颜色(int color) {
		editor.setBrushColor(color);
	}

	@Override
	public void 置画笔粗细(float size) {
		if (!(size > 0) || Float.isInfinite(size)) {
			throw new 非法参数错误();
		}
		editor.setBrushSize(size);
	}

	@Override
	public void 添加文字(String text, int color) {
		editor.addText(text, color);
	}

	@Override
	public void 添加表情(String text) {
		editor.addEmoji(text);
	}

	@Override
	public boolean 撤销() {
		return editor.undo();
	}

	@Override
	public boolean 重做() {
		return editor.redo();
	}

	@Override
	public void 清空编辑() {
		editor.clearAllViews();
		editor.setFilterEffect(PhotoFilter.NONE);
		currentFilter = PhotoFilter.NONE;
		editCount = 0;
	}

	@Override
	public void 保存图片(String path) {
		if (disposed || saving || !view.isAttachedToWindow() || view.getWidth() <= 0 || view.getHeight() <= 0) {
			throw new 非法参数错误();
		}
		final String target = ComponentSupport.resolveOutputFile(path).getAbsolutePath();
		final boolean shouldClear = clearAfterSave;
		final boolean shouldTrim = trimTransparentBorder;
		final Bitmap.CompressFormat format = outputFormat == 0 ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
		final int quality = outputQuality;
		final long request = ++saveGeneration;
		saving = true;
		editor.saveAsBitmap(saveSettings, new OnBitmapSaveListener() {
			@Override
			public void onBitmapReady(Bitmap bitmap) {
				if (disposed || request != saveGeneration || !view.isAttachedToWindow()) {
					return;
				}
				Bitmap output = bitmap;
				try {
					if (shouldTrim) {
						output = ComponentSupport.trimTransparentEdges(bitmap);
					}
					ComponentSupport.saveImage(output, target, format, quality);
					if (shouldClear) {
						清空编辑();
					}
					saving = false;
					保存完成(target);
				} catch (运行错误 error) {
					saving = false;
					保存失败(error.getMessage());
				} finally {
					// 原图属于上游绘图缓存，只回收本次裁剪新建的结果。
					if (output != bitmap) {
						output.recycle();
					}
				}
			}

			@Override
			public void onFailure(Exception exception) {
				if (!disposed && request == saveGeneration) {
					saving = false;
					保存失败(exception.getMessage() == null ? exception.toString() : exception.getMessage());
				}
			}
		});
	}

	@Override
	public void 保存完成(String path) {
		if (!disposed) {
			EventDispatcher.dispatchEvent(this, "保存完成", path);
		}
	}

	@Override
	public void 保存失败(String message) {
		if (!disposed) {
			EventDispatcher.dispatchEvent(this, "保存失败", message);
		}
	}

	// 长按编辑仅保存当前文字对象，不把 Android View 暴露给 Simple。
	private View editingText;
	private boolean clearAfterSave;
	private SaveSettings saveSettings = new SaveSettings.Builder().setTransparencyEnabled(false).setClearViewsEnabled(false).build();

	@Override
	public void 添加贴图(String image) {
		editor.addImage(ComponentSupport.toBitmap(ComponentSupport.getDrawable(image)));
	}

	@Override
	public void 置画笔透明度(int opacity) {
		if (opacity < 0 || opacity > 100) {
			throw new 非法参数错误();
		}
		editor.setOpacity(opacity);
	}

	@Override
	public void 置橡皮擦粗细(float size) {
		if (!(size > 0) || Float.isInfinite(size)) {
			throw new 非法参数错误();
		}
		editor.setBrushEraserSize(size);
	}

	@Override
	public void 启用橡皮擦() {
		editor.brushEraser();
	}

	@Override
	public String[] 取滤镜名称() {
		PhotoFilter[] filters = PhotoFilter.values();
		String[] names = new String[filters.length];
		for (int i = 0; i < filters.length; i++) {
			names[i] = filters[i].name();
		}
		return names;
	}

	@Override
	public void 置滤镜(String name) {
		try {
			PhotoFilter filter = PhotoFilter.valueOf(name);
			editor.setFilterEffect(filter);
			currentFilter = filter;
		} catch (IllegalArgumentException exception) {
			throw new 非法参数错误();
		}
	}

	@Override
	public void 更新选中文字(String text, int color) {
		if (editingText == null || editingText.getParent() != view) {
			throw new 非法参数错误();
		}
		editor.editText(editingText, text, color);
		editingText = null;
	}

	@Override
	public void 置保存选项(boolean trimTransparentBorder, boolean clearAfterSave) {
		this.clearAfterSave = clearAfterSave;
		this.trimTransparentBorder = trimTransparentBorder;
		// 上游 transparency 实际表示裁边，且其纵向遍历使用了错误的宽高边界；本库独立裁边。
		saveSettings = new SaveSettings.Builder().setTransparencyEnabled(false)
			.setClearViewsEnabled(false).build();
	}

	@Override
	public void 文字编辑请求(String text, int color) {
		if (!disposed) {
			EventDispatcher.dispatchEvent(this, "文字编辑请求", text, color);
		}
	}

	@Override
	public void 编辑对象改变(String type, int count) {
		if (!disposed) {
			EventDispatcher.dispatchEvent(this, "编辑对象改变", type, count);
		}
	}

	@Override
	public void 对象拖动改变(String type, boolean started) {
		if (!disposed) {
			EventDispatcher.dispatchEvent(this, "对象拖动改变", type, started);
		}
	}

	@Override
	public void 销毁() {
		if (disposed) {
			return;
		}
		disposed = true;
		saveGeneration++;
		saving = false;
		editingText = null;
		editor.clearAllViews();
		super.销毁();
	}

	@Override
	public void 隐藏编辑框() {
		editor.clearHelperBox();
	}

	@Override
	public boolean 是否有编辑() {
		return editCount > 0 || currentFilter != PhotoFilter.NONE;
	}

	@Override
	public void 添加样式文字(String text, int color, float size, String font, int background, int gravity) {
		if (text == null || !(size > 0) || Float.isInfinite(size)) {
			throw new 非法参数错误();
		}
		TextStyleBuilder style = new TextStyleBuilder();
		style.withTextSize(size);
		style.withTextColor(color);
		style.withTextFont(ThemeSupport.loadFont(font));
		style.withBackgroundColor(background);
		style.withGravity(gravity);
		editor.addText(text, style);
	}

	@Override
	public void 置输出格式(int format, int quality) {
		if (format < 0 || format > 1 || quality < 0 || quality > 100) {
			throw new 非法参数错误();
		}
		outputFormat = format;
		outputQuality = quality;
	}

}
