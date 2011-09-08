package org.eclipse.linuxtools.internal.cdt.autotools.core;

import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.resources.IProject;

public class RemoteProxyManager {
	
	private static RemoteProxyManager manager;
	private LocalFileProxy lfp;
	
	private RemoteProxyManager() {
		// do nothing
	}
	
	public static RemoteProxyManager getInstance() {
		if (manager == null)
			manager = new RemoteProxyManager();
		return manager;
	}
	
	LocalFileProxy getLocalFileProxy() {
		if (lfp == null)
			lfp = new LocalFileProxy();
		return lfp;
	}
	
	public IRemoteFileProxy getFileProxy(IProject project) {
		return getLocalFileProxy();
	}
	
	public IRemoteCommandLauncher getLauncher(IProject project) {
		return new LocalLauncher();
	}

	public String getOS(IProject project) {
		return Platform.getOS();
	}
}
