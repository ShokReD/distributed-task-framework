package com.distributed_task_framework.autoconfigure.listener;

import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ActualLiquibaseMigrationsChecker {

    private static final String SELECT_FILENAMES_QUERY = "SELECT filename FROM %s";
    private final ApplicationContext applicationContext;

    // TODO make check before main DTF-processes are initialized
    public void check() {
        var springLiquibase = applicationContext.getBean(SpringLiquibase.class);
        var knownMigrationFileNames = knownMigrationFileNames();
        var deployedMigrationFileNames = deployedMigrationFileNames(springLiquibase);
        knownMigrationFileNames.removeAll(deployedMigrationFileNames);
        if (!knownMigrationFileNames.isEmpty()) {
            throw new IllegalStateException("Followed migrations are not deployed " + knownMigrationFileNames);
        }
    }

    @SneakyThrows
    private Set<String> knownMigrationFileNames() {
        try {
            var files = Path.of(Thread.currentThread().getContextClassLoader().getResource("db/changelog/distributed-task-framework").toURI()).toFile().listFiles();
            return Arrays.stream(files)
                .map(File::getName)
                .filter(Objects::nonNull)
                .filter(fileName -> !fileName.equals("db.changelog-aggregator.yaml"))
                .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Unable to find liquibase-scripts related to DTF", e);
            throw new RuntimeException(e);
        }
    }

    private Set<String> deployedMigrationFileNames(SpringLiquibase springLiquibase) {
        var jdbcTemplate = new JdbcTemplate(springLiquibase.getDataSource());
        var fileNames = jdbcTemplate.query(
            SELECT_FILENAMES_QUERY.formatted(springLiquibase.getDatabaseChangeLogTable()),
            new SingleColumnRowMapper<>(String.class)
        );
        return new HashSet<>(fileNames);
    }
}
