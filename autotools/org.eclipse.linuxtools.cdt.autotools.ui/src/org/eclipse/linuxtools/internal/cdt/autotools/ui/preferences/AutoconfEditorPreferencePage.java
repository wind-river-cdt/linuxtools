/*******************************************************************************
 * Copyright (c) 2002, 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Red Hat Inc. - modification to use with Autoconf editor
 *******************************************************************************/

package org.eclipse.linuxtools.internal.cdt.autotools.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.model.WorkbenchViewerComparator;



/**
 * MakeEditorPreferencePage
 * The page for setting the editor options.
 */
public class AutoconfEditorPreferencePage extends AbstractEditorPreferencePage {

	private static String[] fACVersions = {"2.13", "2.59", "2.61"};
	public static final String LATEST_AC_VERSION = fACVersions[fACVersions.length - 1];
	
	private static String[] fAMVersions = {"1.4-p6", "1.9.5", "1.9.6"};
	public static final String LATEST_AM_VERSION = fAMVersions[fAMVersions.length - 1];
	
	/** The keys of the overlay store. */
	private String[][] fSyntaxColorListModel;

	private TableViewer fHighlightingColorListViewer;
	private final List<HighlightingColorListItem> fHighlightingColorList= new ArrayList<HighlightingColorListItem>(5);

	ColorEditor fSyntaxForegroundColorEditor;
	Button fBoldCheckBox;
	Button fItalicCheckBox;
	
	// folding
	protected Button fFoldingCheckbox;
	
	// version
	protected Combo fACVersionCombo;
	protected Combo fAMVersionCombo;
	
	/**
	 * Item in the highlighting color list.
	 * 
	 * @since 3.0
	 */
	private static class HighlightingColorListItem {
		/** Display name */
		private String fDisplayName;
		/** Color preference key */
		private String fColorKey;
		/** Bold preference key */
		private String fBoldKey;
		/** Italic preference key */
		private String fItalicKey;
		/** Item color */
		private Color fItemColor;
		
		/**
		 * Initialize the item with the given values.
		 * 
		 * @param displayName the display name
		 * @param colorKey the color preference key
		 * @param boldKey the bold preference key
		 * @param italicKey the italic preference key
		 * @param itemColor the item color
		 */
		public HighlightingColorListItem(String displayName, String colorKey, String boldKey, String italicKey, Color itemColor) {
			fDisplayName= displayName;
			fColorKey= colorKey;
			fBoldKey= boldKey;
			fItalicKey= italicKey;
			fItemColor= itemColor;
		}
		
		/**
		 * @return the bold preference key
		 */
		public String getBoldKey() {
			return fBoldKey;
		}
		
		/**
		 * @return the bold preference key
		 */
		public String getItalicKey() {
			return fItalicKey;
		}
		
		/**
		 * @return the color preference key
		 */
		public String getColorKey() {
			return fColorKey;
		}
		
		/**
		 * @return the display name
		 */
		public String getDisplayName() {
			return fDisplayName;
		}
		
		/**
		 * @return the item color
		 */
		public Color getItemColor() {
			return fItemColor;
		}
	}

	/**
	 * Color list label provider.
	 * 
	 * @since 3.0
	 */
	private static class ColorListLabelProvider extends LabelProvider implements IColorProvider {

		/*
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			return ((HighlightingColorListItem)element).getDisplayName();
		}
		
		/*
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			return ((HighlightingColorListItem)element).getItemColor();
		}

		/*
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			return null;
		}
	}
	
	/**
	 * Color list content provider.
	 * 
	 * @since 3.0
	 */
	private static class ColorListContentProvider implements IStructuredContentProvider {

		/*
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			return ((List<Object>)inputElement).toArray();
		}

		/*
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	


	/**
	 * 
	 */
	public AutoconfEditorPreferencePage() {
		super();
	}

