package com.alextim.bee.client.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Locale;

import static com.alextim.bee.context.Property.COUNTER_NUMBER_FORMAT;
import static com.alextim.bee.context.Property.MEAS_DATA_NUMBER_FORMAT;


@Getter
@SuperBuilder
public class BdmgData extends BdData {

    private float currentMED;     // Текущая МЭД
    private float averageMED;     // Усредненная за время экспозиции МЭД
    private float accumulatedMED;       // Накопленная доза после запуска режима накопления, Зв
    private float accumulatedPowerMEDP;       // Накопленная доза за время работы БД, Зв

    public final static String title = "МАЭД";
    public final static String measDataUnit = "Зв/час";

    private static final float MILLI_PREFIX = 0.001f;

    @Override
    public float getCurrentMeasData() {
        return MILLI_PREFIX * currentMED;
    }

    @Override
    public float getAverageMeasData() {
        return MILLI_PREFIX * averageMED;
    }

    @Override
    public float getAccumulatedMeasData() {
        return accumulatedMED;
    }

    @Override
    public float getAccumulatedPowerMeasData() {
        return accumulatedPowerMEDP;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getMeasDataUnit() {
        return measDataUnit;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Текущая МАЭД: " + MEAS_DATA_NUMBER_FORMAT + " мЗв/час, " +
                        "Усредненная за время экспозиции МАЭД: " + MEAS_DATA_NUMBER_FORMAT + " мЗв/час, " +
                        System.lineSeparator() +
                        "Накопленная МАЭД после запуска режима накопления: " + COUNTER_NUMBER_FORMAT + " Зв, " +
                        "Накопленная МАЭД за время работы БД: " + COUNTER_NUMBER_FORMAT + " Зв, " +
                        System.lineSeparator() +
                        "Текущий счет: " + COUNTER_NUMBER_FORMAT + " имп/сек, " +
                        "Усредненный за время экспозиции счет: " + COUNTER_NUMBER_FORMAT + " имп/сек, " +
                        System.lineSeparator() +
                        "Интервал времени после запуска режима накопления: %d сек",
                currentMED,
                averageMED,
                accumulatedMED,
                accumulatedPowerMEDP,
                currentScore,
                averageScore,
                accumulatedTime);
    }
}
