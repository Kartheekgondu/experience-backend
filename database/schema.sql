CREATE DATABASE IF NOT EXISTS experience_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE experience_db;

CREATE TABLE IF NOT EXISTS employee_master (
  employee_id VARCHAR(15) PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  designation VARCHAR(100),
  join_date DATE,
  last_working_date DATE,
  email VARCHAR(190)
);

-- Import the REAL values for previous employees from your authoritative HR/Supabase
-- export. Do not insert placeholder values. Example structure only:
-- INSERT INTO employee_master
-- (employee_id, full_name, designation, join_date, last_working_date, email)
-- VALUES ('UXII0022', '<actual full name>', '<actual designation>',
--         '<YYYY-MM-DD joining date>', '<YYYY-MM-DD last working date>', '<actual email>');

-- Verify the production record after importing:
-- SELECT employee_id, full_name, designation, join_date, last_working_date, email
-- FROM employee_master
-- WHERE UPPER(TRIM(employee_id)) = 'UXII0022';
-- Also check historical letter records:
-- SELECT employee_id, employee_name, designation, date_of_joining,
--        last_working_date, recipient_email, created_at
-- FROM experience_sends
-- WHERE UPPER(TRIM(employee_id)) = 'UXII0022'
-- ORDER BY created_at DESC;

-- Spring Boot creates/updates experience_sends automatically through JPA.
