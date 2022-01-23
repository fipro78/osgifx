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
package com.osgifx.console.ui.services;

import static org.osgi.namespace.service.ServiceNamespace.SERVICE_NAMESPACE;

import javax.inject.Inject;
import javax.inject.Named;

import org.controlsfx.control.table.TableFilter;
import org.controlsfx.control.table.TableRowExpanderColumn;
import org.controlsfx.control.table.TableRowExpanderColumn.TableRowDataFeatures;
import org.eclipse.fx.core.di.LocalInstance;
import org.eclipse.fx.core.log.FluentLogger;
import org.eclipse.fx.core.log.Log;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.framework.BundleContext;

import com.osgifx.console.agent.dto.XServiceDTO;
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
public final class ServicesFxController {

    @Log
    @Inject
    private FluentLogger                      logger;
    @Inject
    @LocalInstance
    private FXMLLoader                        loader;
    @Inject
    private DataProvider                      dataProvider;
    @FXML
    private TableView<XServiceDTO>            table;
    @Inject
    @Named("com.osgifx.console.ui.services")
    private BundleContext                     context;
    private TableRowDataFeatures<XServiceDTO> selectedService;

    @FXML
    public void initialize() {
        createControls();
        Fx.disableSelectionModel(table);
        logger.atDebug().log("FXML controller has been initialized");
    }

    private void createControls() {
        final GridPane                            expandedNode   = (GridPane) Fx.loadFXML(loader, context,
                "/fxml/expander-column-content.fxml");
        final ServiceDetailsFxController          controller     = loader.getController();
        final TableRowExpanderColumn<XServiceDTO> expanderColumn = new TableRowExpanderColumn<>(expandedService -> {
                                                                     controller.initControls(expandedService.getValue());
                                                                     if (selectedService != null && selectedService.isExpanded()) {
                                                                         selectedService.toggleExpanded();
                                                                     }
                                                                     selectedService = expandedService;
                                                                     return expandedNode;
                                                                 });

        final TableColumn<XServiceDTO, String> serviceIdColumn = new TableColumn<>("Service ID");

        serviceIdColumn.setPrefWidth(100);
        serviceIdColumn.setCellValueFactory(new DTOCellValueFactory<>("id", String.class));

        final TableColumn<XServiceDTO, String> objectClassColumn = new TableColumn<>("Object Class");

        objectClassColumn.setPrefWidth(600);
        objectClassColumn.setCellValueFactory(new DTOCellValueFactory<>("types", String.class));

        final TableColumn<XServiceDTO, String> registeringBundleColumn = new TableColumn<>("Registering Bundle");

        registeringBundleColumn.setPrefWidth(400);
        registeringBundleColumn.setCellValueFactory(new DTOCellValueFactory<>("registeringBundle", String.class));

        table.getColumns().add(expanderColumn);
        table.getColumns().add(serviceIdColumn);
        table.getColumns().add(objectClassColumn);
        table.getColumns().add(registeringBundleColumn);

        final ObservableList<XServiceDTO> services = dataProvider.services();
        table.setItems(services);
        Fx.sortBy(table, objectClassColumn);

        TableFilter.forTableView(table).apply();
    }

}