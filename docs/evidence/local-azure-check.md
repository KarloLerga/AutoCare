# Local Azure SQL connectivity evidence

Date: 2026-09-16

Command executed from the repository after the Java 25 package build:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "& { . '.\\scripts\\Load-Connection.ps1' -ConfigPath 'EXTERNAL_PRIVATE_CONFIG'; & java -jar '.\\target\\autocare-1.0.0.jar' db-list }"
```

Result: `BLOCKED` by the Azure SQL server firewall. The server returned an explicit message that the current client IP is not allowed to access the server. This proves the request reached the configured SQL Server endpoint; it does not prove successful SQL authentication or reveal the database name.

No firewall rule was changed, no database was created, and no schema/seed write was attempted. The next safe action is to allow only the current public IP on the existing Azure SQL server networking page, wait for propagation, and rerun the same read-only `db-list`. Do not open all IPs or create a replacement resource.

The SQL password and the rejected IP address are intentionally omitted from the repository evidence.
