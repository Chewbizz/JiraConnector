/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.domain.RoleActor;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;


public class RoleActorJsonParser implements JsonObjectParser<RoleActor> {

	private final URI baseJiraUri;

	public RoleActorJsonParser(URI baseJiraUri) {
		this.baseJiraUri = baseJiraUri;
	}

	@Override
	public RoleActor parse(final JSONObject json) throws JSONException {
		final long id = json.getLong("id");
		final String displayName = json.getString("displayName");
		final String type = json.getString("type");
		final String name = json.getString("name");
		return new RoleActor(id, displayName, type, name, parseAvatarUrl(json));
	}

	private URL parseAvatarUrl(final JSONObject json) {
		try {
			final String avatarUrl = JsonParseUtil.getOptionalString(json, "avatarUrl");
			if (avatarUrl != null) {
				URI avatarUri = UriBuilder.fromUri(avatarUrl).build();
				if (avatarUri.isAbsolute()) {
					return avatarUri.toURL();
				} else {
					return UriBuilder.fromUri(baseJiraUri).path(avatarUrl).build().toURL();
				}
			} else {
				return null;
			}
		} catch (MalformedURLException e) {
			return null;
		}
	}

}
