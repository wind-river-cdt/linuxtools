package org.eclipse.linuxtools.internal.cdt.autotools.core;

import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;

public interface IRemoteFileProxy {
	
	public URI toURI(IPath path);
	public URI toURI(String path);
	public IPath toPath(URI uri);
	public String getDirectorySeparator();
	public IFileStore getResource(String path);

}
