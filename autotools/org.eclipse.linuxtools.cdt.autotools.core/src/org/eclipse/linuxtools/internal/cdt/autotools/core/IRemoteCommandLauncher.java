package org.eclipse.linuxtools.internal.cdt.autotools.core;

import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IRemoteCommandLauncher {
	
	public final static int OK = 0;
	
	public Process execute(IPath commandPath, String[] args, String[] env, IPath changeToDirectory, IProgressMonitor monitor) throws CoreException;
	public int waitAndRead(OutputStream output, OutputStream err, IProgressMonitor monitor);
	public String getErrorMessage();
	
}