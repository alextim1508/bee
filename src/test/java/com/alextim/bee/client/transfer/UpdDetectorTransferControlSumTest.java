package com.alextim.bee.client.transfer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.alextim.bee.client.protocol.DetectorCodes.Format.*;


public class UpdDetectorTransferControlSumTest {

    @Test
    public void checkControlSumOfGetSensitivityCommand() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xff,
                (byte) 0xa0,
                (byte) 0x0, (byte) 0x0, //len = 0
                (byte) 0x57, //DATA_KS
                (byte) 0xda //HEADER_KS
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 0;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfSetSensitivityCommand() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xff,
                (byte) 0x80,
                (byte) 0x4, (byte) 0x0, //len = 4
                (byte) 0xb8, //DATA_KS
                (byte) 0x1f, //HEADER_KS
                (byte) 0x0, (byte) 0x0, (byte) 0x20, (byte) 0x41 //10.0f
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 4;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfSetSensitivityAnswer() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x80,
                (byte) 0x0,
                (byte) 0x0, (byte) 0x0,
                (byte) 0x57,
                (byte) 0xc5
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 0;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfGetSensitivityAnswer() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xa0,
                (byte) 0x0,
                (byte) 0x4, (byte) 0x0,
                (byte) 0x62,
                (byte) 0xf4,
                (byte) 0x9a, (byte) 0x99, (byte) 0x99, (byte) 0x3f
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 4;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfGetDeadTimeCommand() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xff,
                (byte) 0xa2,
                (byte) 0x0, (byte) 0x0, //len = 0
                (byte) 0x57, //DATA_KS
                (byte) 0xdc //HEADER_KS
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 0;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfSetDeadTimeCommand() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xff,
                (byte) 0x82,
                (byte) 0x4, (byte) 0x0, //len = 4
                (byte) 0xb8, //DATA_KS
                (byte) 0x21, //HEADER_KS
                (byte) 0x0, (byte) 0x0, (byte) 0x20, (byte) 0x41 //10.0f
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 4;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfGetCorCoefTimeCommand() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xff,
                (byte) 0xa1,
                (byte) 0x4, (byte) 0x0, //len = 4
                (byte) 0x58,//DATA_KS
                (byte) 0xe0,  //HEADER_KS
                (byte) 0x1, (byte) 0x0, (byte) 0x0, (byte) 0x0
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 4;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfSetCorCoefTimeCommand() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xff,
                (byte) 0x81,
                (byte) 0x8, (byte) 0x0, //len = 8
                (byte) 0xd, //DATA_KS
                (byte) 0x79, //HEADER_KS
                (byte) 0x1, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x9a, (byte) 0x99, (byte) 0x41, (byte) 0x41
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 8;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfSetMeasTimeTimeCommand() {
        byte[] array = new byte[]{
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

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 4;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfRestartEvent() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x0, // EVENT_TYPE
                (byte) 0x1, //RESTART
                (byte) 0x10, (byte) 0x0, //Len = 16
                (byte) 0xb3,//DATA_KS
                (byte) 0xb2,//HEADER_KS
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, //reason
                (byte) 0x1, (byte) 0x0, (byte) 0x0, (byte) 0x0, //param
                (byte) 0xbc, (byte) 0xbb, (byte) 0x29, (byte) 0xd9, //ip
                (byte) 0xd2, (byte) 0x4, //port
                (byte) 0xc, (byte) 0x0 //exPort

        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 16;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfRestartCommand() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xff,
                (byte) 0x10,
                (byte) 0x0, (byte) 0x0,
                (byte) 0x57,
                (byte) 0x4a
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 0;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfSetIpCommand() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xff,
                (byte) 0xc0,
                (byte) 0x8, (byte) 0x0,
                (byte) 0x8d,
                (byte) 0x38,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0x41, (byte) 0x5,
                (byte) 0xe4, (byte) 0x10

        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 8;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlSumOfMeasDataEvent() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x0, //EVENT_TYPE
                (byte) 0x10, //STATE
                (byte) 0x1f, (byte) 0x0,  //len = 30
                (byte) 0xb, //DATA_KS
                (byte) 0x28, //HEADER_KS
                (byte) 0x4, //MEASUREMENT
                (byte) 0x1, (byte) 0x0,   //structVersion
                (byte) 0x1, (byte) 0x0,// GAMMA
                (byte) 0x5, (byte) 0x0, (byte) 0x0, (byte) 0x0, //measTime
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, //geoTime
                (byte) 0x0, (byte) 0x0, //geoDataSize
                (byte) 0x0, (byte) 0x0, (byte) 0xa0, (byte) 0x40, //curScore
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x3e, // aveScore
                (byte) 0xcd, (byte) 0xcc, (byte) 0x44, (byte) 0x41, //curMeasData
                (byte) 0xa3, (byte) 0x70, (byte) 0x1d, (byte) 0x40 //aveMeasData
        };


        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 31;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));

    }

    @Test
    public void checkControlSumOfInternalDataEvent() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x0, //EVENT_TYPE
                (byte) 0x20, //INTERNAL_DATA
                (byte) 0x24, (byte) 0x0,  //len = 36
                (byte) 0x4c, //DATA_KS
                (byte) 0x7e, //HEADER_KS
                (byte) 0x9, (byte) 0x0, //structVersion
                (byte) 0x1, (byte) 0x0, //BDType
                (byte) 0x5, (byte) 0x0, (byte) 0x0, (byte) 0x0, //measTime
                (byte) 0x0, (byte) 0x0, //BDInternalMode
                (byte) 0x0, (byte) 0x0, //reserve
                (byte) 0x0, (byte) 0x0, (byte) 0x40, (byte) 0x40, //curScores1
                (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x40, //curScores2
                (byte) 0x99, (byte) 0x99, (byte) 0x19, (byte) 0x3f, //aveScores1
                (byte) 0xcc, (byte) 0xcc, (byte) 0xcc, (byte) 0x3e, //aveScores2
                (byte) 0x33, (byte) 0x33, (byte) 0x43, (byte) 0x41, //temperature
                (byte) 0x0, (byte) 0x0, (byte) 0xcd, (byte) 0x43 //V400
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 36;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));

    }

    @Test
    public void checkControlSumOfAccumulationEvent() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x0, //EVENT_TYPE
                (byte) 0x10, //STATE
                (byte) 0x9, (byte) 0x0, //len = 9;
                (byte) 0x65,//DATA_KS
                (byte) 0x6c,//HEADER_KS
                (byte) 0x3,
                (byte) 0x1, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,

        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 9;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlComeCommand() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x64, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0xFF,
                (byte) 0x20,
                (byte) 0x04, (byte) 0x00,
                (byte) 0x5B,
                (byte) 0xC6,
                (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 4;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

    @Test
    public void checkControlComeCommand2() {
        byte[] array = new byte[]{
                (byte) 0x79,
                (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                (byte) 0x67, (byte) 0x2b, (byte) 0x0, (byte) 0x0,
                (byte) 0x20,
                (byte) 0x0,
                (byte) 0x0, (byte) 0x0,
                (byte) 0x57,
                (byte) 0xed
        };

        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlHeaderCheck(array, 0, HEADER_KS.shift, HEADER_KS.shift));

        int len = 0;
        Assertions.assertDoesNotThrow(() ->
                UpdDetectorTransfer.controlBodyCheck(array, DATA.shift, DATA.shift + len, DATA_KS.shift));
    }

}
