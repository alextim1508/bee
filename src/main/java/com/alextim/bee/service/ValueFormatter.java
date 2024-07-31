package com.alextim.bee.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ValueFormatter {

    public static final Map<Integer,String> prefixes;
    static {
        Map<Integer,String> map = new HashMap<>();
        map.put(0,"");
        map.put(3,"К");
        map.put(6,"М");
        map.put(9,"Г");
        map.put(12,"Т");
        map.put(15,"П");
        map.put(18,"Э");
        map.put(-3,"м");
        map.put(-6,"мк");
        map.put(-9,"н");
        map.put(-12,"п");
        map.put(-15,"ф");
        map.put(-18,"а");
        prefixes = Collections.unmodifiableMap(map);
    }

    private final String unit;
    private final double value;
    private final int significantDigits;

    public ValueFormatter(double value, String unit) {
        this.value = value;
        this.unit = unit;
        this.significantDigits = 3;
    }
    public ValueFormatter(double value, String unit, int significantDigits) {
        this.value = value;
        this.unit = unit;
        this.significantDigits = significantDigits;
    }


    public String toString() {
        if(value <= 0.0 || Double.isInfinite(value)) {
            return value + " "  + unit;
        }

        double val = value;
        int order = 0;

        while(val > 1000.0) {
            val /= 1000.0;
            order += 3;
        }

        while(val < 1.0 && val > 0) {
            val *= 1000.0;
            order -= 3;
        }


        return sigDigRounder(val, significantDigits) + " " + prefixes.get(order) + unit;
    }

    public static double sigDigRounder(double value) {
        return sigDigRounder(value, 3);
    }

    public static double sigDigRounder(double value, int significantDigits) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.round(new MathContext(significantDigits));
        return bd.doubleValue();
    }

    public static double parsingValuePrefix(String prefixValue) {
        if(prefixValue.startsWith("К"))
            return 1_000;
        else if(prefixValue.startsWith("М"))
            return 1_000_000;
        else if(prefixValue.startsWith("Г"))
            return 1_000_000_000;
        else if(prefixValue.startsWith("Т"))
            return 1_000_000_000_000L;
        else if(prefixValue.startsWith("П"))
            return 1_000_000_000_000_000L;
        else if(prefixValue.startsWith("Э"))
            return 1_000_000_000_000_000_000L;
        else if(prefixValue.startsWith("м"))
            return 0.001;
        else if(prefixValue.startsWith("мк"))
            return 0.000_001;
        else if(prefixValue.startsWith("н"))
            return 0.000_000_001;
        else if(prefixValue.startsWith("п"))
            return 0.000_000_000_001;
        else if(prefixValue.startsWith("ф"))
            return 0.000_000_000_000_001;
        else if(prefixValue.startsWith("а"))
            return 0.000_000_000_000_000_001;
        return 1;
    }
}