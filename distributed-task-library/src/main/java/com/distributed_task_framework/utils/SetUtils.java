package com.distributed_task_framework.utils;

import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class SetUtils {
    public <T> Set<T> difference(Set<T> set1, Set<T> set2) {
        var result = new HashSet<>(set1);
        result.removeAll(set2);
        return result;
    }

    public <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        var result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }
}
