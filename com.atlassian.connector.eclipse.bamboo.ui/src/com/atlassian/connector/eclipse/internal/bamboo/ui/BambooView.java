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

package com.atlassian.connector.eclipse.internal.bamboo.ui;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildPlanManager;
import com.atlassian.connector.eclipse.internal.bamboo.core.RefreshBuildsForAllRepositoriesJob;
import com.atlassian.connector.eclipse.internal.bamboo.ui.dialogs.AddLabelOrCommentDialog;
import com.atlassian.connector.eclipse.internal.bamboo.ui.dialogs.AddLabelOrCommentDialog.Type;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RetrieveBuildLogsJob;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RetrieveTestResultsJob;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RunBuildJob;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.util.DateUtil;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.internal.junit.model.JUnitModel;
import org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewRepositoryWizard;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.ViewPart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class BambooView extends ViewPart {

	private static final String CREATE_A_NEW_REPOSITORY_LINK = "Create a new Repository";

	private class OpenInBrowserAction extends BaseSelectionListenerAction {
		public OpenInBrowserAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
					Object selected = it.next();
					if (selected instanceof BambooBuild) {
						String url = BambooUtil.getUrlFromBuild((BambooBuild) selected);
						TasksUiUtil.openUrl(url);
					}
				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			return selection.size() > 0;
		}
	}

	private class AddRepositoryConfigurationAction extends Action {
		@Override
		public void run() {
			Display.getDefault().asyncExec(new Runnable() {
				@SuppressWarnings("restriction")
				public void run() {
					NewRepositoryWizard repositoryWizard = new NewRepositoryWizard(BambooCorePlugin.CONNECTOR_KIND);

					WizardDialog repositoryDialog = new TaskRepositoryWizardDialog(getSite().getShell(),
							repositoryWizard);
					repositoryDialog.create();
					repositoryDialog.getShell().setText("Add New Bamboo Repository...");
					repositoryDialog.setBlockOnOpen(true);
					repositoryDialog.open();
				}
			});
		}
	}

	private class OpenRepositoryConfigurationAction extends BaseSelectionListenerAction {
		private TaskRepository repository;

		public OpenRepositoryConfigurationAction() {
			super(null);
		}

		public OpenRepositoryConfigurationAction(TaskRepository repository) {
			super(null);
			this.repository = repository;
		}

		@Override
		public void run() {
			if (repository != null) {
				openConfiguration();
			} else {
				ISelection s = buildViewer.getSelection();
				if (s instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) s;
					Object selected = selection.iterator().next();
					if (selected instanceof BambooBuild) {
						final BambooBuild build = (BambooBuild) selected;
						repository = TasksUi.getRepositoryManager().getRepository(BambooCorePlugin.CONNECTOR_KIND,
								build.getServerUrl());
						openConfiguration();
					}
				}
			}
		}

		private void openConfiguration() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					TasksUiUtil.openEditRepositoryWizard(repository);
				}
			});
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			return selection.size() == 1;
		}
	}

	private class RunBuildAction extends BaseSelectionListenerAction {
		public RunBuildAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				Object selected = selection.iterator().next();
				if (selected instanceof BambooBuild) {
					final BambooBuild build = (BambooBuild) selected;
					if (build != null) {
						RunBuildJob job = new RunBuildJob(build, TasksUi.getRepositoryManager().getRepository(
								BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()));
						job.schedule();

					}
				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			if (selection.size() != 1) {
				return false;
			}
			BambooBuild build = (BambooBuild) selection.iterator().next();
			if (build != null) {
				return build.getEnabled();
			}
			return false;
		}
	}

	private class ShowBuildLogsAction extends BaseSelectionListenerAction {

		public ShowBuildLogsAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				Object selected = selection.iterator().next();
				if (selected instanceof BambooBuild) {
					final BambooBuild build = (BambooBuild) selected;
					if (build != null) {
						RetrieveBuildLogsJob job = new RetrieveBuildLogsJob(build, TasksUi.getRepositoryManager()
								.getRepository(BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()));
						job.addJobChangeListener(new JobChangeAdapter() {
							@Override
							public void done(IJobChangeEvent event) {
								if (event.getResult() == Status.OK_STATUS) {
									byte[] buildLog = ((RetrieveBuildLogsJob) event.getJob()).getBuildLog();
									prepareConsole();
									MessageConsoleStream messageStream = buildLogConsole.newMessageStream();
									messageStream.print(new String(buildLog));
									try {
										messageStream.close();
									} catch (IOException e) {
										StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
												"Failed to close console message stream"));
									}
								}
							}
						});
						job.schedule();

					}
				}
			}
		}

		private void prepareConsole() {
			IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
			IConsole[] existing = consoleManager.getConsoles();
			for (IConsole element : existing) {
				if (BAMBOO_BUILD_LOG_CONSOLE.equals(element.getName())) {
					buildLogConsole = (MessageConsole) element;
				}
			}
			if (buildLogConsole == null) {
				buildLogConsole = new MessageConsole(BAMBOO_BUILD_LOG_CONSOLE, BambooImages.CONSOLE);
				consoleManager.addConsoles(new IConsole[] { buildLogConsole });
			}
			buildLogConsole.clearConsole();
			consoleManager.showConsoleView(buildLogConsole);

		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			return selection.size() == 1;
		}
	}

	private class AddLabelToBuildAction extends BaseSelectionListenerAction {
		public AddLabelToBuildAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				Object selected = selection.iterator().next();
				if (selected instanceof BambooBuild) {
					final BambooBuild build = (BambooBuild) selected;
					if (build != null) {
						AddLabelOrCommentDialog dialog = new AddLabelOrCommentDialog(getSite().getShell(), build,
								TasksUi.getRepositoryManager().getRepository(BambooCorePlugin.CONNECTOR_KIND,
										build.getServerUrl()), Type.LABEL);
						dialog.open();
					}
				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			return selection.size() == 1;
		}
	}

	private class AddCommentToBuildAction extends BaseSelectionListenerAction {
		public AddCommentToBuildAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				Object selected = selection.iterator().next();
				if (selected instanceof BambooBuild) {
					final BambooBuild build = (BambooBuild) selected;
					if (build != null) {
						AddLabelOrCommentDialog dialog = new AddLabelOrCommentDialog(getSite().getShell(), build,
								TasksUi.getRepositoryManager().getRepository(BambooCorePlugin.CONNECTOR_KIND,
										build.getServerUrl()), Type.COMMENT);
						dialog.open();
					}
				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			return selection.size() == 1;
		}
	}

	private class ShowTestResultsAction extends BaseSelectionListenerAction {

		public ShowTestResultsAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				Object selected = selection.iterator().next();
				if (selected instanceof BambooBuild) {
					final BambooBuild build = (BambooBuild) selected;
					if (build != null) {
						RetrieveTestResultsJob job = new RetrieveTestResultsJob(build, TasksUi.getRepositoryManager()
								.getRepository(BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()));
						job.addJobChangeListener(new JobChangeAdapter() {
							@Override
							public void done(IJobChangeEvent event) {
								if (event.getResult() == Status.OK_STATUS) {
									File testResults = ((RetrieveTestResultsJob) event.getJob()).getTestResultsFile();
									if (testResults != null) {
										showJUnitView(testResults);
									}
								}
							}
						});
						job.schedule();

					}
				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			if (selection.size() != 1) {
				return false;
			}
			BambooBuild build = (BambooBuild) selection.iterator().next();
			if (build != null) {
				return (build.getTestsFailed() + build.getTestsPassed()) > 0;
			}
			return false;
		}

		private void showJUnitView(final File testResults) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@SuppressWarnings("restriction")
				public void run() {
					if (!getSite().getShell().isDisposed()) {
						try {
							JUnitModel.importTestRunSession(testResults);
							getViewSite().getPage().showView(TestRunnerViewPart.NAME);
						} catch (Exception e) {
							StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
									"Error opening JUnit View"));
						}
					}
				}
			});
		}
	}

	private class BuildContentProvider implements ITreeContentProvider {

		private List<BambooBuild> allBuilds;

		public void dispose() {
		}

		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		public Object[] getElements(Object inputElement) {
			return allBuilds.toArray();
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			allBuilds = new ArrayList<BambooBuild>();
			if (newInput != null) {
				boolean hasFailed = false;
				for (Collection<BambooBuild> collection : ((Map<TaskRepository, Collection<BambooBuild>>) newInput).values()) {
					allBuilds.addAll(collection);
					for (BambooBuild build : collection) {
						if (build.getStatus() == BuildStatus.FAILURE) {
							hasFailed = true;
						}
					}
				}
				updateViewIcon(hasFailed);
			}
		}
	}

	private static final String BAMBOO_BUILD_LOG_CONSOLE = "Bamboo Build Log";

	public static final String ID = "com.atlassian.connector.eclipse.bamboo.ui.plans";

	private TreeViewer buildViewer;

	private BambooViewDataProvider bambooDataprovider;

	private final Image buildFailedImage = CommonImages.getImage(BambooImages.STATUS_FAILED);

	private final Image buildPassedImage = CommonImages.getImage(BambooImages.STATUS_PASSED);

	private final Image buildDisabledImage = CommonImages.getImage(BambooImages.STATUS_DISABLED);

	private final Image bambooImage = CommonImages.getImage(BambooImages.BAMBOO);

	private Image currentTitleImage = bambooImage;

	private MessageConsole buildLogConsole;

	private Link link;

	private StackLayout stackLayout;

	private Composite treeComp;

	private Composite linkComp;

	private HashMap<String, TaskRepository> linkedRepositories;

	private BaseSelectionListenerAction openInBrowserAction;

	private Action refreshAction;

	private BaseSelectionListenerAction showBuildLogAction;

	private BaseSelectionListenerAction showTestResultsAction;

	private BaseSelectionListenerAction addLabelToBuildAction;

	private BaseSelectionListenerAction addCommentToBuildAction;

	private BaseSelectionListenerAction runBuildAction;

	private Action addRepoConfigAction;

	private BaseSelectionListenerAction openRepoConfigAction;

	@Override
	public void createPartControl(Composite parent) {
		Composite stackComp = new Composite(parent, SWT.NONE);
		stackLayout = new StackLayout();
		stackComp.setLayout(stackLayout);
		treeComp = new Composite(stackComp, SWT.NONE);
		treeComp.setLayout(new FillLayout());
		linkComp = new Composite(stackComp, SWT.NONE);
		linkComp.setLayout(new FillLayout());

		createLink(linkComp);

		createTreeViewer(treeComp);

		stackLayout.topControl = treeComp;
		stackComp.layout();

		createActions();
		fillTreeContextMenu();
		contributeToActionBars();

		bambooDataprovider = BambooViewDataProvider.getInstance();
		bambooDataprovider.setView(this);
	}

	private void createTreeViewer(Composite parent) {
		buildViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		buildViewer.setContentProvider(new BuildContentProvider());
		buildViewer.setUseHashlookup(true);

		TreeViewerColumn column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Build");
		column.getColumn().setWidth(300);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					return ((BambooBuild) element).getBuildName() + " - " + ((BambooBuild) element).getBuildKey();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof BambooBuild) {
					if (((BambooBuild) element).getEnabled()) {
						switch (((BambooBuild) element).getStatus()) {
						case FAILURE:
							return buildFailedImage;
						case SUCCESS:
							return buildPassedImage;
						}
					}
				}
				return buildDisabledImage;
			}
		});

		column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Status");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					BambooBuild build = ((BambooBuild) element);
					int totalTests = build.getTestsFailed() + build.getTestsPassed();
					if (totalTests == 0) {
						return "Tests: Testless build";
					} else {
						return NLS.bind("Tests: {0} out of {1} failed", new Object[] { build.getTestsFailed(),
								totalTests });
					}
				}
				return super.getText(element);
			}
		});

		column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Build Reason");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					BambooBuild build = ((BambooBuild) element);
					return build.getBuildReason();
				}
				return super.getText(element);
			}
		});

		column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Last Built");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					BambooBuild build = ((BambooBuild) element);
					return DateUtil.getRelativeBuildTime(build.getBuildCompletedDate());
				}
				return super.getText(element);
			}
		});

		buildViewer.getTree().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				new OpenInBrowserAction().run();
			}
		});
		//GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(planViewer.getControl());
	}

	private void createLink(Composite parent) {
		link = new Link(parent, SWT.NONE);
		fillLink(TasksUi.getRepositoryManager().getRepositories(BambooCorePlugin.CONNECTOR_KIND));
		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String link = event.text;
				if (link.equals(CREATE_A_NEW_REPOSITORY_LINK)) {
					new AddRepositoryConfigurationAction().run();
				} else if (linkedRepositories != null) {
					TaskRepository repository = linkedRepositories.get(link);
					if (repository != null) {
						new OpenRepositoryConfigurationAction(repository).run();
					}
				}
			}
		});
	}

	private void fillLink(Set<TaskRepository> repositories) {
		if (repositories == null || repositories.size() < 1) {
			link.setText(NLS.bind("There are no Bamboo repositories defined. <a>{0}</a> by following this link.",
					CREATE_A_NEW_REPOSITORY_LINK));
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append("No subscriptions to Bamboo build plans are set. Use the following links to set up your subscriptions:");
			builder.append(System.getProperty("line.separator"));
			linkedRepositories = new HashMap<String, TaskRepository>();
			for (TaskRepository repository : repositories) {
				builder.append(System.getProperty("line.separator"));
				builder.append("Subscribe to plans at ");
				builder.append(repository.getRepositoryLabel());
				builder.append(" <a>");
				String linkKey = NLS.bind("[{0}]", repository.getRepositoryUrl());
				builder.append(linkKey);
				builder.append("</a>");
				linkedRepositories.put(linkKey, repository);
			}
			builder.append(System.getProperty("line.separator"));
			builder.append(System.getProperty("line.separator"));
			builder.append(NLS.bind("You can also <a>{0}</a> by following this link.", CREATE_A_NEW_REPOSITORY_LINK));
			link.setText(builder.toString());
		}
	}

	private void fillTreeContextMenu() {
		MenuManager contextMenuManager = new MenuManager("BAMBOO");
		contextMenuManager.add(openInBrowserAction);
		contextMenuManager.add(new Separator());
		contextMenuManager.add(showBuildLogAction);
		contextMenuManager.add(showTestResultsAction);
		contextMenuManager.add(new Separator());
		contextMenuManager.add(runBuildAction);
		contextMenuManager.add(addLabelToBuildAction);
		contextMenuManager.add(addCommentToBuildAction);
		contextMenuManager.add(new Separator());
		contextMenuManager.add(refreshAction);
		contextMenuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		contextMenuManager.add(new Separator());
		contextMenuManager.add(openRepoConfigAction);
		Menu contextMenu = contextMenuManager.createContextMenu(buildViewer.getControl());
		buildViewer.getControl().setMenu(contextMenu);
		getSite().registerContextMenu(contextMenuManager, buildViewer);
	}

	@Override
	public void setFocus() {
		// ignore

	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillPopupMenu(bars.getMenuManager());
		fillToolBar(bars.getToolBarManager());
	}

	private void fillToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(addRepoConfigAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(refreshAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(openInBrowserAction);
		toolBarManager.add(showBuildLogAction);
		toolBarManager.add(showTestResultsAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(runBuildAction);
	}

	private void createActions() {
		refreshAction = new Action() {
			@Override
			public void run() {
				refreshBuilds();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setImageDescriptor(CommonImages.REFRESH);

		openInBrowserAction = new OpenInBrowserAction();
		openInBrowserAction.setEnabled(false);
		openInBrowserAction.setText("Open in Browser");
		openInBrowserAction.setImageDescriptor(CommonImages.BROWSER_SMALL);
		buildViewer.addSelectionChangedListener(openInBrowserAction);

		showBuildLogAction = new ShowBuildLogsAction();
		showBuildLogAction.setText("Show Build Log");
		showBuildLogAction.setImageDescriptor(BambooImages.CONSOLE);
		showBuildLogAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(showBuildLogAction);

		showTestResultsAction = new ShowTestResultsAction();
		showTestResultsAction.setText("Show Test Results");
		showTestResultsAction.setImageDescriptor(BambooImages.JUNIT);
		showTestResultsAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(showTestResultsAction);

		addLabelToBuildAction = new AddLabelToBuildAction();
		addLabelToBuildAction.setText("Add Label to Build...");
		addLabelToBuildAction.setImageDescriptor(BambooImages.LABEL);
		addLabelToBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(addLabelToBuildAction);

		addCommentToBuildAction = new AddCommentToBuildAction();
		addCommentToBuildAction.setText("Add Comment to Build...");
		addCommentToBuildAction.setImageDescriptor(BambooImages.COMMENT);
		addCommentToBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(addCommentToBuildAction);

		runBuildAction = new RunBuildAction();
		runBuildAction.setText("Run Build");
		runBuildAction.setImageDescriptor(BambooImages.RUN_BUILD);
		runBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(runBuildAction);

		addRepoConfigAction = new AddRepositoryConfigurationAction();
		addRepoConfigAction.setText("Add Bamboo Repository...");
		addRepoConfigAction.setImageDescriptor(BambooImages.ADD_REPOSITORY);

		openRepoConfigAction = new OpenRepositoryConfigurationAction();
		openRepoConfigAction.setText("Properties...");
		openRepoConfigAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(openRepoConfigAction);

		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), openRepoConfigAction);
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
	}

	private void refresh(Map<TaskRepository, Collection<BambooBuild>> map) {
		boolean hasSubscriptions = false;
		for (Collection<BambooBuild> builds : map.values()) {
			if (builds.size() > 0) {
				hasSubscriptions = true;
				break;
			}
		}
		boolean isTreeShown = stackLayout.topControl == treeComp;
		if (hasSubscriptions && !isTreeShown) {
			stackLayout.topControl = treeComp;
			treeComp.getParent().layout();
		} else if (!hasSubscriptions) { //refresh link widget even if it is already shown to display updated repositories
			fillLink(map.keySet());
			stackLayout.topControl = linkComp;
			linkComp.getParent().layout();
		}
		buildViewer.setInput(map);
	}

	private void fillPopupMenu(IMenuManager menuManager) {
	}

	public void buildsChanged() {
		if (bambooDataprovider.getBuilds() != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refresh(bambooDataprovider.getBuilds());
				}
			});
		}
	}

	private void updateViewIcon(boolean buildsFailed) {
		if (buildsFailed) {
			currentTitleImage = buildFailedImage;
		} else {
			currentTitleImage = bambooImage;
		}
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

	/*
	 * @see IWorkbenchPart#getTitleImage()
	 */
	@Override
	public Image getTitleImage() {
		if (currentTitleImage == null) {
			return super.getTitleImage();
		}
		return currentTitleImage;
	}

	private void refreshBuilds() {
		RefreshBuildsForAllRepositoriesJob job = new RefreshBuildsForAllRepositoriesJob("Refreshing builds",
				TasksUi.getRepositoryManager());
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				if (((RefreshBuildsForAllRepositoriesJob) event.getJob()).getStatus().isOK()) {
					BuildPlanManager.getInstance().handleFinishedRefreshAllBuildsJob(event);
				}
			}
		});
		job.schedule();
	}
}
