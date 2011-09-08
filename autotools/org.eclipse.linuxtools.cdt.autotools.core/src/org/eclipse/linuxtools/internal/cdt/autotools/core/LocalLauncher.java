package org.eclipse.linuxtools.internal.cdt.autotools.core;

import java.io.OutputStream;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class LocalLauncher implements IRemoteCommandLauncher {

	private CommandLauncher launcher;
	
	public LocalLauncher() {
		launcher = new CommandLauncher();
	}
	
	public Process execute(IPath commandPath, String[] args, String[] env,
			IPath changeToDirectory, IProgressMonitor monitor)
			throws CoreException {
		launcher.showCommand(true);
		return launcher.execute(commandPath, args, env, changeToDirectory, monitor);
	}

	public int waitAndRead(OutputStream output, OutputStream err,
			IProgressMonitor monitor) {
		return launcher.waitAndRead(output, err, monitor);
	}
	
	public String getErrorMessage() {
		return launcher.getErrorMessage();
	}

}
