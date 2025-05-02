package com.distributed_task_framework.autoconfigure;

import com.distributed_task_framework.autoconfigure.listener.ActualLiquibaseMigrationsChecker;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckMigrationsListenerTest {
    private final ApplicationContextRunner baseContextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            DistributedTaskLiquibaseAutoConfiguration.class,
            LiquibaseAutoConfiguration.class
        ))
        .withPropertyValues(
            "distributed-task.enabled=true",
            "distributed-task.liquibase.check-migrations=true",
            "spring.liquibase.enabled=true",
            "spring.liquibase.change-log=classpath:/db/changelog/distributed-task-framework/db.changelog-aggregator.yaml",
            "spring.liquibase.url=jdbc:tc:postgresql:16:///test-db?TC_DAEMON=true",
            "spring.liquibase.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
            "spring.liquibase.user=postgres",
            "spring.liquibase.password=password"
        );

    @Nested
    class ListenerNotLoaded {
        private final ApplicationContextRunner contextRunner = baseContextRunner;

        @Test
        void whenJdbcTemplateIsNotPresentInClasspath() {
            contextRunner
                .withClassLoader(new FilteredClassLoader(JdbcTemplate.class))
                .run(context -> {
                    assertThat(context)
                        .doesNotHaveBean(ActualLiquibaseMigrationsChecker.class);
                });
        }

        @Test
        void whenLiquibaseBeanIsNotLoaded() {
            contextRunner
                .withPropertyValues("spring.liquibase.enabled=false")
                .run(context -> assertThat(context)
                    .doesNotHaveBean(ActualLiquibaseMigrationsChecker.class));
        }

        @Test
        void whenDistributedTaskIsDisabled() {
            contextRunner
                .withPropertyValues("distributed-task.enabled=false")
                .run(context -> assertThat(context)
                    .doesNotHaveBean(ActualLiquibaseMigrationsChecker.class));
        }

        @Test
        void whenMigrationCheckingIsDisabled() {
            contextRunner
                .withPropertyValues("distributed-task.liquibase.check-migrations=false")
                .run(context -> assertThat(context)
                    .doesNotHaveBean(ActualLiquibaseMigrationsChecker.class)
                );
        }
    }

    @Nested
    class ListenerSuccessfulLoaded {

        @Test
        void whenAllMigrationsAreApplied() {
            baseContextRunner
                .run(context -> assertThat(context)
                    .hasNotFailed());
        }
    }
}
