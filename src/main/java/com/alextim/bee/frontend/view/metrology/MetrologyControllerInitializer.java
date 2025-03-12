package com.alextim.bee.frontend.view.metrology;

import com.alextim.bee.frontend.view.NodeController;
import com.alextim.bee.service.ValueFormatter;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.alextim.bee.context.Property.ERROR_NUMBER_SING_DIGITS;
import static com.alextim.bee.context.Property.MEAS_DATA_NUMBER_SING_DIGITS;
import static com.alextim.bee.frontend.MainWindow.PROGRESS_BAR_COLOR;
import static com.alextim.bee.frontend.view.management.ManagementControllerInitializer.ERROR_PARSE_FILED;
import static com.alextim.bee.frontend.view.management.ManagementControllerInitializer.ERROR_PARSE_TITLE;
import static com.alextim.bee.service.ValueFormatter.parsingValuePrefix;
import static com.alextim.bee.service.ValueFormatter.sigDigRounder;
import static javafx.scene.control.Alert.AlertType.ERROR;

@Slf4j
public abstract class MetrologyControllerInitializer extends NodeController {

    private final String CYCLE_AMOUNT_STATE_APP_PARAM = "metrology.cycleAmount";
    private final String MEAS_AMOUNT_STATE_APP_PARAM = "metrology.measAmount";
    private final String REAL_MEAS_DATA_STATE_APP_PARAM = "metrology.realMeasData";

    @FXML
    private AnchorPane pane;

    @FXML
    private TextField cycleAmount;
    @FXML
    private TextField measAmount;
    @FXML
    private TextField realMeasData;
    @FXML
    private TextField error;

    @FXML
    private Label progressLabel;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private TableView<TableRow> table;
    @FXML
    private TableColumn<TableRow, Integer> numberCycleColumn;
    @FXML
    private TableColumn<TableRow, String> aveMeasDataColumn;

    @FXML
    private TextField aveMeasData;

    @FXML
    private Button startBtn;

    @AllArgsConstructor
    public static class TableRow {
        public int numberCycle;
        public float aveMeasData;
        public String unit;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        paneInit();
        initTable();
        initProgressBar();
        initMetrologyFields();
    }

    private void paneInit() {
        /* Bug JavaFX. Other tabs of tabPane get ScrollEvent from current tab */
        pane.addEventHandler(ScrollEvent.ANY, Event::consume);
    }

    private void initTable() {
        numberCycleColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().numberCycle));
        aveMeasDataColumn.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(
                        Float.isNaN(param.getValue().aveMeasData) || Float.isInfinite(param.getValue().aveMeasData) ? "-" :
                                new ValueFormatter(
                                        param.getValue().aveMeasData,
                                        param.getValue().unit,
                                        MEAS_DATA_NUMBER_SING_DIGITS).toString()

                ));
        table.setPlaceholder(new Label(""));
        table.setItems(FXCollections.observableArrayList());
    }

    private void initMetrologyFields() {
        String param = rootController.getAppState().getParam(CYCLE_AMOUNT_STATE_APP_PARAM);
        if (param != null) {
            cycleAmount.setText(param);
        }
        param = rootController.getAppState().getParam(MEAS_AMOUNT_STATE_APP_PARAM);
        if (param != null) {
            measAmount.setText(param);
        }
        param = rootController.getAppState().getParam(REAL_MEAS_DATA_STATE_APP_PARAM);
        if (param != null) {
            realMeasData.setText(param);
        }
    }

    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    private void initProgressBar() {
        progressBar.setStyle("-fx-accent: " + PROGRESS_BAR_COLOR);
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
        progressLabel.setText(String.format(Locale.US, " %.1f%%", 100 * progress));
    }

    public void setError(double error) {
        this.error.setText(
                Double.isNaN(error) || Double.isInfinite(error) ? "-" :
                        String.valueOf(sigDigRounder(error, ERROR_NUMBER_SING_DIGITS)));
    }

    public void setAveMeasData(float aveMeasData, String unit) {
        this.aveMeasData.setText(
                Float.isNaN(aveMeasData) || Float.isInfinite(aveMeasData) ? "-" :
                        new ValueFormatter(
                                aveMeasData,
                                unit,
                                MEAS_DATA_NUMBER_SING_DIGITS).toString()
        );
    }

    public void updateTable(int cycle, float measData, String unit) {
        ObservableList<TableRow> items = table.getItems();
        if (cycle > items.size()) {
            table.getItems().add(new TableRow(0, 0.0f, ""));
        }
        items.get(cycle - 1).numberCycle = cycle;
        items.get(cycle - 1).aveMeasData = measData;
        items.get(cycle - 1).unit = unit;
        table.refresh();
    }

    @FXML
    void startOn(ActionEvent event) {
        log.info("start metrology");

        table.getItems().clear();

        int cycleAmount;
        try {
            cycleAmount = Integer.parseInt(this.cycleAmount.getText());
            log.info("cycleAmount: {}", cycleAmount);
        } catch (Exception e) {
            log.error("startMetrology cycleAmount parsing", e);
            showParsingErrorDialog("Количество циклов");
            return;
        }

        int measAmount;
        try {
            measAmount = Integer.parseInt(this.measAmount.getText());
            log.info("measAmount: {}", measAmount);
        } catch (Exception e) {
            log.error("startMetrology measAmount parsing", e);
            showParsingErrorDialog("Количество измерений одного цикла");
            return;
        }

        float realMeasData;
        try {
            String[] split = this.realMeasData.getText().split(" ");
            log.debug("realMeasData split: {}", split);

            float value = Float.parseFloat(split[0]);
            log.debug("value: {}", value);

            double prefix = 1;
            if (split.length == 2) {
                String prefixTitle = split[1].trim();
                log.debug("prefixTitle: {}", prefixTitle);
                if (!prefixTitle.isEmpty())
                    prefix = parsingValuePrefix(prefixTitle);
            }
            log.info("prefix: {}", prefix);

            realMeasData = (float) (value * prefix);
            log.info(String.format("realMeasData: %f", realMeasData));

        } catch (Exception e) {
            log.error("startMetrology realMeasData parsing", e);
            showParsingErrorDialog(
                    "Действительное значение",
                    "Ошибка преобразования текста в число c приставкой системы СИ" + System.lineSeparator() +
                            e.getMessage() + System.lineSeparator() +
                            "Пример корректных строк: 1.3 мк, 1.2 м, 1.1, 1.0 К и тд");
            return;
        }

        startMetrology(cycleAmount, measAmount, realMeasData);
    }

    protected abstract void startMetrology(int cycleAmount, int measAmount, float realMeasData);

    protected void showParsingErrorDialog(String field) {
        mainWindow.showDialog(ERROR, "Ошибка",
                String.format(ERROR_PARSE_FILED, field),
                ERROR_PARSE_TITLE);
    }

    protected void showParsingErrorDialog(String field, String msg) {
        mainWindow.showDialog(ERROR, "Ошибка",
                String.format(ERROR_PARSE_FILED, field),
                msg);
    }

    public void putStateParam() {
        rootController.getAppState().putParam(CYCLE_AMOUNT_STATE_APP_PARAM, cycleAmount.getText());
        rootController.getAppState().putParam(MEAS_AMOUNT_STATE_APP_PARAM, measAmount.getText());
        rootController.getAppState().putParam(REAL_MEAS_DATA_STATE_APP_PARAM, realMeasData.getText());
    }

    public void setDisableAllButtons(boolean b) {
        startBtn.setDisable(b);
    }
}
