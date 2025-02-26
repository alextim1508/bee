package com.alextim.bee.service;

import com.alextim.bee.client.messages.DetectorEvents.MeasurementDetectorState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MetrologyMeasService {

    private int count;
    private int cycleAmount;
    private int measAmount;
    private float realMeasData;
    private float aveMeasData;

    private final AtomicBoolean run = new AtomicBoolean(false);

    private final List<Float> aveMeasDataList = new ArrayList<>();

    @AllArgsConstructor
    public static class MetrologyMeasurement {
        public int cycle;
        public float measData;
        public String unit;
        public float progress;
        public float error;
    }

    public void run(int cycleAmount, int measAmount, float realMeasData) {
        run.set(true);
        this.count = 0;
        this.cycleAmount = cycleAmount;
        this.measAmount = measAmount;
        this.realMeasData = realMeasData;
        aveMeasDataList.clear();
    }

    public boolean isRun() {
        return run.get();
    }

    public MetrologyMeasurement addMeasToMetrology(MeasurementDetectorState msg) {
        log.info("addMeasToMetrology count: {}", count);

        initAverage(msg.meas.bdData.getCurrentMeasData(), (count % measAmount) + 1);

        float error = calcError();

        if(count % measAmount == 0) {
            aveMeasDataList.add(aveMeasData);
        }

        if( (count + 1) / measAmount == cycleAmount) {
            run.set(false);
        }

        MetrologyMeasurement meas = new MetrologyMeasurement(
                count / measAmount + 1,
                aveMeasData,
                msg.meas.bdData.getMeasDataUnit(),
                1.0f * (count + 1)  / (measAmount * cycleAmount),
                error
        );

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

    public float calcError() {
        float aveMeasData = 0;
        for(float v: aveMeasDataList)
            aveMeasData += v;

        aveMeasData /= aveMeasDataList.size();

        log.info("calcError: Average meas data: {}", aveMeasData);
        log.info("realMeasData: {}", realMeasData);

        float error = 100 * Math.abs(realMeasData - aveMeasData) / realMeasData;
        log.info("error: {}", error);

        return error;
    }
}
