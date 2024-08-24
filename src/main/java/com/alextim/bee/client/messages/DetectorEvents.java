package com.alextim.bee.client.messages;

import com.alextim.bee.client.dto.InternalData;
import com.alextim.bee.client.dto.Measurement;

import java.util.Arrays;

import static com.alextim.bee.client.protocol.DetectorCodes.Error;
import static com.alextim.bee.client.protocol.DetectorCodes.*;
import static com.alextim.bee.client.protocol.DetectorCodes.State.*;

public class DetectorEvents {

    public static class RestartDetector extends SomeEvent {

        public final RestartReason reason;
        public final RestartParam param;
        public final int[] ipAddr;
        public final int ipPort;
        public final int externalDeviceIpPort;

        public RestartDetector(RestartReason reason,
                               RestartParam param,
                               int[] ipAddr,
                               int ipPort,
                               int externalDeviceIpPort,
                               SomeEvent someEvent) {
            super(someEvent.detectorID, someEvent.time, someEvent.eventCode, someEvent.data);
            this.reason = reason;
            this.param = param;
            this.ipAddr = ipAddr;
            this.ipPort = ipPort;
            this.externalDeviceIpPort = externalDeviceIpPort;
        }

        @Override
        public String toString() {
            return String.format("Причина: %s/%s, IP адрес БД: %s, IP порт БД: %s, IP порт внешних устройств: %d",
                    reason.title, param.name(), Arrays.toString(ipAddr), ipPort, externalDeviceIpPort);
        }
    }

    public static class UnknownDetectorState extends SomeEvent {
        public UnknownDetectorState(SomeEvent someEvent) {
            super(someEvent.detectorID, someEvent.time, someEvent.eventCode, someEvent.data);
        }

        @Override
        public String toString() {
            return String.format("%s", UNKNOWN.title);
        }
    }

    public static class InitializationDetectorState extends SomeEvent {
        public InitializationDetectorState(SomeEvent someEvent) {
            super(someEvent.detectorID, someEvent.time, someEvent.eventCode, someEvent.data);
        }

        @Override
        public String toString() {
            return String.format("%s", INITIALIZATION.title);
        }
    }

    public static class ErrorDetectorState extends SomeEvent {

        public final Error error;

        public ErrorDetectorState(Error error, SomeEvent someEvent) {
            super(someEvent.detectorID, someEvent.time, someEvent.eventCode, someEvent.data);
            this.error = error;
        }

        @Override
        public String toString() {
            return String.format("%s. Ошибка: %s", ERROR.title, error.title);
        }
    }

    public static class AccumulationDetectorState extends SomeEvent {

        public final long curTime;
        public final long measTime;

        public AccumulationDetectorState(long curTime, long measTime, SomeEvent someEvent) {
            super(someEvent.detectorID, someEvent.time, someEvent.eventCode, someEvent.data);
            this.curTime = curTime;
            this.measTime = measTime;
        }

        @Override
        public String toString() {
            return String.format("%s. Текущее время: %d, Время измерения: %d", ACCUMULATION.title, curTime, measTime);
        }
    }

    public static class MeasurementDetectorState extends SomeEvent {

        public final Measurement meas;

        public MeasurementDetectorState(Measurement meas, SomeEvent someEvent) {
            super(someEvent.detectorID, someEvent.time, someEvent.eventCode, someEvent.data);
            this.meas = meas;
        }

        @Override
        public String toString() {
            return String.format("%s. %s", MEASUREMENT.title, meas.toString());
        }
    }

    public static class InternalEvent extends SomeEvent {

        public final InternalData internalData;

        public InternalEvent(InternalData internalData, SomeEvent someEvent) {
            super(someEvent.detectorID, someEvent.time, someEvent.eventCode, someEvent.data);
            this.internalData = internalData;
        }

        @Override
        public String toString() {
            return internalData.toString();
        }
    }

    public static class SomeEvent extends DetectorMsg {

        public final Event eventCode;

        public SomeEvent(int detectorID,
                         long time,
                         Event eventCode,
                         byte[] data) {
            super(detectorID, time, data);
            this.eventCode = eventCode;
        }
    }
}