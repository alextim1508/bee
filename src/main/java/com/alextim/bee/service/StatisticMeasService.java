package com.alextim.bee.service;

import com.alextim.bee.client.dto.GeoData;
import com.alextim.bee.client.dto.InternalData;
import com.alextim.bee.client.dto.Measurement;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.alextim.bee.context.Property.DATE_TIME_FORMATTER;

@Slf4j
public class StatisticMeasService {

    @Getter
    public static class StatisticMeasurement {

        private static float count1, count2, count3, count4, countSum;

        private float averageCount1, averageCount2, averageCount3, averageCount4, averageCountSum;

        private float currentCount1, currentCount2, currentCount3, currentCount4, currentCountSum;

        private float currentMeasDataValue, averageMeasDataValue;

        private String measDataTitle, measDataUnit;

        private GeoData geoData;

        private long accInterval;

        private long time = -1;

        private LocalDateTime localDateTime;

        public static void clear() {
            countSum = count1 = count2 = count3 = count4 = 0;
        }

        public float getCount1() {
            return count1;
        }

        public float getCount2() {
            return count2;
        }

        public float getCount3() {
            return count3;
        }

        public float getCount4() {
            return count4;
        }

        public float getCountSum() {
            return countSum;
        }

        @Override
        public String toString() {
            return measDataTitle + ": " + currentMeasDataValue + " / " + averageMeasDataValue + " " + measDataUnit + " " +
                    "Текущее значение счетчика 1: " + currentCount1 +
                    ", Текущее значение счетчик 2: " + currentCount2 +
                    ", Текущее суммарное значение счетчиков: " + currentCountSum +
                    ", Среднее значение счетчика 1: " + averageCount1 +
                    ", Среднее значение счетчика 2: " + averageCount2 +
                    ", Среднее суммарное значение счетчиков: " + averageCountSum +
                    ", Накопленное значение счетчика 1: " + count1 +
                    ", Накопленное значение счетчика 2: " + count2 +
                    ", Накопленное суммарное значение счетчиков: " + countSum +
                    ", Время после включения БД:  " + time +
                    ", Гео данные: " + geoData.lat() +  ", "+ geoData.lon() +
                    ", Дата:  " + (localDateTime != null ? DATE_TIME_FORMATTER.format(localDateTime) : "-");
        }
    }

    public void sumCounts(StatisticMeasurement statMeas) {
        StatisticMeasurement.count1 += statMeas.currentCount1;
        StatisticMeasurement.count2 += statMeas.currentCount2;
        StatisticMeasurement.count3 += statMeas.currentCount3;
        StatisticMeasurement.count4 += statMeas.currentCount4;
        StatisticMeasurement.countSum += statMeas.currentCountSum;
    }

    public void clear() {
        StatisticMeasurement.clear();
    }

    public void addMeasToStatistic(long time, Measurement meas, StatisticMeasurement statMeas) {
        statMeas.currentCountSum = meas.bdData.getCurrentScore();
        statMeas.averageCountSum = meas.bdData.getAverageScore();
        statMeas.currentMeasDataValue = meas.bdData.getCurrentMeasData();
        statMeas.averageMeasDataValue = meas.bdData.getAverageMeasData();
        statMeas.measDataTitle = meas.bdData.getTitle();
        statMeas.measDataUnit = meas.bdData.getMeasDataUnit();
        statMeas.accInterval = meas.bdData.getAccumulatedTime();
        statMeas.geoData = meas.geoData;

        if (statMeas.time == time) {
            sumCounts(statMeas);
            statMeas.localDateTime = LocalDateTime.now();
        } else {
            statMeas.time = time;
        }
    }

    public void addMeasToStatistic(long time, InternalData internalData, StatisticMeasurement statMeas) {
        statMeas.currentCount1 = internalData.currentScores[0];
        statMeas.currentCount2 = internalData.currentScores[1];
        if (internalData.currentScores.length > 2) {
            statMeas.currentCount3 = internalData.currentScores[2];
            statMeas.currentCount4 = internalData.currentScores[3];
        }

        statMeas.averageCount1 = internalData.averageScores[0];
        statMeas.averageCount2 = internalData.averageScores[1];
        if (internalData.averageScores.length > 2) {
            statMeas.averageCount3 = internalData.averageScores[2];
            statMeas.averageCount4 = internalData.averageScores[3];
        }

        if (statMeas.time == time) {
            sumCounts(statMeas);
            statMeas.localDateTime = LocalDateTime.now();
        } else {
            statMeas.time = time;
        }
    }
}
