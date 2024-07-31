package com.alextim.bee.client.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Locale;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BdpnInternalData extends InternalData {
    public float voltage500V;
    public float voltage2500V;

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Версия: %d, Тип блока: %s, Время измерение: %d, Режим: %s, Текущий счет: %.0f, %.0f %s, Средний счет: %.1f, %.1f %s, Температура: %.1f, Высокое напряжение 500V: %.1f, Высокое напряжение 2500V: %.1f",
                version, bdType.title, measTime, mode.title, currentScores[0], currentScores[1], BdData.unit,  averageScores[0], averageScores[1], BdData.unit, temperature, voltage500V, voltage2500V);
    }
}
