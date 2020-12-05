create database demo;

CREATE USER 'confluent'@'%' IDENTIFIED BY 'pwd';
FLUSH PRIVILEGES;
GRANT ALL PRIVILEGES ON demo.* TO 'confluent'@'%';
SHOW GRANTS FOR 'confluent'@'%';

