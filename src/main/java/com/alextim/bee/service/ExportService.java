package com.alextim.bee.service;

import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.service.StatisticMeasService.StatisticMeasurement;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiConsumer;

import static com.alextim.bee.context.Property.DATE_TIME_FORMATTER;

@Slf4j
public class ExportService {

    @SneakyThrows
    public void exportMeasurements(Collection<StatisticMeasurement> measurements, String fileComment, File file,
                                   BiConsumer<Integer, Double> progress) {
        log.info("export to file");

        Iterator<StatisticMeasurement> iterator = measurements.stream()
                .sorted(Comparator.comparingLong(o -> o.time))
                .toList().iterator();

        @Cleanup
        FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);

        fileWriter
                .append("Комментарий: ").append(fileComment)
                .append(System.lineSeparator());

        for (int i = 0; iterator.hasNext(); i++) {
            progress.accept(i + 1, 1.0 * (i + 1) / measurements.size());

            StatisticMeasurement next = iterator.next();

            fileWriter
                    .append(next.measDataTitle + ": " + next.currentMeasDataValue + " "  + next.measDataUnit).append("\t")
                    .append(next.measDataTitle + ", среднее значение: " + next.averageMeasDataValue + " " + next.measDataUnit).append("\t")
                    .append("Текущее значение счетчика 1: " + next.currentCount1).append("\t")
                    .append("Текущее значение счетчик 2: " + next.currentCount2).append("\t")
                    .append("Текущее суммарное значение счетчиков: " + next.currentCountSum).append("\t")
                    .append("Среднее значение счетчика 1: " + next.averageCount1).append("\t")
                    .append("Среднее значение счетчика 2: " + next.averageCount2).append("\t")
                    .append("Среднее суммарное значение счетчиков: " + next.averageCountSum).append("\t")
                    .append("Накопленное значение счетчика 1: " + next.getCount1()).append("\t")
                    .append("Накопленное значение счетчика 2: " + next.getCount2()).append("\t")
                    .append("Накопленное суммарное значение счетчиков: " + next.getCountSum()).append("\t")
                    .append("Время после включения БД:  " + next.time).append(",\t")
                    .append("Гео данные: " + next.geoData.lat() + ", " + next.geoData.lon()).append("\t")
                    .append("Режим работы счетчиков: " + next.mode.title).append("\t")
                    .append("Дата:  " + (next.localDateTime != null ? DATE_TIME_FORMATTER.format(next.localDateTime) : "-"))
                    .append(System.lineSeparator());
        }
        fileWriter.flush();

        log.info("export to file OK");
    }

    @SneakyThrows
    public void exportDetectorMsgs(Collection<DetectorMsg> detectorMsgs, File file, BiConsumer<Integer, Double> progress) {
        log.info("export to file");

        @Cleanup
        FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);

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
                    .append(next.toString().replace(System.lineSeparator(), " ")).append("\t")
                    .append(hexData)
                    .append(System.lineSeparator());
        }

        fileWriter.flush();

        log.info("export to file OK");
    }
}
