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

import java.util.List;

import org.maven.ide.eclipse.project.ProjectImportConfiguration;
import org.maven.ide.eclipse.wizards.MavenImportWizardPage;

/**
 * @author snjeza
 * 
 */
public class ImportMavenProjectsWizardPage extends MavenImportWizardPage {

	public ImportMavenProjectsWizardPage(
			ProjectImportConfiguration importConfiguration) {
		super(importConfiguration);
	}

	@Override
	public void setLocations(List<String> locations) {
		super.setLocations(locations);
		rootDirectoryCombo.setItems(locations.toArray(new String[0]));
		rootDirectoryCombo.select(0);
		scanProjects();
	}

}
