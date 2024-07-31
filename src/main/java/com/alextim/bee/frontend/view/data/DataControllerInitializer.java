package com.alextim.bee.frontend.view.data;

import com.alextim.bee.frontend.view.NodeController;
import com.alextim.bee.frontend.widget.SpectrumWidget;
import com.alextim.bee.frontend.widget.graphs.SimpleGraph;
import com.alextim.bee.service.StatisticMeasService.StatisticMeasurement;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import static com.alextim.bee.frontend.MainWindow.PROGRESS_BAR_COLOR;

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
    private TableView<TableRow> table;
    @FXML
    private TableColumn<TableRow, String> comment;
    @FXML
    private TableColumn<TableRow, Long> count;
    @FXML
    private TableColumn<TableRow, Double> averageCount;
    @FXML
    private TableColumn<TableRow, Long> currentCount;

    @FXML
    private Label status;
    @FXML
    private Label currentMeasTime;
    @FXML
    private Label measDataTitle, meadDataValue;


    @FXML
    private Label progressLabel;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button startBtn, stopBtn;

    protected SpectrumWidget spectrumWidget;
    protected SimpleGraph countGraph;
    protected SimpleGraph currentCountGraph;
    protected SimpleGraph averageCountGraph;

    private ImageView imageView;

    abstract void start(long measTime);

    abstract void stop();

    abstract void save();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        initPane();
        initChart();
        addImageView();
        addGraph();
        tableInitialize();
        fullTable();

        measTimeInit();
        initProgressBar();
    }

    private void initPane() {
        splitPane.setDividerPositions(0.7f);
    }

    private void initChart() {
        spectrumWidget = new SpectrumWidget("Счет");
        AnchorPane spectrumPane = spectrumWidget.getPane();
        graphPane.getChildren().add(spectrumPane);
        AnchorPane.setTopAnchor(spectrumPane, 5.0);
        AnchorPane.setLeftAnchor(spectrumPane, 5.0);
        AnchorPane.setRightAnchor(spectrumPane, 50.0);
        AnchorPane.setBottomAnchor(spectrumPane, 5.0);
    }

    private void addImageView() {
        imageView = new ImageView();
        imageView.setFitHeight(32);
        imageView.setFitWidth(32);
        graphPane.getChildren().add(imageView);
        AnchorPane.setTopAnchor(imageView, 5.0);
        AnchorPane.setRightAnchor(imageView, 5.0);
    }

    public void setGreenCircle() {
        imageView.setImage(mainWindow.getGreenCircleImage());
    }

    public void setRedCircle() {
        imageView.setImage(mainWindow.getRedCircleImage());
    }

    public void setYellowCircle() {
        imageView.setImage(mainWindow.getYellowCircleImage());
    }

    private void addGraph() {
        countGraph = new SimpleGraph(new SimpleStringProperty("Счет"), new SimpleStringProperty(""));
        averageCountGraph = new SimpleGraph(new SimpleStringProperty("Средний счет"), new SimpleStringProperty(""));
        currentCountGraph = new SimpleGraph(new SimpleStringProperty("Текущий счет"), new SimpleStringProperty(""));
        spectrumWidget.addGraph(countGraph);
        spectrumWidget.addGraph(averageCountGraph);
        spectrumWidget.addGraph(currentCountGraph);
    }

    @RequiredArgsConstructor
    public enum MeasTime {
        SEC_5("5 сек", 5),
        SEC_10("10 сек", 10),
        SEC_30("30 сек", 30),
        MIN_1("1 мин", TimeUnit.MINUTES.toSeconds(1)),
        MIN_2("2 мин", TimeUnit.MINUTES.toSeconds(2)),
        MIN_3("3 мин", TimeUnit.MINUTES.toSeconds(3)),
        MIN_5("5 мин", TimeUnit.MINUTES.toSeconds(5)),
        MIN_10("10 мин", TimeUnit.MINUTES.toSeconds(10)),
        MIN_30("30 мин", TimeUnit.MINUTES.toSeconds(30)),
        HOUR_1("1 час", TimeUnit.HOURS.toSeconds(1)),
        HOUR_2("2 часа", TimeUnit.HOURS.toSeconds(2)),
        HOUR_3("3 часа", TimeUnit.HOURS.toSeconds(3)),
        HOUR_5("5 часов", TimeUnit.HOURS.toSeconds(5)),
        HOUR_12("12 часов", TimeUnit.HOURS.toSeconds(12));

        private final String title;

        private final long seconds;
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
    }

    private void initProgressBar() {
        progressBar.setStyle("-fx-accent: " + PROGRESS_BAR_COLOR);
    }

    long getMeasTime() {
        return measTime.getSelectionModel().getSelectedItem().seconds;
    }

    @AllArgsConstructor
    public static class TableRow {
        public String comment;
        public long count;
        public double averageCount;
        public long currentCount;
    }

    private void tableInitialize() {
        comment.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().comment));
        count.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().count));
        currentCount.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().currentCount));
        averageCount.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().averageCount));
        averageCount.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableRow, Double> call(TableColumn<TableRow, Double> tableRowDoubleTableColumn) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            if (item != null) {
                                setText(String.format("%.2f", item));
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

        items.get(0).count = meas.count1;
        items.get(0).averageCount = meas.averageCount1;
        items.get(0).currentCount = meas.currentCount1;

        items.get(1).count = meas.count2;
        items.get(1).averageCount = meas.averageCount2;
        items.get(1).currentCount = meas.currentCount2;

        items.get(2).count = meas.countSum;
        items.get(2).averageCount = meas.averageCountSum;
        items.get(2).currentCount = meas.currentCountSum;

        table.refresh();
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
        progressLabel.setText(String.format(Locale.US, " %.1f%%", 100 * progress));
    }

    public void setState(String text) {
        status.setText(text);
    }

    public void setMeasDataValue(String text) {
        meadDataValue.setText(text);
    }

    public void setMeasDataTitle(String title) {
        measDataTitle.setText(title);
    }

    public void setMeasTime(String text) {
        currentMeasTime.setText(text);
    }

    private void changeDisableStartStopBtn(boolean res) {
        startBtn.setDisable(res);
        stopBtn.setDisable(!res);
    }

    @FXML
    void onStart(ActionEvent event) {
        changeDisableStartStopBtn(true);
        start(getMeasTime());
    }

    @FXML
    void onStop(ActionEvent event) {
        changeDisableStartStopBtn(false);
        stop();
    }

    @FXML
    void onSave(ActionEvent event) {
        save();
    }
}
