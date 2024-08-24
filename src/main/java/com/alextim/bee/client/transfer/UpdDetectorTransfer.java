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

                } catch (SocketException e) {
                    log.error("NEED TO RECONNECT", e);

                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException ex) {
                        log.error("InterruptedException");
                        return;
                    }
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
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
            int size = 0;
            int start = 0;

            if(Thread.currentThread().isInterrupted()) {
                return;
            }

            try {
                if (rcvInd >= rcvBuf.length)
                    throw new RuntimeException("OverFlow " + rcvInd + " " + rcvBuf.length);

                log.info("==========================");
                log.info("rcv index before receiving: {}", rcvInd);
                log.info("available rcv buffer size before receiving: {}", rcvBuf.length - rcvInd);

                DatagramPacket packet = new DatagramPacket(rcvBuf, rcvInd, rcvBuf.length - rcvInd, address, port);


                socket.receive(packet);
                System.out.println("BUFFER");
                for (int i = 0; i < rcvBuf.length; i++) {
                    System.out.printf("%x ", rcvBuf[i]);
                }
                System.out.println();


                System.out.println("-------------------------------NEW MESSAGE");

                size += packet.getLength();
                log.info("received bytes size: {}", size);

            } catch (SocketException e) {
                return;
            } catch (IOException e) {
                log.error("Socket error", e);
                throw new RuntimeException(e);
            }

            if (size > 0) {
                rcvInd += size;
                log.info("rcvInd after receiving: {}", rcvInd);

                while (start < rcvInd) {

                    while (start < rcvInd) {
                        if (rcvBuf[start] == START_PACKAGE_BYTE)
                            break;
                        else
                            start++;
                    }

                    if (socketListener.isInterrupted()) {
                        log.info("socketListener is interrupted");
                        return;
                    }

                    log.info("------------------- found start: {}", start);
                    if (start != 0) {
                        System.arraycopy(rcvBuf, start, rcvBuf, 0, rcvInd - start);
                        rcvInd = rcvInd - start;
                        start = 0;
                    }
                    log.info("rcv index after shift: {}", rcvInd);

                    int len = Short.toUnsignedInt(
                            ByteBuffer.wrap(new byte[]{rcvBuf[LEN.shift+ 1], rcvBuf[LEN.shift ]}).getShort());
                    log.info("len: {}", len);

//                    System.out.println("rcvInd = " + rcvInd);
//                    System.out.println("DATA.shift + len = " + (DATA.shift + len));
//                    if (rcvInd < DATA.shift + len) {
//                        System.out.println("ER2");
//                        log.info("break");
//                        break;
//                    }

                    for (int i = 0; i < DATA.shift + len; i++) {
                        System.out.printf("%x ", rcvBuf[i]);
                    }
                    System.out.println();

                    try {
                        controlHeaderCheck();
                    } catch (Exception e) {
                        log.error("", e);
                    }

                    try {
                        controlBodyCheck(len);
                    } catch (Exception e) {
                        log.error("", e);
                    }

                    byte[] data = new byte[DATA.shift + len];
                    System.arraycopy(rcvBuf, start, data, 0, data.length);

                    if (consumer != null) {
                        consumer.accept(data);
                        log.info("payload data is handled");
                    }

                    start = DATA.shift + len;
                    log.info("start after one handling: {}", start);
                }

                if (rcvInd != start) {
                    System.arraycopy(rcvBuf, start, rcvBuf, 0, rcvInd - start);
                }

                rcvInd = rcvInd - start;
            } else {
                log.error("Socket error. Size: {}", size);

                throw new RuntimeException("Socket error. Size:" + size);
            }
        }
    }

    protected void controlBodyCheck(int len) {
        System.out.println("controlBodyCheck");
        byte ks = CONTROL_SUM_BASE;
        for (int j = DATA.shift; j < DATA.shift + len + 1; j++) {
            System.out.printf("%x ", rcvBuf[j]);
            ks += rcvBuf[j];
        }
        System.out.println();
        System.out.printf("ks = %x\n", ks);

        if (ks != rcvBuf[DATA_KS.shift]) {
            throw new RuntimeException(rcvBuf[DATA_KS.shift] + " " + ks + " InvalidDataKS");
        }
    }

    protected void controlHeaderCheck() {
        System.out.println("controlHeaderCheck");
        byte ks = CONTROL_SUM_BASE;
        for (int j = 0; j < HEADER_KS.shift; j++) {
            System.out.printf("%x ", rcvBuf[j]);
            ks += rcvBuf[j];
        }
        System.out.println();
        System.out.printf("ks = %x\n", ks);

        if (ks != rcvBuf[HEADER_KS.shift]) {
            throw new RuntimeException(rcvBuf[HEADER_KS.shift] + " " + ks + " InvalidHeaderKS");
        }
    }

    protected static void setControlSum(byte[] byteArray) {
        byte ks = CONTROL_SUM_BASE;
        for (int i = DATA.shift; i < byteArray.length; i++)
            ks += byteArray[i];
        byteArray[DATA_KS.shift] = ks;

        ks = CONTROL_SUM_BASE;
        for (int i = 0; i < HEADER_KS.shift; i++)
            ks += byteArray[i];
        byteArray[HEADER_KS.shift] = ks;
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
        if(socketListener != null) {
            socketListener.interrupt();
            log.info("socketListener is interrupted");
        }

        socket.close();
        log.info("socket is closed");

        log.info("detector transfer is shutdown");
    }
}