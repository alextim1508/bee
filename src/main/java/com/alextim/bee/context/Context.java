package com.alextim.bee.context;


import com.alextim.bee.RootController;
import com.alextim.bee.client.DetectorClient;
import com.alextim.bee.client.DetectorClientAbstract;
import com.alextim.bee.frontend.MainWindow;
import com.alextim.bee.service.ExportService;
import com.alextim.bee.service.StatisticMeasService;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import static com.alextim.bee.context.Property.*;

@Slf4j
public class Context {

    @Getter
    private RootController rootController;

    private DetectorClientAbstract detectorClient;
    private StatisticMeasService statisticMeasService;
    private ExportService exportService;
    private AppState appState;

    public Context(MainWindow mainWindow, String[] args) {
        readAppProperty();

        createBeans(mainWindow);
    }

    @SneakyThrows
    private void readAppProperty() {
        Properties properties = new Properties();
        try {
            @Cleanup Reader reader = new BufferedReader(new FileReader(
                    System.getProperty("user.dir") + "/config/application.properties", StandardCharsets.UTF_8));

            log.info("File application.properties from currently dir is found!");
            properties.load(reader);

        } catch (Exception e) {
            log.info("There are default properties!");

            @Cleanup InputStream resourceAsStream = Context.class.getClassLoader()
                    .getResourceAsStream("application.properties");
            @Cleanup Reader resourceReader = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);
            properties.load(resourceReader);
        }

        initAppProperties(properties);
    }

    private void initAppProperties(Properties properties) {
        TITLE_APP = (String) properties.get("app.title");
        log.info("TITLE_APP: {}", TITLE_APP);

        FRONTEND_FOR_DETECTOR = (String) properties.get("app.frontend-for-detector");
        log.info("FRONTEND_FOR_DETECTOR: {}", FRONTEND_FOR_DETECTOR);

        initTransferProperties(properties);

        initNumberFormatProperties(properties);
    }

    private void initTransferProperties(Properties properties) {
        TRANSFER_TO_DETECTOR_ID = Integer.parseInt((String) properties.get("app.transfer.detector-id"));
        log.info("TRANSFER_TO_DETECTOR_ID: {}", TRANSFER_TO_DETECTOR_ID);

        TRANSFER_IP = (String) properties.get("app.transfer.ip");
        log.info("TRANSFER_IP: {}", TRANSFER_IP);

        TRANSFER_RCV_PORT = Integer.parseInt((String) properties.get("app.transfer.rcv-port"));
        log.info("TRANSFER_RCV_PORT: {}", TRANSFER_RCV_PORT);

        TRANSFER_TR_PORT = Integer.parseInt((String) properties.get("app.transfer.tr-port"));
        log.info("TRANSFER_TR_PORT: {}", TRANSFER_TR_PORT);

        TRANSFER_RCV_BUFFER_SIZE = Integer.parseInt((String) properties.get("app.transfer.rcv-bfr-size"));
        log.info("TRANSFER_RCV_BUFFER_SIZE: {}", TRANSFER_RCV_BUFFER_SIZE);
    }

    private void initNumberFormatProperties(Properties properties) {
        COUNTER_NUMBER_FORMAT = (String) properties.get("app.view.counter-float-number-formatting");
        log.info("COUNTER_NUMBER_FORMAT: {}", COUNTER_NUMBER_FORMAT);

        MEAS_DATA_NUMBER_FORMAT = (String) properties.get("app.view.meas-data-float-number-formatting");
        log.info("MEAS_DATA_NUMBER_FORMATTING: {}", MEAS_DATA_NUMBER_FORMAT);

        OTHER_NUMBER_FORMAT = (String) properties.get("app.view.other-float-number-formatting");
        log.info("OTHER_NUMBER_FORMAT: {}", OTHER_NUMBER_FORMAT);

        MEAS_DATA_NUMBER_SING_DIGITS = Integer.parseInt((String) properties.get("app.view.meas-data-float-number-sign-digits"));
        log.info("MEAS_DATA_NUMBER_SING_DIGITS: {}", MEAS_DATA_NUMBER_SING_DIGITS);
    }

    void createBeans(MainWindow mainWindow) {
        createStateApp();
        createServices();
        createRootController(mainWindow);
    }

    private void createStateApp() {
        appState = new AppState(new File(System.getProperty("user.dir") + "/AppParams.txt"));
        try {
            appState.readParam();
        } catch (Exception e) {
            log.error("ReadParam error", e);
        }
    }

    private void createServices() {
        detectorClient =
                new DetectorClient(TRANSFER_IP, TRANSFER_RCV_PORT, TRANSFER_TR_PORT, TRANSFER_RCV_BUFFER_SIZE, new LinkedBlockingQueue<>());

        statisticMeasService = new StatisticMeasService();

        exportService = new ExportService();
    }

    private void createRootController(MainWindow mainWindow) {
        log.info("Creating root controller");

        rootController = new RootController(
                appState,
                mainWindow,
                detectorClient,
                statisticMeasService,
                exportService);
    }

}
