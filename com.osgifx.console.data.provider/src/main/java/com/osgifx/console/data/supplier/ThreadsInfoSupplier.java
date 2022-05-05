/*******************************************************************************
 * Copyright 2021-2022 Amit Kumar Mondal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.osgifx.console.data.supplier;

import static com.osgifx.console.data.supplier.ThreadsInfoSupplier.THREADS_ID;
import static com.osgifx.console.event.topics.CommonEventTopics.DATA_RETRIEVED_THREADS_TOPIC;
import static com.osgifx.console.supervisor.Supervisor.AGENT_DISCONNECTED_EVENT_TOPIC;
import static com.osgifx.console.util.fx.ConsoleFxHelper.makeNullSafe;
import static javafx.collections.FXCollections.observableArrayList;

import org.eclipse.fx.core.ThreadSynchronize;
import org.eclipse.fx.core.log.FluentLogger;
import org.eclipse.fx.core.log.LoggerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;

import com.osgifx.console.agent.dto.XThreadDTO;
import com.osgifx.console.supervisor.Supervisor;

import javafx.collections.ObservableList;

@Component
@SupplierID(THREADS_ID)
@EventTopics(AGENT_DISCONNECTED_EVENT_TOPIC)
public final class ThreadsInfoSupplier implements RuntimeInfoSupplier, EventHandler {

	public static final String THREADS_ID = "threads";

	@Reference
	private LoggerFactory     factory;
	@Reference
	private EventAdmin        eventAdmin;
	@Reference
	private Supervisor        supervisor;
	@Reference
	private ThreadSynchronize threadSync;
	private FluentLogger      logger;

	private final ObservableList<XThreadDTO> threads = observableArrayList();

	@Activate
	void activate() {
		logger = FluentLogger.of(factory.createLogger(getClass().getName()));
	}

	@Override
	public synchronized void retrieve() {
		logger.atInfo().log("Retrieving threads info from remote runtime");
		final var agent = supervisor.getAgent();
		if (agent == null) {
			logger.atWarning().log("Agent is not connected");
			return;
		}
		threads.setAll(makeNullSafe(agent.getAllThreads()));
		RuntimeInfoSupplier.sendEvent(eventAdmin, DATA_RETRIEVED_THREADS_TOPIC);
		logger.atInfo().log("Threads info retrieved successfully");
	}

	@Override
	public ObservableList<?> supply() {
		return threads;
	}

	@Override
	public void handleEvent(final Event event) {
		threadSync.asyncExec(threads::clear);
	}
}
