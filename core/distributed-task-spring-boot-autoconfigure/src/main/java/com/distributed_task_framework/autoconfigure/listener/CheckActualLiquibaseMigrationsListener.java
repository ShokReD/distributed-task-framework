package com.distributed_task_framework.autoconfigure.listener;

import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
@RequiredArgsConstructor
public class CheckActualLiquibaseMigrationsListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final String SELECT_FILENAMES_QUERY = "SELECT filename FROM %s";
    private static final String LIQUIBASE_SCRIPTS_PATH = "db/changelog/distributed-task-framework";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        var springLiquibase = event.getApplicationContext().getBean(SpringLiquibase.class);
        var knownMigrationFileNames = knownMigrationFileNames(event);
        var deployedMigrationFileNames = deployedMigrationFileNames(springLiquibase);
        knownMigrationFileNames.removeAll(deployedMigrationFileNames);
        if (!knownMigrationFileNames.isEmpty()) {
            throw new IllegalStateException("Followed migrations are not deployed " + knownMigrationFileNames);
        }
    }

    @SneakyThrows
    private Set<String> knownMigrationFileNames(ContextRefreshedEvent event) {
        var fileNames = new HashSet<String>();
        var resource = event.getApplicationContext().getClassLoader().getResource(LIQUIBASE_SCRIPTS_PATH);
        if (resource != null) {
            var path = resource.getPath();
            var jarPath = path.substring(5, path.indexOf("!"));
            try (var jarFile = new JarFile(jarPath)) {
                var entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    var name = entry.getName();
                    if (name.startsWith(LIQUIBASE_SCRIPTS_PATH) && (name.endsWith(".yaml") || name.endsWith(".yml"))) {
                        fileNames.add(name);
                    }
                }
            }
        }
        fileNames.removeIf(fileName -> fileName.endsWith("db.changelog-aggregator.yaml"));
        return fileNames;
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
