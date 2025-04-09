package com.alextim.bee.service;

import com.alextim.bee.client.messages.DetectorEvents.MeasurementDetectorState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AccumulationMeasService {

    int count;
    float aveMeasData;
    int measAmount;

    private final AtomicBoolean run = new AtomicBoolean(false);

    @AllArgsConstructor
    public static class AccumulatedMeasurement {
        public float aveMeasData;
        public String unit;
        public float progress;
    }

    public void run(int measAmount) {
        run.set(true);
        this.count = 0;
        this.measAmount = measAmount;
    }

    public boolean isRun() {
        return run.get();
    }

    public AccumulatedMeasurement addMeasToAccumulation(MeasurementDetectorState msg) {
        log.info("addMeasToAccumulation count: {}", count);

        initAverage(msg.meas.bdData.getCurrentMeasData(), count + 1);

        AccumulatedMeasurement meas = new AccumulatedMeasurement(
                aveMeasData,
                msg.meas.bdData.getMeasDataUnit(),
                1.0f * (count + 1) / measAmount
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
