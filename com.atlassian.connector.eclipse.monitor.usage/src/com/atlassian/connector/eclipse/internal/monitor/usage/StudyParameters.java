/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Leah Findlater - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.monitor.usage;

import java.util.Collection;

/**
 * @author Mik Kersten
 * @author Leah Findlater
 */
public class StudyParameters {

	private String title = UiUsageMonitorPlugin.DEFAULT_TITLE;

	private String description = UiUsageMonitorPlugin.DEFAULT_DESCRIPTION;

	private String acceptedUrlList = UiUsageMonitorPlugin.DEFAULT_ACCEPTED_URL_LIST;

	private String useContactField = UiUsageMonitorPlugin.DEFAULT_CONTACT_CONSENT_FIELD;

	private String formsConsent = UiUsageMonitorPlugin.DEFAULT_ETHICS_FORM;

	private String version = UiUsageMonitorPlugin.DEFAULT_VERSION;

	private Collection<UsageCollector> usageCollectors;

	public String getFormsConsent() {
		return formsConsent;
	}

	public void setFormsConsent(String formsConsent) {
		if (formsConsent != null) {
			this.formsConsent = formsConsent;
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (description != null) {
			this.description = description;
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		if (title != null) {
			this.title = title;
		}
	}

	public String getAcceptedUrlList() {
		return this.acceptedUrlList;
	}

	public void setAcceptedUrlList(String acceptedUrlList) {
		if (acceptedUrlList != null) {
			this.acceptedUrlList = acceptedUrlList;
		}
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		if (version != null) {
			this.version = version;
		}
	}

	public String getUseContactField() {
		return useContactField;
	}

	public void setUseContactField(String useContactField) {
		if (useContactField != null) {
			this.useContactField = useContactField;
		}
	}

	public void setUsageCollectors(Collection<UsageCollector> usageCollectors) {
		this.usageCollectors = usageCollectors;
	}

	public Collection<UsageCollector> getUsageCollectors() {
		return usageCollectors;
	}
}
