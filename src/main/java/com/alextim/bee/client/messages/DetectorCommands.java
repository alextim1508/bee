package com.alextim.bee.client.messages;

import com.alextim.bee.client.dto.DebugSetting;
import com.alextim.bee.client.dto.GeoData;
import com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.alextim.bee.client.protocol.DetectorCodes.Command;
import static com.alextim.bee.client.protocol.DetectorCodes.CommandStatus;
import static com.alextim.bee.client.transfer.UpdDetectorTransfer.wrapToPackage;

public class DetectorCommands {

    public static class RestartDetectorCommand extends SomeCommand {

        public RestartDetectorCommand(int detectorID) {
            super(detectorID, 0, Command.RESTART,
                    wrapToPackage(detectorID, 0, Command.RESTART, new byte[0]));
        }

        @Override
        public String toString() {
            return Command.RESTART.title;
        }
    }


    public static class GetVersionCommand extends SomeCommand {

        public GetVersionCommand(int detectorID) {
            super(detectorID, 0, Command.GET_VERSION,
                    wrapToPackage(detectorID, 0, Command.GET_VERSION, new byte[0]));
        }

        @Override
        public String toString() {
            return Command.GET_VERSION.title;
        }
    }

    public static class GetVersionAnswer extends SomeCommandAnswer {

        public String version;

        public GetVersionAnswer(String version, SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
            this.version = version;
        }

        @Override
        public String toString() {
            return Command.GET_VERSION.title + "." +
                    " Версия: " + version;
        }
    }


    public static class SetMeasTimeCommand extends SomeCommand {

        public long measTime;

        public SetMeasTimeCommand(int detectorID, long measTime) {
            super(detectorID, 0, Command.SET_MEAS_TIME,
                    wrapToPackage(detectorID, 0, Command.SET_MEAS_TIME, getData(measTime)));

            this.measTime = measTime;
        }

        private static byte[] getData(long measTime) {
            int unsignedMeasTime = (int) measTime;
            byte[] array = ByteBuffer.allocate(4).putInt(unsignedMeasTime).array();
            return new byte[]{
                    array[3], array[2], array[1], array[0]
            };
        }

        @Override
        public String toString() {
            return Command.SET_MEAS_TIME.title + "." +
                    " Времени экспозиции: " + measTime;
        }
    }

    public static class SetMeasTimeAnswer extends SomeCommandAnswer {
        public SetMeasTimeAnswer(SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
        }

        @Override
        public String toString() {
            return Command.SET_MEAS_TIME.title;
        }
    }


    public static class SetSensitivityCommand extends SomeCommand {

        public float sensitivity;

        public SetSensitivityCommand(int detectorID, float sensitivity) {
            super(detectorID, 0, Command.SET_SENSITIVITY,
                    wrapToPackage(detectorID, 0, Command.SET_SENSITIVITY, getData(sensitivity)));

            this.sensitivity = sensitivity;
        }

        private static byte[] getData(float sensitivity) {
            byte[] array = ByteBuffer.allocate(4).putFloat(sensitivity).array();
            return new byte[]{
                    array[3], array[2], array[1], array[0]
            };
        }

        @Override
        public String toString() {
            return Command.SET_SENSITIVITY.title + "." +
                    " Чувствительность: " + sensitivity;
        }
    }

    public static class SetSensitivityAnswer extends SomeCommandAnswer {

        public SetSensitivityAnswer(SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
        }

        @Override
        public String toString() {
            return Command.SET_SENSITIVITY.title;
        }
    }


    public static class GetSensitivityCommand extends SomeCommand {
        public GetSensitivityCommand(int detectorID) {
            super(detectorID, 0, Command.GET_SENSITIVITY,
                    wrapToPackage(detectorID, 0, Command.GET_SENSITIVITY, new byte[0]));
        }

        @Override
        public String toString() {
            return Command.GET_SENSITIVITY.title;
        }
    }

    public static class GetSensitivityAnswer extends SomeCommandAnswer {

        public float sensitivity;

        public GetSensitivityAnswer(float sensitivity, SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
            this.sensitivity = sensitivity;
        }

        @Override
        public String toString() {
            return Command.GET_SENSITIVITY.title + "." +
                    " Чувствительность: " + sensitivity;
        }
    }


