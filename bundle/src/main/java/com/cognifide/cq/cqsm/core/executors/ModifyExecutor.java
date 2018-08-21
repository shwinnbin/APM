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
package com.cognifide.cq.cqsm.core.executors;

import com.cognifide.cq.cqsm.api.executors.Mode;
import com.cognifide.cq.cqsm.api.logger.Progress;
import com.cognifide.cq.cqsm.api.scripts.EventListener;
import com.cognifide.cq.cqsm.api.scripts.Script;
import com.cognifide.cq.cqsm.api.scripts.ScriptFinder;
import com.cognifide.cq.cqsm.api.scripts.ScriptManager;
import com.cognifide.cq.cqsm.core.Apm;
import com.cognifide.cq.cqsm.core.utils.MessagingUtils;
import com.cognifide.cq.cqsm.core.utils.sling.OperateCallback;
import com.cognifide.cq.cqsm.core.utils.sling.SlingHelper;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.jcr.RepositoryException;

import static com.cognifide.cq.cqsm.core.scripts.ScriptFilters.filterOnModify;

@Component(immediate = true)
@Properties({@Property(name = Constants.SERVICE_DESCRIPTION, value = "CQSM Script Modification Executor"),
		@Property(name = Constants.SERVICE_VENDOR, value = Apm.VENDOR_NAME)})
public class ModifyExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(ModifyExecutor.class);

	/**
	 * Reference needed for proper event hook up on activation
	 */
	@Reference
	private EventListener eventListener;

	@Reference
	private ScriptManager scriptManager;

	@Reference
	private ScriptFinder scriptFinder;

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Activate
	private synchronized void activate(ComponentContext ctx) {
		SlingHelper.operateTraced(resolverFactory, new OperateCallback() {
			@Override
			public void operate(ResourceResolver resolver) throws Exception {
				runModified(resolver);
			}
		});
	}

	private void runModified(ResourceResolver resolver) throws PersistenceException {
		final List<Script> scripts = scriptFinder.findAll(filterOnModify(resolver), resolver);

		if (scripts.size() > 0) {
			LOG.info("Executor will try to run following scripts: {}", scripts.size());
			LOG.info(MessagingUtils.describeScripts(scripts));

			for (Script script : scripts) {
				runSafe(resolver, script);
			}
		} else {
			LOG.info("Executor has not detected any changes");
		}
	}

	private void runSafe(ResourceResolver resolver, Script script) throws PersistenceException {
		final String scriptPath = script.getPath();

		try {
			scriptManager.process(script, Mode.VALIDATION, resolver);
			if (script.isValid()) {
				Progress progress = scriptManager.process(script, Mode.AUTOMATIC_RUN, resolver);
				logStatus(scriptPath, progress.isSuccess());
			} else {
				LOG.warn(String.format("Executor won't execute script - it is not valid: %s", scriptPath));
			}
		} catch (RepositoryException e) {
			LOG.error("Script cannot be processed because of repository error: {}", scriptPath, e);
		}
	}

	private void logStatus(String scriptPath, boolean success) {
		if (success) {
			LOG.info("Script successfully executed: {}", scriptPath);
		} else {
			LOG.error("Script cannot be executed properly: {}", scriptPath);
		}
	}
}
