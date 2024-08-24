package com.alextim.bee.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Property {
    public static String TITLE_APP;
    public static String SOFTWARE_VERSION;
    public static String FRONTEND_FOR_DETECTOR;
    public static Integer TRANSFER_TO_DETECTOR_ID;
    public static String TRANSFER_IP;
    public static Integer TRANSFER_PORT;
    public static Integer TRANSFER_RCV_BUFFER_SIZE;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("H:mm:ss:SSS");
}
