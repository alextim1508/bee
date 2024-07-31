package com.alextim.bee.client;

import com.alextim.bee.client.messages.DetectorCommands.*;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.client.protocol.DetectorCodes;

import java.util.concurrent.LinkedBlockingQueue;

public class DetectorUdpClient extends DetectorClientInit {
    public DetectorUdpClient(String IP, int port, LinkedBlockingQueue<DetectorMsg> queue, int rcvBufSize) {
        super(IP, port, rcvBufSize, queue);
    }

    public void restart(RestartDetectorCommand restartDetectorCommand) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.RESTART, 0);
        }
    }

    public void getVersion(GetVersionCommand getVersionCommand) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.GET_VERSION, 0);
        }
    }

    public void setMeasTime(SetMeasTimeCommand setMeasTimeCommand) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_MEAS_TIME, 0);
        }
    }

    public void setSensitivity(SetSensitivityCommand setSensitivityCommand) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_SENSITIVITY, 0);
        }
    }

    public void getSensitivity(GetSensitivityCommand getSensitivityCommand) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.GET_SENSITIVITY, 0);
        }
    }

    public void setDeadTime(SetDeadTimeCommand setDeadTimeCommand) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_DEAD_TIME, 0);
        }
    }

    public void getDeadTime(GetDeadTimeCommand getDeadTimeCommand) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.GET_DEAD_TIME, 0);
        }
    }

    public void setCounterCorrectCoeff(SetCounterCorrectCoeffCommand setCounterCorrectCoeff) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_CORRECT_COFF, 0);
        }
    }

    public void getCounterCorrectCoeff(GetCounterCorrectCoeffCommand getCounterCorrectCoeff) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.GET_CORRECT_COFF, 0);
        }
    }

    public void setGeoDataCommand(SetGeoDataCommand setGeoDataCommand) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_GEO_DATA, 0);
        }
    }

    public void changeIpCommand(ChangeIpCommand changeIpCommand) {
        if (isConnected.get()) {
            transfer.writeCommand(DetectorCodes.Command.SET_IP_ADDR, 0);
        }
    }
}
