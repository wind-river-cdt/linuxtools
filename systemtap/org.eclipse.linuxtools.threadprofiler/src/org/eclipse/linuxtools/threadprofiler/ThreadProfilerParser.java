package org.eclipse.linuxtools.threadprofiler;

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.callgraph.core.SystemTapParser;

public class ThreadProfilerParser extends SystemTapParser {
	
	@Override
	protected void initialize() {
	}

	@Override
	public IStatus nonRealTimeParsing() {
		return realTimeParsing();
	}
	int counter = 0;
	@Override
	public IStatus realTimeParsing() {
		if (!(internalData instanceof BufferedReader))
			return Status.CANCEL_STATUS;
		
		if (!(view instanceof ThreadProfilerView))
			return Status.CANCEL_STATUS;
		
		BufferedReader buff = (BufferedReader) internalData;

		String line;
		
		try {
			while ((line = buff.readLine()) != null) {
				if (line.equals("--"))
					((ThreadProfilerView) view).tick();
				if (line.contains(", ")) {
					String[] blargh = line.split(", ");
					((ThreadProfilerView) view).addDataPoints(counter++, blargh);
				} else {
					String[] data = line.split(":");
					//TODO: Log error
					try {
						int tid = Integer.parseInt(data[0]);
						((ThreadProfilerView) view).addThread(tid, data[1]);
					} catch (NumberFormatException e) {
						//Do nothing
					}
				}
			}
			view.update();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 

		return Status.OK_STATUS;
	}

}
