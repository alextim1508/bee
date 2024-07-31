package com.alextim.bee.client.dto;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public abstract class BdData {
    public final float currentScore;
    public final float averageScore;
    public final static String unit = "имп/сек";
}