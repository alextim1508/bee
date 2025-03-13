package com.alextim.bee.frontend.view.data;

import com.alextim.bee.client.dto.BdmgData;
import com.alextim.bee.client.dto.BdpnData;
import com.alextim.bee.frontend.view.NodeController;
import com.alextim.bee.frontend.widget.GraphWidget;
import com.alextim.bee.frontend.widget.graphs.SimpleGraph;
import com.alextim.bee.service.StatisticMeasService.StatisticMeasurement;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import static com.alextim.bee.context.Property.*;
import static com.alextim.bee.frontend.view.data.DataControllerInitializer.MeasTime.SEC_10;

@Slf4j
public abstract class DataControllerInitializer extends NodeController {

    @FXML
    private SplitPane splitPane;
    @FXML
    private AnchorPane graphPane;
    @FXML
    private AnchorPane controlPane;

    @FXML
    private ComboBox<MeasTime> measTime;


    @FXML
    private StackPane stackPane;
    @FXML
    private AnchorPane simpleUserPane;
    @FXML
    private Label curCount, aveCount;

    @FXML
    private TableView<TableRow> table;
    @FXML
    private TableColumn<TableRow, Float> count;
    @FXML
    private TableColumn<TableRow, String> comment;
    @FXML
    private TableColumn<TableRow, Float> averageCount;
    @FXML
    private TableColumn<TableRow, Float> currentCount;

    @FXML
    private Label mode;

    @FXML
    private Label currentMeasTime;
    @FXML
    private Label measDataTitle, meadDataValue;
    @FXML
    private Label geoData;

    @FXML
    private Button startBtn, stopBtn;

    @FXML
    private TextField fileComment;

    protected GraphWidget graphWidget;
    protected SimpleGraph currentMeasDataGraph;
    protected SimpleGraph averageMeasDataGraph;
    protected SimpleGraph accumulatedMeasDataGraph;
    protected SimpleGraph accumulatedPowerMeasDataGraph;

    @FXML
    private ImageView imageView;
    @FXML
    private Label imageViewLabel1, imageViewLabel2, imageViewLabel3;

    abstract void start(long measTime);

    abstract void stop();

    abstract void save();

    abstract void setNewMeasTime(MeasTime value);

    abstract void clear();

    private final String MEAS_TIME_STATE_APP_PARAM = "data.measTime";
    private final String COMMENT_STATE_APP_PARAM = "data.comment";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        initPane();
        initChart();

        tableInitialize();

        if(USER_APP != null && USER_APP.equalsIgnoreCase(SUPER_USER)) {
            simpleUserPane.setVisible(false);
        } else {
            table.setVisible(false);
        }

        fullTable();
        measTimeInit();
        measDataTitleInit();
        commentInit();

