package com.alextim.bee.client.dto;


import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Locale;

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
    public float getAccumulatedMeasData() {
        return accumulatedScore;
    }

    @Override
    public float getAccumulatedPowerMeasData() {
        return accumulatedPowerScore;
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
                "Текущий ППН: " + currentDensity + " нейтр./см²сек, " +
                        "Усредненный за время экспозиции ППН: " + averageDensity + " нейтр./см²сек," +
                        System.lineSeparator() +
                        "Накопленный счет после запуска режима накопления: " + accumulatedScore + " имп, " +
                        "Накопленный счет за время работы БД: " + accumulatedPowerScore + " имп," +
                        System.lineSeparator() +
                        "Текущий счет: " + currentScore + " имп/сек, " +
                        "Усредненный за время экспозиции счет: " + averageScore + " имп/сек," +
                        System.lineSeparator() +
                        "Интервал времени после запуска режима накопления: " + accumulatedTime + " сек");
    }
}