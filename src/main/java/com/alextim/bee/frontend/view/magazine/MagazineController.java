package com.alextim.bee.frontend.view.magazine;

import com.alextim.bee.client.messages.DetectorCommands;
import com.alextim.bee.client.messages.DetectorEvents;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.frontend.view.NodeController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MagazineController extends NodeController {


    @FXML
    private AnchorPane pane;

    @FXML
    private TableView<DetectorMsg> table;
    @FXML
    private TableColumn<DetectorMsg, Integer> serialNumber;
    @FXML
    private TableColumn<DetectorMsg, Long> time;
    @FXML
    private TableColumn<DetectorMsg, String> type;
    @FXML
    private TableColumn<DetectorMsg, String> comEvAns;
    @FXML
    private TableColumn<DetectorMsg, String> message;
    @FXML
    private TableColumn<DetectorMsg, String> data;


    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        initTable();
        paneInit();
    }

    private void paneInit() {
        /* bug JavaFX. Other tabs of tabPane get ScrollEvent from current tab*/
        pane.addEventHandler(ScrollEvent.ANY, Event::consume);
    }

    private void initTable() {
        serialNumber.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().detectorID));
        serialNumber.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(String.format("%x", item));
                }
            }
        });

        time.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().time));

        type.setCellValueFactory(param -> {
            if (param.getValue() instanceof DetectorEvents.SomeEvent) {
                return new ReadOnlyObjectWrapper<>("Событие");
            } else if (param.getValue() instanceof DetectorCommands.SomeCommand) {
                return new ReadOnlyObjectWrapper<>("Команда");
            } else if (param.getValue() instanceof DetectorCommands.SomeCommandAnswer) {
                return new ReadOnlyObjectWrapper<>("Ответ на команду");
            } else {
                return new ReadOnlyObjectWrapper<>("Не известно");
            }
        });
        /*
        type.setCellFactory(new Callback<>() {
            @Override
            public TableCell<DetectorMsg, String> call(TableColumn<DetectorMsg, String> detectorMsgStringTableColumn) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        TableRow<DetectorMsg> tableRow = getTableRow();
                        if (empty || tableRow == null || tableRow.getItem() == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle(null);
                        } else {
                            DetectorMsg detectorMsg = tableRow.getItem();
                            if (detectorMsg instanceof DetectorEvents.SomeEvent) {
                                setStyle("-fx-background-color: " + EVENT_COLOR);
                            } else if (detectorMsg instanceof DetectorCommands.SomeCommandAnswer) {
                                setStyle("-fx-background-color: " + COMMAND_COLOR);
                            } else if (detectorMsg instanceof DetectorCommands.SomeCommand) {
                                setStyle("-fx-background-color: " + COMMAND_ANSWER_COLOR);
                            }  else {
                                setStyle("-fx-background-color: " + "#FFFFFF");
                            }

                            setText(item);
                        }
                    }
                };
            }
        });
        */
        comEvAns.setCellValueFactory(param -> {
            if (param.getValue() instanceof DetectorEvents.SomeEvent event) {
                return new ReadOnlyObjectWrapper<>(event.eventCode.title);

            } else if (param.getValue() instanceof DetectorCommands.SomeCommandAnswer answer) {
                return new ReadOnlyObjectWrapper<>(answer.commandCode.title + ". " + answer.commandStatusCode.title);

            } else if (param.getValue() instanceof DetectorCommands.SomeCommand command) {
                return new ReadOnlyObjectWrapper<>(command.commandCode.title);

            } else {
                return new ReadOnlyObjectWrapper<>("Не известно");
            }
        });

        message.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().toString()));

        data.setCellValueFactory(param -> {
            StringBuilder str = new StringBuilder();
            for (byte datum : param.getValue().data)
                str.append(String.format("%02x ", datum));
            return new ReadOnlyObjectWrapper<>(str.toString());
        });

        table.setItems(FXCollections.observableArrayList());
    }

    public void addLog(DetectorMsg msg) {
        table.getItems().add(0, msg);
    }

    public void clear() {
        table.getItems().clear();
    }


    @FXML
    void onSave(ActionEvent event) {
        File file = mainWindow.showFileChooseDialog();
        if (file != null) {
            rootController.saveDetectorMessages(file);
        }
    }
}