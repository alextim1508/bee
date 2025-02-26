module com.alextim.bee {
    requires static lombok;
    requires org.slf4j;

    requires javafx.controls;
    requires javafx.fxml;

    requires de.gsi.chartfx.dataset;
    requires de.gsi.chartfx.chart;

    opens com.alextim.bee.frontend.view.data to javafx.fxml;
    opens com.alextim.bee.frontend.view.magazine to javafx.fxml;
    opens com.alextim.bee.frontend.view.management to javafx.fxml;
    opens com.alextim.bee.frontend.view.metrology to javafx.fxml;
    opens com.alextim.bee.frontend.dialog.progress to javafx.fxml;
    opens com.alextim.bee.frontend.dialog.error to javafx.fxml;

    exports com.alextim.bee;
}