/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.autotools.ui.editors.automake;

import java.io.File;

public class Target implements ITarget {

	String target;

	public Target(String t) {
		target = t;
	}

	public String toString() {
		return target;
	}

	public boolean exits() {
		return new File(target).exists();
	}

	public long lastModified() {
		return new File(target).lastModified();
	}
}
