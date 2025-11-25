package com.distributed_task_framework.model;


public record IntRange(
    RangeBound startInclusive,
    RangeBound endInclusive
) {

    private record RangeBound(
        int value,
        boolean closed
    ) {
    }

    public static IntRange openClosed(int left, int right) {
        return new IntRange(
            new RangeBound(left, false),
            new RangeBound(right, true)
        );
    }

    public static IntRange closedOpen(int left, int right) {
        return new IntRange(
            new RangeBound(left, true),
            new RangeBound(right, false)
        );
    }

    public int lowerEndpoint() {
        return startInclusive.value;
    }

    public int upperEndpoint() {
        return endInclusive.value;
    }
}
