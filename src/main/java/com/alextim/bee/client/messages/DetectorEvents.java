package com.alextim.bee.client.messages;

import com.alextim.bee.client.dto.InternalData;
import com.alextim.bee.client.dto.Measurement;
import lombok.EqualsAndHashCode;

import java.util.Arrays;

import static com.alextim.bee.client.protocol.DetectorCodes.Error;
import static com.alextim.bee.client.protocol.DetectorCodes.*;
import static com.alextim.bee.client.protocol.DetectorCodes.State.*;

public class DetectorEvents {

    @EqualsAndHashCode(callSuper = true)
    public static class RestartDetectorState extends SomeEvent {

        public final RestartReason reason;
        public final RestartParam param;
        public final int[] detectorIpAddr;
        public final int[] sourceIpAddr;
        public final int ipPort;
        public final int externalDeviceIpPort;

        public RestartDetectorState(RestartReason reason,
                                    RestartParam param,
                                    int[] detectorIpAddr,
                                    int[] sourceIpAddr,
                                    int ipPort,
                                    int externalDeviceIpPort,
                                    SomeEvent someEvent) {
            super(someEvent.detectorID, someEvent.time, someEvent.eventCode, someEvent.data);
            this.reason = reason;
            this.param = param;
            this.detectorIpAddr = detectorIpAddr;
            this.sourceIpAddr = sourceIpAddr;
            this.ipPort = ipPort;
            this.externalDeviceIpPort = externalDeviceIpPort;
        }

        @Override
        public String toString() {
            return String.format("Причина: %s%s, IP адрес БД: %s, IP порт БД: %s, IP порт внешних устройств: %s%s",
                    reason.title,
                    param != null ? "/" + param.name() : "",
                    Arrays.toString(detectorIpAddr),
                    ipPort,
                    externalDeviceIpPort,
                    sourceIpAddr != null ? ", IP адрес источника команды перезапуска: " + Arrays.toString(sourceIpAddr) : "");
        }
    }

    @EqualsAndHashCode(callSuper = true)
    public static class UnknownDetectorState extends SomeEvent {
        public UnknownDetectorState(SomeEvent someEvent) {
            super(someEvent.detectorID, someEvent.time, someEvent.eventCode, someEvent.data);
        }

        @Override
        public String toString() {
            return String.format("%s", UNKNOWN.title);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    public static class InitializationDetectorState extends DetectorStateEvent {
        public InitializationDetectorState(DetectorStateEvent detectorStateEvent) {
            super(detectorStateEvent.attentionFlags, detectorStateEvent);
        }

        @Override
        public String toString() {
            return String.format("%s, %s", INITIALIZATION.title, attentionFlags);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    public static class ErrorDetectorState extends DetectorStateEvent {

        public final Error error;

        public ErrorDetectorState(Error error, DetectorStateEvent detectorStateEvent) {
            super(detectorStateEvent.attentionFlags, detectorStateEvent);
            this.error = error;
        }

        @Override
        public String toString() {
            return String.format("%s. Ошибка: %s, %s", ERROR.title, error.title, attentionFlags);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    public static class AccumulationDetectorState extends DetectorStateEvent {

        public final long curTime;
        public final long measTime;

        public AccumulationDetectorState(long curTime, long measTime, DetectorStateEvent detectorStateEvent) {
            super(detectorStateEvent.attentionFlags, detectorStateEvent);
            this.curTime = curTime;
            this.measTime = measTime;
        }

        @Override
        public String toString() {
            return String.format("%s. Текущее время: %d, Время измерения: %d, %s", ACCUMULATION.title, curTime, measTime, attentionFlags);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    public static class MeasurementDetectorState extends DetectorStateEvent {

        public final Measurement meas;

        public MeasurementDetectorState(Measurement meas, DetectorStateEvent detectorStateEvent) {
            super(detectorStateEvent.attentionFlags, detectorStateEvent);
            this.meas = meas;
        }

        @Override
        public String toString() {
            return String.format("%s. %s, %s", MEASUREMENT.title, meas.toString(), attentionFlags);
        }
    }

    @EqualsAndHashCode(callSuper = true)
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

    @EqualsAndHashCode(callSuper = true)
    public static class DetectorStateEvent extends SomeEvent {

        public final AttentionFlags attentionFlags;

        public DetectorStateEvent(AttentionFlags attentionFlags, SomeEvent someEvent) {
            super(someEvent.detectorID, someEvent.time, someEvent.eventCode, someEvent.data);
            this.attentionFlags = attentionFlags;
        }
    }

    @EqualsAndHashCode(callSuper = true)
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
