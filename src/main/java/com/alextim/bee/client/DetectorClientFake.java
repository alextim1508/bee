package com.alextim.bee.client;

import com.alextim.bee.client.dto.BDInternalMode;
import com.alextim.bee.client.messages.DetectorEvents.SomeEvent;
import com.alextim.bee.client.messages.DetectorMsg;
import com.alextim.bee.client.protocol.DetectorCodes;
import com.alextim.bee.client.protocol.DetectorCodes.BDType;
import com.alextim.bee.client.protocol.DetectorCodes.State;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import static com.alextim.bee.client.protocol.DetectorCodes.BDType.*;
import static com.alextim.bee.client.protocol.DetectorCodes.Event.INTERNAL_DATA;
import static com.alextim.bee.client.protocol.DetectorCodes.Event.STATE;
import static com.alextim.bee.client.protocol.DetectorCodes.MsgType.EVENT_TYPE;
import static com.alextim.bee.client.protocol.DetectorCodes.State.INITIALIZATION;

@Slf4j
public class DetectorClientFake extends DetectorClientAbstract {

    private final Random random = new Random();

    public DetectorClientFake(LinkedBlockingQueue<DetectorMsg> queue) {
        super(queue);
    }

    private final int detectorId = 123;
    private int measTime = 1;
    private int geoTime;
    private int detectorTime;

    float curScore, aveScore, curMeasData, aveMeasData;
    float curScores1, curScores2, aveScores1, aveScores2;

    @SneakyThrows
    @Override
    public void start() {
        log.info("=========== DetectorClientFake start ===========");

        queue.add(new SomeEvent(detectorId, detectorTime, STATE, getInitializationBytes()));
        detectorTime += 110;

        new Timer("", true).schedule(new TimerTask() {
            @SneakyThrows
            @Override
            public void run() {

                curScores1 = random.nextInt(3);
                curScores2 = random.nextInt(3);

                float coff = 1 - 1.0f / measTime;
                aveScores1 = coff * aveScores1 + (1 - coff) * curScores1;
                aveScores2 = coff * aveScores2 + (1 - coff) * curScores2;

                curScore = curScores1 + curScores2;
                aveScore = (aveScores1 + aveScores2) / 2f;

                curMeasData = random.nextFloat();
                aveMeasData = 0;

                byte[] bytes = getMeasDataBytes(STATE, State.MEASUREMENT, GAMMA,
                        curScore, aveScore, curMeasData, aveMeasData);
                queue.add(new SomeEvent(detectorId, detectorTime, STATE, bytes));

                detectorTime += 120;

                bytes = getInternalDataBytes(INTERNAL_DATA, GAMMA,
                        curScores1, curScores2, aveScores1, aveScores2);
                queue.add(new SomeEvent(detectorId, detectorTime, INTERNAL_DATA, bytes));

                detectorTime += 110;

                geoTime += 150;
                measTime++;
            }
        }, 0, 1000);
    }

    private byte[] getInitializationBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.write(0x79);
        byte[] arrayID = ByteBuffer.allocate(4).putInt(detectorId).array();
        outputStream.write(new byte[]{arrayID[3], arrayID[2], arrayID[1], arrayID[0]});//ID
        byte[] arrayTime = ByteBuffer.allocate(4).putInt(detectorTime).array();
        outputStream.write(new byte[]{arrayTime[3], arrayTime[2], arrayTime[1], arrayTime[0]});//time

        outputStream.write(EVENT_TYPE.code); //Event type
        outputStream.write(STATE.code);
        outputStream.write(new byte[]{0x00, 0x00});//Len
        outputStream.write(0x00);//DATA_KS
        outputStream.write(0x00);//HEADER_KS

        outputStream.write(INITIALIZATION.code);

