package com.alextim.bee.client.messages;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@AllArgsConstructor
public abstract class DetectorMsg {
    public final int detectorID;
    public final long time;
    public final byte[] data;
}
