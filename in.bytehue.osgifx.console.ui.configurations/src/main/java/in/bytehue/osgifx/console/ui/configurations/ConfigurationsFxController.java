package in.bytehue.osgifx.console.ui.configurations;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Named;

import org.controlsfx.control.table.TableFilter;
import org.controlsfx.control.table.TableRowExpanderColumn;
import org.eclipse.fx.core.di.LocalInstance;
import org.osgi.framework.BundleContext;

import in.bytehue.osgifx.console.agent.dto.XConfigurationDTO;
import in.bytehue.osgifx.console.ui.service.DataProvider;
import in.bytehue.osgifx.console.util.fx.DTOCellValueFactory;
import in.bytehue.osgifx.console.util.fx.Fx;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

public final class ConfigurationsFxController implements Initializable {

    @Inject
    @LocalInstance
    private FXMLLoader loader;

    @Inject
    private DataProvider dataProvider;

    @FXML
    private TableView<XConfigurationDTO> table;

    @Inject
    @Named("in.bytehue.osgifx.console.ui.configurations")
    private BundleContext context;

    private TableRowExpanderColumn.TableRowDataFeatures<XConfigurationDTO> selectedConfiguration;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        table.setSelectionModel(null);
        createControls();
    }

    private void createControls() {
        final GridPane                                  expandedNode   = (GridPane) Fx.loadFXML(loader, context,
                "/fxml/expander-column-content.fxml");
        final ConfigurationEditorFxController           controller     = loader.getController();
        final TableRowExpanderColumn<XConfigurationDTO> expanderColumn = new TableRowExpanderColumn<>(expandedConfig -> {
                                                                           controller.initControls(expandedConfig.getValue());
                                                                           if (selectedConfiguration != null) {
                                                                               selectedConfiguration.toggleExpanded();
                                                                           }
                                                                           selectedConfiguration = expandedConfig;
                                                                           return expandedNode;
                                                                       });

        final TableColumn<XConfigurationDTO, String> pidColumn = new TableColumn<>("PID");

        pidColumn.setPrefWidth(650);
        pidColumn.setCellValueFactory(new DTOCellValueFactory<>("pid", String.class));

        final TableColumn<XConfigurationDTO, String> locationColumn = new TableColumn<>("Location");

        locationColumn.setPrefWidth(200);
        locationColumn.setCellValueFactory(new DTOCellValueFactory<>("location", String.class));

        table.getColumns().add(expanderColumn);
        table.getColumns().add(pidColumn);
        table.getColumns().add(locationColumn);

        final ObservableList<XConfigurationDTO> configurations = dataProvider.configurations();
        table.setItems(configurations);

        TableFilter.forTableView(table).apply();
    }

}
