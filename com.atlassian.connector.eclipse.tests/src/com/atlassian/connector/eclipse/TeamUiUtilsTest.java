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

package com.atlassian.connector.eclipse;

import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.spartez.util.junit3.TestUtil;

import junit.framework.TestCase;

public class TeamUiUtilsTest extends TestCase {

	public void testGetSupportedTeamConnectors() {
		TestUtil.assertHasOnlyElements(TeamUiUtils.getSupportedTeamConnectors(), "Subversive", "Subclipse",
				"Team API (partial support)", "CVS (FishEye only)");
	}

}
