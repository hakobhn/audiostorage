package com.epam.training.microservices.audio.resources.util;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;

import static com.epam.training.microservices.audio.resources.util.SizeUnitBinaryPrefixes.BYTE;
import static com.epam.training.microservices.audio.resources.util.SizeUnitBinaryPrefixes.EB;
import static com.epam.training.microservices.audio.resources.util.SizeUnitBinaryPrefixes.GB;
import static com.epam.training.microservices.audio.resources.util.SizeUnitBinaryPrefixes.KB;
import static com.epam.training.microservices.audio.resources.util.SizeUnitBinaryPrefixes.MB;
import static com.epam.training.microservices.audio.resources.util.SizeUnitBinaryPrefixes.PB;
import static com.epam.training.microservices.audio.resources.util.SizeUnitBinaryPrefixes.TB;

@UtilityClass
public class DataUtils {

    private static DecimalFormat DEC_FORMAT = new DecimalFormat("#.##");

    public static String toHumanReadableSIPrefixes(long size) {
        if (size < 0)
            throw new IllegalArgumentException("Invalid file size: " + size);
        if (size >= EB.getUnitBase()) return formatSize(size, EB, "EB");
        if (size >= PB.getUnitBase()) return formatSize(size, PB, "PB");
        if (size >= TB.getUnitBase()) return formatSize(size, TB, "TB");
        if (size >= GB.getUnitBase()) return formatSize(size, GB, "GB");
        if (size >= MB.getUnitBase()) return formatSize(size, MB, "MB");
        if (size >= KB.getUnitBase()) return formatSize(size, KB, "KB");
        return formatSize(size, BYTE, "Bytes");
    }

    private static String formatSize(long size, SizeUnitBinaryPrefixes prefix, String unitName) {
        return DEC_FORMAT.format((double) size / prefix.getUnitBase()) + " " + unitName;
    }

}
