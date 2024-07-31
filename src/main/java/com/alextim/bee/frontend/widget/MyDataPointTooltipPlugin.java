
package com.alextim.bee.frontend.widget;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.XYChartCss;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.plugins.AbstractDataFormattingPlugin;
import de.gsi.chart.renderer.Renderer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.utils.StyleParser;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.GridDataSet;
import de.gsi.dataset.spi.utils.Tuple;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import lombok.Data;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MyDataPointTooltipPlugin extends AbstractDataFormattingPlugin {
    public static final String STYLE_CLASS_LABEL = "chart-datapoint-tooltip-label";
    public static final int DEFAULT_PICKING_DISTANCE = 5;
    private static final int LABEL_X_OFFSET = 15;
    private static final int LABEL_Y_OFFSET = 5;
    private Label label;
    private DoubleProperty pickingDistance;
    private EventHandler<MouseEvent> mouseMoveHandler;
    private Consumer<DataPoint> selectedPointHandler;

    public MyDataPointTooltipPlugin() {
        this.label = new Label();
        this.pickingDistance = new SimpleDoubleProperty(this, "pickingDistance", 5.0D) {
            protected void invalidated() {
                if (this.get() <= 0.0D) {
                    throw new IllegalArgumentException("The " + this.getName() + " must be a positive value");
                }
            }
        };
        this.mouseMoveHandler = this::updateToolTip;
        this.label.getStyleClass().add("chart-datapoint-tooltip-label");
        this.label.setWrapText(true);
        this.label.setMinWidth(0.0D);
        this.label.setManaged(false);
        this.registerInputEventHandler(MouseEvent.MOUSE_MOVED, this.mouseMoveHandler);
    }

    public MyDataPointTooltipPlugin(double pickingDistance) {
        this();
        this.setPickingDistance(pickingDistance);
    }

    public MyDataPointTooltipPlugin(double pickingDistance, Consumer<DataPoint> selectedPointHandler) {
        this();
        this.setPickingDistance(pickingDistance);
        this.selectedPointHandler = selectedPointHandler;
    }

    protected Optional<DataPoint> findDataPoint(MouseEvent event, Bounds plotAreaBounds) {
        if (!plotAreaBounds.contains(event.getX(), event.getY())) {
            return Optional.empty();
        } else {
            Point2D mouseLocation = this.getLocationInPlotArea(event);
            return this.findNearestDataPointWithinPickingDistance(mouseLocation);
        }
    }

    protected Optional<DataPoint> findNearestDataPointWithinPickingDistance(Point2D mouseLocation) {
        Chart chart = this.getChart();
        if (!(chart instanceof XYChart)) {
            return Optional.empty();
        } else {
            XYChart xyChart = (XYChart)chart;
            ObservableList<DataSet> xyChartDatasets = xyChart.getDatasets();
            return xyChart.getRenderers().stream().flatMap((renderer) -> {
                return Stream.of(renderer.getDatasets(), xyChartDatasets).flatMap(Collection::stream).flatMap((dataset) -> {
                    return this.getPointsCloseToCursor(dataset, renderer, mouseLocation);
                });
            }).reduce((p1, p2) -> {
                return p1.distanceFromMouse <= p2.distanceFromMouse ? p1 : p2;
            });
        }
    }

    protected Stream<DataPoint> getPointsCloseToCursor(DataSet dataset, Renderer renderer, Point2D mouseLocation) {
        Axis xAxis = this.findXAxis(renderer);
        Axis yAxis = this.findYAxis(renderer);

        //todo
        Map<String, String> map = StyleParser.splitIntoMap(dataset.getStyle());
        String color = map.get(XYChartCss.DATASET_STROKE_COLOR.toLowerCase());
        if(Objects.equals(color, Color.TRANSPARENT.toString())) {
            return Stream.empty();
        }

        if (xAxis != null && yAxis != null) {
            return dataset instanceof GridDataSet ? Stream.empty() : (Stream)dataset.lock().readLockGuard(() -> {
                int minIdx = 0;
                int maxIdx = dataset.getDataCount();
                if (this.isDataSorted(renderer)) {
                    double xMin = xAxis.getValueForDisplay(mouseLocation.getX() - this.getPickingDistance());
                    double xMax = xAxis.getValueForDisplay(mouseLocation.getX() + this.getPickingDistance());
                    minIdx = Math.max(0, dataset.getIndex(0, new double[]{xMin}) - 1);
                    maxIdx = Math.min(dataset.getDataCount(), dataset.getIndex(0, new double[]{xMax}) + 1);
                }

                return ((List)IntStream.range(minIdx, maxIdx).mapToObj((i) -> {
                    return this.getDataPointFromDataSet(renderer, dataset, xAxis, yAxis, mouseLocation, i);
                }).filter((p) -> {
                    return p.distanceFromMouse <= this.getPickingDistance();
                }).map((dataPoint) -> {
                    return dataPoint.withFormattedLabel(this.formatLabel(dataPoint));
                }).collect(Collectors.toList())).stream();
            });
        } else {
            return Stream.empty();
        }
    }

    private boolean isDataSorted(Renderer renderer) {
        return renderer instanceof ErrorDataSetRenderer && ((ErrorDataSetRenderer)renderer).isAssumeSortedData();
    }

    private Axis findYAxis(Renderer renderer) {
        return (Axis)renderer.getAxes().stream().filter((ax) -> {
            return ax.getSide().isVertical();
        }).findFirst().orElse(null);
    }

    private Axis findXAxis(Renderer renderer) {
        return (Axis)renderer.getAxes().stream().filter((ax) -> {
            return ax.getSide().isHorizontal();
        }).findFirst().orElse(null);
    }

    protected DataPoint getDataPointFromDataSet(Renderer renderer, DataSet dataset, Axis xAxis, Axis yAxis, Point2D mouseLocation, int index) {
        double xValue = dataset.get(0, index);
        double yValue = dataset.get(1, index);
        double displayPositionX = xAxis.getDisplayPosition(xValue);
        double displayPositionY = yAxis.getDisplayPosition(yValue);
        double distanceFromMouseLocation = (new Point2D(displayPositionX, displayPositionY)).distance(mouseLocation);
        String dataLabelSafe = this.getDataLabelSafe(dataset, index);
        return new DataPoint(renderer, xValue, yValue, dataLabelSafe, distanceFromMouseLocation);
    }

    protected String formatDataPoint(DataPoint dataPoint) {
        return this.formatData(dataPoint.renderer, new Tuple(dataPoint.x, dataPoint.y));
    }

    protected String formatLabel(DataPoint dataPoint) {
        return String.format("%s", dataPoint.label);
    }

    protected String getDataLabelSafe(DataSet dataSet, int index) {
        String labelString = dataSet.getDataLabel(index);
        return labelString == null ? String.format("%s [%d]", dataSet.getName(), index) : labelString;
    }

    public final double getPickingDistance() {
        return this.pickingDistanceProperty().get();
    }

    public final DoubleProperty pickingDistanceProperty() {
        return this.pickingDistance;
    }

    public final void setPickingDistance(double distance) {
        this.pickingDistanceProperty().set(distance);
    }

    protected void updateLabel(MouseEvent event, Bounds plotAreaBounds, DataPoint dataPoint) {
        this.label.setText(dataPoint.formattedLabel);
        double mouseX = event.getX();
        double spaceLeft = mouseX - plotAreaBounds.getMinX();
        double spaceRight = plotAreaBounds.getWidth() - spaceLeft;
        double width = this.label.prefWidth(-1.0D);
        boolean atSide = true;
        double xLocation;
        if (spaceRight >= width + 15.0D) {
            xLocation = mouseX + 15.0D;
        } else if (spaceLeft >= width + 15.0D) {
            xLocation = mouseX - 15.0D - width;
        } else if (width < plotAreaBounds.getWidth()) {
            xLocation = spaceLeft > spaceRight ? plotAreaBounds.getMaxX() - width : plotAreaBounds.getMinX();
            atSide = false;
        } else {
            width = plotAreaBounds.getWidth();
            xLocation = plotAreaBounds.getMinX();
            atSide = false;
        }

        double mouseY = event.getY();
        double spaceTop = mouseY - plotAreaBounds.getMinY();
        double spaceBottom = plotAreaBounds.getHeight() - spaceTop;
        double height = this.label.prefHeight(width);
        double yLocation;
        if (height < spaceBottom) {
            yLocation = mouseY + 5.0D;
        } else if (height < spaceTop) {
            yLocation = mouseY - 5.0D - height;
        } else if (atSide && height < plotAreaBounds.getHeight()) {
            yLocation = spaceTop < spaceBottom ? plotAreaBounds.getMaxY() - height : plotAreaBounds.getMinY();
        } else if (atSide) {
            yLocation = plotAreaBounds.getMinY();
            height = plotAreaBounds.getHeight();
        } else if (spaceBottom > spaceTop) {
            yLocation = mouseY + 5.0D;
            height = spaceBottom - 5.0D;
        } else {
            yLocation = plotAreaBounds.getMinY();
            height = spaceTop - 5.0D;
        }

        this.label.resizeRelocate(xLocation, yLocation, width, height);
    }

    private void updateToolTip(MouseEvent event) {
        Bounds plotAreaBounds = this.getChart().getPlotArea().getBoundsInLocal();
        Optional<DataPoint> dataPoint = this.findDataPoint(event, plotAreaBounds);
        if (dataPoint.isEmpty()) {
            this.getChartChildren().remove(this.label);
        } else {
            this.updateLabel(event, plotAreaBounds, (DataPoint)dataPoint.get());
            if (!this.getChartChildren().contains(this.label)) {
                this.getChartChildren().add(this.label);
                this.label.requestLayout();
            }

            if(selectedPointHandler != null) {
                selectedPointHandler.accept(dataPoint.get());
            }
        }
    }

    @Data
    public static class DataPoint {
        public final Renderer renderer;
        public final double x;
        public final double y;
        public final String label;
        public final String formattedLabel;
        public final double distanceFromMouse;

        public DataPoint(Renderer renderer, double x, double y, String label, double distanceFromMouse, String formattedLabel) {
            this.renderer = renderer;
            this.x = x;
            this.y = y;
            this.label = label;
            this.distanceFromMouse = distanceFromMouse;
            this.formattedLabel = formattedLabel;
        }

        public DataPoint(Renderer renderer, double x, double y, String label, double distanceFromMouse) {
            this(renderer, x, y, label, distanceFromMouse, "");
        }

        public DataPoint withFormattedLabel(String formattedLabel) {
            return new DataPoint(this.renderer, this.x, this.y, formattedLabel, this.distanceFromMouse, formattedLabel);
        }
    }
}
