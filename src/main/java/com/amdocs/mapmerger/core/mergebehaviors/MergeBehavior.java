package com.amdocs.mapmerger.core.mergebehaviors;

public interface MergeBehavior<K,V> {
    V merge(V value1, V value2);
}
