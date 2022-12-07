/*******************************************************************************
 * Copyright (c) 2004, 2009 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 * @author Jacek Jaroczynski
 */
public enum JiraAttribute {

    ACTUAL(JiraConstants.ATTRIBUTE_ACTUAL, JiraFieldType.TEXTFIELD, Messages.JiraAttribute_Time_Spent, false, true,
            "timespent"), //$NON-NLS-1$

    AFFECTSVERSIONS(JiraConstants.ATTRIBUTE_AFFECTSVERSIONS, JiraFieldType.MULTISELECT,
            Messages.JiraAttribute_Affects_Versions, false, false, "versions"), //$NON-NLS-1$

    COMMENT_NEW(TaskAttribute.COMMENT_NEW, JiraFieldType.TEXTAREA, Messages.JiraAttribute_New_Comment, true, false,
            "comment"), //$NON-NLS-1$

    COMPONENTS(JiraConstants.ATTRIBUTE_COMPONENTS, JiraFieldType.MULTISELECT, Messages.JiraAttribute_Components,
            false, false, "components"), //$NON-NLS-1$

    CREATION_DATE(TaskAttribute.DATE_CREATION, JiraFieldType.DATE, Messages.JiraAttribute_Created),

    DESCRIPTION(TaskAttribute.DESCRIPTION, JiraFieldType.TEXTFIELD, Messages.JiraAttribute_Description, true, false,
            "description"), //$NON-NLS-1$

    DUE_DATE(JiraConstants.ATTRIBUTE_DUE_DATE, JiraFieldType.DATE, Messages.JiraAttribute_DUEDATE, false, false,
            "duedate"), //$NON-NLS-1$

    ENVIRONMENT(JiraConstants.ATTRIBUTE_ENVIRONMENT, JiraFieldType.TEXTAREA, Messages.JiraAttribute_Environment,
            false, false, "environment"), //$NON-NLS-1$

    ESTIMATE(JiraConstants.ATTRIBUTE_ESTIMATE, JiraFieldType.TEXTFIELD, Messages.JiraAttribute_Estimate, false, false,
            "timetracking"), //$NON-NLS-1$

    FIXVERSIONS(JiraConstants.ATTRIBUTE_FIXVERSIONS, JiraFieldType.MULTISELECT, Messages.JiraAttribute_Fix_Versions,
            false, false, "fixVersions"), //$NON-NLS-1$

    INITIAL_ESTIMATE(JiraConstants.ATTRIBUTE_INITIAL_ESTIMATE, JiraFieldType.TEXTFIELD,
            Messages.JiraAttribute_Original_Estimate, false, true),

    ISSUE_KEY(TaskAttribute.TASK_KEY, JiraFieldType.TEXTFIELD, Messages.JiraAttribute_Key),

    LINKED_IDS(JiraConstants.ATTRIBUTE_LINKED_IDS, JiraFieldType.TEXTFIELD, Messages.JiraAttribute_Linked_ids, true,
            true),

    MODIFICATION_DATE(TaskAttribute.DATE_MODIFICATION, JiraFieldType.DATE, Messages.JiraAttribute_Modified),

    PARENT_ID(JiraConstants.ATTRIBUTE_ISSUE_PARENT_ID, JiraFieldType.ISSUELINK, Messages.JiraAttribute_Parent_ID,
            true, true),

    PARENT_KEY(JiraConstants.ATTRIBUTE_ISSUE_PARENT_KEY, JiraFieldType.ISSUELINK, Messages.JiraAttribute_Parent,
            false, true),

    PRIORITY(TaskAttribute.PRIORITY, JiraFieldType.SELECT, Messages.JiraAttribute_Priority, false, false, "priority"), //$NON-NLS-1$

    PROJECT(TaskAttribute.PRODUCT, JiraFieldType.PROJECT, Messages.JiraAttribute_Project, false, true),

    RESOLUTION(TaskAttribute.RESOLUTION, JiraFieldType.SELECT, Messages.JiraAttribute_Resolution, true, false,
            "resolution"), //$NON-NLS-1$