        return outputStream.toByteArray();
    }

    private byte[] getMeasDataBytes(DetectorCodes.Event event,
                                    State state,
                                    BDType bdType,
                                    float curScore,
                                    float aveScore,
                                    float curMeasData,
                                    float aveMeasData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.write(0x79);
        byte[] arrayID = ByteBuffer.allocate(4).putInt(detectorId).array();
        outputStream.write(new byte[]{arrayID[3], arrayID[2], arrayID[1], arrayID[0]});//ID
        byte[] arrayTime = ByteBuffer.allocate(4).putInt(detectorTime).array();
        outputStream.write(new byte[]{arrayTime[3], arrayTime[2], arrayTime[1], arrayTime[0]});//time

        outputStream.write(EVENT_TYPE.code); //Event type
        outputStream.write(event.code);
        outputStream.write(new byte[]{0x00, 0x00});//Len
        outputStream.write(0x00);//DATA_KS
        outputStream.write(0x00);//HEADER_KS
        /*---- DATA --*/
        outputStream.write(state.code);
        outputStream.write(new byte[]{0x02, 0x01}); //structVersion
        outputStream.write(new byte[]{bdType.code, 0x00}); //BDType

        byte[] arrayMeasTime = ByteBuffer.allocate(4).putInt(measTime).array();
        outputStream.write(new byte[]{arrayMeasTime[3], arrayMeasTime[2], arrayMeasTime[1], arrayMeasTime[0]}); //measTime

        byte[] arrayGeoTime = ByteBuffer.allocate(4).putInt(geoTime).array();
        outputStream.write(new byte[]{arrayGeoTime[3], arrayGeoTime[2], arrayGeoTime[1], arrayGeoTime[0]}); //geoTime

        outputStream.write(new byte[]{0x00, 0x00}); //geoDataSize


        byte[] arrayScores = ByteBuffer.allocate(4).putFloat(curScore).array();
        outputStream.write(new byte[]{arrayScores[3], arrayScores[2], arrayScores[1], arrayScores[0]}); //curScore
        arrayScores = ByteBuffer.allocate(4).putFloat(aveScore).array();
        outputStream.write(new byte[]{arrayScores[3], arrayScores[2], arrayScores[1], arrayScores[0]}); //aveScore
        arrayScores = ByteBuffer.allocate(4).putFloat(curMeasData).array();
        outputStream.write(new byte[]{arrayScores[3], arrayScores[2], arrayScores[1], arrayScores[0]}); //curMeasData
        arrayScores = ByteBuffer.allocate(4).putFloat(aveMeasData).array();
        outputStream.write(new byte[]{arrayScores[3], arrayScores[2], arrayScores[1], arrayScores[0]}); //aveMeasData

        return outputStream.toByteArray();
    }

    private byte[] getInternalDataBytes(DetectorCodes.Event event,
                                        BDType bdType,
                                        float curScores1,
                                        float curScores2,
                                        float aveScores1,
                                        float aveScores2) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.write(0x79);
        byte[] arrayID = ByteBuffer.allocate(4).putInt(detectorId).array();
        outputStream.write(new byte[]{arrayID[3], arrayID[2], arrayID[1], arrayID[0]});//ID
        byte[] arrayTime = ByteBuffer.allocate(4).putInt(detectorTime).array();
        outputStream.write(new byte[]{arrayTime[3], arrayTime[2], arrayTime[1], arrayTime[0]});//time

        outputStream.write(EVENT_TYPE.code); //Event type
        outputStream.write(event.code);
        outputStream.write(new byte[]{0x00, 0x00});//Len
        outputStream.write(0x00);//DATA_KS
        outputStream.write(0x00);//HEADER_KS
        /*---- DATA --*/
        outputStream.write(new byte[]{0x02, 0x01}); //structVersion
        outputStream.write(new byte[]{bdType.code, 0x00}); //BDType

        byte[] arrayMeasTime = ByteBuffer.allocate(4).putInt(measTime).array();
        outputStream.write(new byte[]{arrayMeasTime[3], arrayMeasTime[2], arrayMeasTime[1], arrayMeasTime[0]}); //measTime

        outputStream.write(new byte[]{BDInternalMode.BD_MODE_PULSE.code, 0x00}); //BDType

        outputStream.write(new byte[]{0x00, 0x00});//reserve

        byte[] arrayScores = ByteBuffer.allocate(4).putFloat(curScores1).array();
        outputStream.write(new byte[]{arrayScores[3], arrayScores[2], arrayScores[1], arrayScores[0]}); //curScores1
        arrayScores = ByteBuffer.allocate(4).putFloat(curScores2).array();
        outputStream.write(new byte[]{arrayScores[3], arrayScores[2], arrayScores[1], arrayScores[0]}); //curScores2
        arrayScores = ByteBuffer.allocate(4).putFloat(aveScores1).array();
        outputStream.write(new byte[]{arrayScores[3], arrayScores[2], arrayScores[1], arrayScores[0]}); //aveScores1
        arrayScores = ByteBuffer.allocate(4).putFloat(aveScores2).array();
        outputStream.write(new byte[]{arrayScores[3], arrayScores[2], arrayScores[1], arrayScores[0]}); //aveScores1
        arrayScores = ByteBuffer.allocate(4).putFloat(8).array();
        outputStream.write(new byte[]{arrayScores[3], arrayScores[2], arrayScores[1], arrayScores[0]}); //temperature
        arrayScores = ByteBuffer.allocate(4).putFloat(9).array();
        outputStream.write(new byte[]{arrayScores[3], arrayScores[2], arrayScores[1], arrayScores[0]}); //voltage400V

        return outputStream.toByteArray();
    }
}
