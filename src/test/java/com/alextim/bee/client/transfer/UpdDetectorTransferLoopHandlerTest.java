package com.alextim.bee.client.transfer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class UpdDetectorTransferLoopHandlerTest {

    @Test
    public void handleOneFullPacketTest() {
        byte[] arr = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xa0, (byte) 0x0,
                (byte) 0x4, (byte) 0x0,
                (byte) 0x62, (byte) 0xf4,
                (byte) 0x9a, (byte) 0x99, (byte) 0x99, (byte) 0x3f
        };

        AtomicInteger handledPacketCount = new AtomicInteger();
        int rcvInd = UpdDetectorTransfer.handle(arr, arr.length, bytes -> {
            handledPacketCount.incrementAndGet();
        });

        Assertions.assertEquals(1, handledPacketCount.get());
        Assertions.assertEquals(0, rcvInd);
    }

    @Test
    public void handleOneFullPacketWithHeadNextPacketTestTest() {
        byte firstByteNextPacket = (byte) 0x79;
        byte secondByteNextPacket = (byte) 0x78;
        byte thirdByteNextPacket = (byte) 0xf1;
        byte[] arr = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xa0, (byte) 0x0,
                (byte) 0x4, (byte) 0x0,
                (byte) 0x62, (byte) 0xf4,
                (byte) 0x9a, (byte) 0x99, (byte) 0x99, (byte) 0x3f,
                /* next packet */
                firstByteNextPacket,
                secondByteNextPacket, (byte) 0xf1
        };

        AtomicInteger handledPacketCount = new AtomicInteger();
        int rcvInd = UpdDetectorTransfer.handle(arr, arr.length, bytes -> {
            handledPacketCount.incrementAndGet();
        });

        Assertions.assertEquals(1, handledPacketCount.get());
        Assertions.assertEquals(3, rcvInd);
        Assertions.assertEquals(firstByteNextPacket, arr[0]);
        Assertions.assertEquals(secondByteNextPacket, arr[1]);
        Assertions.assertEquals(thirdByteNextPacket, arr[2]);
    }

    @Test
    public void handleOneFullPacketWithTallPrevPacketTestTest() {
        byte[] arr = new byte[]{
                /* tall prev packet */
                (byte) 0x56, (byte) 0x34, (byte) 0x12,
                /* packet */
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xa0, (byte) 0x0,
                (byte) 0x4, (byte) 0x0,
                (byte) 0x62, (byte) 0xf4,
                (byte) 0x9a, (byte) 0x99, (byte) 0x99, (byte) 0x3f
        };

        AtomicInteger handledPacketCount = new AtomicInteger();
        int rcvInd = UpdDetectorTransfer.handle(arr, arr.length, bytes -> {
            handledPacketCount.incrementAndGet();
        });

        Assertions.assertEquals(1, handledPacketCount.get());
        Assertions.assertEquals(0, rcvInd);
    }

    @Test
    public void handleTwoFullPacketsTest() {
        byte[] arr = new byte[]{
                /*first packet*/
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xff,
                (byte) 0x82,
                (byte) 0x4, (byte) 0x0, //len = 4
                (byte) 0xb8, //DATA_KS
                (byte) 0x21, //HEADER_KS
                (byte) 0x0, (byte) 0x0, (byte) 0x20, (byte) 0x41,
                /*second packet*/
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xff,
                (byte) 0x20,
                (byte) 0x4, (byte) 0x0,
                (byte) 0x61,
                (byte) 0x68,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0
        };

        AtomicInteger handledPacketCount = new AtomicInteger();
        int rcvInd = UpdDetectorTransfer.handle(arr, arr.length, bytes -> {
            handledPacketCount.incrementAndGet();
        });

        Assertions.assertEquals(2, handledPacketCount.get());
        Assertions.assertEquals(0, rcvInd);
    }

    @Test
    public void handleOneFullAndOneIncorrectPacketsTest() {
        byte[] arr = new byte[]{
                /*first packet*/
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x00, //incorrect
                (byte) 0x82,
                (byte) 0x4, (byte) 0x0, //len = 4
                (byte) 0xb8, //DATA_KS
                (byte) 0x21, //HEADER_KS
                (byte) 0x0, (byte) 0x0, (byte) 0x20, (byte) 0x41,
                /*second packet*/
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xff,
                (byte) 0x20,
                (byte) 0x4, (byte) 0x0,
                (byte) 0x61,
                (byte) 0x68,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0
        };

        AtomicInteger handledPacketCount = new AtomicInteger();
        int rcvInd = UpdDetectorTransfer.handle(arr, arr.length, bytes -> {
            handledPacketCount.incrementAndGet();
        });

        Assertions.assertEquals(1, handledPacketCount.get());
        Assertions.assertEquals(0, rcvInd);
    }

    @Test
    public void dontHandleNotFullPacketsTest() {
        byte[] arr = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xa0
        };

        AtomicInteger handledPacketCount = new AtomicInteger();
        int rcvInd = UpdDetectorTransfer.handle(arr, arr.length, bytes -> {
            handledPacketCount.incrementAndGet();
        });

        Assertions.assertEquals(0, handledPacketCount.get());
        Assertions.assertEquals(arr.length, rcvInd);
    }

    private static void printHex(byte[] arr) {
        for (byte b : arr)
            System.out.printf("%x ", b);
        System.out.println();
    }
}
