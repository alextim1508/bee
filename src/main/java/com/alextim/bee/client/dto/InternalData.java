package com.alextim.bee.client.dto;

import com.alextim.bee.client.protocol.DetectorCodes;
import com.alextim.bee.client.protocol.DetectorCodes.BDType;
import lombok.experimental.SuperBuilder;


@SuperBuilder
public abstract class InternalData {
    public final int version;
    public final BDType bdType;
    public final long measTime;
    public final DetectorCodes.BDInternalMode mode;
    public final int reserve;
    public final float[] currentScores;
    public final float[] averageScores;
    public final float temperature;
}
