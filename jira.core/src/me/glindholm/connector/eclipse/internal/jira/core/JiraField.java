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

import me.glindholm.connector.eclipse.internal.jira.core.TaskSchema.TaskField;

public class JiraField<T> extends TaskField<T> {

    public JiraField(final Class<T> clazz, final String key, final String javaKey, final String label, final String type) {
        super(clazz, key, javaKey, label, type);
    }

}
