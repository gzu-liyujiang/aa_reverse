package jadx.core.dex.nodes;

import jadx.exception.DecodeException;

public interface ILoadable {

	/**
	 * On demand loading
	 *
	 * @throws DecodeException
	 */
	void load() throws DecodeException;

	/**
	 * Free resources
	 */
	void unload();

}
