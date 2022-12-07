/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.service;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraServiceUnavailableException extends JiraException {

    private static final long serialVersionUID = -6648244599873827934L;

    public JiraServiceUnavailableException(final String message) {
        super(message == null ? "Service unavailable" : message); //$NON-NLS-1$
    }

    public JiraServiceUnavailableException(final Throwable cause) {
        super(cause);
    }

}
