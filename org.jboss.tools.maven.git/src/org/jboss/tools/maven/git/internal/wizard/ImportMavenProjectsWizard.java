/*************************************************************************************
 * Copyright (c) 2011 JBoss by Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.git.internal.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.egit.ui.UIIcons;
import org.eclipse.egit.ui.internal.ConfigurationChecker;
import org.eclipse.egit.ui.internal.clone.GitSelectRepositoryPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.progress.IProgressConstants;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.actions.OpenMavenConsoleAction;
import org.maven.ide.eclipse.project.IMavenProjectImportResult;
import org.maven.ide.eclipse.project.MavenProjectInfo;
import org.maven.ide.eclipse.project.ProjectImportConfiguration;

/**
 * @author snjeza
 * 
 */
public class ImportMavenProjectsWizard extends Wizard implements IImportWizard {

	private GitSelectRepositoryPage selectRepoPage = new GitSelectRepositoryPage();
	private ProjectImportConfiguration importConfiguration;
	private ImportMavenProjectsWizardPage mavenProjectsWizardPage;

	/**
	 * Default constructor
	 */
	public ImportMavenProjectsWizard() {
		setWindowTitle("Import Maven projects from a Git Repository");
		setDefaultPageImageDescriptor(UIIcons.WIZBAN_IMPORT_REPO);
		selectRepoPage.setWizard(this);
		setNeedsProgressMonitor(true);
		ConfigurationChecker.checkConfiguration();
		importConfiguration = new ProjectImportConfiguration();
	}

	@Override
	public void addPages() {
		addPage(selectRepoPage);
		mavenProjectsWizardPage = new ImportMavenProjectsWizardPage(
				importConfiguration);
		addPage(mavenProjectsWizardPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public boolean performFinish() {
		if (!mavenProjectsWizardPage.isPageComplete()) {
			return false;
		}
		final Collection<MavenProjectInfo> projects = mavenProjectsWizardPage
				.getProjects();
		final Repository repository = selectRepoPage.getRepository();
		Job job = new WorkspaceJob("Importing Maven projects") {
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				setProperty(IProgressConstants.ACTION_PROPERTY,
						new OpenMavenConsoleAction());
				List<IMavenProjectImportResult> results = null;
				try {
					results = MavenPlugin
							.getDefault()
							.getProjectConfigurationManager()
							.importProjects(projects, importConfiguration,
									monitor);
					if (repository != null) {
						String repoDir = repository.getDirectory().getParent();
						for (IMavenProjectImportResult result : results) {
							IProject project = result.getProject();
							if (project != null && project.isOpen() && project.getLocation() != null) {
								if (project.getLocation().toOSString()
										.startsWith(repoDir)) {
									ConnectProviderOperation connectProviderOperation = new ConnectProviderOperation(
											project, repository.getDirectory());
									connectProviderOperation.execute(monitor);
								}
							}
						}
					}
				} catch (CoreException ex) {
					MavenPlugin.getDefault().getConsole()
							.logError("Projects imported with errors");
					return ex.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(MavenPlugin.getDefault().getProjectConfigurationManager()
				.getRule());
		job.schedule();
		return true;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == selectRepoPage) {
			Repository repository = selectRepoPage.getRepository();
			File directory = repository.getDirectory();
			if (directory != null) {
				List<String> locations = new ArrayList<String>();
				locations.add(directory.getParent());
				mavenProjectsWizardPage.setLocations(locations);
			}
		}
		return super.getNextPage(page);
	}
}
