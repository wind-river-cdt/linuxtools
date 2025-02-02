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

import java.util.ArrayList;

public abstract class Rule extends Parent implements IRule {

	Target target;

	public Rule(Directive parent, Target tgt) {
		this(parent, tgt, new Command[0]);
	}

	public Rule(Directive parent, Target tgt, Command[] cmds) {
		super(parent);
		target = tgt;
		addDirectives(cmds);
	}

	public ICommand[] getCommands() {
		IDirective[] directives = getDirectives();
		ArrayList<IDirective> cmds = new ArrayList<IDirective>(directives.length);
		for (int i = 0; i < directives.length; i++) {
			if (directives[i] instanceof ICommand) {
				cmds.add(directives[i]);
			}
		}
		return (ICommand[])cmds.toArray(new ICommand[0]);
	}

	public ITarget getTarget() {
		return target;
	}

	public void setTarget(Target tgt) {
		target = tgt;
	}

	public boolean equals(Object r) {
		if (r instanceof Rule)
			return ((Rule)r).getTarget().equals(getTarget());
		return false;
	}
	
	public int hashCode() {
		return getTarget().hashCode();
	}

}
