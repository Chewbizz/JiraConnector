/*******************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom field container.
 *
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraCustomField extends JiraIssueField {

    private static final long serialVersionUID = 1L;

    public static final String NONE_ALLOWED_VALUE = "-1"; //$NON-NLS-1$

    private final String key;

    private final List<String> values;

    private boolean readOnly;

    private boolean markupDetected;

    public JiraCustomField(final String id, final String key, final String name, final List<String> values) {
        super(id, name);
        this.key = key;
        this.values = new ArrayList<>(values);
    }

    public String getKey() {
        return key;
    }

    public List<String> getValues() {
        return values;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isMarkupDetected() {
        return markupDetected;
    }

    public void setMarkupDetected(final boolean markupDetected) {
        this.markupDetected = markupDetected;
    }

}
