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

-- Insert one row per employee using your real employee information.
-- The frontend now asks only for employee_id. All other details are fetched
-- from this table. Do not use the sample values as real employee data.
-- INSERT INTO employee_master
-- (employee_id, full_name, designation, join_date, last_working_date, email)
-- VALUES ('UXII0022', 'G Kartheek', 'Software Engineer',
--         '2026-07-01', '2026-11-15', 'employee@example.com');

-- Spring Boot creates/updates experience_sends automatically through JPA.


-- IMPORTANT: employee lookup reads only employee_master and experience_sends.
-- If UXII0022 exists in a legacy database/table, migrate its REAL values into
-- employee_master (do not insert placeholder/sample values). Example:
-- INSERT INTO employee_master
--   (employee_id, full_name, designation, join_date, last_working_date, email)
-- VALUES
--   ('UXII0022', '<real full name>', '<real designation>',
--    '<YYYY-MM-DD joining date>', '<YYYY-MM-DD last working date>', '<real email>')
-- ON DUPLICATE KEY UPDATE
--   full_name=VALUES(full_name), designation=VALUES(designation),
--   join_date=VALUES(join_date), last_working_date=VALUES(last_working_date),
--   email=VALUES(email);

-- Run this against the SAME database configured in Render to verify the row:
-- SELECT employee_id, full_name, designation, join_date, last_working_date, email
-- FROM employee_master WHERE UPPER(TRIM(employee_id)) = 'UXII0022';
-- SELECT employee_id, employee_name, designation, date_of_joining,
--        last_working_date, recipient_email, created_at
-- FROM experience_sends
-- WHERE UPPER(TRIM(employee_id)) = 'UXII0022'
-- ORDER BY created_at DESC;
