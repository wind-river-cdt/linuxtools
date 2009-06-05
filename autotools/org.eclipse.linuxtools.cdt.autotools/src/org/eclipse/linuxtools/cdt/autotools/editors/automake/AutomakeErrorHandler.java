package org.eclipse.linuxtools.cdt.autotools.editors.automake;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.IParent;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.linuxtools.cdt.autotools.AutotoolsPlugin;
import org.eclipse.linuxtools.cdt.autotools.internal.editors.automake.BadDirective;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;


public class AutomakeErrorHandler {
	public static final String AUTOMAKE_ERROR_MARKER_ID = AutotoolsPlugin.PLUGIN_ID
	+ ".parsefileerror";
	
	private IDocument document;
	private AnnotationModel fAnnotationModel;

	public AutomakeErrorHandler(IDocument document)
	{
		this.document = document;
		IEditorInput input = AutomakeEditor.getDefault().getEditorInput();
		this.fAnnotationModel = (AnnotationModel)AutomakeEditorFactory.getDefault().getAutomakefileDocumentProvider().getAnnotationModel(input);
	}
	
	private class AutomakeMarker implements IMarker {
		private Map attributes;
		private String type;
		private long id;
		
		public void delete() throws CoreException {
			// TODO Auto-generated method stub
			
		}
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return super.equals(obj);
		}
		public boolean exists() {
			// TODO Auto-generated method stub
			return true;
		}
		public Object getAdapter(Class adapter) {
			// TODO Auto-generated method stub
			return null;
		}
		public Object getAttribute(String attributeName) throws CoreException {
			return attributes.get(attributeName);
		}
		public boolean getAttribute(String attributeName, boolean defaultValue) {
			Object o = attributes.get(attributeName);
			if (o != null && o instanceof Boolean)
				return ((Boolean)o).booleanValue();
			return defaultValue;
		}
		public int getAttribute(String attributeName, int defaultValue) {
			Object o = attributes.get(attributeName);
			if (o != null && o instanceof Integer)
				return ((Integer)o).intValue();
			return defaultValue;
		}
		public String getAttribute(String attributeName, String defaultValue) {
			Object o = attributes.get(attributeName);
			if (o != null && o instanceof String)
				return (String)o;
			return defaultValue;
		}
		public Map getAttributes() throws CoreException {
			return attributes;
		}
		public Object[] getAttributes(String[] attributeNames)
				throws CoreException {
			Collection c = attributes.values();
			return c.toArray();
		}
		public long getCreationTime() throws CoreException {
			// TODO Auto-generated method stub
			return 0;
		}
		public long getId() {
			return id;
		}
		public IResource getResource() {
			return null;
		}
		public String getType() throws CoreException {
			return type;
		}
		public boolean isSubtypeOf(String superType) throws CoreException {
			if (superType.equals(type))
				return true;
			return false;
		}
		public void setAttribute(String attributeName, boolean value)
				throws CoreException {
			Boolean b = new Boolean(value);
			attributes.put(attributeName, b);
		}
		public void setAttribute(String attributeName, int value)
				throws CoreException {
			Integer i = new Integer(value);
			attributes.put(attributeName, i);	
		}
		public void setAttribute(String attributeName, Object value)
				throws CoreException {
			attributes.put(attributeName, value);
		}
		public void setAttributes(Map map) throws CoreException {
			attributes.putAll(map);
		}
		public void setAttributes(String[] attributeNames, Object[] values)
				throws CoreException {
			for (int i = 0; i < attributeNames.length; ++i) {
				attributes.put(attributeNames[i], values[i]);
			}
		}
		protected Object clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return super.clone();
		}
		public AutomakeMarker(String type, long id) {
			this.type = type;
			this.id = id;
			this.attributes = new HashMap();
		}
	}
	
	private IMarker createMarker(Map attributes, String markerType) throws CoreException {
		IMarker marker= new AutomakeMarker(markerType, -1);
		marker.setAttributes(attributes);
		return marker;
	}

	public void update(IMakefile makefile) {
		removeExistingMarkers();

		// Recursively process all the directives in the Makefile
		checkChildren(makefile);
	}
	
	private void checkChildren(IParent parent) {
		IDirective[] directives = parent.getDirectives();
		for (int i = 0; i < directives.length; i++) {
			IDirective directive = directives[i];
			if (directive instanceof IParent) {
				checkChildren((IParent)directive);
			} else if (directive instanceof BadDirective) {
				
				int lineNumber = directive.getStartLine();
				
				Map map = new HashMap();
				MarkerUtilities.setLineNumber(map, lineNumber);
				// FIXME:  message
				MarkerUtilities.setMessage(map, "Bad Directive");
				map.put(IMarker.MESSAGE, "Bad Directive");
				map.put(IMarker.LOCATION, Integer.valueOf(lineNumber));
				
				Integer charStart = getCharOffset(lineNumber - 1, 0);
				if (charStart != null) {
					map.put(IMarker.CHAR_START, charStart);
				}
				// FIXME:  probably a better way to do this
				// This is the end character
				Integer charEnd = new Integer(getCharOffset(directive.getEndLine(), 0).intValue() - 1);
				if (charEnd != null) {
					map.put(IMarker.CHAR_END, charEnd);
				}
				
				// FIXME:  add severity level
				map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
				
				try {
					IMarker marker = createMarker(map, AUTOMAKE_ERROR_MARKER_ID); // ICModelMarker.C_MODEL_PROBLEM_MARKER);

					MarkerAnnotation annotation = new MarkerAnnotation(marker);
					Position p = new Position(charStart.intValue(),charEnd.intValue() - charStart.intValue());
					fAnnotationModel.addAnnotation(annotation, p);
				} catch (CoreException ce) {
					// do nothing
				}
			}
		}
		return;
	}
	
	public void removeExistingMarkers()
	{
		fAnnotationModel.removeAllAnnotations();
	}
	
	
	private Integer getCharOffset(int lineNumber, int columnNumber)
	{
		try
		{
			return new Integer(document.getLineOffset(lineNumber) + columnNumber);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
