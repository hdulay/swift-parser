CREATE TABLE "kafka_mt103" (
    "id" VARCHAR2(128) NOT NULL,
    "sender" VARCHAR2(128) NOT NULL,
    "receiver" VARCHAR2(128),
    "Field20" VARCHAR(128),
    "value_date" NUMBER(19,0),
    "amount" VARCHAR2(128),
    "raw" CLOB,
    PRIMARY KEY("id")
);

SELECT * FROM "ADMIN"."kafka_mt103";
