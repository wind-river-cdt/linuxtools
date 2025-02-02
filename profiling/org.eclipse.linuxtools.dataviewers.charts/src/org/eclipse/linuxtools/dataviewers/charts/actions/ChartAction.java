/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.charts.actions;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.charts.Activator;
import org.eclipse.linuxtools.dataviewers.charts.dialogs.ChartDialog;
import org.eclipse.linuxtools.dataviewers.charts.view.ChartView;
import org.eclipse.swt.widgets.Shell;

/**
 * An action that open a chart dialog from an <code>AbstractSTViewer</code>.
 * 
 * @see AbstractSTViewer
 */
public class ChartAction extends Action {
	
	/** The dialog */
	private ChartDialog dialog;
	
	/**
	 * The constructor.
	 * 
	 * @param shell the shell used by the dialog
	 * @param viewer the viewer inputed to the disalog
	 */
	public ChartAction(Shell shell, AbstractSTViewer viewer) {
		super("Create chart...", Activator.getImageDescriptor("icons/chart_icon.png"));
		dialog = createDialog(shell, viewer);
		setEnabled(!viewer.getViewer().getSelection().isEmpty());
		viewer.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setEnabled(!event.getSelection().isEmpty());
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		dialog.open();
		Chart chart = dialog.getValue();
		if (chart != null) {
			ChartView.createChartView(chart);
		
		}
	}
	
	protected ChartDialog createDialog(Shell shell,AbstractSTViewer viewer){
		return new ChartDialog(shell,viewer);
	}
}
