package com.alextim.bee.client.dto;

import com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode;
import com.alextim.bee.client.protocol.DetectorCodes.BDType;
import lombok.experimental.SuperBuilder;


@SuperBuilder
public abstract class InternalData {
    public final int version;
    public final BDType bdType;
    public final long measTime;
    public final BDInternalMode mode;
    public final int reserve;
    public final float[] currentScores;
    public final float[] averageScores;
    public final float temperature;
    public final float power;
}
