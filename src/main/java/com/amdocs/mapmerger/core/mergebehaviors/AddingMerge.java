package com.amdocs.mapmerger.core.mergebehaviors;

/**
 * A merging behavior implementation that adds up the two original values
 */
public class AddingMerge implements MergeBehavior<String, Integer> {
    @Override
    public Integer merge(Integer value1, Integer value2) {
        return value1 + value2;
    }
}
