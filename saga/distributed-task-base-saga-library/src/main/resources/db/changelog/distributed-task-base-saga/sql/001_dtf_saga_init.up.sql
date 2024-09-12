CREATE TABLE "_____dtf_saga_context"
(
    "saga_id"               UUID                        NOT NULL,
    "user_name"             VARCHAR(255),
    "created_date_utc"      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "completed_date_utc"    TIMESTAMP WITHOUT TIME ZONE,
    "expiration_date_utc"   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "root_task_id"          BYTEA                       NOT NULL,
    "exception_type"        VARCHAR(255),
    "result"                BYTEA,
    "last_pipeline_context" BYTEA                       NOT NULL,
    CONSTRAINT "_____dtf_saga_context_pkey" PRIMARY KEY ("saga_id")
);
CREATE TABLE "_____dtf_saga_context_dls"
(
    "saga_id"               UUID                        NOT NULL,
    "user_name"             VARCHAR(255),
    "created_date_utc"      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "expiration_date_utc"   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "root_task_id"          BYTEA                       NOT NULL,
    "last_pipeline_context" BYTEA                       NOT NULL,
    CONSTRAINT "_____dtf_saga_context_dls_pkey" PRIMARY KEY ("saga_id")
);