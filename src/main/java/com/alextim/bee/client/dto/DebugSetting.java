package com.alextim.bee.client.dto;

import com.alextim.bee.client.protocol.DetectorCodes.BDInternalMode;
import lombok.Builder;

@Builder
public class DebugSetting {
    public BDInternalMode mode;       // режим работы БД при отладке (enabled=TRUE)
    public long chmQuench;  // длительность сигнала гашения для непрерывного режима высокой чувствительности, мксек
    public long clmQuench;  // длительность сигнала гашения для непрерывного режима низкой чувствительности, мксек
    public long pmInterval; // период сигнала гашения для импульсного режима, мксек
    public long pmQuench;   // длительность сигнала гашения для импульсного режима, мксек
    public long pmHiUp;     // длительность нарастания высокого напряжения после окончания сигнала гашения при котором импульсы еще не регистрируются

    @Override
    public String toString() {
        return "Параметров отладки: " +
                "mode: " + mode +
                ", chmQuench: " + chmQuench +
                ", clmQuench: " + clmQuench +
                ", pmInterval: " + pmInterval +
                ", pmQuench: " + pmQuench +
                ", pmHiUp: " + pmHiUp;
    }
}
