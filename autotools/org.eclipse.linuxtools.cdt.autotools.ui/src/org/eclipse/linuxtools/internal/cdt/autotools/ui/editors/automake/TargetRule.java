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

/**
 * Makefile : ( statement ) *
 * statement :   rule | macro_definition | comments | empty
 * rule :  inference_rule | target_rule
 * inference_rule : target ':' <nl> ( <tab> command <nl> ) +
 * target_rule : target [ ( target ) * ] ':' [ ( prerequisite ) * ] [ ';' command ] <nl> 
                 [ ( <tab> prefix_command command ) * ]
 * macro_definition : string '=' (string)* 
 * comments : '#' (string) *
 * empty : <nl>
 * command : string <nl>
 * target : string
 * prefix_command : '-' | '@' | '+'
 * internal_macro :  "$<" | "$*" | "$@" | "$?" | "$%" 
 */

public class TargetRule extends Rule implements ITargetRule {

	String[] prerequisites;

	public TargetRule(Directive parent, Target target) {
		this(parent, target, new String[0], new Command[0]);
	}

	public TargetRule(Directive parent, Target target, String[] deps) {
		this(parent, target, deps, new Command[0]);
	}

	public TargetRule(Directive parent, Target target, String[] reqs, Command[] commands) {
		super(parent, target, commands);
		prerequisites = reqs.clone();
	}

	public String[] getPrerequisites() {
		return prerequisites.clone();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getTarget().toString());
		buffer.append(':');
		String[] reqs = getPrerequisites();
		for (int i = 0; i < reqs.length; i++) {
			buffer.append(' ').append(reqs[i]);
		}
		buffer.append('\n');
		ICommand[] cmds = getCommands();
		for (int i = 0; i < cmds.length; i++) {
			buffer.append(cmds[i].toString());
		}
		return buffer.toString();
	}
}
