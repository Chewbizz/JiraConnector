/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.IssueCollector;

/**
 * @author Mik Kersten
 */
class JiraIssueCollector implements IssueCollector {

    private final IProgressMonitor monitor;

    private final List<JiraIssue> issues;

    private boolean done = false;

    private final int maxHits;

    JiraIssueCollector(final IProgressMonitor monitor, final List<JiraIssue> issues, final int maxHits) {
        this.monitor = monitor;
        this.issues = issues;
        this.maxHits = maxHits;
    }

    @Override
    public void start() {
    }

    @Override
    public void done() {
        done = true;
    }

    @Override
    public boolean isCancelled() {
        return monitor.isCanceled();
    }

    @Override
    public void collectIssue(final JiraIssue issue) {
        issues.add(issue);
    }

    public boolean isDone() {
        return done;
    }

    @Override
    public int getMaxHits() {
        return maxHits;
    }

}
