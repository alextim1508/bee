package com.alextim.bee.client.messages;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class DetectorMsg {
    public final int detectorID;
    public final long time;
    public final byte[] data;
}
