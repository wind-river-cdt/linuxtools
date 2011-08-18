package org.eclipse.linuxtools.internal.cdt.autotools.core;

import org.eclipse.core.resources.IProject;

public class FileProxyManager {
	
	private static FileProxyManager manager;
	private LocalFileProxy lfp;
	
	private FileProxyManager() {
		// do nothing
	}
	
	public static FileProxyManager getInstance() {
		if (manager == null)
			manager = new FileProxyManager();
		return manager;
	}
	
	LocalFileProxy getLocalFileProxy() {
		if (lfp == null)
			lfp = new LocalFileProxy();
		return lfp;
	}
	
	IRemoteFileProxy getFileProxy(IProject project) {
		return getLocalFileProxy();
	}

}
