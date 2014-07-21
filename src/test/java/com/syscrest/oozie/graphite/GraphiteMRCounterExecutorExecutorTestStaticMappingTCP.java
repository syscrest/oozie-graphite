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
import java.net.ServerSocket;
import java.net.Socket;
import junit.framework.Assert;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.oozie.action.ActionExecutorException;
import org.apache.oozie.service.Services;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class GraphiteMRCounterExecutorExecutorTestStaticMappingTCP {

	private final Logger logger = Logger.getLogger(getClass());

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

	public void setupTCP(final int port) {
		server = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					ServerSocket serverSocket = new ServerSocket(port);
					byte[] receiveData = new byte[1024];

					Socket connectionSocket = serverSocket.accept();
					int length = connectionSocket.getInputStream().read(
							receiveData);
					graphiteData = new String(receiveData, 0, length,
							Charsets.UTF_8);

					connectionSocket.close();
					serverSocket.close();

				} catch (Exception e) {
					logger.error("", e);
				}
			}
		});

		server.start();
	}

	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {
		graphiteData = null;
		if (server != null) {
			server.stop();
		}
	}

	private void runTestTCP(int port, String fileName,
			String... expectedGraphiteData) throws IOException,
			ActionExecutorException, InterruptedException {
		setupTCP(port);
		final String configuration = Resources.toString(
				Resources.getResource(this.getClass(), fileName),
				Charsets.UTF_8);
		final TestWorkflowAction action = new TestWorkflowAction(configuration);

		fixture.start(new TestContext(), action);
		Thread.sleep(500);

		String[] parts = ((graphiteData != null) ? graphiteData.split("\n", -1)
				: new String[] { "\n" });

		Assert.assertEquals(expectedGraphiteData.length, parts.length - 1);
		for (int i = 0; i < expectedGraphiteData.length; i++) {
			Assert.assertEquals(expectedGraphiteData[i], parts[i]);
		}
		Assert.assertEquals("", parts[parts.length - 1].trim());
	}

	@Test
	public void testGraphiteMRCounterExecutor_with_static_mapping_tcp()
			throws ActionExecutorException, IOException, InterruptedException {

		runTestTCP(2004,
				"GraphiteMRCounterExecutor_with_static_mapping_tcp.xml",
				"graphite-prefix.static-name 1234 1369263600");

	}

}
