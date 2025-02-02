/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.filter.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfNoSuchFieldException;


public class TmfFilterMatchesNode extends TmfFilterTreeNode {

	public static final String NODE_NAME = "MATCHES"; //$NON-NLS-1$
	public static final String NOT_ATTR = "not"; //$NON-NLS-1$
	public static final String FIELD_ATTR = "field"; //$NON-NLS-1$
	public static final String REGEX_ATTR = "regex"; //$NON-NLS-1$
	
	private boolean fNot = false;
	private String fField;
	private String fRegex;
	private Pattern fPattern;
	
	public TmfFilterMatchesNode(ITmfFilterTreeNode parent) {
		super(parent);
	}

	public boolean isNot() {
		return fNot;
	}
	
	public void setNot(boolean not) {
		this.fNot = not;
	}
	
	public String getField() {
		return fField;
	}

	public void setField(String field) {
		this.fField = field;
	}

	public String getRegex() {
		return fRegex;
	}

	public void setRegex(String regex) {
		this.fRegex = regex;
		try {
			this.fPattern = Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			this.fPattern = null;
		}
	}

	@Override
	public String getNodeName() {
		return NODE_NAME;
	}

	@Override
	public boolean matches(TmfEvent event) {
		if (fPattern == null) {
			return false ^ fNot;
		}
		try {
			Object value = event.getContent().getField(fField);
			if (value == null) {
				return false ^ fNot;
			}
			String valueString = value.toString();
			return fPattern.matcher(valueString).matches() ^ fNot;
		} catch (TmfNoSuchFieldException e) {
			return false ^ fNot;
		}
	}

	@Override
	public List<String> getValidChildren() {
		return new ArrayList<String>(0);
	}

	@Override
	public String toString() {
		return fField + (fNot ? " not" : "") + " matches \"" + fRegex + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	@Override
	public ITmfFilterTreeNode clone() {
		TmfFilterMatchesNode clone = (TmfFilterMatchesNode) super.clone();
		clone.fField = new String(fField);
		clone.setRegex(new String(fRegex));
		return clone;
	}
	
	public static String regexFix(String pattern) {
		// if the pattern does not contain one of the expressions .* !^
		// (at the beginning) $ (at the end), then a .* is added at the
		// beginning and at the end of the pattern
		if (!(pattern.indexOf(".*") >= 0 || pattern.charAt(0) == '^' || pattern.charAt(pattern.length() - 1) == '$')) { //$NON-NLS-1$
			pattern = ".*" + pattern + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return pattern;
	}
}
