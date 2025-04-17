package com.alextim.bee.service;

import com.alextim.bee.service.StatisticMeasService.StatisticMeasurement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AccumulationMeasService {

    int count;
    float aveMeasData;
    int measAmount;

    private final AtomicBoolean run = new AtomicBoolean(false);

    @AllArgsConstructor
    public static class AccumulatedMeasurement {
        public String measDataTitle;
        public float aveMeasData;
        public String measDataUnit;
        public float progress;
        public float curPower;
        public LocalDateTime localDateTime;
        public int count;
    }

    public void run(int measAmount) {
        run.set(true);
        this.count = 0;
        this.measAmount = measAmount;
    }

    public boolean isRun() {
        return run.get();
    }

    public AccumulatedMeasurement addMeasToAccumulation(StatisticMeasurement msg) {
        log.info("addMeasToAccumulation count: {}", count);

        initAverage(msg.currentMeasDataValue, count + 1);

        AccumulatedMeasurement meas = new AccumulatedMeasurement(
                msg.measDataTitle,
                aveMeasData,
                msg.measDataUnit,
                1.0f * (count + 1) / measAmount,
                msg.power,
                msg.localDateTime,
                count
        );

        if (count + 1 == measAmount) {
            run.set(false);
        }

        count++;

        return meas;
    }

    public void initAverage(float cur, int count) {
        log.info("initAverage: {} / {}", cur, count);

        float coff = 1 - 1.0f / count;
        log.info("coef: {}", coff);

        aveMeasData = coff * aveMeasData + (1 - coff) * cur;
        log.info("aveMeasData: {}", aveMeasData);
    }
}