	protected OverlayPreferenceStore createOverlayStore() {
		fSyntaxColorListModel= new String[][] {
				{AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.autoconf_editor_comment"), ColorManager.AUTOCONF_COMMENT_COLOR, null}, //$NON-NLS-1$
				{AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.autoconf_editor_acmacro"), ColorManager.AUTOCONF_ACMACRO_COLOR, null}, //$NON-NLS-1$
				{AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.autoconf_editor_ammacro"), ColorManager.AUTOCONF_AMMACRO_COLOR, null}, //$NON-NLS-1$
				{AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.autoconf_editor_code_seq"), ColorManager.AUTOCONF_CODESEQ_COLOR, null},  //$NON-NLS-1$
				{AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.autoconf_editor_keyword"), ColorManager.AUTOCONF_KEYWORD_COLOR, null},  //$NON-NLS-1$
				{AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.autoconf_editor_var_ref"), ColorManager.AUTOCONF_VAR_REF_COLOR, null},  //$NON-NLS-1$
				{AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.autoconf_editor_var_set"), ColorManager.AUTOCONF_VAR_SET_COLOR, null},  //$NON-NLS-1$
				{AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.autoconf_editor_default"), ColorManager.AUTOCONF_DEFAULT_COLOR, null},  //$NON-NLS-1$
			};
		ArrayList<OverlayPreferenceStore.OverlayKey> overlayKeys= new ArrayList<OverlayPreferenceStore.OverlayKey>();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_ENABLED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_CONDITIONAL));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_MACRODEF));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_RULE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AutotoolsEditorPreferenceConstants.AUTOMAKE_VERSION));
		
		for (int i= 0; i < fSyntaxColorListModel.length; i++) {
			String colorKey= fSyntaxColorListModel[i][1];
			addTextKeyToCover(overlayKeys, colorKey);
		}
		
		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}

	private void addTextKeyToCover(ArrayList<OverlayPreferenceStore.OverlayKey> overlayKeys, String mainKey) {
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, mainKey));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, mainKey + AutotoolsEditorPreferenceConstants.EDITOR_BOLD_SUFFIX));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, mainKey + AutotoolsEditorPreferenceConstants.EDITOR_ITALIC_SUFFIX));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		AutotoolsUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(), IMakeHelpContextIds.MAKE_EDITOR_PREFERENCE_PAGE);
		getOverlayStore().load();
		getOverlayStore().start();
		
		TabFolder folder= new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());	
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText(AutotoolsPreferencesMessages.getString("AutomakeEditorPreferencePage.syntax")); //$NON-NLS-1$
		item.setControl(createSyntaxPage(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText(AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.folding")); //$NON-NLS-1$
		item.setControl(createFoldingTabContent(folder));
					
		// Allow end-user to select which version of autoconf to use for hover help
		// and syntax checking of macros.
		item= new TabItem(folder, SWT.NONE);
		item.setText(AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.version")); //$NON-NLS-1$
		item.setControl(createVersionTabContent(folder));
					
		initialize();
		
		applyDialogFont(folder);
		return folder;
	}

	private void initialize() {
		
		initializeFields();
		
		for (int i= 0, n= fSyntaxColorListModel.length; i < n; i++) {
			fHighlightingColorList.add(
				new HighlightingColorListItem (fSyntaxColorListModel[i][0], fSyntaxColorListModel[i][1],
						fSyntaxColorListModel[i][1] + AutotoolsEditorPreferenceConstants.EDITOR_BOLD_SUFFIX, 
						fSyntaxColorListModel[i][1] + AutotoolsEditorPreferenceConstants.EDITOR_ITALIC_SUFFIX, null));
		}
		fHighlightingColorListViewer.setInput(fHighlightingColorList);
		fHighlightingColorListViewer.setSelection(new StructuredSelection(fHighlightingColorListViewer.getElementAt(0)));

		for (int i=0, n= fACVersions.length; i < n; i++) {
			fACVersionCombo.setItem(i, fACVersions[i]);
		}
		
		initializeFolding();
		initializeACVersion();
		initializeAMVersion();
	}

	void initializeFolding() {
		boolean enabled= getOverlayStore().getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		fFoldingCheckbox.setSelection(enabled);		
	}
	
	void initializeACVersion() {
		// FIXME: What do we do here?  There is no PreferenceConstants value for this.
		// Perhaps we should be using our own overlay store.
		String version = getOverlayStore().getString(AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION);
		String[] items = fACVersionCombo.getItems();
		// Try and find which list item matches the current preference stored and
		// select it in the list.
		int i;
		for (i = 0; i < items.length; ++i) {
			if (items[i].equals(version))
				break;
		}
		if (i >= items.length)
			i = items.length - 1;
		fACVersionCombo.select(i);
	}

	void initializeAMVersion() {
		// FIXME: What do we do here?  There is no PreferenceConstants value for this.
		// Perhaps we should be using our own overlay store.
		String version = getOverlayStore().getString(AutotoolsEditorPreferenceConstants.AUTOMAKE_VERSION);
		String[] items = fAMVersionCombo.getItems();
		// Try and find which list item matches the current preference stored and
		// select it in the list.
		int i;
		for (i = 0; i < items.length; ++i) {
			if (items[i].equals(version))
				break;
		}
		if (i >= items.length)
			i = items.length - 1;
		fAMVersionCombo.select(i);
	}

	void initializeDefaultFolding() {
		boolean enabled= getOverlayStore().getDefaultBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		fFoldingCheckbox.setSelection(enabled);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.cdt.autotools.ui.preferences.AbstractAutomakeEditorPreferencePage#handleDefaults()
	 */
	protected void handleDefaults() {
		handleSyntaxColorListSelection();
		initializeDefaultFolding();
	}
	
	private Control createSyntaxPage(Composite parent) {
		
		Composite colorComposite= new Composite(parent, SWT.NONE);
		colorComposite.setLayout(new GridLayout());

		Label label= new Label(colorComposite, SWT.LEFT);
		label.setText(AutotoolsPreferencesMessages.getString("AutomakeEditorPreferencePage.Foreground")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite editorComposite= new Composite(colorComposite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		GridData gd= new GridData(GridData.FILL_BOTH);
		editorComposite.setLayoutData(gd);		

		fHighlightingColorListViewer= new TableViewer(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		fHighlightingColorListViewer.setLabelProvider(new ColorListLabelProvider());
		fHighlightingColorListViewer.setContentProvider(new ColorListContentProvider());
		fHighlightingColorListViewer.setComparator(new WorkbenchViewerComparator());
		gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(5);
		fHighlightingColorListViewer.getControl().setLayoutData(gd);
						
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		label= new Label(stylesComposite, SWT.LEFT);
		label.setText(AutotoolsPreferencesMessages.getString("AutomakeEditorPreferencePage.color")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);

		fSyntaxForegroundColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fSyntaxForegroundColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);
		
		fBoldCheckBox= new Button(stylesComposite, SWT.CHECK);
		fBoldCheckBox.setText(AutotoolsPreferencesMessages.getString("AutomakeEditorPreferencePage.bold")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fBoldCheckBox.setLayoutData(gd);
		
		fItalicCheckBox= new Button(stylesComposite, SWT.CHECK);
		fItalicCheckBox.setText(AutotoolsPreferencesMessages.getString("AutomakeEditorPreferencePage.italic")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fItalicCheckBox.setLayoutData(gd);

		fHighlightingColorListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSyntaxColorListSelection();
			}
		});

		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				PreferenceConverter.setValue(getOverlayStore(), item.getColorKey(), fSyntaxForegroundColorEditor.getColorValue());
			}
		});

		fBoldCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				getOverlayStore().setValue(item.getBoldKey(), fBoldCheckBox.getSelection());
			}
		});
				
		fItalicCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item= getHighlightingColorListItem();
				getOverlayStore().setValue(item.getItalicKey(), fItalicCheckBox.getSelection());
			}
		});
				
		return colorComposite;
	}
	

	private Composite createFoldingTabContent(TabFolder folder) {
		Composite composite= new Composite(folder, SWT.NULL);
		// assume parent page uses griddata
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
		composite.setLayoutData(gd);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		//PixelConverter pc= new PixelConverter(composite);
		//layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
		composite.setLayout(layout);
		
		
		/* check box for new editors */
		fFoldingCheckbox= new Button(composite, SWT.CHECK);
		fFoldingCheckbox.setText(AutotoolsPreferencesMessages.getString("AutomakeEditorPreferencePage.foldingenable")); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		fFoldingCheckbox.setLayoutData(gd);
		fFoldingCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean enabled= fFoldingCheckbox.getSelection(); 
				getOverlayStore().setValue(AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_ENABLED, enabled);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		return composite;
	}

	private Composite createVersionTabContent(TabFolder folder) {
		Composite composite= new Composite(folder, SWT.NULL);
		// assume parent page uses griddata
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
		composite.setLayoutData(gd);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		//PixelConverter pc= new PixelConverter(composite);
		//layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
		composite.setLayout(layout);
		
		
		/* check box for new editors */
		fACVersionCombo= new Combo(composite, SWT.CHECK | SWT.DROP_DOWN | SWT.READ_ONLY);
		fACVersionCombo.setItems(fACVersions);
		fACVersionCombo.select(fACVersions.length - 1);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		fACVersionCombo.setLayoutData(gd);
		fACVersionCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int index = fACVersionCombo.getSelectionIndex(); 
				getOverlayStore().setValue(AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION, fACVersionCombo.getItem(index));
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Label label= new Label(composite, SWT.LEFT);
		label.setText(AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.autoconfVersion")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);
		
		/* check box for new editors */
		fAMVersionCombo= new Combo(composite, SWT.CHECK | SWT.DROP_DOWN | SWT.READ_ONLY);
		fAMVersionCombo.setItems(fAMVersions);
		fAMVersionCombo.select(fAMVersions.length - 1);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		fAMVersionCombo.setLayoutData(gd);
		fAMVersionCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int index = fAMVersionCombo.getSelectionIndex(); 
				getOverlayStore().setValue(AutotoolsEditorPreferenceConstants.AUTOMAKE_VERSION, fAMVersionCombo.getItem(index));
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Label label2= new Label(composite, SWT.LEFT);
		label2.setText(AutotoolsPreferencesMessages.getString("AutoconfEditorPreferencePage.automakeVersion")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label2.setLayoutData(gd);

		return composite;
	}

	void handleSyntaxColorListSelection() {
		HighlightingColorListItem item= getHighlightingColorListItem();
		RGB rgb= PreferenceConverter.getColor(getOverlayStore(), item.getColorKey());
		fSyntaxForegroundColorEditor.setColorValue(rgb);		
		fBoldCheckBox.setSelection(getOverlayStore().getBoolean(item.getBoldKey()));
		fItalicCheckBox.setSelection(getOverlayStore().getBoolean(item.getItalicKey()));
	}
	
	/**
	 * Returns the current highlighting color list item.
	 * 
	 * @return the current highlighting color list item
	 * @since 3.0
	 */
	HighlightingColorListItem getHighlightingColorListItem() {
		IStructuredSelection selection= (IStructuredSelection) fHighlightingColorListViewer.getSelection();
		return (HighlightingColorListItem) selection.getFirstElement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		return super.performOk();
	}

	/**
	 * @param preferenceStore
	 */
	public static void initDefaults(IPreferenceStore prefs) {		
		// Makefile Editor color preferences
		PreferenceConverter.setDefault(prefs, ColorManager.AUTOCONF_COMMENT_COLOR, ColorManager.AUTOCONF_COMMENT_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.AUTOCONF_DEFAULT_COLOR, ColorManager.AUTOCONF_DEFAULT_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.AUTOCONF_KEYWORD_COLOR, ColorManager.AUTOCONF_KEYWORD_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.AUTOCONF_VAR_REF_COLOR, ColorManager.AUTOCONF_VAR_REF_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.AUTOCONF_VAR_SET_COLOR, ColorManager.AUTOCONF_VAR_SET_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.AUTOCONF_ACMACRO_COLOR, ColorManager.AUTOCONF_ACMACRO_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.AUTOCONF_AMMACRO_COLOR, ColorManager.AUTOCONF_AMMACRO_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.AUTOCONF_CODESEQ_COLOR, ColorManager.AUTOCONF_CODESEQ_RGB);
		prefs.setDefault(ColorManager.AUTOCONF_CODESEQ_COLOR + AutotoolsEditorPreferenceConstants.EDITOR_ITALIC_SUFFIX, true);
	}

}
