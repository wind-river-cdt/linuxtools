/*******************************************************************************
 * Mostly copied from makefileCompletionProcessor which has the following
 * copyright notice:
 * 
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.linuxtools.internal.cdt.autotools.ui.MakeUIImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;


public class AutomakeCompletionProcessor implements IContentAssistProcessor {

		/**
		 * Simple content assist tip closer. The tip is valid in a range
		 * of 5 characters around its popup location.
		 */
		protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {

			protected int fInstallOffset;

			/*
			 * @see IContextInformationValidator#isContextInformationValid(int)
			 */
			public boolean isContextInformationValid(int offset) {
				return Math.abs(fInstallOffset - offset) < 5;
			}

			/*
			 * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
			 */
			public void install(IContextInformation info, ITextViewer viewer, int offset) {
				fInstallOffset = offset;
			}

			/*
			 * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int, TextPresentation)
			 */
			public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
				return false;
			}
		}

		public static class DirectiveComparator implements Comparator<Object> {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Object o1, Object o2) {
				String name1;
				String name2;

				if (o1 instanceof IMacroDefinition) {
					name1 = ((IMacroDefinition)o1).getName();
				} else if (o1 instanceof IRule) {
					name1 = ((IRule)o1).getTarget().toString();
				} else {
					name1 =""; //$NON-NLS-1$
				}

				if (o2 instanceof IMacroDefinition) {
					name2 = ((IMacroDefinition)o1).getName();
				} else if (o2 instanceof IRule) {
					name2 = ((IRule)o1).getTarget().toString();
				} else {
					name2 =""; //$NON-NLS-1$
				}

				//return String.CASE_INSENSITIVE_ORDER.compare(name1, name2);
				return name1.compareToIgnoreCase(name2);
			}
			
		}
		protected IContextInformationValidator fValidator = new Validator();
		protected Image imageMacro = MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_MACRO);
		protected Image imageTarget = MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_TARGET_RULE);

		protected CompletionProposalComparator comparator = new CompletionProposalComparator();
		protected IEditorPart fEditor;
		protected IWorkingCopyManager fManager;

		public AutomakeCompletionProcessor(IEditorPart editor) {
			fEditor = editor;
			fManager =  AutomakeEditorFactory.getDefault().getWorkingCopyManager();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
		 */
		public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
			WordPartDetector wordPart = new WordPartDetector(viewer, documentOffset);
			boolean macro = WordPartDetector.inMacro(viewer, documentOffset);
			IMakefile makefile = fManager.getWorkingCopy(fEditor.getEditorInput());
			IDirective[] statements = null;
			if (macro) {
				IDirective[] m1 = makefile.getMacroDefinitions();
				IDirective[] m2 = makefile.getBuiltinMacroDefinitions();
				statements = new IDirective[m1.length + m2.length];
				System.arraycopy(m1, 0, statements, 0, m1.length);
				System.arraycopy(m2, 0, statements, m1.length, m2.length);
			} else {
				statements = makefile.getTargetRules();
			}

			ArrayList<ICompletionProposal> proposalList = new ArrayList<ICompletionProposal>(statements.length);

			// iterate over all the different categories
			for (int i = 0; i < statements.length; i++) {
				String name = null;
				Image image = null;
				String infoString = "";//getContentInfoString(name); //$NON-NLS-1$
				if (statements[i] instanceof IMacroDefinition) {
					name = ((IMacroDefinition) statements[i]).getName();
					image = imageMacro;
					infoString = ((IMacroDefinition)statements[i]).getValue().toString();
				} else if (statements[i] instanceof IRule) {
					name = ((IRule) statements[i]).getTarget().toString();
					image = imageTarget;
					infoString = name;
				}
				if (name != null && name.startsWith(wordPart.toString())) {
					IContextInformation info = new ContextInformation(name, infoString);
					String displayString = (name.equals(infoString) ? name : name + " - " + infoString); //$NON-NLS-1$
					ICompletionProposal result =
						new CompletionProposal(
							name,
							wordPart.getOffset(),
							wordPart.toString().length(),
							name.length(),
							image,
							displayString,
							info,
							infoString);
					proposalList.add(result);
				}
			}
			ICompletionProposal[] proposals = (ICompletionProposal[]) proposalList.toArray(new ICompletionProposal[0]);
			Arrays.sort(proposals, comparator);
			return proposals;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
		 */
		public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
			WordPartDetector wordPart = new WordPartDetector(viewer, documentOffset);
			boolean macro = WordPartDetector.inMacro(viewer, documentOffset);
			IMakefile makefile = fManager.getWorkingCopy(fEditor.getEditorInput());
			ArrayList<String> contextList = new ArrayList<String>();
			if (macro) {
				IDirective[] statements = makefile.getMacroDefinitions();
				for (int i = 0; i < statements.length; i++) {
					if (statements[i] instanceof IMacroDefinition) {
						String name = ((IMacroDefinition) statements[i]).getName();
						if (name != null && name.equals(wordPart.toString())) {
							String value = ((IMacroDefinition) statements[i]).getValue().toString();
							if (value != null && value.length() > 0) {
								contextList.add(value);
							}
						}
					}
				}
				statements = makefile.getBuiltinMacroDefinitions();
				for (int i = 0; i < statements.length; i++) {
					if (statements[i] != null) {
						String name = ((IMacroDefinition) statements[i]).getName();
						if (name != null && name.equals(wordPart.toString())) {
							String value = ((IMacroDefinition) statements[i]).getValue().toString();
							if (value != null && value.length() > 0) {
								contextList.add(value);
							}
						}
					}
				}
			}

			IContextInformation[] result = new IContextInformation[contextList.size()];
			for (int i = 0; i < result.length; i++) {
				String context = (String)contextList.get(i);
				result[i] = new ContextInformation(imageMacro, wordPart.toString(), context);
			}
			return result;

		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
		 */
		public char[] getCompletionProposalAutoActivationCharacters() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
		 */
		public char[] getContextInformationAutoActivationCharacters() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
		 */
		public String getErrorMessage() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
		 */
		public IContextInformationValidator getContextInformationValidator() {
			return fValidator;
		}
	
}
