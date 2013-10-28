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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.oozie.util.XLog;

public class GraphiteLogger {

	enum Transport {
		TCP(2003), UDP(2003);

		int standardPort = -1;

		Transport(int port) {
			this.standardPort = port;
		}

		public int getStandardPort() {
			return this.standardPort;
		}

	}

	private final static Charset UTF8 = Charset.forName("UTF-8");

	private final XLog LOGGER = XLog.getLog(getClass());

	private String graphiteHost;

	private int graphitePort;

	private Transport transport;

	private int batchSize;

	private InetAddress address;

	public GraphiteLogger(String graphiteHost) throws Exception {
		this(graphiteHost, Transport.TCP.getStandardPort(), Transport.TCP, 20);
	}

	public GraphiteLogger(String graphiteHost, Integer graphitePort,
			Transport transport) throws Exception {
		this(graphiteHost, graphitePort, transport, 20);
	}

	public GraphiteLogger(String graphiteHost, Integer graphitePort,
			Transport transport, int batchSize) throws Exception {

		this.graphiteHost = graphiteHost;

		this.transport = transport;
		if (this.transport == null) {
			this.transport = Transport.TCP; // defaulting to TCP
		}

		if (graphitePort == null) {
			this.graphitePort = this.transport.getStandardPort();
		} else {
			this.graphitePort = graphitePort;
		}
		this.batchSize = batchSize;

		if (this.graphiteHost == null || this.graphiteHost.isEmpty()) {
			throw new RuntimeException("incomplete configuration "
					+ this.toString());
		}
		address = InetAddress.getByName(graphiteHost);
	}

	public boolean log(long unixTimeStamp, Map<String, ?> stats) {
		if (stats.isEmpty()) {
			return true;
		}
		try {
			StringBuffer buffer = new StringBuffer();
			int count = 0;
			for (Map.Entry<String, ?> stat : stats.entrySet()) {
				buffer.append(stat.getKey()).append(" ")
						.append(stat.getValue()).append(" ")
						.append(unixTimeStamp).append("\n");
				count++;
				if (count == batchSize) {
					send(buffer);
					count = 0;
					buffer = new StringBuffer();
				}
			}
			if (count > 0) {
				send(buffer);
			}

		} catch (Throwable t) {
			LOGGER.warn("Can't log to graphite ", t);
			return false;
		}
		return true;
	}

	private void send(StringBuffer buffer) throws IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("\n" + buffer);
		}
		if (Transport.UDP.equals(this.transport)) {
			sendUDP(buffer);
		} else if (Transport.TCP.equals(this.transport)) {
			sendTCP(buffer);
		} else {
			throw new RuntimeException("can not handle " + this.transport);
		}

	}

	private void sendUDP(StringBuffer buffer) throws IOException {

		byte[] bytes = buffer.toString().getBytes(UTF8);
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
				address, graphitePort);
		DatagramSocket dsocket = new DatagramSocket();
		try {
			dsocket.send(packet);
		} finally {
			dsocket.close();
		}
	}

	private void sendTCP(StringBuffer buffer) throws IOException {

		byte[] bytes = buffer.toString().getBytes(UTF8);
		Socket clientSocket = null;
		try {
			clientSocket = new Socket(this.graphiteHost, this.graphitePort);
			DataOutputStream outToServer = new DataOutputStream(
					clientSocket.getOutputStream());
			outToServer.write(bytes);
			outToServer.flush();
		} finally {
			if (clientSocket != null) {
				clientSocket.close();
			}
		}

	}

	@Override
	public String toString() {
		return "GraphiteLogger [graphiteHost=" + graphiteHost
				+ ", graphitePort=" + graphitePort + ", transport=" + transport
				+ ", batchSize=" + batchSize + "]";
	}

}