package com.alextim.bee.client;

import com.alextim.bee.client.messages.DetectorMsg;
import lombok.AllArgsConstructor;

import java.util.concurrent.LinkedBlockingQueue;

@AllArgsConstructor
public abstract class DetectorClientAbstract {

    protected final LinkedBlockingQueue<DetectorMsg> queue;

    public abstract void connect();

    public DetectorMsg waitAndGetDetectorMsg() throws InterruptedException {
        return queue.take();
    }

    public void clear() {
        queue.clear();
    }
}
