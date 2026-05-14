CREATE DATABASE IF NOT EXISTS hireconnect_auth;
CREATE DATABASE IF NOT EXISTS hireconnect_analytics;
CREATE DATABASE IF NOT EXISTS hireconnect_application;
CREATE DATABASE IF NOT EXISTS hireconnect_interview;
CREATE DATABASE IF NOT EXISTS hireconnect_job;
CREATE DATABASE IF NOT EXISTS hireconnect_notification;
CREATE DATABASE IF NOT EXISTS hireconnect_payment;
CREATE DATABASE IF NOT EXISTS hireconnect_profile;
CREATE DATABASE IF NOT EXISTS hireconnect_subscription;

GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
FLUSH PRIVILEGES;
