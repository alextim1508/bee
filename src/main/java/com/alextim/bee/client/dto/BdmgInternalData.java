package com.alextim.bee.client.dto;


import lombok.experimental.SuperBuilder;

import java.util.Locale;

import static com.alextim.bee.context.Property.COUNTER_NUMBER_FORMAT;
import static com.alextim.bee.context.Property.OTHER_NUMBER_FORMAT;

@SuperBuilder
public class BdmgInternalData extends InternalData {

    public final float voltage400V;

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Версия: %d, " +
                        "Тип БД: %s, " +
                        "Время экспозиции: %d, " +
                        "Режим работы БД: %s, " +
                        System.lineSeparator() +
                        "Текущие счета счетчиков: " + COUNTER_NUMBER_FORMAT + ", " + COUNTER_NUMBER_FORMAT + " имп/сек, " +
                        "Усредненные за время экспозиции счета счетчиков: " + COUNTER_NUMBER_FORMAT + ", " + COUNTER_NUMBER_FORMAT + " имп/сек, " +
                        System.lineSeparator() +
                        "Температура по коду датчика: " + OTHER_NUMBER_FORMAT + " °C, " +
                        "Высокое напряжение 400V:  " + OTHER_NUMBER_FORMAT,
                version,
                bdType.title,
                measTime,
                mode.title,
                currentScores[0], currentScores[1],
                averageScores[0], averageScores[1],
                temperature,
                voltage400V);
    }
}


