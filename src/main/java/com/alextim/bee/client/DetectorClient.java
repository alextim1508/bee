package com.alextim.bee.client;

import com.alextim.bee.client.messages.DetectorCommands.*;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.client.protocol.DetectorCodes;

import java.util.concurrent.LinkedBlockingQueue;

public class DetectorClient extends DetectorClientInitializer {
    public DetectorClient(String IP, int port, LinkedBlockingQueue<DetectorMsg> queue, int rcvBufSize) {
        super(IP, port, rcvBufSize, queue);
    }

    public void restart(RestartDetectorCommand restartDetectorCommand) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.RESTART, 0);
        }
    }

    public void getVersion(GetVersionCommand command) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.GET_VERSION, 0);
        }
    }

    public void setMeasTime(SetMeasTimeCommand command) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_MEAS_TIME, 0);
        }
    }

    public void setSensitivity(SetSensitivityCommand command) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_SENSITIVITY, 0);
        }
    }

    public void getSensitivity(GetSensitivityCommand command) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.GET_SENSITIVITY, 0);
        }
    }

    public void setDeadTime(SetDeadTimeCommand command) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_DEAD_TIME, 0);
        }
    }

    public void getDeadTime(GetDeadTimeCommand command) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.GET_DEAD_TIME, 0);
        }
    }

    public void setCounterCorrectCoeff(SetCounterCorrectCoeffCommand command) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_CORRECT_COFF, 0);
        }
    }

    public void getCounterCorrectCoeff(GetCounterCorrectCoeffCommand command) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.GET_CORRECT_COFF, 0);
        }
    }

    public void setGeoDataCommand(SetGeoDataCommand command) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_GEO_DATA, 0);
        }
    }

    public void changeIpCommand(ChangeIpCommand command) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_IP_ADDR, 0);
        }
    }
}
