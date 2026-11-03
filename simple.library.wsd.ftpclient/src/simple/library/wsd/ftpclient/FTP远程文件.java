package simple.library.wsd.ftpclient;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * FTP 或 SFTP 文件信息的独立快照；修改服务器文件须调用客户端方法。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FTP远程文件 {
	private final String name;
	private final long size;
	private final long modified;
	private final int kind;
	private final String owner;
	private final String group;
	private final String link;
	private final String raw;

	/**
	 * 建立独立的数据快照。
	 *
	 * @param name 条目名称。
	 * @param size 文件字节数。
	 * @param modified 修改时间的 Unix 毫秒值，未知为零。
	 * @param kind 0 文件、1 目录、2 链接、3 未知。
	 * @param owner 所有者。
	 * @param group 所属组。
	 * @param link 链接目标，未知为空文本。
	 * @param raw 服务器原始列表记录。
	 */
	FTP远程文件(
			String name,
			long size,
			long modified,
			int kind,
			String owner,
			String group,
			String link,
			String raw) {
		this.name = name;
		this.size = size;
		this.modified = modified;
		this.kind = kind;
		this.owner = owner;
		this.group = group;
		this.link = link;
		this.raw = raw;
	}

	/**
	 * 名称。
	 *
	 * @return 条目名称。
	 */
	@SimpleProperty
	public String 名称() {
		return name;
	}

	/**
	 * 尺寸。
	 *
	 * @return 文件字节数。
	 */
	@SimpleProperty
	public long 尺寸() {
		return size;
	}

	/**
	 * 修改时间。
	 *
	 * @return 修改时间的 Unix 毫秒值，未知为零。
	 */
	@SimpleProperty
	public long 修改时间() {
		return modified;
	}

	/**
	 * 类型。
	 *
	 * @return 0 文件、1 目录、2 链接、3 未知。
	 */
	@SimpleProperty
	public int 类型() {
		return kind;
	}

	/**
	 * 用户名。
	 *
	 * @return 所有者。
	 */
	@SimpleProperty
	public String 用户名() {
		return owner;
	}

	/**
	 * 隶属组。
	 *
	 * @return 所属组。
	 */
	@SimpleProperty
	public String 隶属组() {
		return group;
	}

	/**
	 * 链接目标。
	 *
	 * @return 链接目标，未知为空文本。
	 */
	@SimpleProperty
	public String 链接目标() {
		return link;
	}

	/**
	 * 原始信息。
	 *
	 * @return 服务器原始列表记录。
	 */
	@SimpleProperty
	public String 原始信息() {
		return raw;
	}

	/**
	 * 判断条目类型。
	 *
	 * @return 是否文件。
	 */
	@SimpleProperty
	public boolean 为文件() {
		return kind == 0;
	}

	/**
	 * 判断条目类型。
	 *
	 * @return 是否目录。
	 */
	@SimpleProperty
	public boolean 为目录() {
		return kind == 1;
	}

	/**
	 * 判断条目类型。
	 *
	 * @return 是否链接。
	 */
	@SimpleProperty
	public boolean 为链接() {
		return kind == 2;
	}

	/**
	 * 判断条目类型。
	 *
	 * @return 是否未知。
	 */
	@SimpleProperty
	public boolean 为未知() {
		return kind == 3;
	}
}
