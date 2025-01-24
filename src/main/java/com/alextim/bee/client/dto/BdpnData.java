package com.alextim.bee.client.dto;


import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Locale;

import static com.alextim.bee.context.Property.COUNTER_NUMBER_FORMAT;
import static com.alextim.bee.context.Property.MEAS_DATA_NUMBER_FORMAT;

@Getter
@SuperBuilder
public class BdpnData extends BdData {

    private float currentDensity;       // Текущая плотность нейтронов, нейтр./см²сек
    private float averageDensity;       // Усредненная за время экспозиции плотность нейтронов
    private float accumulatedScore;            // Накопленный счет после запуска режима накопления, Имп
    private float accumulatedPowerScore;            // Накопленный счет за время работы БД, Имп

    public final static String title = "ППН";
    public final static String measDataUnit = "нейтр./см²сек";

    @Override
    public float getCurrentMeasData() {
        return currentDensity;
    }

    @Override
    public float getAverageMeasData() {
        return averageDensity;
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
                "Текущий ППН: " + MEAS_DATA_NUMBER_FORMAT + " нейтр./см²сек, " +
                        "Усредненный за время экспозиции ППН: " + MEAS_DATA_NUMBER_FORMAT + " нейтр./см²сек, " +
                        System.lineSeparator() +
                        "Накопленный счет после запуска режима накопления: " + COUNTER_NUMBER_FORMAT + " имп, " +
                        "Накопленный счет за время работы БД: " + COUNTER_NUMBER_FORMAT + " имп, " +
                        System.lineSeparator() +
                        "Текущий счет: " + COUNTER_NUMBER_FORMAT + " имп/сек, " +
                        "Усредненный за время экспозиции счет: " + COUNTER_NUMBER_FORMAT + " имп/сек, " +
                        System.lineSeparator() +
                        "Интервал времени после запуска режима накопления: %d сек",
                currentDensity,
                averageDensity,
                accumulatedScore,
                accumulatedPowerScore,
                currentScore,
                averageScore,
                accumulatedTime);
    }
}