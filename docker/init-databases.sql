-- SportFlow Databases Initialization
SELECT 'CREATE DATABASE users_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'users_db')\gexec
SELECT 'CREATE DATABASE clubs_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'clubs_db')\gexec
SELECT 'CREATE DATABASE athletes_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'athletes_db')\gexec
SELECT 'CREATE DATABASE competitions_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'competitions_db')\gexec
SELECT 'CREATE DATABASE federation_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'federation_db')\gexec
SELECT 'CREATE DATABASE live_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'live_db')\gexec
SELECT 'CREATE DATABASE notifications_stats_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notifications_stats_db')\gexec
