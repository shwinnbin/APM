/*-
 * ========================LICENSE_START=================================
 * AEM Permission Management
 * %%
 * Copyright (C) 2018 Cognifide Limited
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package com.cognifide.cq.apm.api.history;

import static com.cognifide.cq.apm.api.history.Entry.SCRIPT_HISTORY_FILE_NAME;

import com.cognifide.cq.apm.api.scripts.Script;
import com.cognifide.cq.apm.core.scripts.ScriptImpl;
import java.util.Date;
import org.apache.sling.api.resource.Resource;

public class HistoryResourceAdapter {

	private Resource resource;

	private Script script;

	public HistoryResourceAdapter(Resource resource) {
		this.resource = resource;
		final Resource scriptResource = this.resource.getChild(SCRIPT_HISTORY_FILE_NAME);
		this.script = scriptResource.adaptTo(ScriptImpl.class);
	}

	public Date getLastModification() {
		return script.getLastModified();
	}

	public Date getLastDryRun() {
		return script.getDryRunLast();
	}

	public Boolean isLastDryRunSuccessful() {
		return script.isDryRunSuccessful();
	}

	public String getFilePath() {
		return String.format("%s/%s", resource.getPath(), SCRIPT_HISTORY_FILE_NAME);
	}
}