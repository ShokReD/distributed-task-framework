package com.distributed_task_framework.saga.services.impl;


import com.distributed_task_framework.model.TaskDef;
import com.distributed_task_framework.saga.annotations.SagaMethod;
import com.distributed_task_framework.saga.models.SagaPipeline;
import com.distributed_task_framework.saga.persistence.repository.SagaContextRepository;
import com.distributed_task_framework.saga.services.SagaManager;
import com.distributed_task_framework.saga.services.SagaRegister;
import com.distributed_task_framework.saga.services.SagaTaskFactory;
import com.distributed_task_framework.service.DistributedTaskService;
import com.distributed_task_framework.service.TaskSerializer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SagaTaskFactoryImpl implements SagaTaskFactory {
    SagaRegister sagaRegister;
    DistributedTaskService distributedTaskService;
    SagaManager sagaManager;
    SagaContextRepository sagaContextRepository;
    TaskSerializer taskSerializer;
    SagaHelper sagaHelper;

    @Override
    public SagaTask sagaTask(TaskDef<SagaPipeline> taskDef,
                             Method method,
                             Object bean,
                             SagaMethod sagaMethodAnnotation) {
        return new SagaTask(
            sagaRegister,
            distributedTaskService,
            sagaManager,
            sagaContextRepository,
            taskSerializer,
            sagaHelper,
            taskDef,
            method,
            bean,
            sagaMethodAnnotation
        );
    }

    @Override
    public SagaRevertTask sagaRevertTask(TaskDef<SagaPipeline> taskDef, Method method, Object bean) {
        return new SagaRevertTask(
            sagaRegister,
            distributedTaskService,
            sagaManager,
            sagaHelper,
            taskDef,
            method,
            bean
        );
    }
}
