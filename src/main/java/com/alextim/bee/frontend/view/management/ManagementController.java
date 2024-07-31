package com.alextim.bee.frontend.view.management;

import com.alextim.bee.frontend.view.NodeController;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ManagementController extends NodeController {

    @FXML
    private AnchorPane pane;

    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        paneInit();
    }

    private void paneInit() {
        /* bug JavaFX. Other tabs of tabPane get ScrollEvent from current tab*/
        pane.addEventFilter(ScrollEvent.ANY, Event::consume);
    }
}
