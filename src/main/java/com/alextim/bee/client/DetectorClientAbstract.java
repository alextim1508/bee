package com.alextim.bee.client;

import com.alextim.bee.client.messages.DetectorMsg;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.LinkedBlockingQueue;

@AllArgsConstructor
public abstract class DetectorClientAbstract {
    @Getter
    protected final LinkedBlockingQueue<DetectorMsg> queue;

    public abstract void start();
}
