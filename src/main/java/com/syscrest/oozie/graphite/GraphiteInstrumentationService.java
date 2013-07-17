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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.oozie.ErrorCode;
import org.apache.oozie.service.InstrumentationService;
import org.apache.oozie.service.SchedulerService;
import org.apache.oozie.service.Service;
import org.apache.oozie.service.ServiceException;
import org.apache.oozie.service.Services;
import org.apache.oozie.util.Instrumentation;
import org.apache.oozie.util.XLog;

import com.syscrest.oozie.graphite.GraphiteLogger.Transport;

public class GraphiteInstrumentationService implements Service {

	private final XLog LOG = XLog.getLog(getClass());

	public static final String CONF_PREFIX = "com.syscrest.oozie.graphite.GraphiteInstrumentationService.";

	public static final String CONF_LOGGING_INTERVAL = CONF_PREFIX
			+ "logging.interval";

	public static final String CONF_GRAPHITE_HOST = CONF_PREFIX
			+ "graphite.host";

	public static final String CONF_GRAPHITE_PORT = CONF_PREFIX
			+ "graphite.port";

	public static final String CONF_GRAPHITE_TRANSPORT = CONF_PREFIX
			+ "graphite.transport";

	public static final String CONF_GRAPHITE_PATH_PREFIX = CONF_PREFIX
			+ "graphite.pathPrefix";

	public static final String CONF_GRAPHITE_METRICS_WHITELIST = CONF_PREFIX
			+ "metrics.whitelist";

	public static final String CONF_GRAPHITE_METRICS_BLACKLIST = CONF_PREFIX
			+ "metrics.blacklist";

	@Override
	public void init(Services services) throws ServiceException {

		final InstrumentationService instrumentationService = services
				.get(InstrumentationService.class);
		if (instrumentationService == null) {
			throw new ServiceException(ErrorCode.E0100, getClass().getName(),
					"InstrumentationService unavailable");
		}

		SchedulerService schedulerService = services
				.get(SchedulerService.class);
		if (schedulerService == null) {
			throw new ServiceException(ErrorCode.E0100, getClass().getName(),
					"SchedulerService unavailable");
		}

		String graphiteHost = services.getConf().get(CONF_GRAPHITE_HOST, null);
		if (graphiteHost == null) {
			throw new ServiceException(ErrorCode.E0100, getClass().getName(),
					"can not handle graphite host configuration ("
							+ CONF_GRAPHITE_HOST
							+ "), make sure to provide exactly one hostname");
		}

		String port = services.getConf().get(CONF_GRAPHITE_PORT, null);
		Integer graphitePort = null;
		if (port != null) {
			graphitePort = Integer.valueOf(port);
		}

		String transportAsString = services.getConf().get(
				CONF_GRAPHITE_TRANSPORT, null);

		GraphiteLogger.Transport graphiteTransport = null;
		if (transportAsString != null) {
			graphiteTransport = Transport.valueOf(transportAsString
					.toUpperCase().trim());
		}

		String pathPrefix = services.getConf().get(CONF_GRAPHITE_PATH_PREFIX,
				null);
		if (pathPrefix == null) {
			throw new ServiceException(ErrorCode.E0100, getClass().getName(),
					"can not handle path prefix configuration ("
							+ CONF_GRAPHITE_HOST
							+ "), make sure to provide exactly one");
		}
		while (pathPrefix.endsWith(".")) {
			LOG.info("path prefix should not end with a dot, removing it");
			pathPrefix = pathPrefix.substring(0, pathPrefix.length() - 1);
			LOG.info("new path prefix \"" + pathPrefix + "\"");
		}

		List<Pattern> whitelistPatterns = new ArrayList<Pattern>();
		try {
			for (String p : services.getConf().getStrings(
					CONF_GRAPHITE_METRICS_WHITELIST, new String[] { ".*" })) {
				whitelistPatterns.add(Pattern.compile(p));
			}
		} catch (PatternSyntaxException pe) {
			LOG.error("whitelist pattern error", pe);
			throw new ServiceException(ErrorCode.E0100, getClass().getName(),
					"whitelist pattern error", pe);
		}

		List<Pattern> blacklistPatterns = new ArrayList<Pattern>();
		try {
			for (String p : services.getConf().getStrings(
					CONF_GRAPHITE_METRICS_BLACKLIST, new String[] {})) {
				blacklistPatterns.add(Pattern.compile(p));
			}
		} catch (PatternSyntaxException pe) {
			LOG.error("blacklist pattern error", pe);
			throw new ServiceException(ErrorCode.E0100, getClass().getName(),
					"blacklist pattern error", pe);
		}

		try {

			int interval = services.getConf().getInt(CONF_LOGGING_INTERVAL, 60);
			if (interval > 0) {
				schedulerService.schedule(new Runner(instrumentationService,
						graphiteHost, graphitePort, graphiteTransport,
						pathPrefix, whitelistPatterns, blacklistPatterns),
						interval, interval, SchedulerService.Unit.SEC);
				LOG.info("GraphiteInstrumentationService setup complete (configured interval = "
						+ interval + " seconds)");
			} else {
				LOG.info("GraphiteInstrumentationService disabled (interval < 1)");
			}
		} catch (Exception e) {
			LOG.error(
					"while initializing Graphite logger and scheduling Runner",
					e);
			throw new ServiceException(
					ErrorCode.E0100,
					getClass().getName(),
					"caught exeception on initialisation of graphite logger + scheduler",
					e);
		}

	}

