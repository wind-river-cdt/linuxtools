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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Makefile : ( statement ) *
 * statement :   rule | macro_definition | comments | empty
 * rule :  inference_rule | target_rule
 * inference_rule : target ':' <nl> ( <tab> command <nl> ) +
 * target_rule : target [ ( target ) * ] ':' [ ( prerequisite ) * ] [ ';' command ] <nl> 
                 [ ( command ) * ]
 * macro_definition : string '=' (string)* 
 * comments : ('#' (string) <nl>) *
 * empty : <nl>
 * command : <tab> prefix_command string <nl>
 * target : string
 * prefix_command : '-' | '@' | '+'
 * internal_macro :  "$<" | "$*" | "$@" | "$?" | "$%" 
 */

public abstract class AbstractMakefile extends Parent implements IMakefile {

	private URI filename;

	public AbstractMakefile(Directive parent) {
		super(parent);
	}

	public abstract IDirective[] getBuiltins();

	public IRule[] getRules() {
		IDirective[] stmts = getDirectives(true);
		List<IRule> array = new ArrayList<IRule>(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof IRule) {
				array.add((IRule)stmts[i]);
			}
		}
		return (IRule[]) array.toArray(new IRule[0]);
	}

	public IRule[] getRules(String target) {
		IRule[] rules = getRules();
		List<IRule> array = new ArrayList<IRule>(rules.length);
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].getTarget().toString().equals(target)) {
				array.add(rules[i]);
			}
		}
		return (IRule[]) array.toArray(new IRule[0]);
	}

	public IInferenceRule[] getInferenceRules() {
		IRule[] rules = getRules();
		List<IInferenceRule> array = new ArrayList<IInferenceRule>(rules.length);
		for (int i = 0; i < rules.length; i++) {
			if (rules[i] instanceof IInferenceRule) {
				array.add((IInferenceRule)rules[i]);
			}
		}
		return (IInferenceRule[]) array.toArray(new IInferenceRule[0]);
	}

	public IInferenceRule[] getInferenceRules(String target) {
		IInferenceRule[] irules = getInferenceRules();
		List<IInferenceRule> array = new ArrayList<IInferenceRule>(irules.length);
		for (int i = 0; i < irules.length; i++) {
			if (irules[i].getTarget().toString().equals(target)) {
				array.add(irules[i]);
			}
		}
		return (IInferenceRule[]) array.toArray(new IInferenceRule[0]);
	}

	public ITargetRule[] getTargetRules() {
		IRule[] trules = getRules();
		List<ITargetRule> array = new ArrayList<ITargetRule>(trules.length);
		for (int i = 0; i < trules.length; i++) {
			if (trules[i] instanceof ITargetRule) {
				array.add((ITargetRule)trules[i]);
			}
		}
		return (ITargetRule[]) array.toArray(new ITargetRule[0]);
	}

	public ITargetRule[] getTargetRules(String target) {
		ITargetRule[] trules = getTargetRules();
		List<ITargetRule> array = new ArrayList<ITargetRule>(trules.length);
		for (int i = 0; i < trules.length; i++) {
			if (trules[i].getTarget().toString().equals(target)) {
				array.add(trules[i]);
			}
		}
		return (ITargetRule[]) array.toArray(new ITargetRule[0]);
	}

	public IMacroDefinition[] getMacroDefinitions() {
		IDirective[] stmts = getDirectives(true);
		List<IMacroDefinition> array = new ArrayList<IMacroDefinition>(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof IMacroDefinition) {
				array.add((IMacroDefinition)stmts[i]);
			}
		}
		return (IMacroDefinition[]) array.toArray(new IMacroDefinition[0]);
	}

	public IMacroDefinition[] getMacroDefinitions(String name) {
		IMacroDefinition[] variables = getMacroDefinitions();
		List<IMacroDefinition> array = new ArrayList<IMacroDefinition>(variables.length);
		for (int i = 0; i < variables.length; i++) {
			if (variables[i].getName().equals(name)) {
				array.add(variables[i]);
			}
		}
		return (IMacroDefinition[]) array.toArray(new IMacroDefinition[0]);
	}

	public IMacroDefinition[] getBuiltinMacroDefinitions() {
		IDirective[] stmts = getBuiltins();
		List<IMacroDefinition> array = new ArrayList<IMacroDefinition>(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof IMacroDefinition) {
				array.add((IMacroDefinition)stmts[i]);
			}
		}
		return (IMacroDefinition[]) array.toArray(new IMacroDefinition[0]);
	}

	public IMacroDefinition[] getBuiltinMacroDefinitions(String name) {
		IMacroDefinition[] variables = getBuiltinMacroDefinitions();
		List<IMacroDefinition> array = new ArrayList<IMacroDefinition>(variables.length);
		for (int i = 0; i < variables.length; i++) {
			if (variables[i].getName().equals(name)) {
				array.add(variables[i]);
			}
		}
		return (IMacroDefinition[]) array.toArray(new IMacroDefinition[0]);
	}

	public IInferenceRule[] getBuiltinInferenceRules() {
		IDirective[] stmts = getBuiltins();
		List<IInferenceRule> array = new ArrayList<IInferenceRule>(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof IInferenceRule) {
				array.add((IInferenceRule)stmts[i]);
			}
		}
		return (IInferenceRule[]) array.toArray(new IInferenceRule[0]);
	}

	public IInferenceRule[] getBuiltinInferenceRules(String target) {
		IInferenceRule[] irules = getBuiltinInferenceRules();
		List<IInferenceRule> array = new ArrayList<IInferenceRule>(irules.length);
		for (int i = 0; i < irules.length; i++) {
			if (irules[i].getTarget().toString().equals(target)) {
				array.add(irules[i]);
			}
		}
		return (IInferenceRule[]) array.toArray(new IInferenceRule[0]);
	}

	public String expandString(String line) {
		return expandString(line, false);
	}

	public String expandString(String line, boolean recursive) {
		int len = line.length();
		boolean foundDollar = false;
		boolean inMacro = false;
		StringBuffer buffer = new StringBuffer();
		StringBuffer macroName = new StringBuffer();
		for (int i = 0; i < len; i++) {
			char c = line.charAt(i);
			switch(c) {
				case '$':
					// '$$' --> '$'
					if (foundDollar) {
						buffer.append(c);
						foundDollar = false;
					} else {
						foundDollar = true;
					}
					break;
				case '(':
				case '{':
					if (foundDollar) {
						inMacro = true;
					} else {
						buffer.append(c);
					}
					break;
				case ')':
				case '}':
					if (inMacro) {
						String name = macroName.toString();
						if (name.length() > 0) {
							IMacroDefinition[] defs = getMacroDefinitions(name);
							if (defs.length == 0) {
								defs = getBuiltinMacroDefinitions(name);
							}
							if (defs.length > 0) {
								String result = defs[0].getValue().toString();
								if (result.indexOf('$') != -1 && recursive) {
									result = expandString(result, recursive);
								}
								buffer.append(result);
							} else { // Do not expand
								buffer.append('$').append('(').append(name).append(')');
							}
						}
						macroName.setLength(0);
						inMacro = false;
					} else {
						buffer.append(c);
					}
					break;
				default:
					if (inMacro) {
						macroName.append(c);
					} else if (foundDollar) {
						String name = String.valueOf(c);
						IMacroDefinition[] defs = getMacroDefinitions(name);
						if (defs.length == 0) {
							defs = getBuiltinMacroDefinitions(name);
						}
						if (defs.length > 0) {
							String result = defs[0].getValue().toString();
							if (result.indexOf('$') != -1 && recursive) {
								result = expandString(result, recursive);
							}
							buffer.append(result);
						} else {
							// nothing found
							buffer.append('$').append(c);
						}
						inMacro = false;
					} else {
						buffer.append(c);
					}
					foundDollar = false;
					break;
			}
		}
		return buffer.toString();
	}
	
	public URI getFileURI() {
		return filename;
	}
	
	public void setFileURI(URI filename) {
	    this.filename = filename;
    }
	

	public IMakefile getMakefile() {
		return this;
	}
	
	public IMakefileReaderProvider getMakefileReaderProvider() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IMakefile#parse(java.net.URI, org.eclipse.cdt.make.core.makefile.IMakefileReaderProvider)
	 */
	public void parse(URI fileURI,
			IMakefileReaderProvider makefileReaderProvider) throws IOException {
		// not used
	}

}
