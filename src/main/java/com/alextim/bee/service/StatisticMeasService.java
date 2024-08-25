package com.alextim.bee.service;

import com.alextim.bee.client.dto.BdmgData;
import com.alextim.bee.client.dto.BdpnData;
import com.alextim.bee.client.dto.InternalData;
import com.alextim.bee.client.dto.Measurement;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;


import static com.alextim.bee.context.Context.DATE_TIME_FORMATTER;

@Slf4j
public class StatisticMeasService {

    @Getter
    public static class StatisticMeasurement {

        private static int count1, count2, countSum;

        private float averageCount1, averageCount2, averageCountSum;

        private int currentCount1, currentCount2, currentCountSum;

        private float measDataValue;

        private String measDataTitle, measDataUnit;

        private long measTime;

        private LocalDateTime localDateTime;

        public static void clear() {
            countSum = count1 = count2 = 0;
        }

        public int getCount1() {
            return count1;
        }

        public int getCount2() {
            return count2;
        }

        public int getCountSum() {
            return countSum;
        }

        @Override
        public String toString() {
                  return "Счетчик 1 =" + currentCount1 +
                    ", Счетчик 2 =" + currentCount2 +
                    ", Суммарное значение счетчиков =" + currentCountSum +
                    ", Среднее значение счетчика 1 =" + averageCount1 +
                    ", Среднее значение счетчика 2 =" + averageCount2 +
                    ", Среднее суммарное значение счетчиков =" + averageCountSum +
                    ", Время измерения = " + measTime +
                    ", Дата = " + DATE_TIME_FORMATTER.format(localDateTime);
        }
    }

    public void initSumCounts(StatisticMeasurement statMeas) {
        StatisticMeasurement.count1 += statMeas.currentCount1;
        StatisticMeasurement.count2 += statMeas.currentCount2;
        StatisticMeasurement.countSum += statMeas.currentCountSum;
    }

    public void clearSumCounts() {
        StatisticMeasurement.clear();
    }

    public void addMeasToStatistic(Measurement meas, StatisticMeasurement statMeas) {
        statMeas.currentCountSum = (int) meas.bdData.currentScore;

        statMeas.averageCountSum = meas.bdData.averageScore;

        if (meas.bdData instanceof BdmgData bdmgData) {
            statMeas.measDataValue = bdmgData.getCurrentMED();
            statMeas.measDataTitle = BdmgData.title;
            statMeas.measDataUnit = BdmgData.unit;

        } else if (meas.bdData instanceof BdpnData bdpnData) {
            statMeas.measDataValue = bdpnData.getCurDensity();
            statMeas.measDataTitle = BdpnData.title;
            statMeas.measDataUnit = BdpnData.unit;
        }

        if (statMeas.measTime == meas.measTime) {
            statMeas.localDateTime = LocalDateTime.now();
        } else {
            statMeas.measTime = meas.measTime;
        }
    }

    public void addMeasToStatistic(InternalData internalData, StatisticMeasurement statMeas) {
        statMeas.currentCount1 = (int) internalData.currentScores[0];
        statMeas.currentCount2 = (int) internalData.currentScores[1];

        statMeas.averageCount1 = internalData.averageScores[0];
        statMeas.averageCount2 = internalData.averageScores[1];

        if (statMeas.measTime == internalData.measTime) {
            statMeas.localDateTime = LocalDateTime.now();
        } else {
            statMeas.measTime = internalData.measTime;
        }
    }
}
