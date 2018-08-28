/*-
 * ========================LICENSE_START=================================
 * AEM Permission Management
 * %%
 * Copyright (C) 2013 Cognifide Limited
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
package com.cognifide.cq.apm.core.servlets;

import com.cognifide.cq.apm.api.executors.Mode;
import com.cognifide.cq.apm.api.logger.Progress;
import com.cognifide.cq.apm.api.progress.ProgressHelper;
import com.cognifide.cq.apm.api.scripts.Script;
import com.cognifide.cq.apm.api.scripts.ScriptFinder;
import com.cognifide.cq.apm.api.scripts.ScriptManager;
import com.cognifide.cq.apm.core.Apm;
import com.cognifide.cq.apm.core.utils.ServletUtils;
import java.io.IOException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;

@SlingServlet(paths = {Apm.SERVLET_PREFIX_PATH + "run"}, methods = {"POST"})
@Service
@Properties({@Property(name = Constants.SERVICE_DESCRIPTION, value = "APM Run Servlet"),
		@Property(name = Constants.SERVICE_VENDOR, value = Apm.VENDOR_NAME)})
public class ScriptRunServlet extends SlingAllMethodsServlet {

	@Reference
	private ScriptManager scriptManager;

	@Reference
	private ScriptFinder scriptFinder;

	@Override
	protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {
		ResourceResolver resolver = request.getResourceResolver();
		final String searchPath = request.getParameter("file");
		final String modeName = request.getParameter("mode");

		if (StringUtils.isEmpty(searchPath)) {
			ServletUtils.writeMessage(response, "error",
					"Please set the script file name: -d \"file=[name]\"");
			return;
		}

		if (StringUtils.isEmpty(modeName)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			ServletUtils.writeMessage(response, "error", "Running mode not specified.");
			return;
		}

		final Script script = scriptFinder.find(searchPath, resolver);
		if (script == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			ServletUtils.writeMessage(response, "error", String.format("Script not found: %s", searchPath));
			return;
		}

		try {
			final Mode mode = Mode.fromString(modeName, Mode.DRY_RUN);
			final Progress progressLogger = scriptManager.process(script, mode, resolver);

			if (progressLogger.isSuccess()) {
				ServletUtils.writeJson(response, ProgressHelper.toJson(progressLogger.getEntries()));
			} else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				ServletUtils.writeJson(response, ProgressHelper.toJson(progressLogger.getLastError()));
			}

		} catch (RepositoryException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			ServletUtils.writeMessage(response, "error", String.format("Script cannot be executed because of"
					+ " repository error: %s", e.getMessage()));
		}
	}
}