    public static class SetDeadTimeCommand extends SomeCommand {
        public final int counterIndex;
        public final BDInternalMode mode;
        public float deadTime;

        public SetDeadTimeCommand(int detectorID, int counterIndex, BDInternalMode mode, float deadTime) {
            super(detectorID, 0, Command.SET_DEAD_TIME,
                    wrapToPackage(detectorID, 0, Command.SET_DEAD_TIME, getData(counterIndex, mode, deadTime)));

            this.counterIndex = counterIndex;
            this.mode = mode;
            this.deadTime = deadTime;
        }

        private static byte[] getData(int counterIndex, BDInternalMode mode, float deadTime) {
            byte[] arrayCounterIndex = ByteBuffer.allocate(2).putShort((short) counterIndex).array();
            byte[] arrayDeadTime = ByteBuffer.allocate(4).putFloat(deadTime).array();
            return new byte[]{
                    arrayCounterIndex[1], arrayCounterIndex[0],
                    mode.code, 0,
                    arrayDeadTime[3], arrayDeadTime[2], arrayDeadTime[1], arrayDeadTime[0]
            };
        }

        @Override
        public String toString() {
            return Command.SET_DEAD_TIME.title + "." +
                    " Мертвое время: " + deadTime +
                    " счетчика " + counterIndex + "," +
                    System.lineSeparator() +
                    "Режим %s" + mode.title;
        }
    }

    public static class SetDeadTimeAnswer extends SomeCommandAnswer {
        public SetDeadTimeAnswer(SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
        }

        @Override
        public String toString() {
            return Command.SET_DEAD_TIME.title;
        }
    }


    public static class GetDeadTimeCommand extends SomeCommand {
        public final int counterIndex;
        public final BDInternalMode mode;

        public GetDeadTimeCommand(int detectorID, int counterIndex, BDInternalMode mode) {
            super(detectorID, 0, Command.GET_DEAD_TIME,
                    wrapToPackage(detectorID, 0, Command.GET_DEAD_TIME, getData(counterIndex, mode)));
            this.counterIndex = counterIndex;
            this.mode = mode;
        }

        private static byte[] getData(int counterIndex, BDInternalMode mode) {
            byte[] arrayCounterIndex = ByteBuffer.allocate(2).putShort((short) counterIndex).array();

            return new byte[]{
                    arrayCounterIndex[1], arrayCounterIndex[0],
                    mode.code, 0
            };
        }

        @Override
        public String toString() {
            return Command.GET_DEAD_TIME.title + "." +
                    " Счетчик " + counterIndex + "," +
                    System.lineSeparator() +
                    "Режим: " + mode.title;
        }
    }

    public static class GetDeadTimeAnswer extends SomeCommandAnswer {

        public final int counterIndex;
        public final BDInternalMode mode;
        public float deadTime;

        public GetDeadTimeAnswer(int counterIndex,
                                 BDInternalMode mode,
                                 float deadTime,
                                 SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
            this.deadTime = deadTime;
            this.mode = mode;
            this.counterIndex = counterIndex;
        }

        @Override
        public String toString() {
            return Command.GET_DEAD_TIME.title + "." +
                    " Мертвое время: " + deadTime +
                    " счетчика " + counterIndex + "," +
                    System.lineSeparator() +
                    "Режим: " + mode.title;
        }
    }


    public static class SetImpulseRangeCounterCommand extends SomeCommand {
        public final int counterIndex;
        public float impulseRangeCounter;

        public SetImpulseRangeCounterCommand(int detectorID, int counterIndex, float impulseRangeCounter) {
            super(detectorID, 0, Command.SET_COUNTER_PMINTERVAL,
                    wrapToPackage(detectorID, 0, Command.SET_COUNTER_PMINTERVAL, getData(counterIndex, impulseRangeCounter)));

            this.counterIndex = counterIndex;
            this.impulseRangeCounter = impulseRangeCounter;
        }

        private static byte[] getData(int counterIndex, float impulseRangeCounter) {
            byte[] arrayCounterIndex = ByteBuffer.allocate(2).putShort((short) counterIndex).array();
            byte[] arrayImpulseRangeCounter = ByteBuffer.allocate(4).putFloat(impulseRangeCounter).array();
            return new byte[]{
                    arrayCounterIndex[1], arrayCounterIndex[0],
                    0, 0,
                    arrayImpulseRangeCounter[3], arrayImpulseRangeCounter[2], arrayImpulseRangeCounter[1], arrayImpulseRangeCounter[0]
            };
        }

