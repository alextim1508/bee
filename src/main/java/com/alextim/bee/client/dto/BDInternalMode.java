package com.alextim.bee.client.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BDInternalMode {
    BD_MODE_CONTINUOUS_HIGH_SENS((byte) 0, "непрер-ый (выс. чувств-ть)"),
    BD_MODE_CONTINUOUS_LOW_SENS((byte) 1, "непрер-ый (низк. чувств-ть)"),
    BD_MODE_PULSE((byte) 2, "импульсный");

    public final byte code;
    public final String title;

    public static BDInternalMode getBDInternalModeByCode(byte code) {
        for (BDInternalMode mode : BDInternalMode.values()) {
            if (mode.code == code) {
                return mode;
            }
        }
        throw new RuntimeException("Unknown BDTypeCode " + code);
    }
}