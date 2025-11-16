package com.distributed_task_framework.persistence.repository;

import com.distributed_task_framework.persistence.entity.PartitionEntity;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
class PartitionRepositoryTest extends BaseRepositoryTest {

    @Test
    void shouldSaveOrUpdateBatch() {
        //when
        var allPartitions = createTestPartitions();

        //do
        var savedPartitions = partitionRepository.saveOrUpdateBatch(allPartitions);

        //verify
        Assertions.assertThat(savedPartitions).containsAll(allPartitions);
        Assertions.assertThat(partitionRepository.findAll()).containsAll(allPartitions);
    }

    @Test
    void shouldFilterExisted() {
        //when
        var existedPartitions = createTestPartitions();
        partitionRepository.saveAll(existedPartitions);
        var partitionsToFilter = new ArrayList<>(existedPartitions) {{
            add(createPartition("unknown", "unknown", 1L));
            add(createPartition("afg1", "t1", 3L));
        }};

        //do
        var actualPartitions = partitionRepository.filterExisted(partitionsToFilter);

        //verify
        Assertions.assertThat(actualPartitions).containsExactlyInAnyOrderElementsOf(existedPartitions);
    }

    @Test
    void shouldFindAllBefore() {
        //when
        var existedPartitions = createTestPartitions();
        partitionRepository.saveAll(existedPartitions);

        //do
        var actualPartitions = partitionRepository.findAllBeforeOrIn(2L);

        //verify
        var expectedPartitions = existedPartitions.stream()
            .filter(partition -> partition.getTimeBucket() <= 2L)
            .toList();
        Assertions.assertThat(actualPartitions).containsAll(expectedPartitions);
    }

    @Test
    void shouldDeleteBatch() {
        //when
        var existedPartitions = createTestPartitions();
        existedPartitions = Lists.newArrayList(partitionRepository.saveAll(existedPartitions));

        //do
        partitionRepository.deleteBatch(existedPartitions);

        //verify
        Assertions.assertThat(partitionRepository.findAll()).isEmpty();
    }

    private Collection<PartitionEntity> createTestPartitions() {
        return List.of(
            createPartition("afg1", "t1", 1L),
            createPartition(null, "t2", 1L),
            createPartition("afg1", "t1", 2L),
            createPartition(null, "t2", 2L),
            createPartition("afg1", "t1", 3L),
            createPartition(null, "t2", 3L)
        );
    }
}