package com.alextim.bee.client.messages;

import com.alextim.bee.client.dto.InternalData;
import com.alextim.bee.client.dto.Measurement;
import lombok.EqualsAndHashCode;

import java.util.Arrays;

import static com.alextim.bee.client.protocol.DetectorCodes.Error;
import static com.alextim.bee.client.protocol.DetectorCodes.*;
import static com.alextim.bee.client.protocol.DetectorCodes.RestartReason.*;
import static com.alextim.bee.client.protocol.DetectorCodes.State.*;

public class DetectorEvents {

    @EqualsAndHashCode(callSuper = true)
    public static class RestartDetectorState extends SomeEvent {

        public final RestartReason reason;
        public final RestartParam param;
        public final int[] detectorIpAddr;
        public final int[] sourceIpAddr;
        public final Integer ipPort;
        public final Integer externalDeviceIpPort;

        public RestartDetectorState(RestartReason reason,
                                    RestartParam param,
                                    int[] detectorIpAddr,
                                    int[] sourceIpAddr,
                                    Integer ipPort,
                                    Integer externalDeviceIpPort,
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
            if (reason == RESTART_COMMAND) {
                return String.format("Причина: %s/%s, IP адрес источника команды перезапуска: %s, IP адрес БД: %s, " +
                                "IP порт БД: %s, IP порт внешних устройств: %s",
                        reason.title,
                        param != null ? param.name() : "-",
                        sourceIpAddr != null ? Arrays.toString(sourceIpAddr) : "-",
                        detectorIpAddr != null ? Arrays.toString(detectorIpAddr) : "-",
                        ipPort != null ? Integer.toString(ipPort) : "-",
                        externalDeviceIpPort != null ? Integer.toString(externalDeviceIpPort) : "-");

            } else if (reason == RESTART_ERROR) {
                return String.format("Причина: %s/%s", reason.title, param.name());

            } else {
                return String.format("Причина: %s/Перезапуск после подачи питания", reason.title);
            }
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
    public static class InitializationDetectorState extends SomeEvent {
        public InitializationDetectorState(SomeEvent someEvent) {
            super(someEvent.detectorID, someEvent.time, someEvent.eventCode, someEvent.data);
        }

        @Override
        public String toString() {
            return String.format("%s", INITIALIZATION.title);
        }
    }

    @EqualsAndHashCode(callSuper = true)
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

    @EqualsAndHashCode(callSuper = true)
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

    @EqualsAndHashCode(callSuper = true)
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
