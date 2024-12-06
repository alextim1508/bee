package com.alextim.bee.client.messages;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ExceptionMessage extends DetectorMsg {

    public Exception exception;

    public ExceptionMessage(int detectorID,
                            long time,
                            Exception exception,
                            byte[] data) {
        super(detectorID, time, data);
        this.exception = exception;
    }

    @Override
    public String toString() {
        return exception.getMessage();
    }
}
