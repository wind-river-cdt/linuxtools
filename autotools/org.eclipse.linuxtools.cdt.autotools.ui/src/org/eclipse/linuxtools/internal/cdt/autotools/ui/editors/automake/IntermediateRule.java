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
 * .INTERMEDIATE
 *   The targets which `.INTERMEDIATE' depends on are treated as intermediate files.
 *   `.INTERMEDIATE' with no prerequisites has no effect.
 */
public class IntermediateRule extends SpecialRule implements IIntermediateRule {

	public IntermediateRule(Directive parent, String[] reqs) {
		super(parent, new Target(GNUMakefileConstants.RULE_INTERMEDIATE), reqs, new Command[0]);
	}

}