        @Override
        public String toString() {
            return Command.SET_COUNTER_PMINTERVAL.title + "." +
                    " Интервала импульсного режима : " + impulseRangeCounter +
                    " счетчика " + counterIndex;
        }
    }

    public static class SetImpulseRangeCounterCommandAnswer extends SomeCommandAnswer {

        public SetImpulseRangeCounterCommandAnswer(SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
        }

        @Override
        public String toString() {
            return Command.SET_COUNTER_PMINTERVAL.title;
        }
    }


    public static class GetImpulseRangeCounterCommand extends SomeCommand {

        public final int counterIndex;

        public GetImpulseRangeCounterCommand(int detectorID, int counterIndex) {
            super(detectorID, 0, Command.GET_COUNTER_PMINTERVAL,
                    wrapToPackage(detectorID, 0, Command.GET_COUNTER_PMINTERVAL, getData(counterIndex)));
            this.counterIndex = counterIndex;
        }

        private static byte[] getData(int counterIndex) {
            byte[] arrayCounterIndex = ByteBuffer.allocate(2).putShort((short) counterIndex).array();

            return new byte[]{
                    arrayCounterIndex[1], arrayCounterIndex[0],
                    0, 0
            };
        }

        @Override
        public String toString() {
            return Command.GET_COUNTER_PMINTERVAL.title + "." +
                    " Счетчик " + counterIndex;
        }
    }

    public static class GetImpulseRangeCounterCommandAnswer extends SomeCommandAnswer {

        public final int counterIndex;
        public float impulseRangeCounter;

        public GetImpulseRangeCounterCommandAnswer(int counterIndex,
                                                   float impulseRangeCounter,
                                                   SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
            this.impulseRangeCounter = impulseRangeCounter;
            this.counterIndex = counterIndex;
        }

        @Override
        public String toString() {
            return Command.GET_COUNTER_PMINTERVAL.title + "." +
                    " Интервала импульсного режима : " + impulseRangeCounter +
                    " счетчика " + counterIndex;
        }
    }


    public static class SetDebugSettingCommand extends SomeCommand {
        public final DebugSetting debugSetting;

        public SetDebugSettingCommand(int detectorID, DebugSetting debugSetting) {
            super(detectorID, 0, Command.SET_DEBUG_SETTINGS,
                    wrapToPackage(detectorID, 0, Command.SET_DEBUG_SETTINGS, getData(debugSetting)));

            this.debugSetting = debugSetting;
        }

        private static byte[] getData(DebugSetting debugSetting) {
            byte[] modeArray = ByteBuffer.allocate(4).putInt(debugSetting.mode.code).array();
            byte[] chmQuenchArray = ByteBuffer.allocate(4).putInt((int) debugSetting.chmQuench).array();
            byte[] clmQuenchArray = ByteBuffer.allocate(4).putInt((int) debugSetting.clmQuench).array();
            byte[] pmIntervalArray = ByteBuffer.allocate(4).putInt((int) debugSetting.pmInterval).array();
            byte[] pmQuenchArray = ByteBuffer.allocate(4).putInt((int) debugSetting.pmQuench).array();
            byte[] pmHiUpArray = ByteBuffer.allocate(4).putInt((int) debugSetting.pmHiUp).array();

            return new byte[]{
                    1, 0, 0, 0, //version
                    1, //enabled
                    modeArray[3], modeArray[2], modeArray[1], modeArray[0],
                    chmQuenchArray[3], chmQuenchArray[2], chmQuenchArray[1], chmQuenchArray[0],
                    clmQuenchArray[3], clmQuenchArray[2], clmQuenchArray[1], clmQuenchArray[0],
                    pmIntervalArray[3], pmIntervalArray[2], pmIntervalArray[1], pmIntervalArray[0],
                    pmQuenchArray[3], pmQuenchArray[2], pmQuenchArray[1], pmQuenchArray[0],
                    pmHiUpArray[3], pmHiUpArray[2], pmHiUpArray[1], pmHiUpArray[0]
            };
        }

