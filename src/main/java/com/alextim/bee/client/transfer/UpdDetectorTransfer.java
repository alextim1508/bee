package com.alextim.bee.client.transfer;

import com.alextim.bee.client.messages.DetectorCommands.SomeCommand;
import com.alextim.bee.client.protocol.DetectorCodes.Command;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static com.alextim.bee.client.protocol.DetectorCodes.CONTROL_SUM_BASE;
import static com.alextim.bee.client.protocol.DetectorCodes.Format.*;
import static com.alextim.bee.client.protocol.DetectorCodes.MsgType.COMMAND_TYPE;
import static com.alextim.bee.client.protocol.DetectorCodes.START_PACKAGE_BYTE;

@Slf4j
public class UpdDetectorTransfer {

    private Thread socketListener;

    private final byte[] rcvBuf;

    private final Consumer<byte[]> consumer;

    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    public UpdDetectorTransfer(Consumer<byte[]> consumer, int rcvBufSize) {
        this.consumer = consumer;
        rcvBuf = new byte[rcvBufSize];
    }

    public void open(String ip, int port, Runnable callback) {
        log.info("open socket: {} / {}", ip, port);

        socketListener = new Thread(() -> {
            while (true) {
                try {
                    log.info("=== CONNECT TO SOCKET! ===");

                    socket = new DatagramSocket();

                    address = InetAddress.getByName(ip);

                    this.port = port;

                    if (callback != null)
                        callback.run();

                    loop();

                    return;
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);

                } catch (Exception e) {
                    log.error("NEED TO RECONNECT", e);

                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException ex) {
                        log.error("InterruptedException");
                        return;
                    }
                }
            }
        });
        socketListener.setName(getClass().getSimpleName() + "Thread");
        socketListener.setPriority(Thread.NORM_PRIORITY);
        socketListener.start();
    }

    private void loop() {
        int rcvInd = 0;

        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            try {
                if (rcvInd >= rcvBuf.length)
                    throw new RuntimeException("OverFlow " + rcvInd + " " + rcvBuf.length);

                DatagramPacket packet = new DatagramPacket(rcvBuf, rcvInd, rcvBuf.length - rcvInd, address, port);
                socket.receive(packet);

                if (packet.getLength() == 0)
                    continue;

                rcvInd += packet.getLength();

            } catch (SocketException e) {
                log.error("SocketException", e);
                return;
            } catch (IOException e) {
                log.error("IOException", e);
                throw new RuntimeException(e);
            }

            rcvInd = handle(rcvBuf, rcvInd, consumer);
        }
    }

    static int handle(byte[] rcvBuf, int rcvInd, Consumer<byte[]> consumer) {
        int start = 0;
        while (start < rcvInd) {

            while (start < rcvInd) {
                if (rcvBuf[start] == START_PACKAGE_BYTE)
                    break;
                else
                    start++;
            }

            if (start != 0) {
                System.arraycopy(rcvBuf, start, rcvBuf, 0, rcvInd - start);
                rcvInd = rcvInd - start;
                start = 0;
            }

            if (rcvInd < LEN.shift + 1) {
                return rcvInd;
            }

            int len = Short.toUnsignedInt(
                    ByteBuffer.wrap(new byte[]{rcvBuf[LEN.shift + 1], rcvBuf[LEN.shift]}).getShort());

            if (rcvInd < DATA.shift + len) {
                return rcvInd;
            }

            try {
                controlHeaderCheck(rcvBuf, 0, HEADER_KS.shift, HEADER_KS.shift);
            } catch (Exception e) {
                log.error("", e);
                start++;
                continue;
            }

            try {
                controlBodyCheck(rcvBuf, DATA.shift, DATA.shift + len, DATA_KS.shift);
            } catch (Exception e) {
                log.error("", e);
                start++;
                continue;
            }

            byte[] data = new byte[DATA.shift + len];
            System.arraycopy(rcvBuf, start, data, 0, data.length);

            consumer.accept(data);

            start = DATA.shift + len;
        }

        rcvInd -= start;

        return rcvInd;
    }

    static void controlBodyCheck(byte[] arr, int i1, int i2, int indexKs) {
        int ks = CONTROL_SUM_BASE;
        for (int j = i1; j < i2; j++)
            ks += Byte.toUnsignedInt(arr[j]);
        ks = ks & 0xFF;

        if (ks != Byte.toUnsignedInt(arr[indexKs])) {
            throw new RuntimeException(String.format("InvalidDataKS: %x != calculated %x ",
                    Byte.toUnsignedInt(arr[indexKs]), ks));
        }
    }

    static void controlHeaderCheck(byte[] arr, int i1, int i2, int indexKs) {
        int ks = CONTROL_SUM_BASE;
        for (int j = i1; j < i2; j++)
            ks += Byte.toUnsignedInt(arr[j]);
        ks = ks & 0xFF;

        if (ks != Byte.toUnsignedInt(arr[indexKs])) {
            throw new RuntimeException(String.format("InvalidHeaderKS: %x != calculated %x ",
                    Byte.toUnsignedInt(arr[indexKs]), ks));
        }
    }

    static void setControlSum(byte[] byteArray) {
        int ks = CONTROL_SUM_BASE;
        for (int i = DATA.shift; i < byteArray.length; i++)
            ks += Byte.toUnsignedInt(byteArray[i]);
        byteArray[DATA_KS.shift] = (byte) (ks & 0xFF);

        ks = CONTROL_SUM_BASE;
        for (int i = 0; i < HEADER_KS.shift; i++)
            ks += Byte.toUnsignedInt(byteArray[i]);
        byteArray[HEADER_KS.shift] = (byte) (ks & 0xFF);
    }

    public static byte[] wrapToPackage(int detectorId, int time, Command commandCode, byte[] data) {
        byte[] bytes = new byte[DATA.shift + data.length];

        bytes[0] = START_PACKAGE_BYTE;

        byte[] array = ByteBuffer.allocate(4).putInt(detectorId).array();
        bytes[1] = array[3];
        bytes[2] = array[2];
        bytes[3] = array[1];
        bytes[4] = array[0];

        array = ByteBuffer.allocate(4).putInt(time).array();
        bytes[5] = array[3];
        bytes[6] = array[2];
        bytes[7] = array[1];
        bytes[8] = array[0];

        bytes[9] = COMMAND_TYPE.code;

        bytes[10] = commandCode.code;

        array = ByteBuffer.allocate(2).putShort((short) data.length).array();
        bytes[11] = array[1];
        bytes[12] = array[0];

        System.arraycopy(data, 0, bytes, DATA.shift, data.length);

        setControlSum(bytes);

        return bytes;
    }

    public void sendData(SomeCommand command) {
        DatagramPacket packet = new DatagramPacket(command.data, command.data.length, address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        if (socketListener != null) {
            socketListener.interrupt();
            log.info("socketListener is interrupted");
        }

        socket.close();
        log.info("socket is closed");

        log.info("detector transfer is shutdown");
    }
}