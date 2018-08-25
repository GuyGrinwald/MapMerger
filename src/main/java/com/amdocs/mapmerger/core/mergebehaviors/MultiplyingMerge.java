package com.amdocs.mapmerger.core.mergebehaviors;

/**
 * A merging behavior implementation that multiplies up the two original values
 */
public class MultiplyingMerge implements MergeBehavior<String, Integer> {
    @Override
    public Integer merge(String key, Integer value1, Integer value2) {
        return value1 * value2;
    }
}
