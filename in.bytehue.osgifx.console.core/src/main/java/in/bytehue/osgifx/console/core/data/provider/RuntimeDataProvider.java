package in.bytehue.osgifx.console.core.data.provider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import in.bytehue.osgifx.console.agent.ConsoleAgent;
import in.bytehue.osgifx.console.agent.dto.XBundleDTO;
import in.bytehue.osgifx.console.agent.dto.XComponentDTO;
import in.bytehue.osgifx.console.agent.dto.XConfigurationDTO;
import in.bytehue.osgifx.console.agent.dto.XEventDTO;
import in.bytehue.osgifx.console.agent.dto.XPropertyDTO;
import in.bytehue.osgifx.console.agent.dto.XServiceDTO;
import in.bytehue.osgifx.console.supervisor.ConsoleSupervisor;
import in.bytehue.osgifx.console.ui.service.DataProvider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@Component
public final class RuntimeDataProvider implements DataProvider {

    @Reference
    private ConsoleSupervisor supervisor;

    private final ObservableList<XBundleDTO>        bundles        = FXCollections.observableArrayList();
    private final ObservableList<XServiceDTO>       services       = FXCollections.observableArrayList();
    private final ObservableList<XComponentDTO>     components     = FXCollections.observableArrayList();
    private final ObservableList<XConfigurationDTO> configurations = FXCollections.observableArrayList();
    private final ObservableList<XEventDTO>         events         = FXCollections.observableArrayList();
    private final ObservableList<XPropertyDTO>      properties     = FXCollections.observableArrayList();

    @Override
    public ObservableList<XBundleDTO> bundles() {
        final ConsoleAgent agent = supervisor.getAgent();
        if (agent == null) {
            return FXCollections.emptyObservableList();
        }
        bundles.clear();
        bundles.addAll(agent.getAllBundles());
        return bundles;
    }

    @Override
    public ObservableList<XServiceDTO> services() {
        final ConsoleAgent agent = supervisor.getAgent();
        if (agent == null) {
            return FXCollections.emptyObservableList();
        }
        services.clear();
        services.addAll(agent.getAllServices());
        return services;
    }

    @Override
    public ObservableList<XComponentDTO> components() {
        final ConsoleAgent agent = supervisor.getAgent();
        if (agent == null) {
            return FXCollections.emptyObservableList();
        }
        components.clear();
        components.addAll(agent.getAllComponents());
        return components;
    }

    @Override
    public ObservableList<XConfigurationDTO> configurations() {
        final ConsoleAgent agent = supervisor.getAgent();
        if (agent == null) {
            return FXCollections.emptyObservableList();
        }
        configurations.clear();
        configurations.addAll(agent.getAllConfigurations());
        return configurations;
    }

    @Override
    public ObservableList<XEventDTO> events() {
        return events;
    }

    @Override
    public ObservableList<XPropertyDTO> properties() {
        final ConsoleAgent agent = supervisor.getAgent();
        if (agent == null) {
            return FXCollections.emptyObservableList();
        }
        properties.clear();
        properties.addAll(agent.getAllProperties());
        return properties;
    }

}
