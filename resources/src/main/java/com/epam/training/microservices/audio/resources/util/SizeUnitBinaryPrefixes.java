package com.epam.training.microservices.audio.resources.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum SizeUnitBinaryPrefixes {
    BYTE(1L),
    KB(BYTE.unitBase * 1000),
    MB(KB.unitBase * 1000),
    GB(MB.unitBase * 1000),
    TB(GB.unitBase * 1000),
    PB(TB.unitBase * 1000),
    EB(PB.unitBase * 1000);

    private final Long unitBase;

    SizeUnitBinaryPrefixes(long unitBase) {
        this.unitBase = unitBase;
    }

    public static List<SizeUnitBinaryPrefixes> unitsInDescending() {
        List<SizeUnitBinaryPrefixes> list = Arrays.asList(values());
        Collections.reverse(list);
        return list;
    }

    public Long getUnitBase() {
        return unitBase;
    }
}
