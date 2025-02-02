/*******************************************************************************
 * Copyright (c) 2008 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.tests;

import org.eclipse.linuxtools.cdt.autotools.tests.autoconf.AutoconfTests;
import org.eclipse.linuxtools.cdt.autotools.tests.editors.EditorTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllAutotoolsTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.cdt.autotools.core.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(AutotoolsProjectTest0.class);
		suite.addTestSuite(AutotoolsProjectNatureTest.class);
		suite.addTestSuite(AutotoolsProjectTest1.class);
		suite.addTestSuite(AutotoolsProjectTest2.class);
		suite.addTest(AutoconfTests.suite());
		suite.addTest(EditorTests.suite());
		//$JUnit-END$
		return suite;
	}

}
