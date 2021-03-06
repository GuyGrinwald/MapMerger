package com.amdocs.mapmerger.core;

import com.amdocs.mapmerger.core.mergebehaviors.AddingMerge;
import com.amdocs.mapmerger.core.mergebehaviors.MergeBehavior;
import com.amdocs.mapmerger.core.mergebehaviors.MultiplyingMerge;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.*;

public class MapMergerTest {
    Map<String, Integer> map1 = new HashMap();
    Map<String, Integer> map2 = new HashMap();
    Map<String, Integer> map3 = new HashMap();

    @Before
    public void setUp() {
        map1.put("key1", 20);
        map1.put("key2", 30);
        map2.put("key3", 40);
        map2.put("key1", 50);
        map3.put("key4", 60);
        map3.put("key1", 70);
    }

    @Test
    public void mergeMapsByAdding() {
        MergeBehavior addingMerge = new AddingMerge();
        MapMerger<String, Integer> merger = new MapMerger<>(addingMerge);

        Map<String, Integer> result = merger.mergeMaps(map1, map2);
        assertTrue(result.containsKey("key1"));
        assertEquals(70, result.get("key1"), 0);

        Map<String, Integer> result2 = merger.mergeMaps(map1, new HashMap<>());
        assertTrue(result2.containsKey("key1"));
        assertEquals(20, result2.get("key1"), 0);
    }

    @Test
    public void mergeMapsByMultiplying() {
        MergeBehavior multiplyingMerge = new MultiplyingMerge();
        MapMerger<String, Integer> merger = new MapMerger<>(multiplyingMerge);

        Map<String, Integer> result = merger.mergeMaps(map1, map2);
        assertTrue(result.containsKey("key1"));
        assertEquals(1000, result.get("key1"), 0);

        Map<String, Integer> result2 = merger.mergeMaps(map1, new HashMap<>());
        assertTrue(result2.containsKey("key1"));
        assertEquals(20, result2.get("key1"), 0);
    }

    @Test
    public void mergeMapListByAdding() {
        MergeBehavior addingMerge = new AddingMerge();
        MapMerger<String, Integer> merger = new MapMerger<>(addingMerge);

        List<Map<String, Integer>> maps = Arrays.asList(map1, map2, map3);
        Map<String, Integer> result = merger.mergeMaps(maps);
        assertTrue(result.containsKey("key1"));
        assertEquals(140, result.get("key1"), 0);

        List<Map<String, Integer>> maps2 = Arrays.asList(map1, map2, new HashMap<>());
        Map<String, Integer> result2 = merger.mergeMaps(maps2);
        assertTrue(result2.containsKey("key1"));
        assertEquals(70, result2.get("key1"), 0);
    }

    @Test
    public void mergeMapListByMultiplying() {
        MergeBehavior multiplyingMerge = new MultiplyingMerge();
        MapMerger<String, Integer> merger = new MapMerger<>(multiplyingMerge);

        List<Map<String, Integer>> maps = Arrays.asList(map1, map2, map3);
        Map<String, Integer> result = merger.mergeMaps(maps);
        assertTrue(result.containsKey("key1"));
        assertEquals(70000, result.get("key1"), 0);

        List<Map<String, Integer>> maps2 = Arrays.asList(map1, map2, new HashMap<>());
        Map<String, Integer> result2 = merger.mergeMaps(maps2);
        assertTrue(result2.containsKey("key1"));
        assertEquals(1000, result2.get("key1"), 0);
    }
}