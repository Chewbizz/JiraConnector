/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts;

import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.ui.AtlassianImages;
import com.atlassian.connector.eclipse.ui.forms.ReflowRespectingSection;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A UI part that is expandable like a tree
 * 
 * @author Shawn Minto
 */
public abstract class ExpandablePart<T, V extends ExpandablePart<T, V>> {

	private Section commentSection;

	private boolean isExpanded;

	private boolean enableToolbar = true;

	private boolean isIncomming = false;

	private final List<V> childrenParts;

	protected final CrucibleReviewEditorPage crucibleEditor;

	protected Review crucibleReview;

	private IReviewActionListener actionListener;

	private FormToolkit formToolkit;

	private ToolBarManager toolBarManager;

	private Label annotationImageLabel;

	private Label annotationsTextLabel;

	public ExpandablePart(CrucibleReviewEditorPage editor, Review crucibleReview) {
		this.crucibleEditor = editor;
		this.crucibleReview = crucibleReview;
		childrenParts = new ArrayList<V>();
	}

	protected void addChildPart(V part) {
		childrenParts.add(part);
	}

	protected List<V> getChildrenParts() {
		return childrenParts;
	}

	public CrucibleReviewEditorPage getCrucibleEditor() {
		return crucibleEditor;
	}

	public Control createControl(Composite parent, final FormToolkit toolkit) {

		this.formToolkit = toolkit;

		int style = ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT;
		if (canExpand()) {
			style |= ExpandableComposite.TWISTIE;
		}

//		if (crucibleEditor == null) {
		style |= ExpandableComposite.EXPANDED;
//		}

		commentSection = new ReflowRespectingSection(toolkit, parent, style, crucibleEditor);
		updateSectionText();
		commentSection.setTitleBarForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		GridData gd = GridDataFactory.fillDefaults().grab(true, false).create();
		if (!canExpand()) {
			gd.horizontalIndent = 9;
		}
		commentSection.setLayoutData(gd);

		final Composite actionsComposite = createSectionAnnotationsAndToolbar(commentSection, toolkit);

		toolBarManager = new ToolBarManager(SWT.FLAT);

		ToolBar toolbarControl = toolBarManager.createControl(actionsComposite);
		toolkit.adapt(toolbarControl);

		if (commentSection.isExpanded() || crucibleEditor == null) {
			isExpanded = true;
			fillToolBar(toolBarManager, isExpanded);
			if (hasContents()) {
				Composite composite = createSectionContents(commentSection, toolkit);
				commentSection.setClient(composite);
				commentSection.addExpansionListener(new ExpansionAdapter() {
					@Override
					public void expansionStateChanged(ExpansionEvent e) {
						fillToolBar(toolBarManager, e.getState());
						if (getSection() != null && !getSection().isDisposed()) {
							getSection().layout();
						}
					}
				});
			}

		} else {
			fillToolBar(toolBarManager, false);
			commentSection.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					isExpanded = e.getState();
					fillToolBar(toolBarManager, isExpanded);
					if (commentSection.getClient() == null) {
						try {
							if (crucibleEditor != null) {
								crucibleEditor.setReflow(false);
							}
							Composite composite = createSectionContents(commentSection, toolkit);

							if (crucibleEditor != null && crucibleEditor.getMenu() != null) {
								crucibleEditor.setMenu(composite, crucibleEditor.getMenu());
							}
							commentSection.setClient(composite);
						} finally {
							if (crucibleEditor != null) {
								crucibleEditor.setReflow(true);
							}
						}
						if (crucibleEditor != null) {
							crucibleEditor.reflow(false);
						}
					}
					if (getSection() != null && !getSection().isDisposed()) {
						getSection().layout();
					}
				}
			});
		}
		return commentSection;
	}

	protected void update() {
		updateSectionText();
		updateAnnotationsArea();
		updateToolbar();
	}

	private void updateSectionText() {
		if (commentSection != null && !commentSection.isDisposed()) {
			commentSection.setText(getSectionHeaderText());
		}
	}

	private void updateToolbar() {
		fillToolBar(toolBarManager, commentSection.isExpanded());
	}

	private void updateAnnotationsArea() {

		ImageDescriptor annotationImage = getAnnotationImage();
		if (annotationImageLabel != null && !annotationImageLabel.isDisposed()) {
			if (annotationImage != null) {
				annotationImageLabel.setImage(AtlassianImages.getImage(annotationImage));
			} else {
				annotationImageLabel.setImage(null);
			}
		}

		String annotationsText = getAnnotationText();
		if (annotationsText == null) {
			annotationsText = "";
		}
		if (annotationsTextLabel != null && !annotationsTextLabel.isDisposed()) {
			annotationsTextLabel.setText(annotationsText);
		}
	}

	protected boolean canExpand() {
		return true;
	}

	protected boolean hasContents() {
		return canExpand();
	}

	public Section getSection() {
		return commentSection;
	}

	private void fillToolBar(ToolBarManager toolbarManager, boolean expanded) {
		if (!enableToolbar) {
			return;
		}

		List<IReviewAction> toolbarActions = getToolbarActions(expanded);

//		for (Control control : actionsComposite.getChildren()) {
//			if (control instanceof ImageHyperlink) {
//				control.setMenu(null);
//				control.dispose();
//			}
//		}

		toolbarManager.removeAll();

		if (toolbarActions != null) {

			for (final IReviewAction action : toolbarActions) {
				action.setActionListener(actionListener);
				toolbarManager.add(action);
//				ImageHyperlink link = createActionHyperlink(actionsComposite, toolkit, action);
//				if (!action.isEnabled()) {
//					link.setEnabled(false);
//				}
			}
		}
		toolbarManager.markDirty();
		toolbarManager.update(true);
//		actionsComposite.getParent().layout();
	}

	protected ImageHyperlink createActionHyperlink(Composite actionsComposite, FormToolkit toolkit, final IAction action) {

		if (action instanceof IReviewAction) {
			((IReviewAction) action).setActionListener(actionListener);
		}
		ImageHyperlink link = toolkit.createImageHyperlink(actionsComposite, SWT.NONE);
		if (action.getImageDescriptor() != null) {
			link.setImage(AtlassianImages.getImage(action.getImageDescriptor()));
		} else {
			link.setText(action.getText());
		}
		link.setToolTipText(action.getToolTipText());
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				action.run();
			}
		});
		return link;
	}

	/**
	 * @return A composite that image hyperlinks can be placed on
	 */
	protected Composite createSectionAnnotationsAndToolbar(Section section, FormToolkit toolkit) {

		Composite toolbarComposite = toolkit.createComposite(section);
		section.setTextClient(toolbarComposite);
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		toolbarComposite.setLayout(rowLayout);

		Composite annotationsComposite = toolkit.createComposite(toolbarComposite);

		rowLayout = new RowLayout();
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		rowLayout.spacing = 0;

		annotationsComposite.setLayout(rowLayout);

		annotationImageLabel = toolkit.createLabel(annotationsComposite, "");

		annotationsTextLabel = toolkit.createLabel(annotationsComposite, "");

		createCustomAnnotations(annotationsComposite, toolkit);

		updateAnnotationsArea();

//		Composite actionsComposite = toolkit.createComposite(toolbarComposite);
//		actionsComposite.setBackground(null);
//		rowLayout = new RowLayout();
//		rowLayout.marginTop = 0;
//		rowLayout.marginBottom = 0;
//		actionsComposite.setLayout(rowLayout);

		return toolbarComposite;
	}

	public boolean isExpanded() {
		return isExpanded && areChildrenExpanded();
	}

	private boolean areChildrenExpanded() {
		for (V child : childrenParts) {
			if (!child.isExpanded()) {
				return false;
			}
		}
		return true;
	}

	public void setExpanded(boolean expanded) {
		if (expanded != commentSection.isExpanded()) {
			EditorUtil.toggleExpandableComposite(expanded, commentSection);
		}
		for (V child : childrenParts) {
			child.setExpanded(expanded);
		}
	}

	public void hookCustomActionRunListener(IReviewActionListener actionRunListener) {
		this.actionListener = actionRunListener;
	}

	public IReviewActionListener getActionListener() {
		return actionListener;
	}

	protected void createCustomAnnotations(Composite toolbarComposite, FormToolkit toolkit) {
		// default do nothing
	}

	public void disableToolbar() {
		enableToolbar = false;
	}

	public void setIncomming(boolean newIncomming) {
		this.isIncomming = newIncomming;
	}

	public boolean isIncomming() {
		return isIncomming;
	}

	public void decorate() {
		Section highlightedSection = getSection();
		if (highlightedSection != null) {
			Control client = highlightedSection.getClient();
			if (client != null) {
				Color color;
				if (isIncomming()) {
					color = crucibleEditor.getColorIncoming();
				} else {
					color = formToolkit.getColors().getBackground();
				}

				highlightControl(client, color);
			}
		}
	}

	private void highlightControl(Control client, Color highlightColor) {
		if (highlightColor == null || highlightColor.isDisposed()) {
			return;
		}
		if (!client.isDisposed()) {
			if (highlightColor != null) {
				client.setBackground(highlightColor);
			}
			if (client instanceof Composite) {
				for (Control child : ((Composite) client).getChildren()) {
					highlightControl(child, highlightColor);
				}
			}
		}
	}

	public void dispose() {
		if (getSection() != null) {
			getSection().dispose();
		}
	}

	protected final V findPart(T comment) {

		for (V part : childrenParts) {
			if (part.represents(comment)) {
				return part;
			}
		}

		return null;
	}

	protected final void updateChildren(Composite composite, FormToolkit toolkit, boolean shouldHighlight,
			Collection<T> childrenObjects) {
		if (childrenObjects.size() > 0) {
			List<T> generalComments = new ArrayList<T>(childrenObjects);
			Collections.sort(generalComments, getComparator());

			// The following code is almost duplicated in the crucible review files part
			List<V> newParts = new ArrayList<V>();

			Control prevControl = null;

			for (int i = 0; i < generalComments.size(); i++) {
				T comment = generalComments.get(i);

				V oldPart = findPart(comment);

				if (oldPart != null) {
					Control commentControl = oldPart.update(composite, toolkit, comment, crucibleReview);
					if (commentControl != null && !commentControl.isDisposed()) {

						GridDataFactory.fillDefaults().grab(true, false).applyTo(commentControl);

						if (prevControl != null) {
							commentControl.moveBelow(prevControl);
						} else if (composite.getChildren().length > 1) {
							commentControl.moveAbove(composite.getChildren()[1]);
						}
						prevControl = commentControl;
					} else {
						Thread.dumpStack();
					}

					newParts.add(oldPart);
				} else {
					V commentPart = createChildPart(comment, crucibleReview, crucibleEditor);
					commentPart.hookCustomActionRunListener(actionListener);
					newParts.add(commentPart);
					Control commentControl = commentPart.createControl(composite, toolkit);

					if (shouldHighlight && shouldHighlight(comment, crucibleEditor)) {
						commentPart.setIncomming(true);
						commentPart.decorate();
					}

					GridDataFactory.fillDefaults().grab(true, false).applyTo(commentControl);
					if (prevControl != null) {
						commentControl.moveBelow(prevControl);
					} else if (composite.getChildren().length > 1) {
						commentControl.moveAbove(composite.getChildren()[1]);
					}
					prevControl = commentControl;
				}
			}

			List<V> toRemove = new ArrayList<V>();

			for (V part : childrenParts) {
				if (!newParts.contains(part)) {
					toRemove.add(part);
				}
			}

			for (V part : toRemove) {
				part.dispose();
			}

			childrenParts.clear();
			childrenParts.addAll(newParts);
		}
	}

	protected abstract boolean shouldHighlight(T comment, CrucibleReviewEditorPage crucibleEditor2);

	protected abstract V createChildPart(T comment, Review crucibleReview2, CrucibleReviewEditorPage crucibleEditor2);

	protected abstract Control update(Composite parentComposite, FormToolkit toolkit, T newComment, Review newReview);

	protected abstract boolean represents(T comment);

	protected abstract Comparator<T> getComparator();

	protected abstract List<IReviewAction> getToolbarActions(boolean expanded);

	protected abstract String getAnnotationText();

	protected abstract ImageDescriptor getAnnotationImage();

	protected abstract String getSectionHeaderText();

	protected abstract Composite createSectionContents(Section section, FormToolkit toolkit);

}