    SECURITY_LEVEL(JiraConstants.ATTRIBUTE_SECURITY_LEVEL, JiraFieldType.SELECT,
            Messages.JiraAttribute_Security_Level, false, false),

    STATUS(TaskAttribute.STATUS, JiraFieldType.SELECT, Messages.JiraAttribute_Status),

    SUBTASK_IDS(JiraConstants.ATTRIBUTE_SUBTASK_IDS, JiraFieldType.TEXTFIELD, Messages.JiraAttribute_Subtask_ids,
            true, true),

    SUBTASK_KEYS(JiraConstants.ATTRIBUTE_SUBTASK_KEYS, JiraFieldType.ISSUELINKS, Messages.JiraAttribute_Subtasks,
            false, true),

    SUMMARY(TaskAttribute.SUMMARY, JiraFieldType.TEXTFIELD, Messages.JiraAttribute_Summary, true, false, "summary"), //$NON-NLS-1$

    TYPE(JiraConstants.ATTRIBUTE_TYPE, JiraFieldType.SELECT, Messages.JiraAttribute_Type, false, false, "issuetype"), //$NON-NLS-1$

    UNKNOWN(null, JiraFieldType.UNKNOWN, Messages.JiraAttribute_unknown, true, true),

    USER_ASSIGNED(TaskAttribute.USER_ASSIGNED, JiraFieldType.USERPICKER, Messages.JiraAttribute_Assigned_to, true,
            false, "assignee"), //$NON-NLS-1$

    USER_REPORTER(TaskAttribute.USER_REPORTER, JiraFieldType.USERPICKER, Messages.JiraAttribute_Reported_by),

    TASK_URL(TaskAttribute.TASK_URL, JiraFieldType.URL, Messages.JiraAttribute_URL),

    PROJECT_ROLES(JiraConstants.ATTRIBUTE_PROJECT_ROLES, JiraFieldType.SELECT, Messages.JiraAttribute_Viewable_by,
            true, false),

    VOTES(JiraConstants.ATTRIBUTE_VOTES, JiraFieldType.TEXTFIELD, Messages.JiraAttribute_Votes, true, true),

    LABELS(JiraConstants.ATTRIBUTE_LABELS, JiraFieldType.LABELS, Messages.JiraAttribute_Labels, false, false),

    RANK(TaskAttribute.RANK, JiraFieldType.RANK, Messages.JiraAttribute_unknown, true, true),

    WATCHERS(JiraConstants.ATTRIBUTE_WATCHERS, JiraFieldType.TEXTAREA, Messages.JiraAttribute_Watchers, true, true);

    public static JiraAttribute valueById(final String id) {
        for (final JiraAttribute attribute : values()) {
            if (id.equals(attribute.id())) {
                return attribute;
            }
        }
        return UNKNOWN;
    }

    private final String id;

    private final boolean isHidden;

    private final boolean isReadOnly;

    private final String name;

    private final String paramName;

    private final JiraFieldType type;

    private JiraAttribute(final String id, final JiraFieldType type, final String name) {
        this.id = id;
        this.type = type;
        this.name = name;
        isHidden = true;
        isReadOnly = true;
        paramName = null;
    }

    private JiraAttribute(final String id, final JiraFieldType type, final String name, final boolean isHidden, final boolean isReadOnly) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.isHidden = isHidden;
        this.isReadOnly = isReadOnly;
        paramName = null;
    }

    private JiraAttribute(final String id, final JiraFieldType type, final String name, final boolean isHidden, final boolean isReadOnly,
            final String paramName) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.isHidden = isHidden;
        this.isReadOnly = isReadOnly;
        this.paramName = paramName;
    }

    public String id() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getParamName() {
        return paramName;
    }

    public JiraFieldType getType() {
        return type;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public String getKind() {
        return isHidden ? null : TaskAttribute.KIND_DEFAULT;
    }

}
