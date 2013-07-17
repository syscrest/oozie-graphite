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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.oozie.action.ActionExecutor.Context;
import org.apache.oozie.client.WorkflowAction.Status;
import org.apache.oozie.client.WorkflowJob;
import org.apache.oozie.service.HadoopAccessorException;
import org.apache.oozie.util.ELEvaluator;

public class TestContext implements Context {

	@Override
	public String getCallbackUrl(String externalStatusVar) {
		return null;
	}

	@Override
	public WorkflowJob getWorkflow() {
		return null;
	}

	@Override
	public ELEvaluator getELEvaluator() {
		return null;
	}

	@Override
	public void setVar(String name, String value) {

	}

	@Override
	public String getVar(String name) {
		return null;
	}

	@Override
	public void setStartData(String externalId, String trackerUri,
			String consoleUrl) {

	}

	@Override
	public void setExecutionData(String externalStatus, Properties actionData) {

	}

	@Override
	public void setExecutionStats(String jsonStats) {

	}

	@Override
	public void setExternalChildIDs(String externalChildIDs) {

	}

	@Override
	public void setEndData(Status status, String signalValue) {

	}

	@Override
	public boolean isRetry() {
		return false;
	}

	@Override
	public void setExternalStatus(String externalStatus) {

	}

	@Override
	public String getRecoveryId() {
		return null;
	}

	@Override
	public void setErrorInfo(String str, String exMsg) {

	}

	@Override
	public Configuration getProtoActionConf() {
		return null;
	}

	@Override
	public Path getActionDir() throws HadoopAccessorException, IOException,
			URISyntaxException {
		return null;
	}

	@Override
	public FileSystem getAppFileSystem() throws HadoopAccessorException,
			IOException, URISyntaxException {
		return null;
	}

}