        addGraph();
    }

    private void initPane() {
        splitPane.setDividerPositions(0.7f);
    }

    private void initChart() {
        graphWidget = new GraphWidget("Значение");
        AnchorPane spectrumPane = graphWidget.getPane();
        graphPane.getChildren().add(spectrumPane);
        AnchorPane.setTopAnchor(spectrumPane, 5.0);
        AnchorPane.setLeftAnchor(spectrumPane, 5.0);
        AnchorPane.setRightAnchor(spectrumPane, 60.0);
        AnchorPane.setBottomAnchor(spectrumPane, 5.0);
    }

    public void setGrayCircle() {
        Platform.runLater(() -> imageView.setImage(mainWindow.getGrayCircleImage()));
    }

    public void setGrayCircleExclamation() {
        Platform.runLater(() -> imageView.setImage(mainWindow.getGrayCircleExclamationImage()));
    }

    public void setYellowCircle() {
        Platform.runLater(() -> imageView.setImage(mainWindow.getYellowCircleImage()));
    }

    public void setYellowCircleExclamation() {
        Platform.runLater(() -> imageView.setImage(mainWindow.getYellowCircleExclamationImage()));
    }

    public void setGreenCircle() {
        Platform.runLater(() -> imageView.setImage(mainWindow.getGreenCircleImage()));
    }

    public void setGreenCircleExclamation() {
        Platform.runLater(() -> imageView.setImage(mainWindow.getGreenCircleExclamationImage()));
    }

    public void setRedCircle() {
        Platform.runLater(() -> imageView.setImage(mainWindow.getRedCircleImage()));
    }

    public void setRedCircleExclamation() {
        Platform.runLater(() -> imageView.setImage(mainWindow.getRedCircleExclamationImage()));
    }

    public void setNoConnect() {
        Platform.runLater(() -> imageView.setImage(mainWindow.getNoConnectImage()));
    }

    public void setImageViewLabel(String text1, String text2, String text3) {
        Platform.runLater(() -> {
            if (text1 != null)
                imageViewLabel1.setText(text1);
            if (text2 != null)
                imageViewLabel2.setText(text2);
            if (text3 != null)
                imageViewLabel3.setText(text3);
        });
    }

    private void addGraph() {
        if(DETECTOR_APP.equals(MG_DETECTOR_APP)) {
            currentMeasDataGraph = new SimpleGraph(new SimpleStringProperty("Текущая МАЭД"),
                    new SimpleStringProperty(""));
            averageMeasDataGraph = new SimpleGraph(new SimpleStringProperty("Усредненная за время экспозиции МАЭД"),
                    new SimpleStringProperty(""));
            accumulatedMeasDataGraph = new SimpleGraph(new SimpleStringProperty("Накопленная МАЭД после запуска режима накопления"),
                    new SimpleStringProperty(""));
            accumulatedPowerMeasDataGraph = new SimpleGraph(new SimpleStringProperty("Накопленная МАЭД за время работы БД"),
                    new SimpleStringProperty(""));

        } else if(DETECTOR_APP.equals(PN_DETECTOR_APP)) {
            currentMeasDataGraph = new SimpleGraph(new SimpleStringProperty("Текущий ППН"),
                    new SimpleStringProperty(""));
            averageMeasDataGraph = new SimpleGraph(new SimpleStringProperty("Усредненный ППН за время экспозиции"),
                    new SimpleStringProperty(""));
            accumulatedMeasDataGraph = new SimpleGraph(new SimpleStringProperty("Накопленный ППН после запуска режима накопления"),
                    new SimpleStringProperty(""));
            accumulatedPowerMeasDataGraph = new SimpleGraph(new SimpleStringProperty("Накопленный ППН за время работы БД"),
                    new SimpleStringProperty(""));
        }

        graphWidget.addGraph(currentMeasDataGraph);
        graphWidget.addGraph(averageMeasDataGraph);
        graphWidget.addGraph(accumulatedMeasDataGraph);
        graphWidget.addGraph(accumulatedPowerMeasDataGraph);
    }

    @RequiredArgsConstructor
    public enum MeasTime {
        SEC_1("1 сек", 1),
        SEC_2("2 сек", 2),
        SEC_3("3 сек", 3),
        SEC_4("4 сек", 4),
        SEC_5("5 сек", 5),
        SEC_10("10 сек", 10),
        SEC_15("15 сек", 15),
        SEC_30("30 сек", 30),
        MIN_1("1 мин", TimeUnit.MINUTES.toSeconds(1)),
        MIN_5("5 мин", TimeUnit.MINUTES.toSeconds(5)),
        MIN_10("10 мин", TimeUnit.MINUTES.toSeconds(10)),
        MIN_30("30 мин", TimeUnit.MINUTES.toSeconds(30)),
        HOUR_1("1 час", TimeUnit.HOURS.toSeconds(1));

        public final String title;

        public final long seconds;
    }

    private void measTimeInit() {
        measTime.setItems(FXCollections.observableArrayList(MeasTime.values()));
        measTime.getSelectionModel().select(MeasTime.MIN_1);
        measTime.setConverter(new StringConverter<>() {
            @Override
            public String toString(MeasTime object) {
                if (object != null)
                    return object.title;
                return "";
            }

            @Override
            public MeasTime fromString(String string) {
                return measTime.getItems().stream().filter(ap ->
                        ap.title.equals(string)).findFirst().orElse(null);
            }
        });

        String param = rootController.getAppState().getParam(MEAS_TIME_STATE_APP_PARAM);
        if (param != null) {
            try {
                measTime.getSelectionModel().select(MeasTime.valueOf(param));
            } catch (Exception e) {
                log.error("", e);
                measTime.getSelectionModel().select(SEC_10);
            }
        } else {
            measTime.getSelectionModel().select(SEC_10);
        }
    }

    public void measDataTitleInit() {
        if(DETECTOR_APP.equals(MG_DETECTOR_APP)) {
            Platform.runLater(() -> {
                measDataTitle.setText(BdmgData.title);
            });
        } else if(DETECTOR_APP.equals(PN_DETECTOR_APP)) {
            measDataTitle.setText(BdpnData.title);
        }
    }

    private void commentInit() {
        String param = rootController.getAppState().getParam(COMMENT_STATE_APP_PARAM);
        if (param != null) {
            fileComment.setText(param);
        }
    }

    @FXML
    private void measTimeOn(ActionEvent event) {
        setNewMeasTime(measTime.getValue());
    }

    protected long getMeasTime() {
        return measTime.getSelectionModel().getSelectedItem().seconds;
    }

    @AllArgsConstructor
    private static class TableRow {
        public String comment;
        public float count;
        public float averageCount;
        public float currentCount;
    }

    private void tableInitialize() {
        comment.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().comment));

        count.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().count));
        count.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableRow, Float> call(TableColumn<TableRow, Float> tableRowDoubleTableColumn) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Float item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            if (item != null) {
                                setText(String.format(COUNTER_NUMBER_FORMAT, item));
                            }
                        }
                    }
                };
            }
        });

        currentCount.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().currentCount));
        currentCount.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableRow, Float> call(TableColumn<TableRow, Float> tableRowDoubleTableColumn) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Float item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            if (item != null) {
                                setText(String.format(COUNTER_NUMBER_FORMAT, item));
                            }
                        }
                    }
                };
            }
        });
        averageCount.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().averageCount));
        averageCount.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableRow, Float> call(TableColumn<TableRow, Float> tableRowDoubleTableColumn) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Float item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            if (item != null) {
                                setText(String.format(COUNTER_NUMBER_FORMAT, item));
                            }
                        }
                    }
                };
            }
        });

        table.setItems(FXCollections.observableArrayList());
    }

    private void fullTable() {

        table.getItems().addAll(
                new TableRow("Счетчик 1", 0, 0, 0),
                new TableRow("Счетчик 2", 0, 0, 0),
                new TableRow("Всего", 0, 0, 0)
        );

    }

    public void updateTable(StatisticMeasurement meas) {
        ObservableList<TableRow> items = table.getItems();

        items.get(0).count = meas.getCount1();
        items.get(0).averageCount = meas.averageCount1;
        items.get(0).currentCount = meas.currentCount1;

        items.get(1).count = meas.getCount2();
        items.get(1).averageCount = meas.averageCount2;
        items.get(1).currentCount = meas.currentCount2;

        items.get(2).count = meas.getCountSum();
        items.get(2).averageCount = meas.averageCountSum;
        items.get(2).currentCount = meas.currentCountSum;

        table.refresh();
    }

    public void setCounts(StatisticMeasurement meas) {
        Platform.runLater(() -> {
            this.curCount.setText(String.valueOf(meas.currentCountSum));
            this.aveCount.setText(String.valueOf(meas.averageCountSum));
        });
    }

    public void setMode(String title) {
        Platform.runLater(() -> {
            mode.setText(title);
        });
    }

    public void setMeasData(String title, String value) {
        Platform.runLater(() -> {
            measDataTitle.setText(title);
            meadDataValue.setText(value);
        });
    }

    public void setMeasTime(String text) {
        Platform.runLater(() -> currentMeasTime.setText(text));
    }

    public void setGeoData(String text) {
        Platform.runLater(() -> geoData.setText(text));
    }

    public String getFileComment() {
        return fileComment.getText();
    }

    protected void changeDisableStartStopBtn(boolean res) {
        startBtn.setDisable(res);
        stopBtn.setDisable(!res);
    }

    @FXML
    void onConnectToDetector(ActionEvent event) {
        log.info("onConnectToDetector");
        changeDisableStartStopBtn(true);
        start(getMeasTime());
    }

    @FXML
    void onDisconnectFromDetector(ActionEvent event) {
        log.info("onDisconnectFromDetector");
        changeDisableStartStopBtn(false);
        stop();
    }

    @FXML
    void onSave(ActionEvent event) {
        save();
    }

    @FXML
    void onClear(ActionEvent event) {
        clear();
    }

    public void putStateParam() {
        rootController.getAppState().putParam(MEAS_TIME_STATE_APP_PARAM, measTime.getValue().name());
        rootController.getAppState().putParam(COMMENT_STATE_APP_PARAM, fileComment.getText());
    }
}
