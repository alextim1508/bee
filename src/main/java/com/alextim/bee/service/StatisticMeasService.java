package com.alextim.bee.service;

import com.alextim.bee.client.dto.BdmgData;
import com.alextim.bee.client.dto.BdpnData;
import com.alextim.bee.client.dto.InternalData;
import com.alextim.bee.client.dto.Measurement;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public class StatisticMeasService {

    @ToString
    public static class StatisticMeasurement {
        public static int count1, count2, countSum;

        public float averageCount1, averageCount2, averageCountSum;

        public int currentCount1, currentCount2, currentCountSum;

        public float measDataValue;

        public double coef;

        public String measDataTitle, measDataUnit;

        public long measTime;
        public long timestamp;

        public boolean isFull;

        public static void clear() {
            countSum = count1 = count2 = 0;
        }
    }


    public void addMeasToStatistic(Measurement meas, StatisticMeasurement statisticMeas) {
        StatisticMeasurement.countSum += meas.bdData.currentScore;

        statisticMeas.currentCountSum = (int) meas.bdData.currentScore;

        statisticMeas.averageCountSum = meas.bdData.averageScore;

        if (meas.bdData instanceof BdmgData bdmgData) {
            statisticMeas.measDataValue = bdmgData.currentMED;
            statisticMeas.measDataTitle = BdmgData.title;
            statisticMeas.measDataUnit = BdmgData.unit;
            statisticMeas.coef = BdmgData.coef;

        } else if (meas.bdData instanceof BdpnData bdpnData) {
            statisticMeas.measDataValue = bdpnData.curDensity;
            statisticMeas.measDataTitle = BdpnData.title;
            statisticMeas.measDataUnit = BdpnData.unit;
            statisticMeas.coef = BdpnData.coef;
        }

        if (statisticMeas.measTime == meas.measTime) {
            statisticMeas.timestamp = System.currentTimeMillis();
            statisticMeas.isFull = true;
        } else {
            statisticMeas.measTime = meas.measTime;
        }
    }

    public void addMeasToStatistic(InternalData internalData, StatisticMeasurement statisticMeas) {
        StatisticMeasurement.count1 += internalData.currentScores[0];
        StatisticMeasurement.count2 += internalData.currentScores[1];

        statisticMeas.currentCount1 = (int) internalData.currentScores[0];
        statisticMeas.currentCount2 = (int) internalData.currentScores[1];

        statisticMeas.averageCount1 = internalData.averageScores[0];
        statisticMeas.averageCount2 = internalData.averageScores[1];

        if (statisticMeas.measTime == internalData.measTime) {
            statisticMeas.timestamp = System.currentTimeMillis();
            statisticMeas.isFull = true;
        } else {
            statisticMeas.measTime = internalData.measTime;
        }
    }
}
