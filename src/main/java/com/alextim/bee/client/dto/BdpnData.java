package com.alextim.bee.client.dto;


import lombok.Getter;

public class BdpnData extends BdData {
    @Getter
    private final float curDensity;
    @Getter
    private final float aveDensity;

    public final static String title = "ППН";
    public final static String unit = "нейтр./см²сек";


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