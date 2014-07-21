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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

public class GraphiteMRCounterExecutorExecutorTestEmptyValue {

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

	public void setupUDP(final int port) {

		server = new Thread() {

			@Override
			public void run() {
				try {
					DatagramSocket serverSocket = new DatagramSocket(port,
							InetAddress.getByName("localhost"));
					byte[] receiveData = new byte[1024];

					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					graphiteData = new String(receivePacket.getData(), 0,
							receivePacket.getLength(), Charsets.UTF_8);
					serverSocket.close();

				} catch (Exception e) {
					logger.error("", e);
				}
			}

		};

		server.start();

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

	private void runTestUDP(int port ,String fileName, String... ExpectedgraphiteData)
			throws IOException, ActionExecutorException, InterruptedException {
		setupUDP(port);

		final String configuration = Resources.toString(
				Resources.getResource(this.getClass(), fileName),
				Charsets.UTF_8);
		final TestWorkflowAction action = new TestWorkflowAction(configuration);

		fixture.start(new TestContext(), action);
		Thread.sleep(500);

		String[] parts = ((graphiteData != null) ? graphiteData.split("\n", -1)
				: new String[] { "\n   " });

		Assert.assertEquals(ExpectedgraphiteData.length, parts.length - 1);
		for (int i = 0; i < ExpectedgraphiteData.length; i++) {
			Assert.assertEquals(ExpectedgraphiteData[i], parts[i]);
		}
		Assert.assertEquals("", parts[parts.length - 1].trim());
	}

	

	@Test
	public void testGraphiteMRCounterExecutor_with_empty_value()
			throws ActionExecutorException, IOException, InterruptedException {

		runTestUDP(2008 , "GraphiteMRCounterExecutor_with_empty_value.xml",
				new String[] {});

	}

}
