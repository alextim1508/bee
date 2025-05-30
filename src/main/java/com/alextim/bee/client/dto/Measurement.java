package com.alextim.bee.client.dto;

import lombok.Builder;

import java.util.Locale;

import static com.alextim.bee.client.protocol.DetectorCodes.BDType;

@Builder
public class Measurement {
    public final int version;
    public final BDType bdType;
    public final long measTime;
    public final long geoTime;
    public final GeoData geoData;
    public final BdData bdData;

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Версия: " + version + ", " +
                        "Тип блока: " + bdType.title + ", " +
                        "Время измерение: " + measTime + "," +
                        System.lineSeparator() +
                        bdData +
                        System.lineSeparator() +
                        geoData);
    }
}