        @Override
        public String toString() {
            return Command.SET_DEBUG_SETTINGS.title + "." +
                    debugSetting;
        }
    }

    public static class SetDebugSettingAnswer extends SomeCommandAnswer {
        public SetDebugSettingAnswer(SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
        }

        @Override
        public String toString() {
            return Command.SET_DEBUG_SETTINGS.title;
        }
    }


    public static class GetDebugSettingCommand extends SomeCommand {

        public GetDebugSettingCommand(int detectorID) {
            super(detectorID, 0, Command.GET_DEBUG_SETTINGS,
                    wrapToPackage(detectorID, 0, Command.GET_DEBUG_SETTINGS, new byte[0]));
        }

        @Override
        public String toString() {
            return Command.GET_DEBUG_SETTINGS.title;
        }
    }

    public static class GetDebugSettingAnswer extends SomeCommandAnswer {

        public final DebugSetting debugSetting;

        public GetDebugSettingAnswer(DebugSetting debugSetting,
                                     SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
            this.debugSetting = debugSetting;
        }

        @Override
        public String toString() {
            return Command.GET_DEBUG_SETTINGS.title + "." +
                    debugSetting;
        }
    }


    public static class SetCounterCorrectCoeffCommand extends SomeCommand {
        public final int counterIndex;
        public final BDInternalMode mode;
        public float counterCorrectCoeff;

        public SetCounterCorrectCoeffCommand(int detectorID, int counterIndex, BDInternalMode mode, float counterCorrectCoeff) {
            super(detectorID, 0, Command.SET_CORRECT_COFF,
                    wrapToPackage(detectorID, 0, Command.SET_CORRECT_COFF, getData(counterIndex, mode, counterCorrectCoeff)));

            this.counterIndex = counterIndex;
            this.mode = mode;
            this.counterCorrectCoeff = counterCorrectCoeff;
        }

        private static byte[] getData(int counterIndex, BDInternalMode mode, float counterCorrectCoeff) {
            byte[] arrayCounterIndex = ByteBuffer.allocate(2).putShort((short) counterIndex).array();
            byte[] arrayCounterCorCoef = ByteBuffer.allocate(4).putFloat(counterCorrectCoeff).array();
            return new byte[]{
                    arrayCounterIndex[1], arrayCounterIndex[0],
                    mode.code, 0,
                    arrayCounterCorCoef[3], arrayCounterCorCoef[2], arrayCounterCorCoef[1], arrayCounterCorCoef[0]
            };
        }

        @Override
        public String toString() {
            return Command.SET_CORRECT_COFF.title + "." +
                    " Корректирующий коэффициент: " + counterCorrectCoeff +
                    " счетчика " + counterIndex + "," +
                    System.lineSeparator() +
                    "Режим: " + mode.title;
        }
    }

    public static class SetCounterCorrectCoeffAnswer extends SomeCommandAnswer {
        public SetCounterCorrectCoeffAnswer(SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
        }

        @Override
        public String toString() {
            return Command.SET_CORRECT_COFF.title;
        }
    }


    public static class GetCounterCorrectCoeffCommand extends SomeCommand {
        public final int counterIndex;
        public final BDInternalMode mode;

        public GetCounterCorrectCoeffCommand(int detectorID, int counterIndex, BDInternalMode mode) {
            super(detectorID, 0, Command.GET_CORRECT_COFF,
                    wrapToPackage(detectorID, 0, Command.GET_CORRECT_COFF, getData(counterIndex, mode)));
            this.counterIndex = counterIndex;
            this.mode = mode;
        }

        private static byte[] getData(int counterIndex, BDInternalMode mode) {
            byte[] arrayCounterIndex = ByteBuffer.allocate(2).putShort((short) counterIndex).array();
            return new byte[]{
                    arrayCounterIndex[1], arrayCounterIndex[0],
                    mode.code, 0
            };
        }

        @Override
        public String toString() {
            return Command.GET_CORRECT_COFF.title + "." +
                    " Счетчик " + counterIndex + "," +
                    System.lineSeparator() +
                    "Режим:  " + mode.title;
        }
    }

    public static class GetCounterCorrectCoeffAnswer extends SomeCommandAnswer {
        public final int counterIndex;
        public final BDInternalMode mode;
        public final float counterCorrectCoeff;

