package com.alextim.bee.client;

import com.alextim.bee.client.messages.DetectorCommands.SomeCommand;
import com.alextim.bee.client.messages.DetectorCommands.SomeCommandAnswer;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.client.messages.ExceptionMessage;
import com.alextim.bee.client.protocol.DetectorCodes.Command;
import com.alextim.bee.client.protocol.DetectorCodes.CommandStatus;
import com.alextim.bee.client.protocol.DetectorCodes.Event;
import com.alextim.bee.client.transfer.UpdDetectorTransfer;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import static com.alextim.bee.client.messages.DetectorEvents.SomeEvent;
import static com.alextim.bee.client.protocol.DetectorCodes.Format.*;
import static com.alextim.bee.client.protocol.DetectorCodes.MsgType.EVENT_TYPE;

@Slf4j
public class DetectorClient extends DetectorClientAbstract {

    private final String IP;
    private final int rcvPort;
    private final int trPort;
    private final int rcvBufSize;
    private UpdDetectorTransfer transfer;

    public DetectorClient(String ip,
                          int rcvPrt,
                          int trPort,
                          int rcvBufSize,
                          LinkedBlockingQueue<DetectorMsg> queue ) {
        super(queue);
        this.IP = ip;
        this.rcvPort = rcvPrt;
        this.trPort = trPort;
        this.rcvBufSize = rcvBufSize;

        createTransfer();
    }

    public void createTransfer() {
        transfer = new UpdDetectorTransfer((bytes) -> {
            log.info("========== New detector message ========== ");

            int detectorID = ByteBuffer.wrap(new byte[]{
                            bytes[ID.shift + 3],
                            bytes[ID.shift + 2],
                            bytes[ID.shift + 1],
                            bytes[ID.shift]})
                    .getInt();
            log.info("DetectorID: {}", detectorID);

            long time = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                            bytes[TIME.shift + 3],
                            bytes[TIME.shift + 2],
                            bytes[TIME.shift + 1],
                            bytes[TIME.shift]})
                    .getInt());
            log.info("Time: {}", time);

            try {
                if (bytes[TYPE.shift] == EVENT_TYPE.code) {
                    Event eventByCode = Event.getEventByCode(bytes[EVT_ANS_CMD.shift]);
                    log.info("Event: {}", eventByCode.title);

                    queue.add(new SomeEvent(detectorID, time, eventByCode, bytes));
                } else {
                    Command commandByCode = Command.getCommandByCode(bytes[TYPE.shift]);
                    log.info("Command: {}", commandByCode.title);

                    CommandStatus statusByCode = CommandStatus.getCommandStatusByCode(bytes[EVT_ANS_CMD.shift]);
                    log.info("Status: {}", statusByCode.title);

                    queue.add(new SomeCommandAnswer(detectorID, time, commandByCode, statusByCode, bytes));
                }
            } catch (Exception e) {
                queue.add(new ExceptionMessage(detectorID, time, e, bytes));
            }

        }, rcvBufSize);
    }

    @Override
    public void connect() {
        transfer.open(IP, rcvPort, trPort);
    }

    @Override
    public void sendCommand(SomeCommand command) {
        transfer.sendData(command);
    }

    @Override
    public void close() {
        transfer.close();
    }
}