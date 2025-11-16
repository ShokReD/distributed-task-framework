package com.distributed_task_framework.autoconfigure.mapper;

import com.distributed_task_framework.autoconfigure.DistributedTaskProperties;
import com.distributed_task_framework.exception.TaskConfigurationException;
import com.distributed_task_framework.settings.BackoffRetry;
import com.distributed_task_framework.settings.CommonSettings;
import com.distributed_task_framework.settings.FixedRetry;
import com.distributed_task_framework.settings.OffRetry;
import com.distributed_task_framework.settings.Retry;
import com.distributed_task_framework.settings.TaskSettings;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DistributedTaskPropertiesMapper {

    public TaskSettings map(DistributedTaskProperties.TaskProperties taskProperties) {
        if (taskProperties == null) {
            return null;
        }

        var builder = TaskSettings.builder()
            .cron(taskProperties.getCron())
            .retry(map(taskProperties.getRetry()))
            .maxParallelInCluster(taskProperties.getMaxParallelInCluster())
            .maxParallelInNode(taskProperties.getMaxParallelInNode())
            .timeout(taskProperties.getTimeout());

        if (taskProperties.getExecutionGuarantees() != null) {
            builder.executionGuarantees(TaskSettings.ExecutionGuarantees.valueOf(taskProperties.getExecutionGuarantees()));
        }
        if (taskProperties.getDltEnabled() != null) {
            builder.dltEnabled(taskProperties.getDltEnabled());
        }

        return builder.build();
    }

    public DistributedTaskProperties.TaskProperties map(TaskSettings taskSettings) {
        if (taskSettings == null) {
            return null;
        }

        var builder = DistributedTaskProperties.TaskProperties.builder()
            .dltEnabled(taskSettings.isDltEnabled())
            .retry(map(taskSettings.getRetry()))
            .maxParallelInCluster(taskSettings.getMaxParallelInCluster())
            .maxParallelInNode(taskSettings.getMaxParallelInNode())
            .timeout(taskSettings.getTimeout());

        if (taskSettings.getExecutionGuarantees() != null) {
            builder.executionGuarantees(taskSettings.getExecutionGuarantees().name());
        }
        if (taskSettings.hasCron()) {
            builder.cron(taskSettings.getCron());
        }

        return builder.build();
    }

    public CommonSettings.StatSettings map(DistributedTaskProperties.Statistics mergedStatistics) {
        if (mergedStatistics == null) {
            return null;
        }

        return CommonSettings.StatSettings.builder()
            .calcInitialDelayMs(mergedStatistics.getCalcInitialDelayMs())
            .calcFixedDelayMs(mergedStatistics.getCalcFixedDelayMs())
            .build();
    }

    public DistributedTaskProperties.Statistics merge(DistributedTaskProperties.Statistics defaultStatistics, DistributedTaskProperties.Statistics statistics) {
        if (statistics == null) {
            return defaultStatistics;
        }

        if (statistics.getCalcInitialDelayMs() != null) {
            defaultStatistics.setCalcInitialDelayMs(statistics.getCalcInitialDelayMs());
        }
        if (statistics.getCalcFixedDelayMs() != null) {
            defaultStatistics.setCalcFixedDelayMs(statistics.getCalcFixedDelayMs());
        }

        return defaultStatistics;
    }

    public DistributedTaskProperties.Statistics map(CommonSettings.StatSettings defaultStatistics) {
        if (defaultStatistics == null) {
            return null;
        }

        return DistributedTaskProperties.Statistics.builder()
            .calcInitialDelayMs(defaultStatistics.getCalcInitialDelayMs())
            .calcFixedDelayMs(defaultStatistics.getCalcFixedDelayMs())
            .build();
    }

    public DistributedTaskProperties.Common merge(DistributedTaskProperties.Common defaultCommon, DistributedTaskProperties.Common common) {
        if (common == null) {
            return defaultCommon;
        }

        if (common.getAppName() != null) {
            defaultCommon.setAppName(common.getAppName());
        }
        if (common.getRegistry() != null) {
            if (defaultCommon.getRegistry() == null) {
                defaultCommon.setRegistry(DistributedTaskProperties.Registry.builder().build());
            }
            merge(defaultCommon.getRegistry(), common.getRegistry());
        }
        if (common.getPlanner() != null) {
            if (defaultCommon.getPlanner() == null) {
                defaultCommon.setPlanner(DistributedTaskProperties.Planner.builder().build());
            }
            merge(defaultCommon.getPlanner(), common.getPlanner());
        }
        if (common.getWorkerManager() != null) {
            if (defaultCommon.getWorkerManager() == null) {
                defaultCommon.setWorkerManager(DistributedTaskProperties.WorkerManager.builder().build());
            }
            merge(defaultCommon.getWorkerManager(), common.getWorkerManager());
        }
        if (common.getStatistics() != null) {
            if (defaultCommon.getStatistics() == null) {
                defaultCommon.setStatistics(DistributedTaskProperties.Statistics.builder().build());
            }
            merge(defaultCommon.getStatistics(), common.getStatistics());
        }
        if (common.getDeliveryManager() != null) {
            if (defaultCommon.getDeliveryManager() == null) {
                defaultCommon.setDeliveryManager(DistributedTaskProperties.DeliveryManager.builder().build());
            }
            merge(defaultCommon.getDeliveryManager(), common.getDeliveryManager());
        }

        return defaultCommon;
    }

    public CommonSettings map(DistributedTaskProperties.Common mergedSettings) {
        if (mergedSettings == null) {
            return null;
        }

        return CommonSettings.builder()
            .appName(mergedSettings.getAppName())
            .build();
    }

    public DistributedTaskProperties.Common map(CommonSettings commonSettings) {
        if (commonSettings == null) {
            return null;
        }

        return DistributedTaskProperties.Common.builder()
            .appName(commonSettings.getAppName())
            .build();
    }

    public Retry map(DistributedTaskProperties.Retry defaultRetryProperties) {
        if (defaultRetryProperties == null) {
            return null;
        }
        if (defaultRetryProperties.getRetryMode().equalsIgnoreCase("fixed")) {
            var fixedProperties = defaultRetryProperties.getFixed();
            return new FixedRetry(
                fixedProperties.getDelay(),
                fixedProperties.getMaxNumber(),
                fixedProperties.getMaxInterval()
            );
        } else if (defaultRetryProperties.getRetryMode().equalsIgnoreCase("backoff")) {
            var backoffProperties = defaultRetryProperties.getBackoff();
            return new BackoffRetry(
                backoffProperties.getInitialDelay(),
                backoffProperties.getDelayPeriod(),
                backoffProperties.getMaxRetries(),
                backoffProperties.getMaxDelay()
            );
        } else if (defaultRetryProperties.getRetryMode().equalsIgnoreCase("off")) {
            return new OffRetry();
        } else {
            throw new IllegalArgumentException("Invalid type of retry mode '%s'. Allowed 'fixed', 'backoff', 'off'".formatted(defaultRetryProperties.getRetryMode()));
        }
    }

    public DistributedTaskProperties.Retry map(Retry defaultRetrySettings) {
        if (defaultRetrySettings == null) {
            return null;
        }

        return switch (defaultRetrySettings) {
            case FixedRetry fixedRetry -> DistributedTaskProperties.Retry.builder()
                .retryMode("fixed")
                .fixed(fixedToFixed1(fixedRetry))
                .build();
            case BackoffRetry backoffRetry -> DistributedTaskProperties.Retry.builder()
                .retryMode("backoff")
                .backoff(backoffToBackoff1(backoffRetry))
                .build();
            case OffRetry ignored -> DistributedTaskProperties.Retry.builder()
                .retryMode("off")
                .build();
        };
    }

    public DistributedTaskProperties.Retry mergeInternal(DistributedTaskProperties.Retry defaultRetrySettings, DistributedTaskProperties.Retry retry) {
        if (retry == null) {
            return defaultRetrySettings;
        }

        if (retry.getRetryMode() != null) {
            defaultRetrySettings.setRetryMode(retry.getRetryMode());
        }
        if (retry.getFixed() != null) {
            if (defaultRetrySettings.getFixed() == null) {
                defaultRetrySettings.setFixed(DistributedTaskProperties.Fixed.builder().build());
            }
            merge(defaultRetrySettings.getFixed(), retry.getFixed());
        }
        if (retry.getBackoff() != null) {
            if (defaultRetrySettings.getBackoff() == null) {
                defaultRetrySettings.setBackoff(DistributedTaskProperties.Backoff.builder().build());
            }
            merge(defaultRetrySettings.getBackoff(), retry.getBackoff());
        }

        return defaultRetrySettings;
    }

    public CommonSettings.DeliveryManagerSettings map(DistributedTaskProperties.DeliveryManager mergedRegistry) {
        if (mergedRegistry == null) {
            return null;
        }

        CommonSettings.DeliveryManagerSettings.DeliveryManagerSettingsBuilder deliveryManagerSettings = CommonSettings.DeliveryManagerSettings.builder();

        deliveryManagerSettings.remoteApps(remoteAppsToRemoteApps(mergedRegistry.getRemoteApps()));
        deliveryManagerSettings.watchdogInitialDelayMs(mergedRegistry.getWatchdogInitialDelayMs());
        deliveryManagerSettings.watchdogFixedDelayMs(mergedRegistry.getWatchdogFixedDelayMs());
        deliveryManagerSettings.batchSize(mergedRegistry.getBatchSize());
        deliveryManagerSettings.connectionTimeout(mergedRegistry.getConnectionTimeout());
        deliveryManagerSettings.responseTimeout(mergedRegistry.getResponseTimeout());
        deliveryManagerSettings.readTimeout(mergedRegistry.getReadTimeout());
        deliveryManagerSettings.writeTimeout(mergedRegistry.getWriteTimeout());
        deliveryManagerSettings.retry(map(mergedRegistry.getRetry()));
        deliveryManagerSettings.manageDelay(mapRangeDelayProperty(mergedRegistry.getManageDelay()));

        return deliveryManagerSettings.build();
    }

    public DistributedTaskProperties.DeliveryManager mapInternal(CommonSettings.DeliveryManagerSettings deliveryManagerSettings) {
        if (deliveryManagerSettings == null) {
            return null;
        }

        DistributedTaskProperties.DeliveryManager.DeliveryManagerBuilder deliveryManager = DistributedTaskProperties.DeliveryManager.builder();

        deliveryManager.remoteApps(remoteAppsToRemoteApps1(deliveryManagerSettings.getRemoteApps()));
        deliveryManager.watchdogInitialDelayMs(deliveryManagerSettings.getWatchdogInitialDelayMs());
        deliveryManager.watchdogFixedDelayMs(deliveryManagerSettings.getWatchdogFixedDelayMs());
        deliveryManager.batchSize(deliveryManagerSettings.getBatchSize());
        deliveryManager.connectionTimeout(deliveryManagerSettings.getConnectionTimeout());
        deliveryManager.responseTimeout(deliveryManagerSettings.getResponseTimeout());
        deliveryManager.readTimeout(deliveryManagerSettings.getReadTimeout());
        deliveryManager.writeTimeout(deliveryManagerSettings.getWriteTimeout());
        deliveryManager.retry(map(deliveryManagerSettings.getRetry()));

        return deliveryManager.build();
    }

    public DistributedTaskProperties.DeliveryManager merge(DistributedTaskProperties.DeliveryManager deliveryManagerSettings, DistributedTaskProperties.DeliveryManager workerManager) {
        if (workerManager == null) {
            return deliveryManagerSettings;
        }

        if (workerManager.getRemoteApps() != null) {
            deliveryManagerSettings.setRemoteApps(workerManager.getRemoteApps());
        }
        if (workerManager.getWatchdogInitialDelayMs() != null) {
            deliveryManagerSettings.setWatchdogInitialDelayMs(workerManager.getWatchdogInitialDelayMs());
        }
        if (workerManager.getWatchdogFixedDelayMs() != null) {
            deliveryManagerSettings.setWatchdogFixedDelayMs(workerManager.getWatchdogFixedDelayMs());
        }
        if (workerManager.getBatchSize() != null) {
            deliveryManagerSettings.setBatchSize(workerManager.getBatchSize());
        }
        if (workerManager.getConnectionTimeout() != null) {
            deliveryManagerSettings.setConnectionTimeout(workerManager.getConnectionTimeout());
        }
        if (workerManager.getResponseTimeout() != null) {
            deliveryManagerSettings.setResponseTimeout(workerManager.getResponseTimeout());
        }
        if (workerManager.getReadTimeout() != null) {
            deliveryManagerSettings.setReadTimeout(workerManager.getReadTimeout());
        }
        if (workerManager.getWriteTimeout() != null) {
            deliveryManagerSettings.setWriteTimeout(workerManager.getWriteTimeout());
        }
        if (workerManager.getRetry() != null) {
            if (deliveryManagerSettings.getRetry() == null) {
                deliveryManagerSettings.setRetry(DistributedTaskProperties.Retry.builder().build());
            }
            mergeInternal(deliveryManagerSettings.getRetry(), workerManager.getRetry());
        }

        return deliveryManagerSettings;
    }

    public CommonSettings.WorkerManagerSettings map(DistributedTaskProperties.WorkerManager mergedRegistry) {
        if (mergedRegistry == null) {
            return null;
        }

        CommonSettings.WorkerManagerSettings.WorkerManagerSettingsBuilder workerManagerSettings = CommonSettings.WorkerManagerSettings.builder();

        workerManagerSettings.maxParallelTasksInNode(mergedRegistry.getMaxParallelTasksInNode());
        workerManagerSettings.manageDelay(mapRangeDelayProperty(mergedRegistry.getManageDelay()));

        return workerManagerSettings.build();
    }

    public DistributedTaskProperties.WorkerManager merge(DistributedTaskProperties.WorkerManager defaultWorkerManager, DistributedTaskProperties.WorkerManager workerManager) {
        if (workerManager == null) {
            return defaultWorkerManager;
        }

        if (workerManager.getMaxParallelTasksInNode() != null) {
            defaultWorkerManager.setMaxParallelTasksInNode(workerManager.getMaxParallelTasksInNode());
        }

        return defaultWorkerManager;
    }

    public DistributedTaskProperties.WorkerManager mapInternal(CommonSettings.WorkerManagerSettings workerManagerSettings) {
        if (workerManagerSettings == null) {
            return null;
        }

        DistributedTaskProperties.WorkerManager.WorkerManagerBuilder workerManager = DistributedTaskProperties.WorkerManager.builder();

        workerManager.maxParallelTasksInNode(workerManagerSettings.getMaxParallelTasksInNode());

        return workerManager.build();
    }

    public CommonSettings.RegistrySettings map(DistributedTaskProperties.Registry mergedRegistry) {
        if (mergedRegistry == null) {
            return null;
        }

        CommonSettings.RegistrySettings.RegistrySettingsBuilder registrySettings = CommonSettings.RegistrySettings.builder();

        registrySettings.updateInitialDelayMs(mergedRegistry.getUpdateInitialDelayMs());
        registrySettings.updateFixedDelayMs(mergedRegistry.getUpdateFixedDelayMs());
        registrySettings.maxInactivityIntervalMs(mergedRegistry.getMaxInactivityIntervalMs());
        registrySettings.cacheExpirationMs(mergedRegistry.getCacheExpirationMs());
        registrySettings.cpuCalculatingTimeWindow(mergedRegistry.getCpuCalculatingTimeWindow());

        return registrySettings.build();
    }

    public DistributedTaskProperties.Registry merge(DistributedTaskProperties.Registry defaultPropertiesRegistry, DistributedTaskProperties.Registry registry) {
        if (registry == null) {
            return defaultPropertiesRegistry;
        }

        if (registry.getUpdateInitialDelayMs() != null) {
            defaultPropertiesRegistry.setUpdateInitialDelayMs(registry.getUpdateInitialDelayMs());
        }
        if (registry.getUpdateFixedDelayMs() != null) {
            defaultPropertiesRegistry.setUpdateFixedDelayMs(registry.getUpdateFixedDelayMs());
        }
        if (registry.getMaxInactivityIntervalMs() != null) {
            defaultPropertiesRegistry.setMaxInactivityIntervalMs(registry.getMaxInactivityIntervalMs());
        }
        if (registry.getCacheExpirationMs() != null) {
            defaultPropertiesRegistry.setCacheExpirationMs(registry.getCacheExpirationMs());
        }
        if (registry.getCpuCalculatingTimeWindow() != null) {
            defaultPropertiesRegistry.setCpuCalculatingTimeWindow(registry.getCpuCalculatingTimeWindow());
        }

        return defaultPropertiesRegistry;
    }

    public DistributedTaskProperties.Registry map(CommonSettings.RegistrySettings defaultRegistrySettings) {
        if (defaultRegistrySettings == null) {
            return null;
        }

        DistributedTaskProperties.Registry.RegistryBuilder registry = DistributedTaskProperties.Registry.builder();

        registry.updateInitialDelayMs(defaultRegistrySettings.getUpdateInitialDelayMs());
        registry.updateFixedDelayMs(defaultRegistrySettings.getUpdateFixedDelayMs());
        registry.maxInactivityIntervalMs(defaultRegistrySettings.getMaxInactivityIntervalMs());
        registry.cacheExpirationMs(defaultRegistrySettings.getCacheExpirationMs());
        registry.cpuCalculatingTimeWindow(defaultRegistrySettings.getCpuCalculatingTimeWindow());

        return registry.build();
    }

    public DistributedTaskProperties.Planner mapInternal(CommonSettings.PlannerSettings defaultPlannerSettings) {
        if (defaultPlannerSettings == null) {
            return null;
        }

        DistributedTaskProperties.Planner.PlannerBuilder planner = DistributedTaskProperties.Planner.builder();

        planner.watchdogInitialDelayMs(defaultPlannerSettings.getWatchdogInitialDelayMs());
        planner.watchdogFixedDelayMs(defaultPlannerSettings.getWatchdogFixedDelayMs());
        planner.maxParallelTasksInClusterDefault(defaultPlannerSettings.getMaxParallelTasksInClusterDefault());
        planner.batchSize(defaultPlannerSettings.getBatchSize());
        planner.newBatchSize(defaultPlannerSettings.getNewBatchSize());
        planner.deletedBatchSize(defaultPlannerSettings.getDeletedBatchSize());
        planner.affinityGroupScannerTimeOverlap(defaultPlannerSettings.getAffinityGroupScannerTimeOverlap());
        planner.partitionTrackingTimeWindow(defaultPlannerSettings.getPartitionTrackingTimeWindow());
        planner.nodeCpuLoadingLimit(defaultPlannerSettings.getNodeCpuLoadingLimit());
        planner.planFactor(defaultPlannerSettings.getPlanFactor());

        return planner.build();
    }

    public CommonSettings.PlannerSettings map(DistributedTaskProperties.Planner mergedPlanner) {
        if (mergedPlanner == null) {
            return null;
        }

        CommonSettings.PlannerSettings.PlannerSettingsBuilder plannerSettings = CommonSettings.PlannerSettings.builder();

        plannerSettings.watchdogInitialDelayMs(mergedPlanner.getWatchdogInitialDelayMs());
        plannerSettings.watchdogFixedDelayMs(mergedPlanner.getWatchdogFixedDelayMs());
        plannerSettings.maxParallelTasksInClusterDefault(mergedPlanner.getMaxParallelTasksInClusterDefault());
        plannerSettings.batchSize(mergedPlanner.getBatchSize());
        plannerSettings.newBatchSize(mergedPlanner.getNewBatchSize());
        plannerSettings.deletedBatchSize(mergedPlanner.getDeletedBatchSize());
        plannerSettings.affinityGroupScannerTimeOverlap(mergedPlanner.getAffinityGroupScannerTimeOverlap());
        plannerSettings.nodeCpuLoadingLimit(mergedPlanner.getNodeCpuLoadingLimit());
        plannerSettings.planFactor(mergedPlanner.getPlanFactor());
        plannerSettings.partitionTrackingTimeWindow(mergedPlanner.getPartitionTrackingTimeWindow());

        return plannerSettings.build();
    }

    public DistributedTaskProperties.Planner merge(DistributedTaskProperties.Planner defaultPropertiesPlanner, DistributedTaskProperties.Planner planner) {
        if (planner == null) {
            return defaultPropertiesPlanner;
        }

        if (planner.getWatchdogInitialDelayMs() != null) {
            defaultPropertiesPlanner.setWatchdogInitialDelayMs(planner.getWatchdogInitialDelayMs());
        }
        if (planner.getWatchdogFixedDelayMs() != null) {
            defaultPropertiesPlanner.setWatchdogFixedDelayMs(planner.getWatchdogFixedDelayMs());
        }
        if (planner.getMaxParallelTasksInClusterDefault() != null) {
            defaultPropertiesPlanner.setMaxParallelTasksInClusterDefault(planner.getMaxParallelTasksInClusterDefault());
        }
        if (planner.getBatchSize() != null) {
            defaultPropertiesPlanner.setBatchSize(planner.getBatchSize());
        }
        if (planner.getNewBatchSize() != null) {
            defaultPropertiesPlanner.setNewBatchSize(planner.getNewBatchSize());
        }
        if (planner.getDeletedBatchSize() != null) {
            defaultPropertiesPlanner.setDeletedBatchSize(planner.getDeletedBatchSize());
        }
        if (planner.getAffinityGroupScannerTimeOverlap() != null) {
            defaultPropertiesPlanner.setAffinityGroupScannerTimeOverlap(planner.getAffinityGroupScannerTimeOverlap());
        }
        if (planner.getPartitionTrackingTimeWindow() != null) {
            defaultPropertiesPlanner.setPartitionTrackingTimeWindow(planner.getPartitionTrackingTimeWindow());
        }
        if (planner.getNodeCpuLoadingLimit() != null) {
            defaultPropertiesPlanner.setNodeCpuLoadingLimit(planner.getNodeCpuLoadingLimit());
        }
        if (planner.getPlanFactor() != null) {
            defaultPropertiesPlanner.setPlanFactor(planner.getPlanFactor());
        }
        Map<Integer, Integer> map = planner.getPollingDelay();
        if (defaultPropertiesPlanner.getPollingDelay() != null) {
            if (map != null) {
                defaultPropertiesPlanner.getPollingDelay().clear();
                defaultPropertiesPlanner.getPollingDelay().putAll(map);
            }
        } else {
            if (map != null) {
                defaultPropertiesPlanner.setPollingDelay(new LinkedHashMap<>(map));
            }
        }

        return defaultPropertiesPlanner;
    }

    public DistributedTaskProperties.TaskProperties mergeInternal(DistributedTaskProperties.TaskProperties defaultSettings, DistributedTaskProperties.TaskProperties taskProperties) {
        if (taskProperties == null) {
            return defaultSettings;
        }

        if (taskProperties.getExecutionGuarantees() != null) {
            defaultSettings.setExecutionGuarantees(taskProperties.getExecutionGuarantees());
        }
        if (taskProperties.getDltEnabled() != null) {
            defaultSettings.setDltEnabled(taskProperties.getDltEnabled());
        }
        if (taskProperties.getCron() != null) {
            defaultSettings.setCron(taskProperties.getCron());
        }
        if (taskProperties.getMaxParallelInCluster() != null) {
            defaultSettings.setMaxParallelInCluster(taskProperties.getMaxParallelInCluster());
        }
        if (taskProperties.getMaxParallelInNode() != null) {
            defaultSettings.setMaxParallelInNode(taskProperties.getMaxParallelInNode());
        }
        if (taskProperties.getTimeout() != null) {
            defaultSettings.setTimeout(taskProperties.getTimeout());
        }

        return defaultSettings;
    }

    public DistributedTaskProperties.Backoff merge(DistributedTaskProperties.Backoff defaultBackoff, DistributedTaskProperties.Backoff backoff) {
        if (backoff == null) {
            return defaultBackoff;
        }

        if (backoff.getInitialDelay() != null) {
            defaultBackoff.setInitialDelay(backoff.getInitialDelay());
        }
        if (backoff.getDelayPeriod() != null) {
            defaultBackoff.setDelayPeriod(backoff.getDelayPeriod());
        }
        if (backoff.getMaxRetries() != null) {
            defaultBackoff.setMaxRetries(backoff.getMaxRetries());
        }
        if (backoff.getMaxDelay() != null) {
            defaultBackoff.setMaxDelay(backoff.getMaxDelay());
        }

        return defaultBackoff;
    }

    public DistributedTaskProperties.Fixed merge(DistributedTaskProperties.Fixed defaultBackoff, DistributedTaskProperties.Fixed backoff) {
        if (backoff == null) {
            return defaultBackoff;
        }

        if (backoff.getDelay() != null) {
            defaultBackoff.setDelay(backoff.getDelay());
        }
        if (backoff.getMaxNumber() != null) {
            defaultBackoff.setMaxNumber(backoff.getMaxNumber());
        }
        if (backoff.getMaxInterval() != null) {
            defaultBackoff.setMaxInterval(backoff.getMaxInterval());
        }

        return defaultBackoff;
    }

    protected DistributedTaskProperties.Fixed fixedToFixed1(FixedRetry fixed) {
        if (fixed == null) {
            return null;
        }

        DistributedTaskProperties.Fixed.FixedBuilder fixed1 = DistributedTaskProperties.Fixed.builder();

        fixed1.delay(fixed.getDelay());
        fixed1.maxNumber(fixed.getMaxNumber());
        fixed1.maxInterval(fixed.getMaxInterval());

        return fixed1.build();
    }

    protected DistributedTaskProperties.Backoff backoffToBackoff1(BackoffRetry backoff) {
        if (backoff == null) {
            return null;
        }

        DistributedTaskProperties.Backoff.BackoffBuilder backoff1 = DistributedTaskProperties.Backoff.builder();

        backoff1.initialDelay(backoff.getInitialDelay());
        backoff1.delayPeriod(backoff.getDelayPeriod());
        backoff1.maxRetries(backoff.getMaxRetries());
        backoff1.maxDelay(backoff.getMaxDelay());

        return backoff1.build();
    }

    protected CommonSettings.RemoteApps remoteAppsToRemoteApps(DistributedTaskProperties.RemoteApps remoteApps) {
        if (remoteApps == null) {
            return null;
        }

        CommonSettings.RemoteApps.RemoteAppsBuilder remoteApps1 = CommonSettings.RemoteApps.builder();

        Map<String, URL> map = remoteApps.getAppToUrl();
        if (map != null) {
            remoteApps1.appToUrl(new LinkedHashMap<>(map));
        }

        return remoteApps1.build();
    }

    protected DistributedTaskProperties.RemoteApps remoteAppsToRemoteApps1(CommonSettings.RemoteApps remoteApps) {
        if (remoteApps == null) {
            return null;
        }

        DistributedTaskProperties.RemoteApps.RemoteAppsBuilder remoteApps1 = DistributedTaskProperties.RemoteApps.builder();

        Map<String, URL> map = remoteApps.getAppToUrl();
        if (map != null) {
            remoteApps1.appToUrl(new LinkedHashMap<>(map));
        }

        return remoteApps1.build();
    }


    public CommonSettings merge(CommonSettings commonSettings,
                                DistributedTaskProperties.Common common) {
        if (common == null) {
            return commonSettings;
        }
        CommonSettings.RegistrySettings registrySettings = common.getRegistry() != null ?
            merge(commonSettings.getRegistrySettings().toBuilder().build(), common.getRegistry()) :
            commonSettings.getRegistrySettings();
        CommonSettings.PlannerSettings plannerSettings = common.getPlanner() != null ?
            merge(commonSettings.getPlannerSettings(), common.getPlanner()) :
            commonSettings.getPlannerSettings();
        CommonSettings.WorkerManagerSettings workerManagerSettings = common.getWorkerManager() != null ?
            merge(commonSettings.getWorkerManagerSettings(), common.getWorkerManager()) :
            commonSettings.getWorkerManagerSettings();
        //todo: test for these properties
        CommonSettings.StatSettings statSettings = common.getStatistics() != null ?
            merge(commonSettings.getStatSettings(), common.getStatistics()) :
            commonSettings.getStatSettings();
        CommonSettings.DeliveryManagerSettings deliveryManagerSettings = common.getDeliveryManager() != null ?
            merge(commonSettings.getDeliveryManagerSettings(), common.getDeliveryManager()) :
            commonSettings.getDeliveryManagerSettings();
        return mergeInternal(commonSettings, common).toBuilder()
            .registrySettings(registrySettings)
            .plannerSettings(plannerSettings)
            .workerManagerSettings(workerManagerSettings)
            .statSettings(statSettings)
            .deliveryManagerSettings(deliveryManagerSettings)
            .build();
    }

    public CommonSettings.StatSettings merge(CommonSettings.StatSettings defaultStatistics,
                                             DistributedTaskProperties.Statistics statistics) {
        DistributedTaskProperties.Statistics defaultPropertiesRegistry = map(defaultStatistics);
        DistributedTaskProperties.Statistics mergedRegistry = merge(defaultPropertiesRegistry, statistics);
        return map(mergedRegistry);
    }

    public CommonSettings mergeInternal(CommonSettings commonSettings,
                                        DistributedTaskProperties.Common common) {
        DistributedTaskProperties.Common defaultCommon = map(commonSettings);
        DistributedTaskProperties.Common mergedSettings = merge(defaultCommon, common);
        return map(mergedSettings);
    }

    /**
     * @noinspection UnstableApiUsage
     */
    public CommonSettings.DeliveryManagerSettings merge(CommonSettings.DeliveryManagerSettings deliveryManagerSettings,
                                                        DistributedTaskProperties.DeliveryManager deliveryManager) {
        var mergedManagerSettings = mergeInternal(deliveryManagerSettings, deliveryManager);
        if (!deliveryManager.getManageDelay().isEmpty()) {
            ImmutableRangeMap<Integer, Integer> polingDelay = mapRangeDelayProperty(deliveryManager.getManageDelay());
            mergedManagerSettings = mergedManagerSettings.toBuilder()
                .manageDelay(polingDelay)
                .build();
        }
        mergedManagerSettings = mergedManagerSettings.toBuilder()
            .retry(merge(deliveryManagerSettings.getRetry(), deliveryManager.getRetry()))
            .build();
        return mergedManagerSettings;
    }

    public Retry merge(Retry defaultRetrySettings, DistributedTaskProperties.Retry retry) {
        if (retry == null) {
            return defaultRetrySettings;
        }
        if (retry.getRetryMode().equalsIgnoreCase("fixed")) {
            var fixedProperties = retry.getFixed();
            if (defaultRetrySettings instanceof FixedRetry fixedRetry) {
                return new FixedRetry(
                    or(fixedProperties.getDelay(), fixedRetry.getDelay()),
                    or(fixedProperties.getMaxNumber(), fixedRetry.getMaxNumber()),
                    or(fixedProperties.getMaxInterval(), fixedRetry.getMaxInterval())
                );
            } else {
                return new FixedRetry(
                    fixedProperties.getDelay(),
                    fixedProperties.getMaxNumber(),
                    fixedProperties.getMaxInterval()
                );
            }
        } else if (retry.getRetryMode().equalsIgnoreCase("backoff")) {
            var backoffProperties = retry.getBackoff();
            if (defaultRetrySettings instanceof BackoffRetry backoffRetry) {
                return new BackoffRetry(
                    or(backoffProperties.getInitialDelay(), backoffRetry.getInitialDelay()),
                    or(backoffProperties.getDelayPeriod(), backoffRetry.getDelayPeriod()),
                    or(backoffProperties.getMaxRetries(), backoffRetry.getMaxRetries()),
                    or(backoffProperties.getMaxDelay(), backoffRetry.getMaxDelay())
                );
            } else {
                return new BackoffRetry(
                    backoffProperties.getInitialDelay(),
                    backoffProperties.getDelayPeriod(),
                    backoffProperties.getMaxRetries(),
                    backoffProperties.getMaxDelay()
                );
            }
        } else if (retry.getRetryMode().equalsIgnoreCase("off")) {
            return new OffRetry();
        } else {
            throw new IllegalArgumentException("Invalid type of retry mode '%s'. Allowed 'fixed', 'backoff', 'off'".formatted(retry.getRetryMode()));
        }
    }

    public CommonSettings.DeliveryManagerSettings mergeInternal(CommonSettings.DeliveryManagerSettings deliveryManagerSettings,
                                                                DistributedTaskProperties.DeliveryManager deliveryManager) {
        DistributedTaskProperties.DeliveryManager defaultDeliveryManager = map(deliveryManagerSettings);
        DistributedTaskProperties.DeliveryManager mergedRegistry = merge(defaultDeliveryManager, deliveryManager);
        return map(mergedRegistry);
    }

    /**
     * @noinspection UnstableApiUsage
     */
    public DistributedTaskProperties.DeliveryManager map(CommonSettings.DeliveryManagerSettings deliveryManagerSettings) {
        var deliveryManager = mapInternal(deliveryManagerSettings);
        Map<Integer, Integer> manageDelay = new HashMap<>();
        deliveryManagerSettings.getManageDelay().asMapOfRanges()
            .forEach((range, limit) -> manageDelay.put(range.upperEndpoint(), limit));
        return deliveryManager.toBuilder()
            .manageDelay(manageDelay)
            .build();
    }

    /**
     * @noinspection UnstableApiUsage
     */
    public CommonSettings.WorkerManagerSettings merge(CommonSettings.WorkerManagerSettings workerManagerSettings,
                                                      DistributedTaskProperties.WorkerManager workerManager) {
        var mergedManagerSettings = mergeInternal(workerManagerSettings, workerManager);
        if (!workerManager.getManageDelay().isEmpty()) {
            ImmutableRangeMap<Integer, Integer> manageDelay = mapRangeDelayProperty(workerManager.getManageDelay());
            mergedManagerSettings = mergedManagerSettings.toBuilder()
                .manageDelay(manageDelay)
                .build();
        }
        return mergedManagerSettings;
    }

    public CommonSettings.WorkerManagerSettings mergeInternal(CommonSettings.WorkerManagerSettings workerManagerSettings,
                                                              DistributedTaskProperties.WorkerManager workerManager) {
        DistributedTaskProperties.WorkerManager defaultWorkerManager = map(workerManagerSettings);
        DistributedTaskProperties.WorkerManager mergedRegistry = merge(defaultWorkerManager, workerManager);
        return map(mergedRegistry);
    }

    /**
     * @noinspection UnstableApiUsage
     */
    public DistributedTaskProperties.WorkerManager map(CommonSettings.WorkerManagerSettings workerManagerSettings) {
        var workerManager = mapInternal(workerManagerSettings);
        Map<Integer, Integer> manageDelay = new HashMap<>();
        workerManagerSettings.getManageDelay().asMapOfRanges()
            .forEach((range, limit) -> manageDelay.put(range.upperEndpoint(), limit));
        return workerManager.toBuilder()
            .manageDelay(manageDelay)
            .build();
    }

    public CommonSettings.RegistrySettings merge(CommonSettings.RegistrySettings defaultRegistrySettings,
                                                 DistributedTaskProperties.Registry registry) {
        DistributedTaskProperties.Registry defaultPropertiesRegistry = map(defaultRegistrySettings);
        DistributedTaskProperties.Registry mergedRegistry = merge(defaultPropertiesRegistry, registry);
        return map(mergedRegistry);
    }

    /**
     * @noinspection UnstableApiUsage
     */
    public CommonSettings.PlannerSettings merge(CommonSettings.PlannerSettings defaultPlannerSettings,
                                                DistributedTaskProperties.Planner planner) {
        defaultPlannerSettings = mergeInternal(defaultPlannerSettings, planner);
        var plannerBuilder = defaultPlannerSettings.toBuilder();
        if (!planner.getPollingDelay().isEmpty()) {
            ImmutableRangeMap<Integer, Integer> pollingDelay = mapRangeDelayProperty(planner.getPollingDelay());
            plannerBuilder.pollingDelay(pollingDelay);
        }
        return plannerBuilder.build();
    }

    /**
     * @noinspection UnstableApiUsage
     */
    public ImmutableRangeMap<Integer, Integer> mapRangeDelayProperty(Map<Integer, Integer> rangeDelay) {
        List<Integer> orderedNumbers = rangeDelay.keySet().stream()
            .sorted()
            .toList();
        var rangeMapBuilder = ImmutableRangeMap.<Integer, Integer>builder();
        int lastNumber = -1;
        for (int number : orderedNumbers) {
            if (number <= lastNumber) {
                throw new TaskConfigurationException("Incorrect ordering of polling-delay");
            }
            rangeMapBuilder.put(Range.openClosed(lastNumber, number), rangeDelay.get(number));
            lastNumber = number;
        }
        return rangeMapBuilder.build();
    }

    /**
     * @noinspection UnstableApiUsage
     */
    public DistributedTaskProperties.Planner map(CommonSettings.PlannerSettings defaultPlannerSettings) {
        DistributedTaskProperties.Planner result = mapInternal(defaultPlannerSettings);
        Map<Integer, Integer> pollingDelay = new HashMap<>();
        defaultPlannerSettings.getPollingDelay().asMapOfRanges()
            .forEach((range, limit) -> pollingDelay.put(range.upperEndpoint(), limit));
        return result.toBuilder()
            .pollingDelay(pollingDelay)
            .build();
    }

    public CommonSettings.PlannerSettings mergeInternal(CommonSettings.PlannerSettings defaultPlannerSettings,
                                                        DistributedTaskProperties.Planner planner) {
        DistributedTaskProperties.Planner defaultPropertiesPlanner = map(defaultPlannerSettings);
        DistributedTaskProperties.Planner mergedPlanner = merge(defaultPropertiesPlanner, planner);
        return map(mergedPlanner);
    }

    public DistributedTaskProperties.TaskProperties merge(DistributedTaskProperties.TaskProperties defaultSettings,
                                                          DistributedTaskProperties.TaskProperties taskProperties) {
        DistributedTaskProperties.Retry defaultRetry = defaultSettings.getRetry() != null ? defaultSettings.getRetry() :
            DistributedTaskProperties.Retry.builder().build();
        DistributedTaskProperties.Backoff defaultBackoff = defaultSettings.getRetry() != null && defaultSettings.getRetry().getBackoff() != null ?
            defaultSettings.getRetry().getBackoff().toBuilder().build() :
            DistributedTaskProperties.Backoff.builder().build();

        DistributedTaskProperties.Fixed defaultFixed = defaultSettings.getRetry() != null && defaultSettings.getRetry().getFixed() != null ?
            defaultSettings.getRetry().getFixed().toBuilder().build() :
            DistributedTaskProperties.Fixed.builder().build();

        var result = mergeInternal(defaultSettings, taskProperties);
        if (taskProperties.getRetry() != null) {
            defaultRetry = mergeInternal(defaultRetry, taskProperties.getRetry());
        }
        if (taskProperties.getRetry() != null && taskProperties.getRetry().getBackoff() != null) {
            defaultBackoff = merge(defaultBackoff, taskProperties.getRetry().getBackoff());
        }
        if (taskProperties.getRetry() != null && taskProperties.getRetry().getFixed() != null) {
            defaultFixed = merge(defaultFixed, taskProperties.getRetry().getFixed());
        }
        return result.toBuilder()
            .retry(defaultRetry.toBuilder()
                .backoff(defaultBackoff)
                .fixed(defaultFixed)
                .build())
            .build();
    }

    private <T> T or(T value, T other) {
        return value != null ? value : other;
    }
}

