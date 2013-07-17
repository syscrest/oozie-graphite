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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.oozie.action.ActionExecutor;
import org.apache.oozie.action.ActionExecutorException;
import org.apache.oozie.action.ActionExecutorException.ErrorType;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.util.DateUtils;
import org.apache.oozie.util.XLog;
import org.apache.oozie.util.XmlUtils;
import org.jdom.Element;
import org.json.simple.JSONValue;

public class GraphiteMRCounterExecutor extends ActionExecutor {

	private final XLog LOGGER = XLog.getLog(getClass());

	public static final String ACTION_TYPE = "graphite-mr-counter";

	public static final String ATTRIBUTE_GRAPHITE_HOST = "graphite-host";
	public static final String ATTRIBUTE_GRAPHITE_PORT = "graphite-port";
	public static final String ATTRIBUTE_GRAPHITE_TRANSPORT = "graphite-transport";
	public static final String ATTRIBUTE_METRICS_PATH_PREFIX = "metrics-path-prefix";
	public static final String ATTRIBUTE_NOMINAL_TIME = "nominal-time";

	public static final String ELEMENT_NAME_COUNTER = "counter";
	public static final String ELEMENT_NAME_SOURCE = "source";
	public static final String ELEMENT_NAME_MAPPING = "mapping";

	public static final String MAPPING_ELEMENT_ATTRIBUTE_RENAME_TO = "rename-to";
	public static final String MAPPING_ELEMENT_ATTRIBUTE_MATCHES = "matches";

	public static final Pattern DOT_ALL_PATTERN = Pattern.compile(".*");

	private GraphiteLogger graphiteLogger;

	private String metricsPathPrefix;

	private TreeMap<String, Long> graphiteData = new TreeMap<String, Long>();

	public GraphiteMRCounterExecutor() {
		super(ACTION_TYPE);
	}

	public GraphiteMRCounterExecutor(String type, long retryInterval) {
		super(type, retryInterval);

	}

	public GraphiteMRCounterExecutor(String type) {
		super(type);

	}

	@Override
	public void start(final Context context, final WorkflowAction action)
			throws ActionExecutorException {
		try {

			Element actionXml = XmlUtils.parseXml(action.getConf());

			if (actionXml.getAttributeValue(ATTRIBUTE_GRAPHITE_PORT) == null) {
				// defaulting to TCP transport and standard port (2003)
				if (actionXml.getAttributeValue(ATTRIBUTE_GRAPHITE_TRANSPORT) == null) {
					graphiteLogger = new GraphiteLogger(
							actionXml
									.getAttributeValue(ATTRIBUTE_GRAPHITE_HOST),
							null, null);
				} else {
					// use provided transport + standard port
					graphiteLogger = new GraphiteLogger(
							actionXml
									.getAttributeValue(ATTRIBUTE_GRAPHITE_HOST),
							null,
							GraphiteLogger.Transport.valueOf(actionXml
									.getAttributeValue(ATTRIBUTE_GRAPHITE_TRANSPORT)));
				}
			} else {
				graphiteLogger = new GraphiteLogger(
						actionXml.getAttributeValue(ATTRIBUTE_GRAPHITE_HOST),
						Integer.parseInt(actionXml
								.getAttributeValue(ATTRIBUTE_GRAPHITE_PORT)),
						GraphiteLogger.Transport.valueOf(actionXml
								.getAttributeValue(ATTRIBUTE_GRAPHITE_TRANSPORT)));
			}

			metricsPathPrefix = actionXml
					.getAttributeValue(ATTRIBUTE_METRICS_PATH_PREFIX);

			// downgrading to oozie 3.1.3:
			// use DateUtils.parseDateUTC
			// (note: oozie 3.1.3 has no explicit timezone
			// configuration and use UTC per default)
			//
			long unixTimestamp = DateUtils.parseDateOozieTZ(
					actionXml.getAttributeValue(ATTRIBUTE_NOMINAL_TIME))
					.getTime() / 1000;

			@SuppressWarnings("unchecked")
			List<Element> children = actionXml.getChildren();
			for (Element child : children) {
				if (ELEMENT_NAME_COUNTER.equals(child.getName())) {
					Counter counter = new Counter(child);
					if (counter.getSource() != null) {
						apply(counter);
					}
				}
			}
			if (!graphiteLogger.log(unixTimestamp, graphiteData)) {
				throw new RuntimeException(
						"could not send metrics to graphite " + graphiteData);
			}
		} catch (final Exception e) {
			LOGGER.error(ACTION_TYPE + " failed", e);
			throw new ActionExecutorException(ErrorType.ERROR, e.toString(),
					e.getMessage());
		}
		context.setExecutionData("OK", null);
	}

