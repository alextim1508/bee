package com.alextim.bee.client.transfer;

import com.alextim.bee.client.dto.*;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.client.messages.ExceptionMessage;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.alextim.bee.client.messages.DetectorCommands.*;
import static com.alextim.bee.client.messages.DetectorEvents.*;
import static com.alextim.bee.client.protocol.DetectorCodes.Error;
import static com.alextim.bee.client.protocol.DetectorCodes.*;
import static com.alextim.bee.client.protocol.DetectorCodes.Format.DATA;
import static com.alextim.bee.client.protocol.DetectorCodes.RestartReason.RESTART_COMMAND;
import static com.alextim.bee.client.protocol.DetectorCodes.RestartReason.RESTART_ERROR;


@Slf4j
public class DetectorParser {

    public static DetectorMsg parse(DetectorMsg msg) {
        log.debug("========Parse msg========");
        try {
            if (msg.getClass() == SomeEvent.class) {
                return parseEvent((SomeEvent) msg);

            } else if (msg.getClass() == SomeCommandAnswer.class) {
                return parseCommand((SomeCommandAnswer) msg);

            } else {
                return msg;
            }
        } catch (Exception e) {
            log.error("", e);
            return new ExceptionMessage(
                    msg.detectorID,
                    msg.time,
                    new RuntimeException("Ошибка разбора сообщения"),
                    msg.data);
        }
    }

    static DetectorMsg parseEvent(SomeEvent event) {
        log.debug("EventCode.code {}", event.eventCode);

        if (event.eventCode.code == Event.RESTART.code) {
            return getRestartDetectorState(event);

        } else if (event.eventCode.code == Event.STATE.code) {
            byte zero = event.data[DATA.shift]; // зарезервировано, д.б. 0

            State state = State.getStateByCode(event.data[DATA.shift + 1]);
            log.debug("State: {}", state);

            byte attentionFlagCode = event.data[DATA.shift + 2];
            log.debug("attentionFlagCode: {}", attentionFlagCode);
            AttentionFlags attentionFlag = new AttentionFlags(
                    AttentionFlag.getAttentionFlagByCode(attentionFlagCode));
            log.debug("AttentionFlag: {}", attentionFlag);

            switch (state) {
                case UNKNOWN -> {
                    log.debug("UNKNOWN EVENT: {}", getHexString(event));
                    return new UnknownDetectorState(event);
                }
                case INITIALIZATION -> {
                    return new InitializationDetectorState(new DetectorStateEvent(attentionFlag, event));
                }
                case ERROR -> {
                    return getErrorDetectorState(new DetectorStateEvent(attentionFlag, event));
                }
                case ACCUMULATION -> {
                    return getAccumulationDetectorState(new DetectorStateEvent(attentionFlag, event));
                }
                case MEASUREMENT -> {
                    return getMeasurementStateDetector(new DetectorStateEvent(attentionFlag, event));
                }
            }

        } else if (event.eventCode.code == Event.INTERNAL_DATA.code) {
            return getInternalEvent(event);
        }

        throw new RuntimeException("Unknown detectorEventCode: " + event.eventCode.code);
    }

