drop table if exists SYSTEM_ITEM;

CREATE TABLE SYSTEM_ITEM
(
    ID          VARCHAR PRIMARY KEY,
    URL         VARCHAR,
    MODIFIED_AT VARCHAR NOT NULL,
    PARENT_ID   VARCHAR,
    ITEM_TYPE        VARCHAR NOT NULL,
    ITEM_SIZE        INT
);
