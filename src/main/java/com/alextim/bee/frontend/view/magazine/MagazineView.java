package com.alextim.bee.frontend.view.magazine;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MagazineView {
    public Pane getView(ResourceBundle bundle) {
        try {
            return FXMLLoader.<AnchorPane>load(MagazineView.class.getResource("MagazineView.fxml"), bundle);
        } catch (IOException e) {
            return new Pane(new TextArea(
                    "Error" + e.getMessage() + "/ " + Arrays.toString(e.getStackTrace())));
        }
    }
}
