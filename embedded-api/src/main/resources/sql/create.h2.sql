CREATE TABLE PREDICTION_MODEL (
    ID_ varchar(64) not null,
    NAME_ varchar(255) not null, -- add unique constraint
    RESOURCE_ longvarbinary,
    primary key (ID_)
);