/*******************************************************************************
 * Copyright 2022 Amit Kumar Mondal
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
package com.osgifx.console.data.manager;

import static com.osgifx.console.util.fx.ConsoleFxHelper.makeNullSafe;

import org.eclipse.fx.core.log.FluentLogger;
import org.eclipse.fx.core.log.LoggerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.collect.EvictingQueue;
import com.osgifx.console.agent.Agent;
import com.osgifx.console.agent.dto.XBundleDTO;
import com.osgifx.console.agent.dto.XComponentDTO;
import com.osgifx.console.agent.dto.XConfigurationDTO;
import com.osgifx.console.agent.dto.XEventDTO;
import com.osgifx.console.agent.dto.XLogEntryDTO;
import com.osgifx.console.agent.dto.XPropertyDTO;
import com.osgifx.console.agent.dto.XServiceDTO;
import com.osgifx.console.agent.dto.XThreadDTO;
import com.osgifx.console.data.provider.DataProvider;
import com.osgifx.console.supervisor.EventListener;
import com.osgifx.console.supervisor.LogEntryListener;
import com.osgifx.console.supervisor.Supervisor;
import com.osgifx.console.util.fx.ObservableQueue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@Component
public final class RuntimeDataProvider implements DataProvider, EventListener, LogEntryListener {

    @Reference
    private LoggerFactory factory;
    @Reference
    private Supervisor    supervisor;
    private FluentLogger  logger;

    private final ObservableQueue<XEventDTO>    events = new ObservableQueue<>(EvictingQueue.create(200));
    private final ObservableQueue<XLogEntryDTO> logs   = new ObservableQueue<>(EvictingQueue.create(1000));

    @Activate
    void activate() {
        logger = FluentLogger.of(factory.createLogger(getClass().getName()));
    }

    @Override
    public synchronized ObservableList<XBundleDTO> bundles() {
        final Agent agent = supervisor.getAgent();
        if (agent == null) {
            logger.atWarning().log("Agent is not connected");
            return FXCollections.emptyObservableList();
        }
        final ObservableList<XBundleDTO> bundles = FXCollections.observableArrayList();
        bundles.addAll(makeNullSafe(agent.getAllBundles()));
        return bundles;
    }

    @Override
    public synchronized ObservableList<XServiceDTO> services() {
        final Agent agent = supervisor.getAgent();
        if (agent == null) {
            logger.atWarning().log("Agent is not connected");
            return FXCollections.emptyObservableList();
        }
        final ObservableList<XServiceDTO> services = FXCollections.observableArrayList();
        services.addAll(makeNullSafe(agent.getAllServices()));
        return services;
    }

    @Override
    public synchronized ObservableList<XComponentDTO> components() {
        final Agent agent = supervisor.getAgent();
        if (agent == null) {
            logger.atWarning().log("Agent is not connected");
            return FXCollections.emptyObservableList();
        }
        final ObservableList<XComponentDTO> components = FXCollections.observableArrayList();
        components.addAll(makeNullSafe(agent.getAllComponents()));
        return components;
    }

    @Override
    public synchronized ObservableList<XConfigurationDTO> configurations() {
        final Agent agent = supervisor.getAgent();
        if (agent == null) {
            logger.atWarning().log("Agent is not connected");
            return FXCollections.emptyObservableList();
        }
        final ObservableList<XConfigurationDTO> configurations = FXCollections.observableArrayList();
        configurations.addAll(makeNullSafe(agent.getAllConfigurations()));
        return configurations;
    }

    @Override
    public synchronized ObservableList<XEventDTO> events() {
        return events;
    }

    @Override
    public synchronized ObservableList<XLogEntryDTO> logs() {
        return logs;
    }

    @Override
    public synchronized void onEvent(final XEventDTO event) {
        events.add(event);
    }

    @Override
    public synchronized void logged(final XLogEntryDTO logEntry) {
        logs.add(logEntry);
    }

    @Override
    public synchronized ObservableList<XPropertyDTO> properties() {
        final Agent agent = supervisor.getAgent();
        if (agent == null) {
            logger.atWarning().log("Agent is not connected");
            return FXCollections.emptyObservableList();
        }
        final ObservableList<XPropertyDTO> properties = FXCollections.observableArrayList();
        properties.addAll(makeNullSafe(agent.getAllProperties()));
        return properties;
    }

    @Override
    public synchronized ObservableList<XThreadDTO> threads() {
        final Agent agent = supervisor.getAgent();
        if (agent == null) {
            logger.atWarning().log("Agent is not connected");
            return FXCollections.emptyObservableList();
        }
        final ObservableList<XThreadDTO> threads = FXCollections.observableArrayList();
        threads.addAll(makeNullSafe(agent.getAllThreads()));
        return threads;
    }

}