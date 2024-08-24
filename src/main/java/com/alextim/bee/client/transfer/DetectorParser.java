package com.alextim.bee.client.transfer;

import com.alextim.bee.client.dto.*;
import com.alextim.bee.client.messages.DetectorMsg;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

import static com.alextim.bee.client.messages.DetectorCommands.*;
import static com.alextim.bee.client.messages.DetectorEvents.*;
import static com.alextim.bee.client.protocol.DetectorCodes.Error;
import static com.alextim.bee.client.protocol.DetectorCodes.*;
import static com.alextim.bee.client.protocol.DetectorCodes.Format.DATA;


@Slf4j
public class DetectorParser {

    public static DetectorMsg parse(DetectorMsg msg) {
        log.info("========Parse msg========");

        if (msg.getClass() == SomeEvent.class) {
            return parseEvent((SomeEvent) msg);
        } else if (msg.getClass() == SomeCommandAnswer.class) {
            return parseCommand((SomeCommandAnswer) msg);
        }
        throw new RuntimeException("Unknown DetectorMsg");
    }

    private static DetectorMsg parseEvent(SomeEvent event) {
        log.info("EventCode.code {}", event.eventCode);

        if (event.eventCode.code == Event.RESTART.code) {
            return getRestartDetector(event);

        } else if (event.eventCode.code == Event.STATE.code) {
            log.info("STATE EVENT: {}", getHexString(event));

            State state = State.getStateByCode(event.data[DATA.shift]);
            log.info("State: {}", state);

            switch (state) {
                case UNKNOWN -> {
                    return new UnknownDetectorState(event);
                }
                case INITIALIZATION -> {
                    log.info("INITIALIZATION EVENT: {}", getHexString(event));
                    return new InitializationDetectorState(event);
                }
                case ERROR -> {
                    return getErrorDetectorState(event);
                }
                case ACCUMULATION -> {
                    return getAccumulationDetectorState(event);
                }
                case MEASUREMENT -> {
                    return getMeasurementStateDetector(event);
                }
            }

        } else if (event.eventCode.code == Event.INTERNAL_DATA.code) {
            return getInternalEvent(event);
        }

        throw new RuntimeException("Unknown detectorEventCode: " + event.eventCode.code);
    }