    static DetectorMsg parseCommand(SomeCommandAnswer answer) {
        log.debug("CommandCode: {} {}", answer.commandCode.title, answer.commandStatusCode.title);

        if (answer.commandCode.code == Command.GET_VERSION.code) {
            log.debug("GET_VERSION: {}", getHexString(answer));

            short length = ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 1],
                            answer.data[DATA.shift]})
                    .getShort();
            log.debug("length: {}", length);

            byte[] bytes = new byte[length];
            for (int i = 0; i < length; i++) {
                bytes[i] = answer.data[DATA.shift + 2 + i];
            }
            String version = new String(bytes, StandardCharsets.UTF_8);
            log.debug("version: {}", version);

            return new GetVersionAnswer(version, answer);

        } else if (answer.commandCode.code == Command.SET_MEAS_TIME.code) {
            log.debug("SET_MEAS_TIME: {}", getHexString(answer));
            return new SetMeasTimeAnswer(answer);

        } else if (answer.commandCode.code == Command.SET_SENSITIVITY.code) {
            log.debug("SET_SENSITIVITY: {}", getHexString(answer));
            return new SetSensitivityAnswer(answer);

        } else if (answer.commandCode.code == Command.SET_COUNTER_PMINTERVAL.code) {
            log.debug("SET_COUNTER_PMINTERVAL: {}", getHexString(answer));
            return new SetImpulseRangeCounterCommandAnswer(answer);

        } else if (answer.commandCode.code == Command.SET_DEAD_TIME.code) {
            log.debug("SET_DEAD_TIME: {}", getHexString(answer));
            return new SetDeadTimeAnswer(answer);

        } else if (answer.commandCode.code == Command.SET_DEBUG_SETTINGS.code) {
            log.debug("SET_DEBUG_SETTINGS: {}", getHexString(answer));
            return new SetDebugSettingAnswer(answer);

        } else if (answer.commandCode.code == Command.GET_SENSITIVITY.code) {
            log.debug("GET_SENSITIVITY: {}", getHexString(answer));

            float sensitivity = ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 3],
                            answer.data[DATA.shift + 2],
                            answer.data[DATA.shift + 1],
                            answer.data[DATA.shift]})
                    .getFloat();
            log.debug("Sensitivity: {}", sensitivity);
            return new GetSensitivityAnswer(sensitivity, answer);


        } else if (answer.commandCode.code == Command.GET_DEAD_TIME.code) {
            log.debug("GET_DEAD_TIME: {}", getHexString(answer));

            int counterIndex = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 1],
                            answer.data[DATA.shift]})
                    .getShort());
            log.debug("CounterIndex: {}", counterIndex);

            BDInternalMode mode = BDInternalMode.getBDInternalModeByCode(answer.data[DATA.shift + 2]);
            log.debug("BDInternalMode: {}", mode);

            float deadTime = ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 7],
                            answer.data[DATA.shift + 6],
                            answer.data[DATA.shift + 5],
                            answer.data[DATA.shift + 4]})
                    .getFloat();
            log.debug("deadTime: {}", deadTime);

            return new GetDeadTimeAnswer(counterIndex, mode, deadTime, answer);

        } else if (answer.commandCode.code == Command.GET_DEBUG_SETTINGS.code) { //todo
            log.debug("GET_DEBUG_SETTINGS: {}", getHexString(answer));

            int version = ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 3],
                            answer.data[DATA.shift + 2],
                            answer.data[DATA.shift + 1],
                            answer.data[DATA.shift]})
                    .getInt();
            log.debug("version: {}", version);

            boolean enabled = answer.data[DATA.shift + 4] == 1;
            log.debug("enabled: {}", enabled);

            BDInternalMode mode = BDInternalMode.getBDInternalModeByCode(answer.data[DATA.shift + 5]);
            log.debug("BDInternalMode: {}", mode);

            long chmQuench = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 12],
                            answer.data[DATA.shift + 11],
                            answer.data[DATA.shift + 10],
                            answer.data[DATA.shift + 9]})
                    .getInt());
            log.debug("chmQuench: {}", chmQuench);

            long clmQuench = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 16],
                            answer.data[DATA.shift + 15],
                            answer.data[DATA.shift + 14],
                            answer.data[DATA.shift + 13]})
                    .getInt());
            log.debug("clmQuench: {}", clmQuench);

            long pmInterval = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 20],
                            answer.data[DATA.shift + 19],
                            answer.data[DATA.shift + 18],
                            answer.data[DATA.shift + 17]})
                    .getInt());
            log.debug("pmInterval: {}", pmInterval);

            long pmQuench = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 24],
                            answer.data[DATA.shift + 23],
                            answer.data[DATA.shift + 22],
                            answer.data[DATA.shift + 21]})
                    .getInt());
            log.debug("pmQuench: {}", pmQuench);

            long pmHiUp = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 28],
                            answer.data[DATA.shift + 27],
                            answer.data[DATA.shift + 26],
                            answer.data[DATA.shift + 25]})
                    .getInt());
            log.debug("pmHiUp: {}", pmHiUp);

            DebugSetting debugSetting = DebugSetting.builder()
                    .mode(mode)
                    .chmQuench(chmQuench)
                    .clmQuench(clmQuench)
                    .pmInterval(pmInterval)
                    .pmQuench(pmQuench)
                    .pmHiUp(pmHiUp)
                    .build();

            return new GetDebugSettingAnswer(debugSetting, answer);

        } else if (answer.commandCode.code == Command.SET_CORRECT_COFF.code) {
            log.debug("SET_CORRECT_COFF: {}", getHexString(answer));
            return new SetCounterCorrectCoeffAnswer(answer);

        } else if (answer.commandCode.code == Command.GET_CORRECT_COFF.code) {
            log.debug("GET_CORRECT_COFF: {}", getHexString(answer));

            int counterIndex = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 1],
                            answer.data[DATA.shift]})
                    .getShort());
            log.debug("CounterIndex: {}", counterIndex);

            BDInternalMode mode = BDInternalMode.getBDInternalModeByCode(answer.data[DATA.shift + 2]);
            log.debug("BDInternalMode: {}", mode);

            float counterCorrectCoff = ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 7],
                            answer.data[DATA.shift + 6],
                            answer.data[DATA.shift + 5],
                            answer.data[DATA.shift + 4]})
                    .getFloat();
            log.debug("CounterCorrectCoff: {}", counterCorrectCoff);

            return new GetCounterCorrectCoeffAnswer(counterIndex, mode, counterCorrectCoff, answer);

        } else if (answer.commandCode.code == Command.GET_COUNTER_PMINTERVAL.code) {
            log.debug("GET_COUNTER_PMINTERVAL: {}", getHexString(answer));

            int counterIndex = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 1],
                            answer.data[DATA.shift]})
                    .getShort());
            log.debug("CounterIndex: {}", counterIndex);

            float impulseRangeCounter = ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 7],
                            answer.data[DATA.shift + 6],
                            answer.data[DATA.shift + 5],
                            answer.data[DATA.shift + 4]})
                    .getFloat();
            log.debug("impulseRangeCounter: {}", impulseRangeCounter);

            return new GetImpulseRangeCounterCommandAnswer(counterIndex, impulseRangeCounter, answer);

        } else if (answer.commandCode.code == Command.SET_GEO_DATA.code) {
            log.debug("SET_GEO_DATA: {}", getHexString(answer));
            return new SetGeoDataAnswer(answer);
        }

        throw new RuntimeException("Unknown detectorCommandAnswerCode: " + answer.commandCode.code);
    }

    static RestartDetectorState getRestartDetectorState(SomeEvent event) {
        log.debug("RESTART EVENT: {}", getHexString(event));

        RestartReason reason = RestartReason.getRestartReasonByCode(event.data[DATA.shift]);
        log.debug("Reason: {}", reason);

        RestartParam param = null;
        int[] sourceIpAddr = null;

        if (reason == RESTART_COMMAND) {
            sourceIpAddr = new int[]{
                    Byte.toUnsignedInt(event.data[DATA.shift + 4]),
                    Byte.toUnsignedInt(event.data[DATA.shift + 5]),
                    Byte.toUnsignedInt(event.data[DATA.shift + 6]),
                    Byte.toUnsignedInt(event.data[DATA.shift + 7])
            };
            log.debug("sourceIpAddr: {}", sourceIpAddr);

        } else if (reason == RESTART_ERROR) {
            param = RestartParam.getRestartParamByCode(event.data[DATA.shift + 4]);
            log.debug("Param: {}", param);
        }

        int[] detectorIpAddr = new int[]{
                Byte.toUnsignedInt(event.data[DATA.shift + 8]),
                Byte.toUnsignedInt(event.data[DATA.shift + 9]),
                Byte.toUnsignedInt(event.data[DATA.shift + 10]),
                Byte.toUnsignedInt(event.data[DATA.shift + 11])
        };
        log.debug("detectorIpAddr: {}", detectorIpAddr);

        int ipPort = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 13],
                        event.data[DATA.shift + 12]})
                .getShort());
        log.debug("IpPort: {}", ipPort);

        int ipPortExtDevice = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 15],
                        event.data[DATA.shift + 14]})
                .getShort());
        log.debug("IpPortExtDevice: {}", ipPortExtDevice);

        return new RestartDetectorState(reason, param, detectorIpAddr, sourceIpAddr, ipPort, ipPortExtDevice, event);
    }

    static ErrorDetectorState getErrorDetectorState(DetectorStateEvent event) {
        log.debug("ERROR EVENT: {}", getHexString(event));

        Error error = Error.getErrorByCode(event.data[DATA.shift + 4]);
        log.debug("Error: {}", error);
        return new ErrorDetectorState(error, event);
    }

    static AccumulationDetectorState getAccumulationDetectorState(DetectorStateEvent event) {
        log.debug("ACCUMULATION EVENT: {}", getHexString(event));

        long curTime = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 7],
                        event.data[DATA.shift + 6],
                        event.data[DATA.shift + 5],
                        event.data[DATA.shift + 4]})
                .getInt());
        log.debug("CutTime: {}", curTime);

        long measTime = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 11],
                        event.data[DATA.shift + 10],
                        event.data[DATA.shift + 9],
                        event.data[DATA.shift + 8]})
                .getInt());
        log.debug("MeasTime: {}", measTime);

        return new AccumulationDetectorState(curTime, measTime, event);
    }

    static MeasurementDetectorState getMeasurementStateDetector(DetectorStateEvent event) {
        log.debug("MEASUREMENT EVENT : {}", getHexString(event));

        int structVersion = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 5],
                        event.data[DATA.shift + 4]})
                .getShort());
        log.debug("StructVersion: {}", structVersion);

        BDType bdType = BDType.getBDTypeByCode((byte) ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 7],
                        event.data[DATA.shift + 6]})
                .getShort()
        );
        log.debug("BdType: {}", bdType);

        long measTime = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 11],
                        event.data[DATA.shift + 10],
                        event.data[DATA.shift + 9],
                        event.data[DATA.shift + 8]})
                .getInt());
        log.debug("MeasTime: {}", measTime);

        long geoTime = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 15],
                        event.data[DATA.shift + 14],
                        event.data[DATA.shift + 13],
                        event.data[DATA.shift + 12]})
                .getInt());
        log.debug("GeoTime: {}", geoTime);

        int geoDataSize = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 17],
                        event.data[DATA.shift + 16]})
                .getShort());
        log.debug("geoDataSize: {}", geoDataSize);

        float lat = 0, lon = 0;
        if (geoDataSize == 8) {
            lat = ByteBuffer.wrap(new byte[]{
                            event.data[DATA.shift + 18 + 3],
                            event.data[DATA.shift + 18 + 2],
                            event.data[DATA.shift + 18 + 1],
                            event.data[DATA.shift + 18]})
                    .getFloat();
            log.debug("lat: {}", lat);
            lon = ByteBuffer.wrap(new byte[]{
                            event.data[DATA.shift + 18 + 7],
                            event.data[DATA.shift + 18 + 6],
                            event.data[DATA.shift + 18 + 5],
                            event.data[DATA.shift + 18 + 4]})
                    .getFloat();
            log.debug("lon: {}", lon);
        }

        GeoData geoData = new GeoData(lat, lon);

        float curScore = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 21 + geoDataSize],
                        event.data[DATA.shift + 20 + geoDataSize],
                        event.data[DATA.shift + 19 + geoDataSize],
                        event.data[DATA.shift + 18 + geoDataSize]})
                .getFloat();
        log.debug("CurScore: {}", curScore);

        float aveScore = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 25 + geoDataSize],
                        event.data[DATA.shift + 24 + geoDataSize],
                        event.data[DATA.shift + 23 + geoDataSize],
                        event.data[DATA.shift + 22 + geoDataSize]})
                .getFloat();
        log.debug("AveScore: {}", aveScore);

        float curMeasData = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 29 + geoDataSize],
                        event.data[DATA.shift + 28 + geoDataSize],
                        event.data[DATA.shift + 27 + geoDataSize],
                        event.data[DATA.shift + 26 + geoDataSize]})
                .getFloat();
        log.debug("CurMeasData: {}", curMeasData);

        float aveMeasData = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 33 + geoDataSize],
                        event.data[DATA.shift + 32 + geoDataSize],
                        event.data[DATA.shift + 31 + geoDataSize],
                        event.data[DATA.shift + 30 + geoDataSize]})
                .getFloat();
        log.debug("AveMeasData: {}", aveMeasData);

        long accInterval = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 37 + geoDataSize],
                        event.data[DATA.shift + 36 + geoDataSize],
                        event.data[DATA.shift + 35 + geoDataSize],
                        event.data[DATA.shift + 34 + geoDataSize]})
                .getInt());
        log.debug("AccInterval: {}", accInterval);

        float accMeasDataT = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 41 + geoDataSize],
                        event.data[DATA.shift + 40 + geoDataSize],
                        event.data[DATA.shift + 39 + geoDataSize],
                        event.data[DATA.shift + 38 + geoDataSize]})
                .getFloat();
        log.debug("AccMeasDataT: {}", accMeasDataT);

        float accMeasDataP = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 45 + geoDataSize],
                        event.data[DATA.shift + 44 + geoDataSize],
                        event.data[DATA.shift + 43 + geoDataSize],
                        event.data[DATA.shift + 42 + geoDataSize]})
                .getFloat();
        log.debug("AccMeasDataP: {}", accMeasDataP);

        BdData bdData;
        if (bdType == BDType.GAMMA) {
            bdData = BdmgData.builder()
                    .currentMED(curMeasData)
                    .averageMED(aveMeasData)
                    .accumulatedMED(accMeasDataT)
                    .accumulatedPowerMEDP(accMeasDataP)
                    .currentScore(curScore)
                    .averageScore(aveScore)
                    .accumulatedTime(accInterval)
                    .build();

        } else {
            bdData = BdpnData.builder()
                    .currentDensity(curMeasData)
                    .averageDensity(aveMeasData)
                    .accumulatedScore(accMeasDataT)
                    .accumulatedPowerScore(accMeasDataP)
                    .currentScore(curScore)
                    .averageScore(aveScore)
                    .accumulatedTime(accInterval)
                    .build();
        }

        Measurement measurement = Measurement.builder()
                .version(structVersion)
                .bdType(bdType)
                .measTime(measTime)
                .geoTime(geoTime)
                .geoData(geoData)
                .bdData(bdData)
                .build();

        return new MeasurementDetectorState(measurement, event);
    }

    static InternalEvent getInternalEvent(SomeEvent event) {
        log.debug("INTERNAL_DATA EVENT : {}", getHexString(event));

        int structVersion = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 1],
                        event.data[DATA.shift]})
                .getShort());
        log.debug("structVersion: {}", structVersion);

        BDType bdType = BDType.getBDTypeByCode((byte) ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 3],
                        event.data[DATA.shift + 2]})
                .getShort()
        );
        log.debug("BdType: {}", bdType);

        long measTime = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 7],
                        event.data[DATA.shift + 6],
                        event.data[DATA.shift + 5],
                        event.data[DATA.shift + 4]})
                .getInt());
        log.debug("MeasTime: {}", measTime);

        BDInternalMode bdInternalMode = BDInternalMode.getBDInternalModeByCode(
                (byte) ByteBuffer.wrap(new byte[]{
                                event.data[DATA.shift + 9],
                                event.data[DATA.shift + 8]})
                        .getShort()
        );
        log.debug("BdInternalMode: {}", bdInternalMode);

        int reserve = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 11],
                        event.data[DATA.shift + 10]})
                .getShort());
        log.debug("Reserve: {}", reserve);

        float curScores1 = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 15],
                        event.data[DATA.shift + 14],
                        event.data[DATA.shift + 13],
                        event.data[DATA.shift + 12]})
                .getFloat();
        log.debug("CurScores1: {}", curScores1);

        float curScores2 = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 19],
                        event.data[DATA.shift + 18],
                        event.data[DATA.shift + 17],
                        event.data[DATA.shift + 16]})
                .getFloat();
        log.debug("CurScores2: {}", curScores2);

        float aveScores1 = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 23],
                        event.data[DATA.shift + 22],
                        event.data[DATA.shift + 21],
                        event.data[DATA.shift + 20]})
                .getFloat();
        log.debug("AveScores1: {}", aveScores1);

        float aveScores2 = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 27],
                        event.data[DATA.shift + 26],
                        event.data[DATA.shift + 25],
                        event.data[DATA.shift + 24]})
                .getFloat();
        log.debug("AveScores2: {}", aveScores2);

        float temperature = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 31],
                        event.data[DATA.shift + 30],
                        event.data[DATA.shift + 29],
                        event.data[DATA.shift + 28]})
                .getFloat();
        log.debug("Temperature: {}", temperature);

        if (bdType == BDType.GAMMA) {
            float voltage400V = ByteBuffer.wrap(new byte[]{
                            event.data[DATA.shift + 35],
                            event.data[DATA.shift + 34],
                            event.data[DATA.shift + 33],
                            event.data[DATA.shift + 32]})
                    .getFloat();
            log.debug("Voltage400V: {}", voltage400V);

            float power = ByteBuffer.wrap(new byte[]{
                            event.data[DATA.shift + 39],
                            event.data[DATA.shift + 38],
                            event.data[DATA.shift + 37],
                            event.data[DATA.shift + 36]})
                    .getFloat();
            log.debug("power: {}", power);

            BdmgInternalData bdmgInternalData = BdmgInternalData.builder()
                    .voltage400V(voltage400V)
                    .version(structVersion)
                    .bdType(bdType)
                    .measTime(measTime)
                    .mode(bdInternalMode)
                    .reserve(reserve)
                    .currentScores(new float[]{curScores1, curScores2})
                    .averageScores(new float[]{aveScores1, aveScores2})
                    .temperature(temperature)
                    .power(power)
                    .build();
            return new InternalEvent(bdmgInternalData, event);
        } else {
            float voltage500V = ByteBuffer.wrap(new byte[]{
                            event.data[DATA.shift + 35],
                            event.data[DATA.shift + 34],
                            event.data[DATA.shift + 33],
                            event.data[DATA.shift + 32]})
                    .getFloat();
            log.debug("Voltage500V: {}", voltage500V);

            float voltage2500V = ByteBuffer.wrap(new byte[]{
                            event.data[DATA.shift + 39],
                            event.data[DATA.shift + 38],
                            event.data[DATA.shift + 37],
                            event.data[DATA.shift + 36]})
                    .getFloat();
            log.debug("Voltage2500V: {}", voltage2500V);

            float power = ByteBuffer.wrap(new byte[]{
                            event.data[DATA.shift + 43],
                            event.data[DATA.shift + 42],
                            event.data[DATA.shift + 41],
                            event.data[DATA.shift + 40]})
                    .getFloat();
            log.debug("power: {}", power);

            BdpnInternalData bdpnInternalData = BdpnInternalData.builder()
                    .voltage500V(voltage500V)
                    .voltage2500V(voltage2500V)
                    .version(structVersion)
                    .bdType(bdType)
                    .measTime(measTime)
                    .mode(bdInternalMode)
                    .reserve(reserve)
                    .currentScores(new float[]{curScores1, curScores2})
                    .averageScores(new float[]{aveScores1, aveScores2})
                    .temperature(temperature)
                    .power(power)
                    .build();
            return new InternalEvent(bdpnInternalData, event);
        }
    }

    static String getHexString(DetectorMsg msg) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < msg.data.length; i++)
            s.append(String.format("%x ", msg.data[i]));
        return s.toString();
    }
}
