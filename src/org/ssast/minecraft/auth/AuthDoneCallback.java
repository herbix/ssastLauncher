package org.ssast.minecraft.auth;

/**
 * 
 * @author Chaos
 * @since SSAST Launcher 1.3.2
 */
public interface AuthDoneCallback {

	/**
	 * When login is done, ServerAuth class object should call this
	 * method to make the runner do next operations.
	 * @param authObject ServerAuth object self
	 * @param succeed Whether the login is succeed
	 */
	public void authDone(ServerAuth authObject, boolean succeed);

}
