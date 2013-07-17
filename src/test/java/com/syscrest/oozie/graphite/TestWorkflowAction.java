/*
 * Copyright 2013 Thomas Memenga - Syscrest GmbH
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.syscrest.oozie.graphite;

import java.util.Date;

import org.apache.oozie.client.WorkflowAction;

public class TestWorkflowAction implements WorkflowAction {

	String conf = null;

	public TestWorkflowAction(String configuration) {
		conf = configuration;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getCred() {
		return null;
	}

	@Override
	public String getType() {
		return null;
	}

	@Override
	public String getConf() {
		return conf;
	}

	@Override
	public Status getStatus() {
		return null;
	}

	@Override
	public int getRetries() {
		return 0;
	}

	@Override
	public int getUserRetryCount() {
		return 0;
	}

	@Override
	public int getUserRetryMax() {
		return 0;
	}

	@Override
	public int getUserRetryInterval() {
		return 0;
	}

	@Override
	public Date getStartTime() {
		return null;
	}

	@Override
	public Date getEndTime() {
		return null;
	}

	@Override
	public String getTransition() {
		return null;
	}

	@Override
	public String getData() {
		return null;
	}

	@Override
	public String getStats() {
		return null;
	}

	@Override
	public String getExternalChildIDs() {
		return null;
	}

	@Override
	public String getExternalId() {
		return null;
	}

	@Override
	public String getExternalStatus() {
		return null;
	}

	@Override
	public String getTrackerUri() {
		return null;
	}

	@Override
	public String getConsoleUrl() {
		return null;
	}

	@Override
	public String getErrorCode() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

}
