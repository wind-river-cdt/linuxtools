package org.eclipse.linuxtools.internal.cdt.autotools.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class LocalFileProxy implements IRemoteFileProxy {

	public URI toURI(IPath path) {
		return path.toFile().toURI();
	}

	public URI toURI(String path) {
		try {
			return new URI(path);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public IPath toPath(URI uri) {
		return new Path(uri.getPath());
	}

	public String getDirectorySeparator() {
		// TODO Auto-generated method stub
		return System.getProperty("file.separator"); //$NON-NLS-1$
	}

	public IFileStore getResource(String path) {
		// TODO Auto-generated method stub
		return EFS.getLocalFileSystem().getStore(new Path(path));
	}

}
