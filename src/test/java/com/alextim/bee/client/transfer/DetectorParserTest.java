package com.alextim.bee.client.transfer;

import com.alextim.bee.client.protocol.DetectorCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.alextim.bee.client.messages.DetectorEvents.ErrorDetectorState;
import static com.alextim.bee.client.messages.DetectorEvents.SomeEvent;
import static com.alextim.bee.client.transfer.DetectorParser.getErrorDetectorState;

public class DetectorParserTest {

    @Test
    public void getErrorDetectorStateTest() {
        SomeEvent someEvent = new SomeEvent(
                305419896, 10, DetectorCodes.Event.STATE,
                new byte[]{
                        (byte) 0x79,
                        (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
                        (byte) 0xa, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                        (byte) 0x0, //EVENT_TYPE
                        (byte) 0x10, //STATE
                        (byte) 0x2, (byte) 0x0, //Len = 2
                        (byte) 0x5a, //DATA_KS
                        (byte) 0x5a, //HEADER_KS
                        (byte) 0x2, //ERROR
                        (byte) 0x1 //er
                }
        );

        ErrorDetectorState expectedMsg = new ErrorDetectorState(
                DetectorCodes.Error.NO_MEMORY, someEvent
        );

        Assertions.assertEquals(expectedMsg, getErrorDetectorState(someEvent));
    }
}
