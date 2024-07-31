package com.alextim.bee.client.dto;

import java.util.Locale;

public class BdmgData extends BdData {
    private final float currentMED;
    private final float averageMED;

    public final static String title = "МАЭД";
    public final static String unit = "Зв/час";

    public float getCurrentMED() {
        return 0.001f * currentMED;
    }

    public float getAverageMED() {
        return 0.001f * averageMED;
    }

    public BdmgData(float currentMED, float averageMED, float curScore, float aveScore) {
        super(curScore, aveScore);
        this.currentMED = currentMED;
        this.averageMED = averageMED;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Текущая МЭД: %.1f %s Средняя МЭД: %.1f %s Текущий счет: %.0f %s, Средний счет: %.1f %s",
                currentMED, "м" + unit,
                averageMED, "м" + unit,
                currentScore, BdData.unit,
                averageScore, BdData.unit);
    }
}
