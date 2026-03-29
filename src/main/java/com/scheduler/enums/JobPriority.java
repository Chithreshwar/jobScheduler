package com.scheduler.enums;

/**
 * Relative importance for scheduling order among due jobs (HIGH, MEDIUM, LOW).
 * <p>
 * Declared LOW → MEDIUM → HIGH so ordinal order matches {@code ORDER BY priority DESC}
 * (HIGH first, then MEDIUM, then LOW) with Spring Data.
 */
public enum JobPriority {
    LOW,
    MEDIUM,
    HIGH
}
