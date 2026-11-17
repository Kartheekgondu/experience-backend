# Supabase to local MySQL migration

This backend removes Supabase REST API and Supabase Storage usage.

## What changed
- Employee lookup now reads from MySQL `employee_master`.
- Letter history is stored in MySQL `experience_sends`.
- PDF files are stored locally under `./uploads/experience-letters` by default.
- PDF retrieval uses `/api/experience-pdf?path=<filename>`.
- Email uses Spring Mail / SMTP instead of the Resend API.
- Frontend API routes are retained where practical.

## 1. Create the database
Run `database/schema.sql` in MySQL Workbench or the MySQL CLI.

## 2. Configure environment variables (Windows PowerShell)
Set these in the terminal before starting Spring Boot:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/experience_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YOUR_LOCAL_MYSQL_PASSWORD"
$env:FRONTEND_ORIGIN="http://localhost:5173"
```

Email is optional. To enable sending, configure your SMTP provider:

```powershell
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="your-sender@gmail.com"
$env:MAIL_PASSWORD="YOUR_GOOGLE_APP_PASSWORD"
$env:MAIL_FROM="your-sender@gmail.com"
```

Do not commit passwords or app passwords to source control. Revoke/rotate credentials that were previously included in shared project files.

## 3. Start the backend
From this directory:

```bash
mvn clean spring-boot:run
```

Health check: `http://localhost:9090/api/health`

## 4. Employee data
Insert employee records into `employee_master`. The employee lookup endpoint is:
`GET /api/employee/{employeeId}`

## 5. Frontend changes required
This ZIP contains the backend project only. The frontend ZIP/source was not present in the uploaded `Sexperience.zip`.
Update any frontend Supabase SDK calls to call the Spring Boot endpoints instead:
- employee lookup: `GET /api/employee/{employeeId}`
- save letter: `POST /api/save-experience` (multipart form)
- send letter: `POST /api/send-experience` (multipart form)
- history: `GET /api/experience-history/{employeeId}`
- get PDF URL: `GET /api/experience-pdf-url?path=<filename>`

The returned PDF URL is relative to the API host. Prefix it with your configured backend base URL if opening it from the browser.

## Note
Spring Boot's `ddl-auto=update` creates `experience_sends` and updates entity tables. `employee_master` is also mapped as an entity; create it with the supplied SQL before first run.


## Live deployment lookup fix / verification

The `/api/employee/{employeeId}` endpoint now matches `employee_master.employee_id`
case-insensitively and ignores accidental surrounding spaces. It also searches
`experience_sends` case-insensitively (latest record first). The frontend sends
the ID as uppercase.

**Important:** this application does not automatically read the old Supabase or
any other legacy database. Render must point to the MySQL database containing
the migrated employee/history rows. Run the two verification SELECT statements
in `database/schema.sql` against the exact database used by the deployed backend.
If both return zero rows, migrate the actual employee details into `employee_master`
using the INSERT template in that file. Do not use the sample values.

Set these Render environment variables: `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. For email, set
`MAIL_USERNAME`, `MAIL_PASSWORD`, and optionally `MAIL_FROM`. Credentials have
been removed from the source defaults; rotate any database or SMTP credentials
that were previously committed in source files or shared archives.
