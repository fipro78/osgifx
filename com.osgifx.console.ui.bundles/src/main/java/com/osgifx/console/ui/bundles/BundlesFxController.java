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
package com.osgifx.console.ui.bundles;

import static org.osgi.namespace.service.ServiceNamespace.SERVICE_NAMESPACE;

import javax.inject.Inject;
import javax.inject.Named;

import org.controlsfx.control.table.TableFilter;
import org.controlsfx.control.table.TableRowExpanderColumn;
import org.eclipse.fx.core.di.LocalInstance;
import org.eclipse.fx.core.log.FluentLogger;
import org.eclipse.fx.core.log.Log;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.framework.BundleContext;

import com.osgifx.console.agent.dto.XBundleDTO;
import com.osgifx.console.data.provider.DataProvider;
import com.osgifx.console.util.fx.DTOCellValueFactory;
import com.osgifx.console.util.fx.Fx;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

@Requirement(effective = "active", namespace = SERVICE_NAMESPACE, filter = "(objectClass=com.osgifx.console.data.provider.DataProvider)")
public final class BundlesFxController {

    @Log
    @Inject
    private FluentLogger                                            logger;
    @Inject
    @LocalInstance
    private FXMLLoader                                              loader;
    @FXML
    private TableView<XBundleDTO>                                   table;
    @Inject
    private DataProvider                                            dataProvider;
    @Inject
    @Named("com.osgifx.console.ui.bundles")
    private BundleContext                                           context;
    private TableRowExpanderColumn.TableRowDataFeatures<XBundleDTO> selectedBundle;

    @FXML
    public void initialize() {
        createControls();
        Fx.disableSelectionModel(table);
        logger.atDebug().log("FXML controller has been initialized");
    }

    private void createControls() {
        final GridPane                           expandedNode   = (GridPane) Fx.loadFXML(loader, context,
                "/fxml/expander-column-content.fxml");
        final BundleDetailsFxController          controller     = loader.getController();
        final TableRowExpanderColumn<XBundleDTO> expanderColumn = new TableRowExpanderColumn<>(expandedBundle -> {
                                                                    controller.initControls(expandedBundle.getValue());
                                                                    if (selectedBundle != null && selectedBundle.isExpanded()) {
                                                                        selectedBundle.toggleExpanded();
                                                                    }
                                                                    selectedBundle = expandedBundle;
                                                                    return expandedNode;
                                                                });

        final TableColumn<XBundleDTO, String> symbolicNameColumn = new TableColumn<>("Symbolic Name");

        symbolicNameColumn.setPrefWidth(450);
        symbolicNameColumn.setCellValueFactory(new DTOCellValueFactory<>("symbolicName", String.class));

        final TableColumn<XBundleDTO, String> versionColumn = new TableColumn<>("Version");

        versionColumn.setPrefWidth(450);
        versionColumn.setCellValueFactory(new DTOCellValueFactory<>("version", String.class));

        final TableColumn<XBundleDTO, String> statusColumn = new TableColumn<>("State");

        statusColumn.setPrefWidth(200);
        statusColumn.setCellValueFactory(new DTOCellValueFactory<>("state", String.class));

        table.getColumns().add(expanderColumn);
        table.getColumns().add(symbolicNameColumn);
        table.getColumns().add(versionColumn);
        table.getColumns().add(statusColumn);

        final ObservableList<XBundleDTO> bundles = dataProvider.bundles();
        table.setItems(bundles);
        Fx.sortBy(table, symbolicNameColumn);

        TableFilter.forTableView(table).apply();
    }

}