package com.alextim.bee.client.dto;

import java.util.Locale;

public class BdmgData extends BdData {
    public final float currentMED;
    public final float averageMED;

    public final static String title = "МЭД";
    public final static String unit = "Зв/час";
    public final static double coef = 0.001;

    public BdmgData(float currentMED, float averageMED, float curScore, float aveScore) {
        super(curScore, aveScore);
        this.currentMED = currentMED;
        this.averageMED = averageMED;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,"Текущая МЭД: %.1f %s Средняя МЭД: %.1f %s Текущий счет: %.0f %s, Средний счет: %.1f %s",
                currentMED, unit,
                averageMED, unit,
                currentScore, BdData.unit,
                averageScore, BdData.unit);
    }
}