	@Override
	public void destroy() {
		// nothing to clean up
	}

	@Override
	public Class<? extends Service> getInterface() {
		return GraphiteInstrumentationService.class;
	}

	private static class Runner implements Runnable {

		XLog LOG = XLog.getLog(Runner.class);

		InstrumentationService service = null;

		String pathPrefix = null;

		private String graphiteHost = null;

		private Integer graphitePort = null;

		private GraphiteLogger.Transport graphiteTransport = null;

		List<Pattern> whiteList = null;

		List<Pattern> blackList = null;

		Map<String, Object> stats = null;

		public Runner(InstrumentationService service, String host,
				Integer port, GraphiteLogger.Transport transport,
				String pathPrefix, List<Pattern> whiteList,
				List<Pattern> blackList) {
			this.service = service;
			this.graphiteHost = host;
			this.graphitePort = port;
			this.graphiteTransport = transport;
			this.pathPrefix = pathPrefix;
			this.whiteList = whiteList;
			this.blackList = blackList;

		}

		public void run() {
			try {
				final GraphiteLogger graphiteLogger = new GraphiteLogger(
						graphiteHost, graphitePort, graphiteTransport);

				stats = new TreeMap<String, Object>();
				for (Map.Entry<String, Map<String, Map<String, Object>>> data1 : service
						.get().getAll().entrySet()) {
					for (Map.Entry<String, Map<String, Object>> data2 : data1
							.getValue().entrySet()) {
						for (Map.Entry<String, Object> data3 : data2.getValue()
								.entrySet()) {
							String basePath = pathPrefix + "." + data1.getKey()
									+ "." + data2.getKey() + "."
									+ data3.getKey();
							Object value = data3.getValue();
							transform(basePath, value);
						}
					}
				}
				if (!stats.isEmpty()) {
					if (!graphiteLogger.log(System.currentTimeMillis() / 1000,
							stats)) {
						LOG.warn("could not send instrumentation data to graphite");
					}
				}
			} catch (Throwable ex) {
				LOG.warn(
						"error while pushing instrumentation data into graphite",
						ex);
			}
		}

		private void transform(String basePath, Object value) {
			if (value instanceof String || value instanceof Boolean) {
				return;
			} else if (value instanceof Number) {
				add(basePath, value);
			} else if (value instanceof Instrumentation.Element<?>) {
				Object elementValue = ((Instrumentation.Element<?>) value)
						.getValue();
				if (elementValue instanceof Instrumentation.Timer) {
					Instrumentation.Timer timer = (Instrumentation.Timer) elementValue;
					add(basePath + ".own", timer.getOwn());
					add(basePath + ".ownAvg", timer.getOwnAvg());
					add(basePath + ".ownMax", timer.getOwnMax());
					add(basePath + ".ownMin", timer.getOwnMin());
					add(basePath + ".ownSquareSum", timer.getOwnSquareSum());
					add(basePath + ".ownStdDev", timer.getOwnStdDev());
					add(basePath + ".ticks", timer.getTicks());
					add(basePath + ".total", timer.getTotal());
					add(basePath + ".totalAvg", timer.getTotalAvg());
					add(basePath + ".totalMax", timer.getTotalMax());
					add(basePath + ".totalMin", timer.getTotalMin());
					add(basePath + ".totalSquareSum", timer.getTotalSquareSum());
					add(basePath + ".totalStdDev", timer.getTotalStdDev());
				} else {
					transform(basePath, elementValue);
				}
			} else {
				LOG.warn("can not handle " + basePath + " = " + value);
			}
		}

		private void add(String metricName, Object value) {
			for (Pattern wp : whiteList) {
				if (wp.matcher(metricName).matches()) {
					for (Pattern bp : blackList) {
						if (bp.matcher(metricName).matches()) {
							return;
						}
					}
					if (stats.put(metricName, value) != null) {
						throw new RuntimeException("duplicate metric name \""
								+ metricName + "\"");
					}
					break;
				}
			}
		}
	}
}
