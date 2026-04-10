-- EventFlow seed data is initialized programmatically via DataInitializer.java
-- This file intentionally left blank.
-- To add custom seed SQL, add INSERT statements here after the JPA schema is created.
INSERT INTO users (first_name, last_name, email, password_hash, role, active)
VALUES ('Admin', 'User', 'admin@example.com', 'temporary_hash', 'ADMIN', true);