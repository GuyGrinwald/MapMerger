package com.amdocs.mapmerger.core;

import com.amdocs.mapmerger.core.mergebehaviors.MergeBehavior;

import java.util.Map;

public class MapMerger<K, V> {
    private final MergeBehavior<K, V> mergeBehavior;

    public MapMerger(MergeBehavior<K, V> mergeBehavior) {
        this.mergeBehavior = mergeBehavior;
    }

    /**
     * Merges both maps into a new maps. Original maps are altered in the process.
     */
    public Map<K, V> mergeMaps(Map<K, V> map1, Map<K, V> map2) {
        Map<K, V> smallerMap = map1.size() <= map2.size() ? map1 : map2;
        Map<K, V> biggerMap = map1.size() > map2.size() ? map1 : map2;

        smallerMap.entrySet().stream()
                .forEach(entry -> {
                    K key = entry.getKey();
                    V newValue = entry.getValue();

                    if (biggerMap.containsKey(key)) {
                        newValue = mergeBehavior.merge(key, entry.getValue(), biggerMap.get(key));
                    }

                    biggerMap.put(entry.getKey(), newValue);
                });

        return biggerMap;
    }
}
