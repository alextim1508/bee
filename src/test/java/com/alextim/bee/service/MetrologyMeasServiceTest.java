package com.alextim.bee.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetrologyMeasServiceTest {

    @Test
    public void calcAveMeasDataTest() {
        MetrologyMeasService metrologyMeasService = new MetrologyMeasService();

        metrologyMeasService.initAverage(1.2f, 1);
        metrologyMeasService.initAverage(1.3f, 2);
        metrologyMeasService.initAverage(1.4f, 3);
        metrologyMeasService.initAverage(1.5f, 4);

        float actual = (1.2f + 1.3f + 1.4f + 1.5f) / 4;

        Assertions.assertEquals(metrologyMeasService.aveMeasData, actual, 0.0001);
    }
}
