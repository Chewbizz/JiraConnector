/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;

/**
 * @author Brock Janiczak
 */
public class JiraGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    private JiraUser[] users;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public JiraUser[] getUsers() {
        return users;
    }

    public void setUsers(final JiraUser[] users) {
        this.users = users;
    }
}
