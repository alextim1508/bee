package com.alextim.bee.context;


import com.alextim.bee.RootController;
import com.alextim.bee.client.DetectorClientAbstract;
import com.alextim.bee.client.DetectorClientFake;
import com.alextim.bee.frontend.MainWindow;
import com.alextim.bee.service.ExportService;
import com.alextim.bee.service.StatisticMeasService;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class Context {

    @Getter
    RootController rootController;

    private DetectorClientAbstract detectorClient;
    private StatisticMeasService statisticMeasService;
    private ExportService exportService;

    public Context(MainWindow mainWindow, String[] args) {
        readAppProperty();

        createBeans(mainWindow);
    }

    @SneakyThrows
    void readAppProperty() {
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

        appPropertiesInit(properties);
    }

    private void appPropertiesInit(Properties properties) {
        TITLE_APP = (String) properties.get("app.title");
        log.info("TITLE_APP: {}", TITLE_APP);
    }

    void createBeans(MainWindow mainWindow) {
        createServices();
        createRootController(mainWindow);
    }


    private void createServices() {
        detectorClient = new DetectorClientFake(new LinkedBlockingQueue<>());

        statisticMeasService = new StatisticMeasService();
        exportService = new ExportService();
    }

    private void createRootController(MainWindow mainWindow) {
        log.info("Creating root controller");
        rootController = new RootController(mainWindow, detectorClient, statisticMeasService, exportService);
    }

    public static String TITLE_APP;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("H:mm:ss:SSS");
}