        public GetCounterCorrectCoeffAnswer(int counterIndex,
                                            BDInternalMode mode,
                                            float counterCorrectCoeff,
                                            SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
            this.counterIndex = counterIndex;
            this.mode = mode;
            this.counterCorrectCoeff = counterCorrectCoeff;
        }

        @Override
        public String toString() {
            return Command.GET_CORRECT_COFF.title + "." +
                    " Корректирующий коэффициент: " + counterCorrectCoeff +
                    " счетчика " + counterIndex + "," +
                    System.lineSeparator() +
                    "Режим: " + mode.title;
        }
    }


    public static class SetGeoDataCommand extends SomeCommand {

        public final GeoData geoData;

        public SetGeoDataCommand(int detectorID, GeoData geoData) {
            super(detectorID, 0, Command.SET_GEO_DATA,
                    wrapToPackage(detectorID, 0, Command.SET_GEO_DATA, getData(geoData)));
            this.geoData = geoData;
        }

        private static byte[] getData(GeoData geoData) {
            byte[] arraySize = ByteBuffer.allocate(2).putShort((short) 8).array();
            byte[] arrayLat = ByteBuffer.allocate(4).putFloat(geoData.lat()).array();
            byte[] arrayLon = ByteBuffer.allocate(4).putFloat(geoData.lon()).array();
            return new byte[]{
                    arraySize[1], arraySize[0],
                    arrayLat[3], arrayLat[2], arrayLat[1], arrayLat[0],
                    arrayLon[3], arrayLon[2], arrayLon[1], arrayLon[0]
            };
        }

        @Override
        public String toString() {
            return Command.SET_GEO_DATA.title + ". " + geoData;
        }
    }

    public static class SetGeoDataAnswer extends SomeCommandAnswer {

        public SetGeoDataAnswer(SomeCommandAnswer answer) {
            super(answer.detectorID, answer.time, answer.commandCode, answer.commandStatusCode, answer.data);
        }

        @Override
        public String toString() {
            return Command.SET_GEO_DATA.title;
        }
    }


    public static class ChangeIpCommand extends SomeCommand {
        public final int[] ipAddr;
        public final int ipPort;
        public final int externalDeviceIpPort;

        public ChangeIpCommand(int detectorID, int[] ipAddr, int ipPort, int externalDeviceIpPort) {
            super(detectorID, 0, Command.SET_IP_ADDR,
                    wrapToPackage(detectorID, 0, Command.SET_IP_ADDR, getData(ipAddr, ipPort, externalDeviceIpPort)));

            this.ipAddr = ipAddr;
            this.ipPort = ipPort;
            this.externalDeviceIpPort = externalDeviceIpPort;
        }


        private static byte[] getData(int[] ipAddr, int ipPort, int externalDeviceIpPort) {
            short unsignedIpPort = (short) ipPort;
            byte[] array1 = ByteBuffer.allocate(2).putShort(unsignedIpPort).array();

            short unsignedExternalDeviceIpPort = (short) externalDeviceIpPort;
            byte[] array2 = ByteBuffer.allocate(2).putShort(unsignedExternalDeviceIpPort).array();

            return new byte[]{
                    (byte) ipAddr[0], (byte) ipAddr[1], (byte) ipAddr[2], (byte) ipAddr[3],
                    array1[1], array1[0],
                    array2[1], array2[0]
            };
        }

        @Override
        public String toString() {
            return Command.SET_IP_ADDR.title + "." +
                    " IP адрес: " + Arrays.toString(ipAddr) +
                    ", IP порт: " + ipPort +
                    ", IP порт внешних устройств: " + externalDeviceIpPort;
        }
    }


    public static class SomeCommandAnswer extends SomeCommand {
        public final CommandStatus commandStatusCode;

        public SomeCommandAnswer(int detectorID,
                                 long time,
                                 Command commandCode,
                                 CommandStatus commandStatusCode,
                                 byte[] data) {
            super(detectorID, time, commandCode, data);
            this.commandStatusCode = commandStatusCode;
        }
    }

    public static class SomeCommand extends DetectorMsg {
        public final Command commandCode;

        public SomeCommand(int detectorID,
                           long time,
                           Command commandCode,
                           byte[] data) {
            super(detectorID, time, data);
            this.commandCode = commandCode;
        }
    }
}
