-- Select the real Azure SQL application database in VS Code MSSQL before running.
-- Read-only. No credentials belong in this file.
SELECT DB_NAME() AS DatabaseName, DATABASEPROPERTYEX(DB_NAME(), 'Collation') AS DatabaseCollation;

SELECT s.name AS SchemaName,t.name AS TableName,c.column_id,c.name AS ColumnName,
       ty.name AS SqlType,c.max_length,c.precision,c.scale,c.is_nullable,c.is_identity,
       c.collation_name
FROM sys.tables t JOIN sys.schemas s ON s.schema_id=t.schema_id
JOIN sys.columns c ON c.object_id=t.object_id JOIN sys.types ty ON ty.user_type_id=c.user_type_id
WHERE s.name=N'dbo' ORDER BY t.name,c.column_id;

SELECT t.name AS TableName,i.name AS IndexName,i.is_unique,i.is_primary_key,
       i.filter_definition,c.name AS ColumnName,ic.key_ordinal,ic.is_included_column
FROM sys.indexes i JOIN sys.tables t ON t.object_id=i.object_id
JOIN sys.index_columns ic ON ic.object_id=i.object_id AND ic.index_id=i.index_id
JOIN sys.columns c ON c.object_id=ic.object_id AND c.column_id=ic.column_id
ORDER BY t.name,i.index_id,ic.key_ordinal;

SELECT OBJECT_NAME(f.parent_object_id) AS ParentTable,pc.name AS ParentColumn,
       OBJECT_NAME(f.referenced_object_id) AS ReferencedTable,rc.name AS ReferencedColumn,
       fk.name AS ForeignKeyName,fk.is_disabled,fk.is_not_trusted
FROM sys.foreign_key_columns f JOIN sys.columns pc
ON pc.object_id=f.parent_object_id AND pc.column_id=f.parent_column_id
JOIN sys.columns rc ON rc.object_id=f.referenced_object_id AND rc.column_id=f.referenced_column_id
JOIN sys.foreign_keys fk ON fk.object_id=f.constraint_object_id;

SELECT OBJECT_NAME(parent_object_id) AS ParentTable,name,definition,is_disabled,is_not_trusted
FROM sys.check_constraints;

SELECT OBJECT_SCHEMA_NAME(referencing_id) AS RefSchema,OBJECT_NAME(referencing_id) AS RefObject,
       referenced_schema_name,referenced_entity_name
FROM sys.sql_expression_dependencies;
