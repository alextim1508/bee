package com.alextim.bee;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

public class StartController {
    private Label header;
    private TextArea log;
    public Pane getStartPane(String initHeader) {
        AnchorPane startPane = new AnchorPane();
        header = new Label(initHeader);
        header.setFont(new Font(24));
        header.setAlignment(Pos.CENTER);
        log = new TextArea();

        startPane.getChildren().addAll(header, log);
        AnchorPane.setTopAnchor(header, 5.0);
        AnchorPane.setLeftAnchor(header, 5.0);
        AnchorPane.setRightAnchor(header, 5.0);

        AnchorPane.setTopAnchor(log, 75.0);
        AnchorPane.setBottomAnchor(log, 5.0);
        AnchorPane.setLeftAnchor(log, 5.0);
        AnchorPane.setRightAnchor(log, 5.0);
        return startPane;
    }

    public void setHeader(String text) {
        header.setText(text);
    }

    public void addLog(String text) {
        log.appendText(text);
    }
}
