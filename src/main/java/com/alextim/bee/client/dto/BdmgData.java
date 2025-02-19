package com.alextim.bee.client.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Locale;


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
                "Текущая МАЭД: " + currentMED + " мЗв/час, " +
                        "Усредненная за время экспозиции МАЭД: " + averageMED + " мЗв/час," +
                        System.lineSeparator() +
                        "Накопленная МАЭД после запуска режима накопления: " + accumulatedMED + " Зв, " +
                        "Накопленная МАЭД за время работы БД: " + accumulatedPowerMEDP + " Зв," +
                        System.lineSeparator() +
                        "Текущий счет: " + currentScore + " имп/сек, " +
                        "Усредненный за время экспозиции счет: " + averageScore + " имп/сек, " +
                        System.lineSeparator() +
                        "Интервал времени после запуска режима накопления: " + accumulatedTime + " сек");
    }
}
