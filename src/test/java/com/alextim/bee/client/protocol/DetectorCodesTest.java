package com.alextim.bee.client.protocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.alextim.bee.client.protocol.DetectorCodes.AttentionFlag.*;

public class DetectorCodesTest {

    @Test
    public void getPowerLowAttentionFlagByCodeTest() {
        Assertions.assertEquals(Set.of(POWER_LOW), getAttentionFlagByCode((byte) 1));
    }

    @Test
    public void getPowerHighAttentionFlagByCodeTest() {
        Assertions.assertEquals(Set.of(POWER_HIGH), getAttentionFlagByCode((byte) 2));
    }

    @Test
    public void getTemperatureHighAttentionFlagByCodeTest() {
        Assertions.assertEquals(Set.of(TEMPERATURE_HIGH), getAttentionFlagByCode((byte) 32));
    }

    @Test
    public void getTemperatureHighAndPowerLowAttentionFlagsByCodeTest() {
        Assertions.assertEquals(Set.of(POWER_LOW, TEMPERATURE_HIGH), getAttentionFlagByCode((byte) 33));
    }

}
