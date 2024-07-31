package com.alextim.bee.client.dto;


public class BdpnData extends BdData {
    public final float curDensity;
    public final float aveDensity;

    public final static String title = "ППН";
    public final static String unit = "нейтр./см²сек";

    public final static double coef = 1;

    public BdpnData(float curDensity, float aveDensity, float curScore, float aveScore) {
        super(curScore, aveScore);
        this.curDensity = curDensity;
        this.aveDensity = aveDensity;
    }

    @Override
    public String toString() {
        return String.format("Текущий ППН: %.1f %s Средний ППН: %.1f %s Текущий счет: %.0f %s, Средний счет: %.1f %s",
                curDensity, unit,
                aveDensity, unit,
                currentScore, BdData.unit,
                averageScore, BdData.unit);
    }
}