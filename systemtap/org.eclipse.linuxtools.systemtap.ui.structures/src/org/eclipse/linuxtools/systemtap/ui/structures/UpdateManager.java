/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.structures;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IUpdateListener;



public class UpdateManager {
	public UpdateManager(int delay) {
		updateListeners = new ArrayList<IUpdateListener>();
		stopped = false;
		disposed = false;
		timer = new Timer("Update Manager", true);
		timer.scheduleAtFixedRate(new Notify(), delay, delay);
	}
	
	/**
	 * Terminates the timer and removes all update listeners.
	 */
	public void stop() {
		if(!stopped) {
			stopped = true;
			timer.cancel();
			for(int i=0; i<updateListeners.size(); i++)
				removeUpdateListener((IUpdateListener)updateListeners.get(i));
		}
	}
	
	public void addUpdateListener(IUpdateListener l) {
		if(!updateListeners.contains(l))
			updateListeners.add(l);
	}
	public void removeUpdateListener(IUpdateListener l) {
		if(updateListeners.contains(l))
			updateListeners.remove(l);
	}
	
	public boolean isRunning() {
		return !stopped;
	}
	
	public void dispose() {
		if(!disposed) {
			disposed = true;
			stop();
			timer = null;
			updateListeners = null;
		}
	}
	
	/**
	 * Handle any events that are timed to occur.
	 */
	private class Notify extends TimerTask {
		public void run() {
			try{
			if(!stopped) {
				for(int i = 0; i < updateListeners.size(); i++)
					((IUpdateListener)(updateListeners.get(i))).handleUpdateEvent();
			}
			}catch(Exception e) {;}
		}
			
	}
	
	private Timer timer;
	private ArrayList<IUpdateListener> updateListeners;
	private boolean stopped;
	private boolean disposed;
}
