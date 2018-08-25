package com.amdocs.mapmerger.core;

import com.amdocs.mapmerger.core.mergebehaviors.MergeBehavior;
import com.amdocs.mapmerger.core.utils.MapUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapMerger<K, V> {
    private final MergeBehavior<K, V> mergeBehavior;

    public MapMerger(MergeBehavior<K, V> mergeBehavior) {
        this.mergeBehavior = mergeBehavior;
    }

    /**
     * Merges both maps into a new maps. Original maps are not guaranteed to be preserved.
     */
    public Map<K, V> mergeMaps(Map<K, V> map1, Map<K, V> map2) {
        DCMapMerger mergeTask = new DCMapMerger(null, mergeBehavior);
        return mergeTask.mergeMaps(map1, map2);
    }

    /**
     * Merges the maps in the list to a single map. Original maps are not guaranteed to be preserved.
     */
    public Map<K, V> mergeMaps(List<Map<K, V>> maps) {
        DCMapMerger mergeTask = new DCMapMerger(maps, mergeBehavior);
        ForkJoinPool forkJoinPool = new ForkJoinPool(ForkJoinPool.getCommonPoolParallelism());
        return (Map<K, V>) forkJoinPool.invoke(mergeTask);
    }

    /**
     * Merges both maps into a new maps. Original maps are preserved. This methods performs well for small maps.
     * It's up to the caller to measure it's performance.
     */
    public Map<K, V> smallMapMerge(Map<K, V> map1, Map<K, V> map2) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(ForkJoinPool.getCommonPoolParallelism());

        try {
            forkJoinPool.submit(() -> {
                Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                        .parallel()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                mergeBehavior::merge
                        ));
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("An Error occurred while merging the maps");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Merges both maps into a new maps. Original maps are preserved. This methods performs well for small maps.
     * It's up to the caller to measure it's performance.
     */
    public Map<K, V> smallMapMerge(List<Map<K, V>> maps) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(ForkJoinPool.getCommonPoolParallelism());

        try {
            return forkJoinPool.submit(() -> maps.stream()
                    .flatMap(map -> map.entrySet().stream())
                    .parallel()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            mergeBehavior::merge
                    ))).get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("An Error occurred while merging the maps");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * A Fork Join (Divide and Conquer) task for merging maps
     */
    private class DCMapMerger<K, V> extends RecursiveTask<Map<K, V>> {
        private final static int WORK_THRESHOLD = 2;

        private final MergeBehavior<K, V> mergeBehavior;
        private final List<Map<K,V>> maps;

        public DCMapMerger(List<Map<K,V>> maps, MergeBehavior<K, V> mergeBehavior) {
            this.mergeBehavior = mergeBehavior;
            this.maps = maps;
        }

        @Override
        protected Map<K, V> compute() {
            //if number of maps in the list is larger than 2, break up the list
            if(maps.size() >= WORK_THRESHOLD) {
                List<DCMapMerger<K, V>> subtasks = new ArrayList<>();
                subtasks.addAll(createSubtasks());

                for(DCMapMerger<K, V> subtask : subtasks){
                    subtask.fork();
                }

                return mergeMaps(subtasks.get(0).join(), subtasks.get(1).join());

            } else {
                // handle odd list sizes
                return maps.get(0);
            }
        }

        private Map<K, V> mergeMaps(Map<K, V> map1, Map<K, V> map2) {
            if (MapUtils.isEmpty(map1)) {
                return  map2;
            }

            if (MapUtils.isEmpty(map2)) {
                return  map1;
            }

            Map<K, V> smallerMap = map1.size() <= map2.size() ? map1 : map2;
            Map<K, V> biggerMap = map1.size() > map2.size() ? map1 : map2;

            // iterate over smaller map for efficiency
            smallerMap.entrySet().stream()
                    .forEach(entry -> {
                        biggerMap.merge(entry.getKey(),
                                entry.getValue(),
                                mergeBehavior::merge);
                    });

            return biggerMap;
        }

        private List<DCMapMerger<K, V>> createSubtasks() {
            List<DCMapMerger<K, V>> subtasks = new ArrayList<>();

            DCMapMerger<K, V> subtask1 = new DCMapMerger(maps.subList(0, maps.size() / 2), mergeBehavior);
            DCMapMerger<K, V> subtask2 = new DCMapMerger(maps.subList((maps.size() / 2), maps.size()), mergeBehavior);

            subtasks.add(subtask1);
            subtasks.add(subtask2);

            return subtasks;
        }
    }
}
