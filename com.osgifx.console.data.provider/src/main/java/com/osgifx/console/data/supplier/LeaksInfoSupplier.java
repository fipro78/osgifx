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

import static com.osgifx.console.data.supplier.LeaksInfoSupplier.LEAKS_ID;
import static com.osgifx.console.data.supplier.OSGiEventAdminTopics.BUNDLE_EVENTS_TOPIC;
import static com.osgifx.console.event.topics.BundleActionEventTopics.BUNDLE_ACTION_EVENT_TOPICS;
import static com.osgifx.console.supervisor.Supervisor.AGENT_CONNECTED_EVENT_TOPIC;
import static com.osgifx.console.supervisor.Supervisor.AGENT_DISCONNECTED_EVENT_TOPIC;
import static com.osgifx.console.util.fx.ConsoleFxHelper.makeNullSafe;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.fx.core.ThreadSynchronize;
import org.eclipse.fx.core.log.FluentLogger;
import org.eclipse.fx.core.log.LoggerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;

import com.osgifx.console.agent.dto.XBundleDTO;
import com.osgifx.console.agent.dto.XEventDTO;
import com.osgifx.console.supervisor.EventListener;
import com.osgifx.console.supervisor.Supervisor;

import javafx.collections.ObservableList;

@Component
@SupplierID(LEAKS_ID)
@EventTopics({ AGENT_CONNECTED_EVENT_TOPIC, AGENT_DISCONNECTED_EVENT_TOPIC, BUNDLE_ACTION_EVENT_TOPICS })
public final class LeaksInfoSupplier implements RuntimeInfoSupplier, EventHandler, EventListener {

	public static final String LEAKS_ID = "leaks";

	@Reference
	private LoggerFactory     factory;
	@Reference
	private Supervisor        supervisor;
	@Reference
	private ThreadSynchronize threadSync;
	private FluentLogger      logger;

	private final ObservableList<XBundleDTO> leaks = synchronizedObservableList(observableArrayList());

	@Activate
	void activate() {
		supervisor.addOSGiEventListener(this);
		logger = FluentLogger.of(factory.createLogger(getClass().getName()));
	}

	@Deactivate
	void deactivate() {
		supervisor.removeOSGiEventListener(this);
	}

	@Override
	public synchronized void retrieve() {
		logger.atInfo().log("Retrieving classloader leaks info from remote runtime");
		final var agent = supervisor.getAgent();
		if (agent == null) {
			logger.atWarning().log("Agent is not connected");
			return;
		}
		leaks.setAll(makeNullSafe(agent.getClassloaderLeaks()));
		logger.atInfo().log("Classloader leaks info retrieved successfully");
	}

	@Override
	public ObservableList<?> supply() {
		return leaks;
	}

	@Override
	public void handleEvent(final Event event) {
		final var topic = event.getTopic();
		if (AGENT_CONNECTED_EVENT_TOPIC.equals(topic)) {
			CompletableFuture.runAsync(this::retrieve);
			return;
		}
		if (AGENT_DISCONNECTED_EVENT_TOPIC.equals(topic)) {
			threadSync.asyncExec(leaks::clear);
			return;
		}
		final var agent = supervisor.getAgent();
		if (agent == null) {
			logger.atInfo().log("Agent is not connected");
			return;
		}
		// if the remote runtime has EventAdmin installed, we retrieve the values on
		// EventAdmin events, otherwise, retrieve the values on e4 events
		if (!agent.isEventAdminAvailable()) {
			CompletableFuture.runAsync(this::retrieve);
		}
	}

	@Override
	public void onEvent(final XEventDTO event) {
		CompletableFuture.runAsync(this::retrieve);
	}

	@Override
	public Collection<String> topics() {
		return List.of(BUNDLE_EVENTS_TOPIC);
	}

}