	private void apply(Counter counter) throws RuntimeException {

		for (Map.Entry<String, Long> sourceEntry : counter.getSource()
				.entrySet()) {
			for (Map.Entry<Pattern, String> mapping : counter.getRules()
					.entrySet()) {
				Matcher matcher = mapping.getKey()
						.matcher(sourceEntry.getKey());
				if (matcher.matches()) {
					String metricsName = metricsPathPrefix
							+ "."
							+ (mapping.getValue() != null ? matcher
									.replaceFirst(mapping.getValue())
									: sourceEntry.getKey());

					// TODO sanitize metric name properly, for now strip out
					// whitespaces and replace them with underlines
					metricsName = metricsName.replaceAll("\\s", "_");

					if (graphiteData.containsKey(metricsName)) {
						throw new RuntimeException("duplicate metricsName = \""
								+ metricsName + "\", aborting");
					} else {
						graphiteData.put(metricsName, sourceEntry.getValue());
					}
				}
			}
		}
	}

	@Override
	public void end(final Context context, final WorkflowAction action)
			throws ActionExecutorException {
		final String externalStatus = action.getExternalStatus();
		final WorkflowAction.Status status = "OK".equals(externalStatus) ? WorkflowAction.Status.OK
				: WorkflowAction.Status.ERROR;
		context.setEndData(status, this.getActionSignal(status));
	}

	@Override
	public void check(final Context context, final WorkflowAction action)
			throws ActionExecutorException {
		// nothing to to, it is a synchronized action
	}

	@Override
	public void kill(final Context context, final WorkflowAction action)
			throws ActionExecutorException {
		// nothing to to, it is a synchronized action
	}

	@Override
	public boolean isCompleted(final String externalStatus) {
		// nothing to to, it is a synchronized action
		return true;
	}

	private static class Counter {

		@Override
		public String toString() {
			return "Counter [source=" + source + ", rules=" + rules + "]";
		}

		Map<String, Long> source = null;

		Map<Pattern, String> rules = new TreeMap<Pattern, String>();

		public Map<String, Long> getSource() {
			return source;
		}

		public Map<Pattern, String> getRules() {
			return rules;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Counter(Element counterElement) {

			if (counterElement != null) {
				List<Element> children = counterElement.getChildren();
				for (Element child : children) {
					if (ELEMENT_NAME_SOURCE.equals(child.getName())) {
						source = (Map) JSONValue.parse(child.getText());
					} else if (ELEMENT_NAME_MAPPING.equals(child.getName())) {
						Pattern pattern = null;
						String patternStr = child
								.getAttributeValue(MAPPING_ELEMENT_ATTRIBUTE_MATCHES);
						if (patternStr == null) {
							pattern = DOT_ALL_PATTERN;
						} else {
							pattern = Pattern.compile(patternStr);
						}
						String renameRule = child
								.getAttributeValue(MAPPING_ELEMENT_ATTRIBUTE_RENAME_TO);
						rules.put(pattern, renameRule);
					}
				}
				// defaulting to DOT ALL if no explicit mappings were provided
				if (rules.size() == 0) {
					rules.put(DOT_ALL_PATTERN, null);
				}
			}

		}
	}

}
