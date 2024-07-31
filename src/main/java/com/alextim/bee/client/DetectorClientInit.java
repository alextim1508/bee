package com.alextim.bee.client;

import com.alextim.bee.client.messages.DetectorCommands.*;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.client.protocol.DetectorCodes.Command;
import com.alextim.bee.client.protocol.DetectorCodes.CommandStatus;
import com.alextim.bee.client.protocol.DetectorCodes.Event;
import com.alextim.bee.client.transfer.DetectorTransfer;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alextim.bee.client.messages.DetectorEvents.SomeEvent;
import static com.alextim.bee.client.protocol.DetectorCodes.Format.*;
import static com.alextim.bee.client.protocol.DetectorCodes.MsgType.EVENT_TYPE;

@Slf4j
public class DetectorClientInit extends DetectorClientAbstract {

    private final String IP;
    private final int port;
    private final int rcvBufSize;

    protected DetectorTransfer transfer;

    protected AtomicBoolean isConnected = new AtomicBoolean(false);

    public DetectorClientInit(String IP, int port, int rcvBufSize, LinkedBlockingQueue<DetectorMsg> queue) {
        super(queue);
        this.IP = IP;
        this.port = port;
        this.rcvBufSize = rcvBufSize;
    }

    @Override
    public void connect() {
        transfer = new DetectorTransfer("DetectorTransfer", (bytes) -> {
            log.info("========New some detector message========");

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
        }, rcvBufSize);

        startOpenSocketThread();
    }

    private void startOpenSocketThread() {
        Thread thread = new Thread(() -> {
            transfer.open(IP, port, () ->
                    Platform.runLater(() -> {
                        log.info("Connect to bdServer");
                        isConnected.set(true);
                    })
            );
        });
        thread.setName("startOpenSocketThread");
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((t, e) -> {
            log.error("", e);
        });
        thread.start();
    }

    public void shutdown() {
        log.info("transfer shutdown");
        transfer.shutdown();
        log.info("transfer shutdown OK");
    }
}