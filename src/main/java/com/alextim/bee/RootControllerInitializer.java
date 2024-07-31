package com.alextim.bee;

import com.alextim.bee.client.DetectorClientAbstract;
import com.alextim.bee.frontend.MainWindow;
import com.alextim.bee.frontend.view.NodeController;
import com.alextim.bee.service.ExportService;
import com.alextim.bee.service.StatisticMeasService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@RequiredArgsConstructor
@Slf4j
public class RootControllerInitializer {

    protected final MainWindow mainWindow;
    protected final DetectorClientAbstract detectorClient;
    protected final StatisticMeasService statisticMeasService;
    protected final ExportService exportService;

    protected ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    protected Map<String, NodeController> children = new HashMap<>();

    public void addChild(String name, NodeController child) {
        log.debug("{} is added to root controller children", name);
        children.put(name, child);
    }

    public NodeController getChild(String name) {
        return children.get(name);
    }
}
