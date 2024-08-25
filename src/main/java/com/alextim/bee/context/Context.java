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
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
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

    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Context(MainWindow mainWindow, String[] args) {
        readAppProperty();

        createBeans(mainWindow);
    }

    @SneakyThrows
    private void readAppProperty() {
        Properties properties = new Properties();
        try {
            @Cleanup Reader reader = new BufferedReader(new FileReader(
                    System.getProperty("user.dir") + "/config/application.properties"));

            log.info("File application.properties from currently dir is found!");
            properties.load(reader);

        } catch (Exception e) {
            log.info("There are default properties!");

            @Cleanup InputStream resourceAsStream = Context.class.getClassLoader()
                    .getResourceAsStream("application.properties");
            @Cleanup Reader resourceReader = new InputStreamReader(resourceAsStream, "UTF-8");
            properties.load(resourceReader);
        }

        initAppProperties(properties);
    }

    private void initAppProperties(Properties properties) {
        TITLE_APP = (String) properties.get("app.title");
        log.info("TITLE_APP: {}", TITLE_APP);

        FRONTEND_FOR_DETECTOR = (String) properties.get("app.frontendForDetector");
        log.info("FRONTEND_FOR_DETECTOR: {}", FRONTEND_FOR_DETECTOR);

        SOFTWARE_VERSION = (String) properties.get("app.version");
        log.info("SOFTWARE_VERSION: {}", SOFTWARE_VERSION);

        TRANSFER_TO_DETECTOR_ID = Integer.parseInt((String) properties.get("app.transfer.detectorId"));
        log.info("TRANSFER_TO_DETECTOR_ID: {}", TRANSFER_TO_DETECTOR_ID);

        TRANSFER_IP = (String) properties.get("app.transfer.ip");
        log.info("TRANSFER_IP: {}", TRANSFER_IP);

        TRANSFER_PORT = Integer.parseInt((String) properties.get("app.transfer.port"));
        log.info("TRANSFER_PORT: {}", TRANSFER_PORT);

        TRANSFER_RCV_BUFFER_SIZE = Integer.parseInt((String) properties.get("app.transfer.rcvBufferSize"));
        log.info("TRANSFER_RCV_BUFFER_SIZE: {}", TRANSFER_RCV_BUFFER_SIZE);
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
                new DetectorClient(TRANSFER_IP, TRANSFER_PORT, TRANSFER_RCV_BUFFER_SIZE, new LinkedBlockingQueue<>());

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
