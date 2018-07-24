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
package com.cognifide.cq.cqsm.core.history;

import com.cognifide.cq.cqsm.api.logger.ProgressEntry;
import com.cognifide.cq.cqsm.api.logger.Status;
import java.util.Iterator;
import java.util.List;

public final class HistoryHelper {

	public static Boolean isRunSuccessful(List<ProgressEntry> progressSummary) {
		Boolean isSuccessful = Boolean.TRUE;
		final Iterator<ProgressEntry> executionSummaryIterator = progressSummary.iterator();

		while (executionSummaryIterator.hasNext() && isSuccessful) {
			final ProgressEntry operation = executionSummaryIterator.next();
			if (Status.ERROR.equals(operation.getStatus())) {
				isSuccessful = Boolean.FALSE;
			}
		}
		return isSuccessful;
	}



}