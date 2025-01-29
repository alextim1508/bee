package com.alextim.bee.client.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@Getter
public abstract class BdData {

    protected float currentScore;       //Текущий счет, имп/сек
    protected float averageScore;       //Усредненный счет за время экспозиции, имп/сек
    protected long accumulatedTime;     //Интервал времени после запуска режима накопления, сек

    public abstract float getCurrentMeasData();

    public abstract float getAverageMeasData();

    public abstract float getAccumulatedMeasData();

    public abstract float getAccumulatedPowerMeasData();

    public abstract String getTitle();

    public abstract String getMeasDataUnit();
}