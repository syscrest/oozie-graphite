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

import org.apache.hadoop.conf.Configuration;
import org.apache.oozie.action.ActionExecutorException;
import org.apache.oozie.service.Services;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class GraphiteMRCounterExecutorExecutorTestHostMissing {

	GraphiteMRCounterExecutor fixture = null;

	String graphiteData = null;

	private Thread server = null;

	@Before
	public void setUp() throws Exception {
		System.setProperty(Services.OOZIE_HOME_DIR, "/tmp");
		final Configuration oozieConf = new Configuration();

		fixture = new GraphiteMRCounterExecutor() {

			@Override
			public Configuration getOozieConf() {
				return oozieConf;
			}

		};
	}

	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {
		graphiteData = null;
		if (server != null) {
			server.stop();
		}
	}

	@Test(expected = ActionExecutorException.class)
	public void testStart_graphite_host_empty() throws ActionExecutorException,
			IOException {

		final String configuration = Resources.toString(Resources.getResource(
				this.getClass(),
				"GraphiteMRCounterExecutor_graphite-host_missing.xml"),
				Charsets.UTF_8);
		final TestWorkflowAction action = new TestWorkflowAction(configuration);

		fixture.start(new TestContext(), action);
	}

}
