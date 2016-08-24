CREATE TABLE PREDICTION_MODEL (
    ID_ varchar(64) not null,
    NAME_ varchar(255) not null, -- add unique constraint
    RESOURCE_ longvarbinary,
    primary key (ID_)
);

CREATE TABLE PREDICTION_PRIOR (
    MODEL_ID_ varchar(64) not null, -- make foreign key
    DESCRIBED_VARIABLE_ varchar(255) not null -- make composite key with model_id
    DATA_ longvarbinary
);