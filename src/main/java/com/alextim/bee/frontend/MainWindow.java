package com.alextim.bee.frontend;


import com.alextim.bee.RootController;
import com.alextim.bee.frontend.dialog.error.ErrorDialog;
import com.alextim.bee.frontend.dialog.progress.ProgressDialog;
import com.alextim.bee.frontend.view.data.DataView;
import com.alextim.bee.frontend.view.magazine.MagazineView;
import com.alextim.bee.frontend.view.management.ManagementView;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class MainWindow {
    public static String ROOT_KEY = "ROOT";
    public static String PARENT_KEY = "PARENT";
    public static String PROGRESS_BAR_COLOR = "#3fd03f";
    public static String EVENT_COLOR = "#3fd03f";
    public static String COMMAND_COLOR = "#3fd03f";
    public static String COMMAND_ANSWER_COLOR = "#3fd03f";

    private final Stage owner;

    @Getter
    private Image iconImage,
            grayCircleImage, grayCircleExclamationImage,
            yellowCircleImage, yellowCircleExclamationImage,
            greenCircleImage, greenCircleExclamationImage,
            redCircleImage, redCircleExclamationImage,
            noConnectImage;

    @SneakyThrows
    private void loadIcons() {
        @Cleanup
        InputStream iconResAsStream = MainWindow.class.getResourceAsStream("icon/icon.png");
        iconImage = new Image(Objects.requireNonNull(iconResAsStream));

        @Cleanup
        InputStream grayCircleAsStream = MainWindow.class.getResourceAsStream("icon/circle-gray.png");
        grayCircleImage = new Image(Objects.requireNonNull(grayCircleAsStream));
        @Cleanup
        InputStream grayCircleExclamationAsStream = MainWindow.class.getResourceAsStream("icon/circle-gray-exclamation.png");
        grayCircleExclamationImage = new Image(Objects.requireNonNull(grayCircleExclamationAsStream));

        @Cleanup
        InputStream yellowCircleAsStream = MainWindow.class.getResourceAsStream("icon/circle-yellow.png");
        yellowCircleImage = new Image(Objects.requireNonNull(yellowCircleAsStream));
        @Cleanup
        InputStream yellowCircleExclamationAsStream = MainWindow.class.getResourceAsStream("icon/circle-yellow-exclamation.png");
        yellowCircleExclamationImage = new Image(Objects.requireNonNull(yellowCircleExclamationAsStream));

        @Cleanup
        InputStream greenCircleAsStream = MainWindow.class.getResourceAsStream("icon/circle-green.png");
        greenCircleImage = new Image(Objects.requireNonNull(greenCircleAsStream));
        @Cleanup
        InputStream greenCircleExclamationAsStream = MainWindow.class.getResourceAsStream("icon/circle-green-exclamation.png");
        greenCircleExclamationImage = new Image(Objects.requireNonNull(greenCircleExclamationAsStream));

        @Cleanup
        InputStream redCircleAsStream = MainWindow.class.getResourceAsStream("icon/circle-red.png");
        redCircleImage = new Image(Objects.requireNonNull(redCircleAsStream));
        @Cleanup
        InputStream redCircleExAsStream = MainWindow.class.getResourceAsStream("icon/circle-red-exclamation.png");
        redCircleExclamationImage = new Image(Objects.requireNonNull(redCircleExAsStream));

        @Cleanup
        InputStream noConnectAsStream = MainWindow.class.getResourceAsStream("icon/no-connect.png");
        noConnectImage = new Image(Objects.requireNonNull(noConnectAsStream));
    }

    private ResourceBundle getBundle(RootController rootController, MainWindow mainWindow) {
        return new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                if (key.equals(ROOT_KEY))
                    return rootController;
                if (key.equals(PARENT_KEY))
                    return mainWindow;
                return null;
            }

            @Override
            public Enumeration<String> getKeys() {
                return Collections.enumeration(Arrays.asList(ROOT_KEY, PARENT_KEY));
            }
        };
    }

    public AnchorPane createMainWindow(RootController rootController) {
        log.info("Creation main window");

        ResourceBundle bundle = getBundle(rootController, this);

        TabPane tabPane = new TabPane(
                new Tab("Данные", new DataView().getView(bundle)),
                new Tab("Журнал", new MagazineView().getView(bundle)),
                new Tab("Параметры", new ManagementView().getView(bundle))
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        log.info("basePane is built");

        log.info("Creation main window is created");

        AnchorPane pane = new AnchorPane(tabPane);

        AnchorPane.setTopAnchor(tabPane, 0.);
        AnchorPane.setLeftAnchor(tabPane, 0.);
        AnchorPane.setRightAnchor(tabPane, 0.);
        AnchorPane.setBottomAnchor(tabPane, 0.);

        AnchorPane.setTopAnchor(pane, 0.);
        AnchorPane.setLeftAnchor(pane, 0.);
        AnchorPane.setRightAnchor(pane, 0.);
        AnchorPane.setBottomAnchor(pane, 0.);

        loadIcons();

        return pane;
    }

    public void showError(Throwable throwable) {
        log.error("", throwable);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String stackTrace = sw.toString();

        String lastErrMsg = null;
        while (throwable != null) {
            lastErrMsg = throwable.getClass().getSimpleName() + "/" + throwable.getMessage();
            throwable = throwable.getCause();
        }

        ErrorDialog dialog = new ErrorDialog(lastErrMsg, stackTrace);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.show();
    }

    public boolean showDialog(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.initOwner(owner);
        alert.setHeaderText(header);
        alert.setGraphic(null);
        alert.setContentText(content);
        alert.setResizable(true);
        Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(e -> alert.hide());

        ArrayList<ButtonType> buttons = new ArrayList<>();
        ButtonType buttonTypeYes = new ButtonType("Да");
        buttons.add(buttonTypeYes);

        ButtonType buttonTypeNo = new ButtonType("Нет");
        if (type.equals(Alert.AlertType.WARNING)) {
            buttons.add(buttonTypeNo);
        }

        alert.getButtonTypes().setAll(buttons);

        ButtonType result = alert.showAndWait().orElse(null);
        if (result == null)
            return false;

        return result.equals(buttonTypeYes);
    }

    public File showFileChooseDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        return fileChooser.showSaveDialog(owner);
    }

    public ProgressDialog showProgressDialog(DoubleProperty progressProperty, StringProperty stringProperty) {
        ProgressDialog dialog = new ProgressDialog(progressProperty, stringProperty);
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.show();
        return dialog;
    }
}
