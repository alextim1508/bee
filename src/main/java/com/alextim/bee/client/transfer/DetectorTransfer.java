package com.alextim.bee.client.transfer;

import com.alextim.bee.client.protocol.DetectorCodes.Command;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

@Slf4j
public class DetectorTransfer {

    protected Socket socket;
    protected Thread socketListener;

    protected final byte[] rcvBuf;

    protected static final int TR_BUF_SIZE = 1024;
    protected final byte[] trBuf = new byte[TR_BUF_SIZE];

    protected final int HEADER_LEN = 6;
    protected final byte START_BYTE = (byte) 0x79;
    protected final byte CONTROL_BASE = (byte) 0x57;

    private Consumer<byte[]> consumer;
    private String name;

    public DetectorTransfer(String name, Consumer<byte[]> consumer, int rcvBufSize) {
        this.consumer = consumer;
        this.name = name;
        rcvBuf = new byte[rcvBufSize];
    }

    public void open(String ip, int port, Runnable callback) {
        log.info("open socket");
        socketListener = new Thread(() -> {
            while (true) {
                try {
                    log.info("=== CONNECT TO SOCKET! ===");
                    socket = new Socket(ip, port);

                    log.info("CONNECT TO SOCKET OK");

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        log.error("InterruptedException");
                        return;
                    }

                    log.info("SLEEPING OK");

                    if(callback != null)
                        callback.run();
                    log.info("CALLBACK OK");
                    loop();
                } catch (Exception e) {
                    log.error("", e);

                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException ex) {
                        log.error("InterruptedException");
                        return;
                    }

                    log.info("NEED TO RECONNECT");
                }
            }
        });
        socketListener.setName(getClass().getSimpleName()+"Thread");
        socketListener.setPriority(Thread.NORM_PRIORITY);
        socketListener.start();
    }

    public void shutdown() {
        log.info("socketListener interrupt");
        if(socketListener != null) {
            socketListener.interrupt();
        }
    }

    private void loop() {
        int rcvInd = 0;

        while (true) {
            int size = 0;
            int start = 0;

            try {
                if(socketListener.isInterrupted()) {
                    log.info("socketListener is interrupted");
                    return;
                }

                if (rcvInd >= rcvBuf.length)
                    throw new RuntimeException("OverFlow " + rcvInd  + " " + rcvBuf.length);

                log.info("==========================");
                log.info("rcv index before receiving: {}",rcvInd);
                log.info("available rcv buffer size before receiving: {}", rcvBuf.length - rcvInd);
                size = socket.getInputStream().read(rcvBuf, rcvInd, rcvBuf.length - rcvInd);
                log.info("received bytes size: {}", size);

            } catch (IOException e) {
                log.error("Socket error", e);
                throw new RuntimeException(e);
            }

            if (size > 0) {
                rcvInd += size;
                log.info("rcvInd after receiving: {}", rcvInd);

                while (start < rcvInd) {
                    while (start < rcvInd) {
                        if (rcvBuf[start] == START_BYTE)
                            break;
                        else
                            start++;
                    }

                    if(socketListener.isInterrupted()) {
                        log.info("socketListener is interrupted");
                        return;
                    }

                    log.info("------------------- found start: {}", start);
                    if(start != 0)
                        System.arraycopy(rcvBuf, start, rcvBuf, 0, rcvInd - start);
                    rcvInd = rcvInd - start;
                    log.info("rcv index after shift: {}", rcvInd);
                    start = 0;

                    if (rcvInd < HEADER_LEN + 1) { //todo <= -> <
                        break;
                    }

                    if(!controlHeaderCheck()) {
                        start++;
                        log.info("continue");
                        continue;
                    }

                    int len = Short.toUnsignedInt(
                            ByteBuffer.wrap(new byte[]{rcvBuf[4], rcvBuf[3]}).getShort());
                    log.info("len: {}", len);

                    if (rcvInd < HEADER_LEN + 1 + len) { //todo <= -> <
                        log.info("break");
                        break;
                    }

                    controlBodyCheck(len);


                    byte[] data = new byte[len]; //CHANGE TO ROW DATA !!!  //todo!!!!!!!!!!!
                    //dont forget!

                    log.info("payload data is found");

                    for (int i = 0; i < data.length; i++) {
                        data[i] = rcvBuf[HEADER_LEN + 1 + i];
                    }

                    StringBuilder bytes = new StringBuilder();
                    for (int i = 0; i < data.length && i < 10; i++)
                        bytes.append(String.format("%x ", data[i]));
                    log.info("First bytes: {}", bytes);

                    if (consumer != null) {
                        consumer.accept(rcvBuf);
                        log.info("payload data is handled");
                    }

                    start = HEADER_LEN + 1 + len;
                    log.info("start after one handling: {}", start);
                }

                log.info("__start after handling {}", start);
                log.info("shift {}", rcvInd - start);
                if(rcvInd != start)
                    System.arraycopy(rcvBuf, start, rcvBuf, 0, rcvInd - start);
                rcvInd = rcvInd - start;
                log.info("__rcvInd after handling {}", rcvInd);

            } else {
                log.error("Socket error. Size: {}", size);

                throw new RuntimeException("Socket error. Size:" + size);
            }
        }
    }

    private void controlBodyCheck(int len) {
        int ks = CONTROL_BASE;
        for (int j = HEADER_LEN + 1; j < HEADER_LEN + 1 + len; j++)
            ks += rcvBuf[j];

        if (((byte) (ks & 0xFF)) != rcvBuf[HEADER_LEN - 1]) {
            throw new RuntimeException(rcvBuf[HEADER_LEN - 1] + " " + ((byte) (ks & 0xFF)) + " InvalidDataKS");
        }
    }

    private boolean controlHeaderCheck() {
        int ks = CONTROL_BASE;
        for (int j = 1; j < HEADER_LEN; j++)
            ks += rcvBuf[j];

        if (((byte) (ks & 0xFF)) != rcvBuf[HEADER_LEN]) {
            String er = ((byte) ks & 0xFF) + " " + rcvBuf[HEADER_LEN] + " InvalidDataKS";
            log.error("Control header: {}", er);
            return false;
        }
        return true;
    }

    public void writeCommand(Command commandCode, long id) {
        Arrays.fill(trBuf, (byte)0);
        byte[] data = ByteBuffer.allocate(8).putLong(id).array();

        trBuf[0] = START_BYTE;
        trBuf[1] = (byte) (commandCode.code >> 8); //todo!
        trBuf[2] = commandCode.code;

        trBuf[3] = (byte) data.length;
        trBuf[4] = (byte) (data.length << 8);

        byte bodyControl = CONTROL_BASE;
        for (int i = 0; i < data.length; i++) {
            bodyControl += data[data.length - 1 - i];
            trBuf[i + HEADER_LEN + 1] = data[data.length - 1 - i];
        }
        trBuf[5] = bodyControl;

        byte headerControl = CONTROL_BASE;
        for (int i = 1; i < HEADER_LEN + 1; i++)
            headerControl += trBuf[i];
        trBuf[6] = headerControl;

        try {
            socket.getOutputStream().write(trBuf, 0, 1 + HEADER_LEN + data.length);
        } catch (IOException e) {
            log.error("Socket command error ", e);
            throw new RuntimeException(e);
        }
    }
}