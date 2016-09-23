CREATE TABLE PREDICTION_MODEL (
    ID_ varchar(64) not null,
    NAME_ varchar(255) not null,
    RESOURCE_ longvarbinary,
    primary key (ID_)
);

CREATE TABLE PREDICTION_PRIOR (
    MODEL_ID_ varchar(64) not null,
    DESCRIBED_VARIABLE_ varchar(255) not null,
    DATA_ longvarbinary,
    
);

alter table PREDICTION_MODEL
    add constraint PREDICTION_MODEL_NAME_UNIQUE
    unique (NAME_);
    
alter table PREDICTION_PRIOR
    add constraint PREDICTION_PRIOR_MODEL_ID_FK
    foreign key (MODEL_ID_)
    references PREDICTION_MODEL (ID_);
    
alter table PREDICTION_PRIOR
    add constraint PREDICTION_PRIOR_MODEL_VARIABLE_UNIQUE
    unique (MODEL_ID_, DESCRIBED_VARIABLE_);
    