/*******************************************************************************
 * Copyright (c) 2004-2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.propertypage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpm.core.utils.RPMQuery;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

public class ProvidesPage extends AbstractRPMPropertyPage {

	private static final String RPM_QL = Messages
			.getString("ProvidesPage.Provides"); //$NON-NLS-1$

	private static final int QL_FIELD_WIDTH = 80;

	private static final int QL_FIELD_HEIGHT = 40;

	private Text rpm_qlText;

	@Override
	protected void addFields(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// RPM labels and text fields setup

		Label rpmDescriptionLabel = new Label(composite, SWT.NONE);
		rpmDescriptionLabel.setText(RPM_QL);
		rpm_qlText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY
				| SWT.V_SCROLL | SWT.WRAP);
		GridData gdQL = new GridData();
		gdQL.widthHint = convertWidthInCharsToPixels(QL_FIELD_WIDTH);
		gdQL.heightHint = convertWidthInCharsToPixels(QL_FIELD_HEIGHT);
		rpm_qlText.setLayoutData(gdQL);

		// Populate RPM text fields
		try {
			String rpm_ql = RPMQuery.getProvides((IFile) getElement());
			rpm_qlText.setText(rpm_ql);
		} catch (CoreException e) {
			StatusManager.getManager().handle(new StatusAdapter(e.getStatus()),
					StatusManager.LOG | StatusManager.SHOW);
		}

	}

}