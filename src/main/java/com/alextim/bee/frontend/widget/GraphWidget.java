package com.alextim.bee.frontend.widget;


import com.alextim.bee.frontend.widget.graphs.AbstractGraph;
import de.gsi.chart.XYChart;
import de.gsi.chart.XYChartCss;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.axes.spi.format.DefaultTimeFormatter;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.utils.StyleParser;
import de.gsi.dataset.DataSet;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import static com.alextim.bee.frontend.MainWindow.PROGRESS_BAR_COLOR;
import static com.alextim.bee.frontend.widget.GraphWidget.GraphFrontendState.COLOR_CHANGED;
import static com.alextim.bee.frontend.widget.GraphWidget.GraphFrontendState.ENABLED;

public class GraphWidget {

    private final List<AbstractGraph> graphs;
    private final AnchorPane pane;


    public AnchorPane getPane() {
        return pane;
    }

    private XYChart chart;
    private Zoomer zoomPlugin;
    private DefaultNumericAxis xAxis, yAxis;
    private GridPane legend, tools;

    private int toolCount = 0, legendCount = 0;

    public GraphWidget(String yAxisLabel) {
        graphs = new ArrayList<>();

        initLegend();
        initTool();

        initChart(yAxisLabel);

        Button originBtn = addBtnZoom();

        pane = new AnchorPane();
        pane.getChildren().addAll(chart, legend, originBtn);

        AnchorPane.setTopAnchor(chart, 0.0);
        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setRightAnchor(chart, 0.0);

        AnchorPane.setTopAnchor(legend, 5.0);
        AnchorPane.setRightAnchor(legend, 5.0);

        AnchorPane.setBottomAnchor(originBtn, 0.0);
        AnchorPane.setLeftAnchor(originBtn, 0.0);
    }

    public void addGraph(AbstractGraph graph) {
        graphs.add(graph);

        chart.getDatasets().addAll(graph.getDataSetList());

        addLegend(graph);

        zoomPlugin.zoomOrigin();

        byFrontendStyleSetter.accept(ENABLED, graph);
    }

    private void addLegend(AbstractGraph graph) {
        CheckBox checkBox = new CheckBox();
        checkBox.textProperty().bind(graph.getTitle());
        checkBox.setSelected(true);

        checkBox.selectedProperty().addListener((observable, oldSelectedValue, newSelectedValue) -> {
            graph.setShow(newSelectedValue);

            if(newSelectedValue) {
                chart.getDatasets().addAll(graph.getDataSetList());
            } else {
                chart.getDatasets().removeAll(graph.getDataSetList());
            }

            forceToRedraw();
        });

        ColorPicker colorPicker = new ColorPicker(Color.web(graph.getColor()));
        colorPicker.setMaxWidth(30);
        colorPicker.setOnAction(event -> {
            graph.setColor(colorPicker.getValue().toString());

            if (checkBox.isSelected())
                byFrontendStyleSetter.accept(COLOR_CHANGED, graph);

            forceToRedraw();
        });


        ProgressBar progressBar = new ProgressBar();
        progressBar.setStyle("-fx-accent: " + PROGRESS_BAR_COLOR);
        if (graph.getProgress() != null)
            progressBar.progressProperty().bind(graph.getProgress());
        else
            progressBar.setProgress(1);

        Label progressLabel = new Label();
        if (graph.getProgressTitle() != null) {
            progressLabel.textProperty().bind(graph.getProgressTitle());
        }

        Label addInformation = new Label();
        if (graph.getAddInformation() != null)
            addInformation.textProperty().bind(graph.getAddInformation());

        if (graph.getProgressTitle() == null && graph.getProgress() == null)
            legend.addRow(legendCount++, addInformation, checkBox, colorPicker);
        else
            legend.addRow(legendCount++, addInformation, new StackPane(progressBar, progressLabel), checkBox, colorPicker);
    }

    public void forceToRedraw() {
        chart.requestLayout();
    }

    private void initLegend() {
        legend = new GridPane();
        legend.setVgap(10);
        legend.setHgap(10);
    }

    private void initTool() {
        tools = new GridPane();
        tools.setVgap(10);
        tools.setHgap(10);
    }

    private void initChart(String yAxisLabel) {
        yAxis = new DefaultNumericAxis(yAxisLabel, null);
        yAxis.setMin(0);
        yAxis.setMax(100);

        final long now = System.currentTimeMillis();
        xAxis = new TimeAxis("Время", "с", now, 10 * now, 1000);

        chart = new XYChart(xAxis, yAxis);
        chart.setLegendVisible(false);

        zoomPlugin = getZoomPlugin();
        chart.getPlugins().add(zoomPlugin);
        chart.getPlugins().add(new MyDataPointTooltipPlugin(10, null));


        ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
        renderer.setPolyLineStyle(LineStyle.NORMAL);
        renderer.setErrorType(ErrorStyle.NONE);

        chart.getRenderers().clear();
        chart.getRenderers().add(renderer);
    }

