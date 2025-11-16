package com.distributed_task_framework.perf_test.model;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class Hierarchy {
    public static final String JOIN_HIERARCHY_NAME = "join";

    List<String> hierarchy;

    private Hierarchy() {
        this.hierarchy = List.of();
    }

    private Hierarchy(List<String> hierarchy) {
        this.hierarchy = hierarchy;
    }

    public static Hierarchy createEmpty() {
        return new Hierarchy();
    }

    public static Hierarchy create(int level) {
        return createEmpty().addLevel(level);
    }

    public Hierarchy addLevel(int level) {
        return new Hierarchy(new ArrayList<>(hierarchy) {{
            add(String.valueOf(level));
        }});
    }

    public Hierarchy addLevel(String level) {
        return new Hierarchy(new ArrayList<>(hierarchy) {{
            add(level);
        }});
    }

    @Override
    public String toString() {
        return hierarchy.stream()
            .map(Object::toString)
            .collect(Collectors.joining(".", "[", "]"));
    }
}
