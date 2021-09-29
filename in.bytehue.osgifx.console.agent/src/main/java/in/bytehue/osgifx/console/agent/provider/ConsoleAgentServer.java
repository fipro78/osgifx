package in.bytehue.osgifx.console.agent.provider;

import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.lib.startlevel.StartLevelRuntimeHandler;
import aQute.remote.agent.AgentServer;
import in.bytehue.osgifx.console.agent.ConsoleAgent;
import in.bytehue.osgifx.console.agent.dto.ConfigurationDTO;

public final class ConsoleAgentServer extends AgentServer implements ConsoleAgent, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ServiceTracker<ServiceComponentRuntime, ServiceComponentRuntime> scrTracker;
    private final ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>           configAdminTracker;

    public ConsoleAgentServer(final String name, final BundleContext context, final File cache) {
        this(name, context, cache, StartLevelRuntimeHandler.absent());
    }

    public ConsoleAgentServer(final String name, final BundleContext context, final File cache,
            final StartLevelRuntimeHandler startlevels) {
        super(name, context, cache, startlevels);

        scrTracker         = new ServiceTracker<>(context, ServiceComponentRuntime.class, null);
        configAdminTracker = new ServiceTracker<>(context, ConfigurationAdmin.class, null);

        scrTracker.open();
        configAdminTracker.open();
    }

    @Override
    public Collection<ComponentDescriptionDTO> getComponentDescriptionDTOs() {
        // @formatter:off
        return Optional.ofNullable(scrTracker.getService())
                       .map(ServiceComponentRuntime::getComponentDescriptionDTOs)
                       .orElse(emptyList());
        // @formatter:on
    }

    @Override
    public Collection<ComponentConfigurationDTO> getComponentConfigurationDTOs(
            final ComponentDescriptionDTO description) {
        // @formatter:off
        return Optional.ofNullable(scrTracker.getService())
                       .map(scr -> scr.getComponentConfigurationDTOs(description))
                       .orElse(emptyList());
        // @formatter:on
    }

    @Override
    public void enableComponent(final ComponentDescriptionDTO description) {
        // @formatter:off
        Optional.ofNullable(scrTracker.getService())
                .map(scr -> scr.enableComponent(description))
                .ifPresent(p -> {
                    try {
                        p.getValue();
                    } catch (InvocationTargetException | InterruptedException e) {
                        logger.error("Cannot enable component '{}'", description);
                    }
                });
        // @formatter:on
    }

    @Override
    public void disableComponent(final ComponentDescriptionDTO description) {
     // @formatter:off
        Optional.ofNullable(scrTracker.getService())
                .map(scr -> scr.disableComponent(description))
                .ifPresent(p -> {
                    try {
                        p.getValue();
                    } catch (InvocationTargetException | InterruptedException e) {
                        logger.error("Cannot disable component '{}'", description);
                    }
                });
        // @formatter:on
    }

    @Override
    public Collection<ServiceReferenceDTO> getServiceReferences(final String filter) throws Exception {
        return getFramework().services;
    }

    @Override
    public Collection<ConfigurationDTO> listConfigurations(final String filter)
            throws IOException, InvalidSyntaxException {
        final ConfigurationAdmin configAdmin = configAdminTracker.getService();
        if (configAdmin == null) {
            return Collections.emptyList();
        }
        final List<ConfigurationDTO> configurations = new ArrayList<>();
        for (final Configuration configuration : configAdmin.listConfigurations(filter)) {
            final ConfigurationDTO dto = toDTO(configuration);
            configurations.add(dto);
        }
        return configurations;
    }

    @Override
    public void deleteConfiguration(final String pid) throws IOException {
        final ConfigurationAdmin configAdmin = configAdminTracker.getService();
        if (configAdmin == null) {
            return;
        }
        try {
            for (final Configuration configuration : configAdmin.listConfigurations(null)) {
                if (configuration.getPid().equals(pid)) {
                    configuration.delete();
                }
            }
        } catch (IOException | InvalidSyntaxException e) {
            logger.error("Cannot delete configuration '{}'", pid);
        }
    }

    @Override
    public void updateConfiguration(final String pid, final Map<String, Object> newProperties) throws IOException {
        final ConfigurationAdmin configAdmin = configAdminTracker.getService();
        if (configAdmin == null) {
            return;
        }
        try {
            for (final Configuration configuration : configAdmin.listConfigurations(null)) {
                if (configuration.getPid().equals(pid)) {
                    configuration.update(new Hashtable<>(newProperties));
                }
            }
        } catch (IOException | InvalidSyntaxException e) {
            logger.error("Cannot update configuration '{}'", pid);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        scrTracker.close();
        configAdminTracker.close();
    }

    private ConfigurationDTO toDTO(final Configuration configuration) {
        final ConfigurationDTO dto = new ConfigurationDTO();

        dto.pid        = configuration.getPid();
        dto.factoryPid = configuration.getFactoryPid();
        dto.properties = toMap(configuration.getProperties());

        return dto;
    }

    private Map<String, Object> toMap(final Dictionary<String, Object> dictionary) {
        final List<String> keys = Collections.list(dictionary.keys());
        return keys.stream().collect(Collectors.toMap(identity(), dictionary::get));
    }

}