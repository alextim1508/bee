package com.alextim.bee.client.dto;

import lombok.experimental.SuperBuilder;

import java.util.Locale;

import static com.alextim.bee.context.Property.COUNTER_NUMBER_FORMAT;
import static com.alextim.bee.context.Property.OTHER_NUMBER_FORMAT;


@SuperBuilder
public class BdpnInternalData extends InternalData {

    public float voltage500V;
    public float voltage2500V;

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Версия: %d, " +
                        "Тип БД: %s, " +
                        "Время экспозиции: %d, " +
                        "Режим работы БД: %s, " +
                        System.lineSeparator() +
                        "Текущие счета счетчиков: " +
                        COUNTER_NUMBER_FORMAT + ", " + COUNTER_NUMBER_FORMAT + " имп/сек, " +
                        "Усредненные за время экспозиции счета счетчиков: " +
                        COUNTER_NUMBER_FORMAT + ", " + COUNTER_NUMBER_FORMAT + " имп/сек, " +
                        System.lineSeparator() +
                        "Температура по коду датчика: " + OTHER_NUMBER_FORMAT + " °C, " +
                        "Высокое напряжение 500V: " + OTHER_NUMBER_FORMAT + " В, " +
                        "Высокое напряжение 2500V: " + OTHER_NUMBER_FORMAT + " В, " +
                        "Напряжение питания: " + OTHER_NUMBER_FORMAT + " В ",
                version,
                bdType.title,
                measTime,
                mode.title,
                currentScores[0], currentScores[1],
                averageScores[0], averageScores[1],
                temperature, voltage500V, voltage2500V, power);
    }
}
