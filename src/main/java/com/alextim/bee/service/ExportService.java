package com.alextim.bee.service;

import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.service.StatisticMeasService.StatisticMeasurement;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;

@Slf4j
public class ExportService {

    @SneakyThrows
    public void exportMeasurements(Collection<StatisticMeasurement> measurements, File file, BiConsumer<Integer, Double> progress) {
        log.info("export to file");

        @Cleanup
        FileWriter fileWriter = new FileWriter(file);

        Iterator<StatisticMeasurement> iterator = measurements.iterator();

        for (int i = 0; iterator.hasNext(); i++) {
            progress.accept(i + 1, 1.0 * (i + 1) / measurements.size());

            StatisticMeasurement next = iterator.next();

            fileWriter
                    .append(next.toString()).append("\t")
                    .append(System.lineSeparator());
        }

        fileWriter.flush();

        log.info("export to file OK");
    }

    @SneakyThrows
    public void exportDetectorMsgs(Collection<DetectorMsg> detectorMsgs, File file, BiConsumer<Integer, Double> progress) {
        log.info("export to file");

        @Cleanup
        FileWriter fileWriter = new FileWriter(file);

        Iterator<DetectorMsg> iterator = detectorMsgs.iterator();

        for (int i = 0; iterator.hasNext(); i++) {
            progress.accept(i + 1, 1.0 * (i + 1) / detectorMsgs.size());

            DetectorMsg next = iterator.next();

            StringBuilder hexData = new StringBuilder();
            for (byte datum : next.data) {
                hexData.append(String.format("%x ", datum));
            }

            fileWriter
                    .append(Integer.toString(next.detectorID)).append("\t")
                    .append(Long.toString(next.time)).append("\t")
                    .append(next.toString()).append("\t")
                    .append(hexData)
                    .append(System.lineSeparator());
        }

        fileWriter.flush();

        log.info("export to file OK");
    }
}
