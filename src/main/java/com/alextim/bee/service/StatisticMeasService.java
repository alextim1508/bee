package com.alextim.bee.service;

import com.alextim.bee.client.dto.GeoData;
import com.alextim.bee.client.dto.InternalData;
import com.alextim.bee.client.dto.Measurement;
import com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.alextim.bee.context.Property.DATE_TIME_FORMATTER;

@Slf4j
public class StatisticMeasService {

    public static class StatisticMeasurement {

        public static float count1, count2,  countSum;

        public float averageCount1, averageCount2, averageCountSum;

        public float currentCount1, currentCount2, currentCountSum;

        public float currentMeasDataValue, averageMeasDataValue, accumulatedMeasDataValue, accumulatedPowerMeasDataValue;

        public String measDataTitle, measDataUnit;

        public GeoData geoData;

        public long accInterval;

        public long time = -1;

        public BDInternalMode mode;

        public float power;

        public LocalDateTime localDateTime;

        public static void clear() {
            countSum = count1 = count2 = 0;
        }

        public float getCount1() {
            return count1;
        }

        public float getCount2() {
            return count2;
        }

        public float getCountSum() {
            return countSum;
        }

        @Override
        public String toString() {
            return measDataTitle + ": " + currentMeasDataValue + " / " + averageMeasDataValue + " " + measDataUnit + ", " +
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
                    ", Режим работы счетчиков: " + mode.title +
                    ", Дата:  " + (localDateTime != null ? DATE_TIME_FORMATTER.format(localDateTime) : "-");
        }
    }

    public void sumCounts(StatisticMeasurement statMeas) {
        StatisticMeasurement.count1 += statMeas.currentCount1;
        StatisticMeasurement.count2 += statMeas.currentCount2;
        StatisticMeasurement.countSum += statMeas.currentCountSum;
    }

    public void clear() {
        StatisticMeasurement.clear();
    }

    public void addMeasToStatistic(long time, Measurement meas, float coef, StatisticMeasurement statMeas) {
        statMeas.currentCountSum = meas.bdData.getCurrentScore();
        statMeas.averageCountSum = meas.bdData.getAverageScore();
        statMeas.currentMeasDataValue = meas.bdData.getCurrentMeasData() * coef;
        statMeas.averageMeasDataValue = meas.bdData.getAverageMeasData() * coef;
        statMeas.accumulatedMeasDataValue = meas.bdData.getAccumulatedMeasData() * coef;
        statMeas.accumulatedPowerMeasDataValue = meas.bdData.getAccumulatedPowerMeasData() * coef;
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

        statMeas.averageCount1 = internalData.averageScores[0];
        statMeas.averageCount2 = internalData.averageScores[1];

        statMeas.mode = internalData.mode;

        statMeas.power = internalData.power;

        if (statMeas.time == time) {
            sumCounts(statMeas);
            statMeas.localDateTime = LocalDateTime.now();
        } else {
            statMeas.time = time;
        }
    }
}