    protected class TimeAxis extends DefaultNumericAxis {
        public TimeAxis(String label, String unit, final double min, final double max, final double tick) {
            super(label, min, max, tick);
            setUnit(unit);
            setAutoRanging(true);
            setAutoRangeRounding(true);
            setAxisLabelFormatter(new DefaultTimeFormatter() {
                private final SimpleDateFormat format = new SimpleDateFormat("H:mm:ss");

                @Override
                public String toString(Number utcValueSeconds) {
                    return format.format(new Date(utcValueSeconds.longValue()));
                }
            });
            VBox.setVgrow(this, Priority.ALWAYS);
        }
    }

    private Button addBtnZoom() {
        Label label = new Label();
        label.setStyle("-fx-font-family: FontAwesome; -fx-font-size: 20.0; ");
        label.setText("\uf0b2");
        Button zoomOut = new Button((String) null, label);
        zoomOut.setPadding(new Insets(3.0, 3.0, 3.0, 3.0));
        zoomOut.setTooltip(new Tooltip("Вписать в границы осей"));
        zoomOut.setOnAction((evt) -> {
            zoomPlugin.zoomOrigin();
        });
        return zoomOut;
    }

    private Zoomer getZoomPlugin() {
        Zoomer zoomer = new Zoomer();
        zoomer.setAddButtonsToToolBar(false);
        zoomer.setAutoZoomEnabled(true);
        zoomer.setSliderVisible(false);
        return zoomer;
    }

    public static void setStyle(DataSet dataSet, String color, int width, int dashSize) {
        String style = dataSet.getStyle();

        Map<String, String> map = StyleParser.splitIntoMap(style);
        map.put(XYChartCss.DATASET_STROKE_COLOR.toLowerCase(), color);
        map.put(XYChartCss.STROKE_WIDTH.toLowerCase(), Integer.toString(width));

        if (dashSize != 0)
            map.put(XYChartCss.STROKE_DASH_PATTERN.toLowerCase(), Integer.toString(dashSize));

        dataSet.setStyle(StyleParser.mapToString(map));
    }

    public static void setStyle(DataSet dataSet, String color, int width) {
        setStyle(dataSet, color, width, 0);
    }

    public static void setStyle(DataSet dataSet, String color) {
        setStyle(dataSet, color, 1, 0);
    }

    public static void setTransparent(DataSet dataSet) {
        setStyle(dataSet, Color.TRANSPARENT.toString());
    }

    public enum GraphFrontendState {
        COLOR_CHANGED, CONTENT_CHANGED, ENABLED, DISABLED
    }

    public void setByFrontendStyleSetter(BiConsumer<GraphFrontendState, AbstractGraph> frontendStyleSetter) {
        byFrontendStyleSetter = frontendStyleSetter;
    }

    private BiConsumer<GraphFrontendState, AbstractGraph> byFrontendStyleSetter = (state, graph) -> {
        if (state.equals(ENABLED) || state.equals(COLOR_CHANGED)) {
            graph.getDataSetList().forEach(dataSet -> setStyle(dataSet, graph.getColor()));

        } else {
            graph.getDataSetList().forEach(GraphWidget::setTransparent);
        }
    };

    @Deprecated
    public static void setStyle(javafx.scene.chart.XYChart.Series<?, ?> series, String color, int width) {
        series.getNode().lookup(".chart-series-line").setStyle(
                "-fx-stroke: " + color + ";" +
                        (width > 0 ? "-fx-stroke-width: " + width + ";" : ""));
    }

    @Deprecated
    public static void setPointStyle(javafx.scene.chart.XYChart.Series<?, ?> series, String color) {
        for (javafx.scene.chart.XYChart.Data<?, ?> entry : series.getData())
            entry.getNode().setStyle("-fx-background-color: " + color + ";");
    }

    @Deprecated
    public static void setTransparent(javafx.scene.chart.XYChart.Series<?, ?> series) {
        series.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: transparent;");
    }

    @Deprecated
    public static String get16baseColorString(Color color) {
        StringBuilder builder = new StringBuilder("#");

        String s = Integer.toString((int) (255 * color.getRed()), 16);
        if (s.length() < 2)
            builder.append('0');
        builder.append(s);

        s = Integer.toString((int) (255 * color.getGreen()), 16);
        if (s.length() < 2)
            builder.append('0');
        builder.append(s);

        s = Integer.toString((int) (255 * color.getBlue()), 16);
        if (s.length() < 2)
            builder.append('0');
        builder.append(s);

        return builder.toString();
    }

    public void clear() {
        graphs.clear();
        chart.getDatasets().clear();
        legend.getChildren().clear();
        legendCount = 0;
    }

    public List<AbstractGraph> getGraphs() {
        return graphs;
    }
}
