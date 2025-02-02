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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.filters;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.RangeFilter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.MockDataSet;
import org.eclipse.ui.XMLMemento;


import junit.framework.TestCase;

public class RangeFilterTest extends TestCase {
	public RangeFilterTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		filter = new RangeFilter(0, new Integer(1), new Integer(2), RangeFilter.INSIDE_BOUNDS | RangeFilter.INCLUSIVE);
	}

	public void testRangeFilter() {
		RangeFilter filter = new RangeFilter(-1, new Integer(3), new Integer(5), RangeFilter.INSIDE_BOUNDS);
		assertNotNull(filter);
	}
	
	public void testFilter() {
		int width = 4;
		int height = 10;
		int wrap = height / 3;
		ArrayList[] data = MockDataSet.buildArray(width, height, wrap);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		ArrayList[] data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(6, data2[0].size());
		assertEquals("1", data2[0].get(0));
		assertEquals("2", data2[0].get(1));
		assertEquals("1", data2[0].get(2));
		assertEquals("2", data2[0].get(3));

		assertEquals(data[2].get(1), data2[2].get(0));
		assertEquals(data[2].get(2), data2[2].get(1));
		assertEquals(data[2].get(4), data2[2].get(2));
		assertEquals(data[2].get(5), data2[2].get(3));


		filter = new RangeFilter(0, new Integer(0), new Integer(2), RangeFilter.INSIDE_BOUNDS);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(3, data2[0].size());
		assertEquals("1", data2[0].get(0));
		assertEquals("1", data2[0].get(1));
		assertEquals("1", data2[0].get(2));

		assertEquals(data[2].get(1), data2[2].get(0));
		assertEquals(data[2].get(4), data2[2].get(1));
		assertEquals(data[2].get(7), data2[2].get(2));


		filter = new RangeFilter(0, new Integer(0), new Integer(2), RangeFilter.OUTSIDE_BOUNDS | RangeFilter.INCLUSIVE);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(7, data2[0].size());
		assertEquals("0", data2[0].get(0));
		assertEquals("2", data2[0].get(1));
		assertEquals("0", data2[0].get(2));
		assertEquals("2", data2[0].get(3));

		assertEquals(data[2].get(0), data2[2].get(0));
		assertEquals(data[2].get(2), data2[2].get(1));
		assertEquals(data[2].get(3), data2[2].get(2));
		assertEquals(data[2].get(5), data2[2].get(3));


		filter = new RangeFilter(0, new Integer(0), new Integer(2), RangeFilter.INSIDE_BOUNDS);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(3, data2[0].size());
		assertEquals("1", data2[0].get(0));
		assertEquals("1", data2[0].get(1));
		assertEquals("1", data2[0].get(2));

		assertEquals(data[2].get(1), data2[2].get(0));
		assertEquals(data[2].get(4), data2[2].get(1));
		assertEquals(data[2].get(7), data2[2].get(2));
		

		filter = new RangeFilter(0, new Integer(0), new Integer(2), RangeFilter.OUTSIDE_BOUNDS);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(0, data2[0].size());
		
		
		filter = new RangeFilter(-1, new Integer(1), new Integer(3), 0);
		assertNull(filter.filter(data));
	}
	
	public void testGetID() {
		assertTrue(RangeFilter.ID.equals(filter.getID()));
	}

	public void testWriteXML() {
		filter.writeXML(XMLMemento.createWriteRoot("test"));
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	RangeFilter filter;
}
