package com.alextim.bee.client.dto;

import lombok.experimental.SuperBuilder;

import java.util.Locale;


@SuperBuilder
public class BdpnInternalData extends InternalData {

    public float voltage500V;
    public float voltage2500V;

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Версия: " + version + ", " +
                        "Тип БД: " + bdType.title + ", " +
                        "Время экспозиции: " + measTime + ", " +
                        "Режим работы БД: " + mode.title + "," +
                        System.lineSeparator() +
                        "Текущие счета счетчиков: " +
                        currentScores[0] + " " + currentScores[1] + " имп/сек, " +
                        "Усредненные за время экспозиции счета счетчиков: " +
                        averageScores[0] + " " + averageScores[1] + " имп/сек," +
                        System.lineSeparator() +
                        "Температура по коду датчика: " + temperature + " °C, " +
                        "Высокое напряжение 500V: " + voltage500V + " В, " +
                        "Высокое напряжение 2500V: " + voltage2500V + " В, " +
                        "Напряжение питания: " + power + " В ");
    }
}
