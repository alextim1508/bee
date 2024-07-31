package com.alextim.bee.client.messages;

import static com.alextim.bee.client.protocol.DetectorCodes.Command;
import static com.alextim.bee.client.protocol.DetectorCodes.CommandStatus;

public class DetectorCommands {

    public static class RestartDetectorCommand extends SomeCommand {
        public RestartDetectorCommand(SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
        }
    }


    public static class GetVersionCommand extends SomeCommand {
        public GetVersionCommand(SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
        }
    }

    public static class GetVersionAnswer extends SomeCommandAnswer {

        public byte[] version;

        public GetVersionAnswer(byte[] version, SomeCommandAnswer commandAnswer) {
            super(commandAnswer.detectorID, commandAnswer.time, commandAnswer.commandCode, commandAnswer.commandStatusCode, commandAnswer.data);
            this.version = version;
        }
    }


    public static class SetMeasTimeCommand extends SomeCommand {

        public long measTime;

        public SetMeasTimeCommand(long measTime, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
            this.measTime = measTime;
        }
    }

    public static class SetMeasTimeAnswer extends SomeCommandAnswer {
        public SetMeasTimeAnswer(SomeCommandAnswer commandAnswer) {
            super(commandAnswer.detectorID, commandAnswer.time, commandAnswer.commandCode, commandAnswer.commandStatusCode, commandAnswer.data);
        }
    }


    public static class SetSensitivityCommand extends SomeCommand {

        public float sensitivity;

        public SetSensitivityCommand(float sensitivity, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
            this.sensitivity = sensitivity;
        }
    }

    public static class SetSensitivityAnswer extends SomeCommandAnswer {

        public SetSensitivityAnswer(SomeCommandAnswer commandAnswer) {
            super(commandAnswer.detectorID, commandAnswer.time, commandAnswer.commandCode, commandAnswer.commandStatusCode, commandAnswer.data);
        }
    }


    public static class GetSensitivityCommand extends SomeCommand {
        public GetSensitivityCommand(SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
        }
    }

    public static class GetSensitivityAnswer extends SomeCommandAnswer {

        public float sensitivity;

        public GetSensitivityAnswer(float sensitivity, SomeCommandAnswer command) {
            super(command.detectorID, command.time, command.commandCode, command.commandStatusCode, command.data);
            this.sensitivity = sensitivity;
        }
    }


    public static class SetDeadTimeCommand extends SomeCommand {
        public float deadTime;

        public SetDeadTimeCommand(float deadTime, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
            this.deadTime = deadTime;
        }
    }

    public static class SetDeadTimeAnswer extends SomeCommandAnswer {
        public SetDeadTimeAnswer(SomeCommandAnswer command) {
            super(command.detectorID, command.time, command.commandCode, command.commandStatusCode, command.data);
        }
    }


    public static class GetDeadTimeCommand extends SomeCommand {
        public GetDeadTimeCommand(SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
        }
    }

    public static class GetDeadTimeAnswer extends SomeCommandAnswer {

        public float deadTime;

        public GetDeadTimeAnswer(float deadTime, SomeCommandAnswer command) {
            super(command.detectorID, command.time, command.commandCode, command.commandStatusCode, command.data);
            this.deadTime = deadTime;
        }
    }


    public static class SetCounterCorrectCoeffCommand extends SomeCommand {
        public long counterIndex;
        public float counterCorrectCoeff;

        public SetCounterCorrectCoeffCommand(long counterIndex, float counterCorrectCoeff, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
            this.counterIndex = counterIndex;
            this.counterCorrectCoeff = counterCorrectCoeff;
        }
    }

    public static class SetCounterCorrectCoeffAnswer extends SomeCommandAnswer {
        public SetCounterCorrectCoeffAnswer(SomeCommandAnswer command) {
            super(command.detectorID, command.time, command.commandCode, command.commandStatusCode, command.data);
        }
    }


    public static class GetCounterCorrectCoeffCommand extends SomeCommand {
        public final long counterIndex;

        public GetCounterCorrectCoeffCommand(long counterIndex, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
            this.counterIndex = counterIndex;
        }
    }

    public static class GetCounterCorrectCoeffAnswer extends SomeCommandAnswer {
        public final long counterIndex;
        public final float counterCorrectCoeff;

        public GetCounterCorrectCoeffAnswer(long counterIndex,
                                            float counterCorrectCoeff,
                                            SomeCommandAnswer command) {
            super(command.detectorID, command.time, command.commandCode, command.commandStatusCode, command.data);
            this.counterIndex = counterIndex;
            this.counterCorrectCoeff = counterCorrectCoeff;
        }
    }


    public static class SetGeoDataCommand extends SomeCommand {
        public final long geoData;

        public SetGeoDataCommand(long geoData, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
            this.geoData = geoData;
        }
    }

    public static class SetGeoDataAnswer extends SomeCommandAnswer {

        public SetGeoDataAnswer(SomeCommandAnswer command) {
            super(command.detectorID, command.time, command.commandCode, command.commandStatusCode, command.data);
        }
    }


    public static class ChangeIpCommand extends SomeCommand {
        public final short[] ipAddr;
        public final int ipPort;
        public final int externalDeviceIpPort;

        public ChangeIpCommand(short[] ipAddr,
                               int ipPort,
                               int externalDeviceIpPort,
                               SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
            this.ipAddr = ipAddr;
            this.ipPort = ipPort;
            this.externalDeviceIpPort = externalDeviceIpPort;
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