    private static DetectorMsg parseCommand(SomeCommandAnswer answer) {
        log.info("CommandCode: {} {}", answer.commandCode.title, answer.commandStatusCode.title);

        if (answer.commandCode.code == Command.GET_VERSION.code) {
            log.info("GET_VERSION: {}", answer.data);

            short length = ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 1],
                            answer.data[DATA.shift]})
                    .getShort();
            log.info("length: {}", length);

            byte[] version = new byte[length];
            for (int i = 0; i < length; i++) {
                version[i] = answer.data[DATA.shift + 2 + i];
            }

            return new GetVersionAnswer(version, answer);

        } else if (answer.commandCode.code == Command.SET_MEAS_TIME.code) {
            log.info("SET_MEAS_TIME: {}", answer.data);
            return new SetMeasTimeAnswer(answer);

        } else if (answer.commandCode.code == Command.SET_SENSITIVITY.code) {
            log.info("SET_SENSITIVITY: {}", answer.data);
            return new SetSensitivityAnswer(answer);

        } else if (answer.commandCode.code == Command.GET_SENSITIVITY.code) {
            log.info("GET_SENSITIVITY: {}", answer.data);

            float sensitivity = ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 3],
                            answer.data[DATA.shift + 2],
                            answer.data[DATA.shift + 1],
                            answer.data[DATA.shift]})
                    .getFloat();
            log.info("Sensitivity: {}", sensitivity);
            return new GetSensitivityAnswer(sensitivity, answer);

        } else if (answer.commandCode.code == Command.SET_DEAD_TIME.code) {
            log.info("SET_DEAD_TIME: {}", answer.data);
            return new SetDeadTimeAnswer(answer);

        } else if (answer.commandCode.code == Command.GET_DEAD_TIME.code) {
            log.info("GET_DEAD_TIME: {}", answer.data);

            float deadTime = ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 3],
                            answer.data[DATA.shift + 2],
                            answer.data[DATA.shift + 1],
                            answer.data[DATA.shift]})
                    .getFloat();
            log.info("DeadTime: {}", deadTime);

            return new GetDeadTimeAnswer(deadTime, answer);

        } else if (answer.commandCode.code == Command.SET_CORRECT_COFF.code) {
            log.info("SET_CORRECT_COFF: {}", answer.data);
            return new SetCounterCorrectCoeffAnswer(answer);

        } else if (answer.commandCode.code == Command.GET_CORRECT_COFF.code) {
            log.info("GET_CORRECT_COFF: {}", answer.data);

            long counterIndex = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 3],
                            answer.data[DATA.shift + 2],
                            answer.data[DATA.shift + 1],
                            answer.data[DATA.shift]})
                    .getInt());
            log.info("CounterIndex: {}", counterIndex);

            float counterCorrectCoff = ByteBuffer.wrap(new byte[]{
                            answer.data[DATA.shift + 7],
                            answer.data[DATA.shift + 6],
                            answer.data[DATA.shift + 5],
                            answer.data[DATA.shift + 4]})
                    .getFloat();
            log.info("CounterCorrectCoff: {}", counterCorrectCoff);

            return new GetCounterCorrectCoeffAnswer(counterIndex, counterCorrectCoff, answer);

        } else if (answer.commandCode.code == Command.SET_GEO_DATA.code) {
            log.info("SET_GEO_DATA: {}", answer.data);
            return new SetGeoDataAnswer(answer);
        }

        throw new RuntimeException("Unknown detectorCommandAnswerCode: " + answer.commandCode.code);
    }

    private static ErrorDetectorState getErrorDetectorState(SomeEvent event) {
        log.info("ERROR EVENT: {}", getHexString(event));

        Error error = Error.getErrorByCode(event.data[DATA.shift + 1]);
        log.info("Error: {}", error);
        return new ErrorDetectorState(error, event);
    }

    private static AccumulationDetectorState getAccumulationDetectorState(SomeEvent event) {
        log.info("ACCUMULATION EVENT: {}", getHexString(event));

        long curTime = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 4],
                        event.data[DATA.shift + 3],
                        event.data[DATA.shift + 2],
                        event.data[DATA.shift + 1]})
                .getInt());
        log.info("CutTime: {}", curTime);

        long measTime = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 8],
                        event.data[DATA.shift + 7],
                        event.data[DATA.shift + 6],
                        event.data[DATA.shift + 5]})
                .getInt());
        log.info("MeasTime: {}", measTime);

        return new AccumulationDetectorState(curTime, measTime, event);
    }

    private static RestartDetector getRestartDetector(SomeEvent event) {
        log.info("RESTART EVENT: {}", getHexString(event));

        RestartReason reason = RestartReason.getRestartReasonByCode(event.data[DATA.shift]);
        log.info("Reason: {}", reason);

        RestartParam param = RestartParam.getRestartParamByCode(event.data[DATA.shift + 4]);
        log.info("Param: {}", param);

        int[] ipAddr = new int[]{
                Byte.toUnsignedInt(event.data[DATA.shift + 8]),
                Byte.toUnsignedInt(event.data[DATA.shift + 9]),
                Byte.toUnsignedInt(event.data[DATA.shift + 10]),
                Byte.toUnsignedInt(event.data[DATA.shift + 11])
        };
        log.info("IpAddr: {}", ipAddr);

        int ipPort = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 13],
                        event.data[DATA.shift + 12]})
                .getShort());
        log.info("IpPort: {}", ipPort);

        int ipPortExtDevice = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 15],
                        event.data[DATA.shift + 14]})
                .getShort());
        log.info("IpPortExtDevice: {}", ipPortExtDevice);

        return new RestartDetector(reason, param, ipAddr, ipPort, ipPortExtDevice, event);
    }

    private static InternalEvent getInternalEvent(SomeEvent event) {
        log.info("INTERNAL_DATA EVENT : {}", getHexString(event));

        int structVersion = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 1],
                        event.data[DATA.shift]})
                .getShort());
        log.info("structVersion: {}", structVersion);

        BDType bdType = BDType.getBDTypeByCode(
                (byte) ByteBuffer.wrap(new byte[]{event.data[DATA.shift + 3], event.data[DATA.shift + 2]}).getShort()
        );
        log.info("BdType: {}", bdType);

        long measTime = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 7],
                        event.data[DATA.shift + 6],
                        event.data[DATA.shift + 5],
                        event.data[DATA.shift + 4]})
                .getInt());
        log.info("MeasTime: {}", measTime);

        BDInternalMode bdInternalMode = BDInternalMode.getBDInternalModeByCode(
                (byte) ByteBuffer.wrap(new byte[]{event.data[DATA.shift + 9], event.data[DATA.shift + 8]}).getShort()
        );
        log.info("BdInternalMode: {}", bdInternalMode);

        int reserve = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 11],
                        event.data[DATA.shift + 10]})
                .getShort());
        log.info("Reserve: {}", reserve);

        float curScores1 = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 15],
                        event.data[DATA.shift + 14],
                        event.data[DATA.shift + 13],
                        event.data[DATA.shift + 12]})
                .getFloat();
        log.info("CurScores1: {}", curScores1);

        float curScores2 = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 19],
                        event.data[DATA.shift + 18],
                        event.data[DATA.shift + 17],
                        event.data[DATA.shift + 16]})
                .getFloat();
        log.info("CurScores2: {}", curScores2);

        float aveScores1 = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 23],
                        event.data[DATA.shift + 22],
                        event.data[DATA.shift + 21],
                        event.data[DATA.shift + 20]})
                .getFloat();
        log.info("AveScores1: {}", aveScores1);

        float aveScores2 = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 27],
                        event.data[DATA.shift + 26],
                        event.data[DATA.shift + 25],
                        event.data[DATA.shift + 24]})
                .getFloat();
        log.info("AveScores2: {}", aveScores2);


        float temperature = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 31],
                        event.data[DATA.shift + 30],
                        event.data[DATA.shift + 29],
                        event.data[DATA.shift + 28]})
                .getFloat();
        log.info("Temperature: {}", temperature);

        if (bdType == BDType.GAMMA) {
            float voltage400V = ByteBuffer.wrap(new byte[]{
                            event.data[DATA.shift + 35],
                            event.data[DATA.shift + 34],
                            event.data[DATA.shift + 33],
                            event.data[DATA.shift + 32]})
                    .getFloat();
            log.info("Voltage400V: {}", voltage400V);

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
                    .build();
            return new InternalEvent(bdmgInternalData, event);
        } else {
            float voltage500V = ByteBuffer.wrap(new byte[]{
                            event.data[DATA.shift + 35],
                            event.data[DATA.shift + 34],
                            event.data[DATA.shift + 33],
                            event.data[DATA.shift + 32]})
                    .getFloat();
            log.info("Voltage500V: {}", voltage500V);

            float voltage2500V = ByteBuffer.wrap(new byte[]{
                            event.data[DATA.shift + 39],
                            event.data[DATA.shift + 38],
                            event.data[DATA.shift + 37],
                            event.data[DATA.shift + 36]})
                    .getFloat();
            log.info("Voltage2500V: {}", voltage2500V);

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
                    .build();
            return new InternalEvent(bdpnInternalData, event);
        }
    }

    private static MeasurementDetectorState getMeasurementStateDetector(SomeEvent event) {
        log.info("MEASUREMENT EVENT : {}", getHexString(event));

        int structVersion = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 2],
                        event.data[DATA.shift + 1]})
                .getShort());
        log.info("StructVersion: {}", structVersion);

        BDType bdType = BDType.getBDTypeByCode(
                (byte) ByteBuffer.wrap(new byte[]{event.data[DATA.shift + 4], event.data[DATA.shift + 3]}).getShort()
        );
        log.info("BdType: {}", bdType);

        long measTime = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 8],
                        event.data[DATA.shift + 7],
                        event.data[DATA.shift + 6],
                        event.data[DATA.shift + 5]})
                .getInt());
        log.info("MeasTime: {}", measTime);

        long geoTime = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 12],
                        event.data[DATA.shift + 11],
                        event.data[DATA.shift + 10],
                        event.data[DATA.shift + 9]})
                .getInt());
        log.info("GeoTime: {}", geoTime);

        int geoDataSize = Short.toUnsignedInt(ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 14],
                        event.data[DATA.shift + 13]})
                .getShort());
        short[] geoData = new short[geoDataSize]; //size ==0 todo!!
        log.info("GeoData: {}", geoData);

        float curScore = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 18 + geoDataSize],
                        event.data[DATA.shift + 17 + geoDataSize],
                        event.data[DATA.shift + 16 + geoDataSize],
                        event.data[DATA.shift + 15 + geoDataSize]})
                .getFloat();
        log.info("CurScore: {}", curScore);

        float aveScore = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 22 + geoDataSize],
                        event.data[DATA.shift + 21 + geoDataSize],
                        event.data[DATA.shift + 20 + geoDataSize],
                        event.data[DATA.shift + 19 + geoDataSize]})
                .getFloat();
        log.info("AveScore: {}", aveScore);

        float curMeasData = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 26 + geoDataSize],
                        event.data[DATA.shift + 25 + geoDataSize],
                        event.data[DATA.shift + 24 + geoDataSize],
                        event.data[DATA.shift + 23 + geoDataSize]})
                .getFloat();
        log.info("CurMeasData: {}", curMeasData);

        float aveMeasData = ByteBuffer.wrap(new byte[]{
                        event.data[DATA.shift + 30 + geoDataSize],
                        event.data[DATA.shift + 29 + geoDataSize],
                        event.data[DATA.shift + 28 + geoDataSize],
                        event.data[DATA.shift + 27 + geoDataSize]})
                .getFloat();
        log.info("AveMeasData: {}", aveMeasData);

        BdData bdData;
        if (bdType == BDType.GAMMA) {
            bdData = new BdmgData(curMeasData, aveMeasData, curScore, aveScore);
        } else {
            bdData = new BdpnData(curMeasData, aveMeasData, curScore, aveScore);
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

    private static StringBuilder getHexString(SomeEvent event) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < event.data.length; i++)
            s.append(String.format("%x ", event.data[i]));
        return s;
    }
}
