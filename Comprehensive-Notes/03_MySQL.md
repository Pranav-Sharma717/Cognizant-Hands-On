# 03 — MySQL (Absolute Basics → Interview Ready)

> The Cognizant FSE stack pairs Spring Data JPA with **MySQL 8**. You need SQL fluency *and* an understanding of what Hibernate generates underneath.
> **Reference:** [MySQL 8.0 Reference Manual](https://dev.mysql.com/doc/refman/8.0/en/)

---

## 1. Foundations

### 1.1 What is a database / RDBMS?
- **Database** — an organised collection of data.
- **DBMS** — software to store, retrieve and manage it.
- **RDBMS** — a DBMS where data lives in **relations (tables)** of rows and columns, with relationships enforced by keys. Based on E. F. Codd's relational model (1970).
- **MySQL** — the world's most popular open-source RDBMS (Oracle-owned). Client-server, SQL-based, supports pluggable storage engines.

### 1.2 Core vocabulary
| Term | Meaning |
|---|---|
| Table / Relation | A 2-D structure of rows and columns |
| Row / Tuple / Record | One entity instance |
| Column / Attribute / Field | One property |
| Degree | number of columns |
| Cardinality | number of rows |
| Schema | the structure/blueprint (tables, types, constraints) |
| Instance | the data at a given moment |
| Domain | the set of legal values for a column |

### 1.3 SQL sub-languages ⭐ (very common question)
| Category | Full form | Commands | Auto-commit? |
|---|---|---|---|
| **DDL** | Data Definition Language | `CREATE`, `ALTER`, `DROP`, `TRUNCATE`, `RENAME` | ✅ implicit commit |
| **DML** | Data Manipulation Language | `INSERT`, `UPDATE`, `DELETE`, (`MERGE`) | ❌ needs COMMIT |
| **DQL** | Data Query Language | `SELECT` | n/a |
| **DCL** | Data Control Language | `GRANT`, `REVOKE` | ✅ |
| **TCL** | Transaction Control Language | `COMMIT`, `ROLLBACK`, `SAVEPOINT`, `SET TRANSACTION` | n/a |

### 1.4 MySQL storage engines
| Engine | Transactions | Locking | FK | Use |
|---|---|---|---|---|
| **InnoDB** (default since 5.5) | ✅ ACID | row-level | ✅ | **everything** |
| MyISAM | ❌ | table-level | ❌ | legacy, read-heavy |
| MEMORY | ❌ | table | ❌ | temp/cache |

> Interview: *"Why InnoDB?"* → ACID transactions, row-level locking (better concurrency), foreign keys, crash recovery via redo log, clustered index on the primary key, MVCC.

---

## 2. Data Types

### Numeric
| Type | Bytes | Notes |
|---|---|---|
| `TINYINT` | 1 | −128…127 (`TINYINT(1)` = boolean) |
| `SMALLINT` | 2 | |
| `MEDIUMINT` | 3 | |
| `INT` / `INTEGER` | 4 | most common; `UNSIGNED` doubles the positive range |
| `BIGINT` | 8 | IDs at scale |
| `DECIMAL(p,s)` / `NUMERIC` | var | **exact** — use for money |
| `FLOAT` / `DOUBLE` | 4 / 8 | approximate — never for money |
| `BOOLEAN` | 1 | alias for `TINYINT(1)` |

### String
| Type | Notes |
|---|---|
| `CHAR(n)` | fixed length, right-padded, fast for fixed codes |
| `VARCHAR(n)` | variable, 1–2 byte length prefix, max row size 65,535 bytes |
| `TEXT` / `MEDIUMTEXT` / `LONGTEXT` | 64 KB / 16 MB / 4 GB, stored off-page |
| `BLOB` family | binary |
| `ENUM('A','B')` / `SET(...)` | constrained string lists |
| `JSON` | native JSON with validation & path operators (5.7+) |

### Date & Time
| Type | Range | Notes |
|---|---|---|
| `DATE` | 1000-01-01 … 9999-12-31 | `YYYY-MM-DD` |
| `TIME` | −838:59:59 … 838:59:59 | |
| `DATETIME` | 1000 … 9999 | 8 bytes, **no timezone conversion** |
| `TIMESTAMP` | 1970 … 2038 | 4 bytes, **converted to/from UTC**, auto-update |
| `YEAR` | 1901–2155 | |

⭐ `DATETIME` vs `TIMESTAMP`: `TIMESTAMP` is stored as UTC and converted using the session time zone; `DATETIME` is stored literally. `TIMESTAMP` supports `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`. `TIMESTAMP` hits the 2038 problem.

**JPA mapping:** `DATE`→`LocalDate`, `TIME`→`LocalTime`, `DATETIME`→`LocalDateTime`, `TIMESTAMP`→`Instant`/`OffsetDateTime`.

### Charset
Always use `utf8mb4` (true 4-byte UTF-8, supports emoji). `utf8` in MySQL is a broken 3-byte alias.
```sql
CREATE DATABASE app CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

---

## 3. DDL — defining structure

```sql
-- Database
CREATE DATABASE IF NOT EXISTS cognizant_fse;
USE cognizant_fse;
SHOW DATABASES;  SHOW TABLES;  DESCRIBE employees;  SHOW CREATE TABLE employees;

-- Table
CREATE TABLE departments (
    dept_id    INT AUTO_INCREMENT PRIMARY KEY,
    dept_name  VARCHAR(50) NOT NULL UNIQUE,
    location   VARCHAR(50) DEFAULT 'Chennai'
) ENGINE=InnoDB;

CREATE TABLE employees (
    emp_id     INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50)    NOT NULL,
    last_name  VARCHAR(50)    NOT NULL,
    email      VARCHAR(100)   NOT NULL UNIQUE,
    salary     DECIMAL(10,2)  NOT NULL CHECK (salary > 0),   -- CHECK enforced from 8.0.16
    hire_date  DATE           NOT NULL DEFAULT (CURRENT_DATE),
    dept_id    INT,
    manager_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_emp_mgr  FOREIGN KEY (manager_id) REFERENCES employees(emp_id),
    INDEX idx_emp_lastname (last_name),
    INDEX idx_emp_dept_salary (dept_id, salary)
) ENGINE=InnoDB;

-- ALTER
ALTER TABLE employees ADD COLUMN phone VARCHAR(15);
ALTER TABLE employees MODIFY COLUMN phone VARCHAR(20) NOT NULL;
ALTER TABLE employees CHANGE COLUMN phone mobile VARCHAR(20);   -- rename + retype
ALTER TABLE employees DROP COLUMN mobile;
ALTER TABLE employees ADD CONSTRAINT uq_email UNIQUE (email);
ALTER TABLE employees DROP FOREIGN KEY fk_emp_dept;
ALTER TABLE employees RENAME TO staff;

-- Removal
DROP TABLE IF EXISTS employees;
TRUNCATE TABLE employees;
```

### ⭐ DELETE vs TRUNCATE vs DROP (guaranteed question)
| | `DELETE` | `TRUNCATE` | `DROP` |
|---|---|---|---|
| Type | DML | DDL | DDL |
| Removes | selected rows | all rows | rows **+ table structure** |
| `WHERE` | ✅ | ❌ | ❌ |
| Rollback | ✅ (in a txn) | ❌ implicit commit | ❌ |
| Speed | slow (row-by-row, logged) | **fast** (drops & recreates the tablespace) | fast |
| Fires triggers | ✅ | ❌ | ❌ |
| Resets AUTO_INCREMENT | ❌ | ✅ | n/a |
| Needs a FK-referencing table to be empty | n/a | ✅ fails if referenced | ✅ fails if referenced |

---

## 4. Constraints & Keys

| Constraint | Purpose |
|---|---|
| `NOT NULL` | value required |
| `UNIQUE` | no duplicates (allows multiple `NULL`s in MySQL) |
| `PRIMARY KEY` | unique + not null; **one per table**; clustered index in InnoDB |
| `FOREIGN KEY` | referential integrity to another table's PK/UK |
| `CHECK` | boolean condition (enforced from MySQL 8.0.16) |
| `DEFAULT` | fallback value |
| `AUTO_INCREMENT` | auto-generated surrogate key |

### Key types ⭐
| Key | Definition |
|---|---|
| **Super key** | any attribute set that uniquely identifies a row |
| **Candidate key** | a *minimal* super key (no redundant attribute) |
| **Primary key** | the chosen candidate key; unique + NOT NULL |
| **Alternate key** | candidate keys not chosen as PK |
| **Composite key** | PK made of 2+ columns |
| **Foreign key** | references another table's PK/UK |
| **Surrogate key** | artificial key (`AUTO_INCREMENT` id) with no business meaning |
| **Natural key** | a real-world attribute used as PK (email, PAN) |

### Referential actions
```sql
FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
  ON DELETE CASCADE | SET NULL | RESTRICT | NO ACTION | SET DEFAULT
  ON UPDATE CASCADE
```
- `CASCADE` — child rows deleted/updated too
- `SET NULL` — child FK set to NULL (column must be nullable)
- `RESTRICT`/`NO ACTION` — **default** — block the parent delete

---

## 5. DML — INSERT / UPDATE / DELETE

```sql
INSERT INTO departments (dept_name, location) VALUES ('Engineering', 'Chennai');
INSERT INTO departments (dept_name) VALUES ('HR'), ('Finance'), ('Sales');   -- multi-row
INSERT INTO archive_emp SELECT * FROM employees WHERE hire_date < '2020-01-01';

-- Upsert
INSERT INTO departments (dept_id, dept_name) VALUES (1, 'Eng')
ON DUPLICATE KEY UPDATE dept_name = VALUES(dept_name);
REPLACE INTO departments (dept_id, dept_name) VALUES (1, 'Eng');  -- delete + insert

UPDATE employees SET salary = salary * 1.10 WHERE dept_id = 1;
UPDATE employees e JOIN departments d ON e.dept_id = d.dept_id
   SET e.salary = e.salary * 1.05 WHERE d.dept_name = 'Engineering';

DELETE FROM employees WHERE emp_id = 10;
DELETE e FROM employees e JOIN departments d ON e.dept_id = d.dept_id
   WHERE d.dept_name = 'Sales';
```
> ⚠️ MySQL Workbench runs in **safe update mode** by default — an `UPDATE`/`DELETE` without a key-based `WHERE` is rejected. `SET SQL_SAFE_UPDATES = 0;` to disable (only when you mean it).

---

## 6. SELECT — the workhorse

### 6.1 Syntax and logical execution order ⭐⭐
```sql
SELECT   DISTINCT col, AGG(col)     -- 5
FROM     table                      -- 1
JOIN     other ON cond              -- 2
WHERE    row_condition              -- 3
GROUP BY col                        -- 4
HAVING   group_condition            -- 6
ORDER BY col ASC|DESC               -- 7
LIMIT    n OFFSET m;                -- 8
```
**Written order:** SELECT → FROM → WHERE → GROUP BY → HAVING → ORDER BY → LIMIT
**Logical (execution) order:** `FROM` → `JOIN` → `WHERE` → `GROUP BY` → `HAVING` → `SELECT` → `DISTINCT` → `ORDER BY` → `LIMIT`

> **Why this matters:** because `SELECT` runs *after* `WHERE`, you **cannot** use a column alias in `WHERE`. You *can* use it in `ORDER BY` and `HAVING` (MySQL extension).

### 6.2 Filtering
```sql
SELECT * FROM employees WHERE salary > 50000;
WHERE salary BETWEEN 40000 AND 80000              -- inclusive
WHERE dept_id IN (1, 2, 3)
WHERE dept_id NOT IN (SELECT dept_id FROM closed_depts)
WHERE first_name LIKE 'P%'        -- starts with P
WHERE email LIKE '%@gmail.com'
WHERE first_name LIKE '_ranav'    -- _ = exactly one char
WHERE manager_id IS NULL          -- NEVER  = NULL
WHERE first_name REGEXP '^[AEIOU]'
WHERE salary > 50000 AND (dept_id = 1 OR dept_id = 2);
```

### 6.3 NULL handling ⭐
`NULL` means *unknown*, not zero or empty string. Any arithmetic/comparison with `NULL` yields `NULL`.
```sql
NULL = NULL          -- NULL (not TRUE!)
IS NULL / IS NOT NULL           -- correct test
IFNULL(col, 0)                  -- MySQL
COALESCE(a, b, c, 'default')    -- standard SQL, first non-null
NULLIF(a, b)                    -- NULL if a = b, else a
<=>                             -- MySQL null-safe equal: NULL <=> NULL is TRUE
```
Aggregates **ignore** `NULL`s — except `COUNT(*)` which counts rows.
```sql
SELECT COUNT(*), COUNT(manager_id) FROM employees;   -- differ if some managers are NULL
```

### 6.4 Sorting & limiting
```sql
ORDER BY salary DESC, last_name ASC
ORDER BY FIELD(status, 'HIGH','MED','LOW')        -- custom order
LIMIT 10;                 -- top 10
LIMIT 10 OFFSET 20;       -- rows 21–30  (page 3, size 10)
LIMIT 20, 10;             -- same, MySQL shorthand (offset, count)
```
> **This is exactly what Spring Data's `Pageable` becomes.** `PageRequest.of(2, 10)` → `LIMIT 10 OFFSET 20`.
> Deep offsets are slow (the DB still scans skipped rows) → prefer **keyset pagination**: `WHERE id > :lastId ORDER BY id LIMIT 10`.

---

## 7. Functions

### Aggregate (with `GROUP BY`)
```sql
COUNT(*) COUNT(col) COUNT(DISTINCT col)
SUM(col) AVG(col) MIN(col) MAX(col)
GROUP_CONCAT(name SEPARATOR ', ')
STDDEV(col) VARIANCE(col)
```

### String
```sql
CONCAT(a,b)  CONCAT_WS('-',a,b)  LENGTH(s)  CHAR_LENGTH(s)
UPPER/LOWER  TRIM/LTRIM/RTRIM  SUBSTRING(s,pos,len)  LEFT/RIGHT(s,n)
REPLACE(s,from,to)  INSTR(s,sub)  LOCATE(sub,s)  REVERSE(s)
LPAD(s,n,'0')  RPAD  REPEAT(s,n)  FORMAT(num,2)
```

### Numeric
```sql
ROUND(x,2)  CEIL(x)  FLOOR(x)  ABS(x)  MOD(a,b)  POWER(a,b)  SQRT(x)  RAND()  TRUNCATE(x,d)
```

### Date
```sql
NOW()  CURDATE()  CURTIME()  UTC_TIMESTAMP()
DATE_ADD(d, INTERVAL 7 DAY)   DATE_SUB(d, INTERVAL 1 MONTH)
DATEDIFF(d1,d2)   TIMESTAMPDIFF(YEAR, hire_date, CURDATE())
DATE_FORMAT(d, '%d-%m-%Y %H:%i:%s')   STR_TO_DATE('01-01-2024','%d-%m-%Y')
YEAR(d) MONTH(d) DAY(d) DAYNAME(d) QUARTER(d) LAST_DAY(d)
```

### Conditional
```sql
SELECT name,
  CASE WHEN salary > 100000 THEN 'High'
       WHEN salary >  50000 THEN 'Medium'
       ELSE 'Low' END AS band
FROM employees;

SELECT IF(salary > 50000, 'High', 'Low') FROM employees;   -- MySQL-specific
```

---

## 8. GROUP BY & HAVING ⭐

```sql
SELECT dept_id, COUNT(*) AS headcount, AVG(salary) AS avg_sal, MAX(salary) AS top
FROM employees
WHERE hire_date >= '2020-01-01'      -- filters ROWS   (before grouping)
GROUP BY dept_id
HAVING AVG(salary) > 60000           -- filters GROUPS (after grouping)
ORDER BY avg_sal DESC;
```

| `WHERE` | `HAVING` |
|---|---|
| Filters individual rows | Filters grouped results |
| Runs **before** `GROUP BY` | Runs **after** `GROUP BY` |
| Cannot use aggregate functions | **Can** use aggregates |
| Works without `GROUP BY` | Normally used with `GROUP BY` |
| Can use indexes | Operates on the intermediate result |

> **ONLY_FULL_GROUP_BY** is enabled by default in MySQL 8. Every non-aggregated column in `SELECT` must be in `GROUP BY` (or functionally dependent on the grouping key). This is correct standard SQL — don't disable it.

`WITH ROLLUP` adds subtotal/grand-total rows:
```sql
SELECT dept_id, COUNT(*) FROM employees GROUP BY dept_id WITH ROLLUP;
```

---

## 9. JOINS ⭐⭐⭐ (the most-tested SQL topic)

```
   A        B                A        B              A        B
 ┌───┬────┬───┐           ┌───┬────┬───┐          ┌───┬────┬───┐
 │   │▓▓▓▓│   │           │▓▓▓│▓▓▓▓│   │          │   │▓▓▓▓│▓▓▓│
 └───┴────┴───┘           └───┴────┴───┘          └───┴────┴───┘
   INNER JOIN                LEFT JOIN               RIGHT JOIN
```

```sql
-- INNER JOIN — only matching rows on both sides
SELECT e.first_name, d.dept_name
FROM employees e INNER JOIN departments d ON e.dept_id = d.dept_id;

-- LEFT (OUTER) JOIN — all left rows; NULLs where no match
SELECT e.first_name, d.dept_name
FROM employees e LEFT JOIN departments d ON e.dept_id = d.dept_id;

-- Rows with NO match (anti-join) — very common question
SELECT e.first_name FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id
WHERE d.dept_id IS NULL;

-- RIGHT JOIN — all right rows
SELECT e.first_name, d.dept_name
FROM employees e RIGHT JOIN departments d ON e.dept_id = d.dept_id;

-- CROSS JOIN — Cartesian product (m × n rows)
SELECT * FROM sizes CROSS JOIN colors;

-- SELF JOIN — a table joined to itself (employee ↔ manager)
SELECT e.first_name AS employee, m.first_name AS manager
FROM employees e LEFT JOIN employees m ON e.manager_id = m.emp_id;

-- FULL OUTER JOIN — ❌ MySQL does not support it. Emulate:
SELECT * FROM a LEFT JOIN b ON a.id=b.id
UNION
SELECT * FROM a RIGHT JOIN b ON a.id=b.id;

-- NATURAL JOIN — auto-joins on same-named columns (avoid: fragile)
SELECT * FROM employees NATURAL JOIN departments;
```

| Join | Returns |
|---|---|
| `INNER` | intersection |
| `LEFT [OUTER]` | all left + matched right (NULL-filled) |
| `RIGHT [OUTER]` | all right + matched left |
| `FULL OUTER` | union of both (emulate in MySQL) |
| `CROSS` | every combination |
| `SELF` | any join type onto the same table |

⭐ **Trap:** putting the right-table filter in `WHERE` of a `LEFT JOIN` silently turns it into an inner join.
```sql
-- ❌ becomes INNER JOIN
FROM a LEFT JOIN b ON a.id = b.a_id WHERE b.status = 'X'
-- ✅ keeps it OUTER
FROM a LEFT JOIN b ON a.id = b.a_id AND b.status = 'X'
```

---

## 10. Subqueries & Set operations

```sql
-- Scalar subquery
SELECT * FROM employees WHERE salary > (SELECT AVG(salary) FROM employees);

-- IN / NOT IN
SELECT * FROM employees WHERE dept_id IN (SELECT dept_id FROM departments WHERE location='Chennai');

-- Correlated subquery (re-evaluated per outer row) — highest earner per dept
SELECT * FROM employees e
WHERE salary = (SELECT MAX(salary) FROM employees WHERE dept_id = e.dept_id);

-- EXISTS — usually faster than IN for large sets, and NULL-safe
SELECT * FROM departments d
WHERE EXISTS (SELECT 1 FROM employees e WHERE e.dept_id = d.dept_id);

-- Derived table (subquery in FROM — must be aliased)
SELECT dept_id, avg_sal FROM
  (SELECT dept_id, AVG(salary) avg_sal FROM employees GROUP BY dept_id) t
WHERE avg_sal > 60000;

-- ANY / ALL
SELECT * FROM employees WHERE salary > ALL (SELECT salary FROM employees WHERE dept_id=2);
```
> ⚠️ `NOT IN` with a subquery that returns any `NULL` returns **no rows**. Use `NOT EXISTS` instead.

### Set operators
```sql
SELECT city FROM customers UNION     SELECT city FROM suppliers;   -- dedupes (slower)
SELECT city FROM customers UNION ALL SELECT city FROM suppliers;   -- keeps duplicates (faster)
SELECT city FROM customers INTERSECT SELECT city FROM suppliers;   -- MySQL 8.0.31+
SELECT city FROM customers EXCEPT    SELECT city FROM suppliers;   -- MySQL 8.0.31+
```
Rules: same column count, compatible types, `ORDER BY` only at the very end.

---

## 11. CTEs & Window Functions (MySQL 8) ⭐ — impress the interviewer

### CTE — Common Table Expression
```sql
WITH dept_avg AS (
    SELECT dept_id, AVG(salary) AS avg_sal FROM employees GROUP BY dept_id
)
SELECT e.first_name, e.salary, d.avg_sal
FROM employees e JOIN dept_avg d ON e.dept_id = d.dept_id
WHERE e.salary > d.avg_sal;
```

### Recursive CTE — org chart / hierarchy
```sql
WITH RECURSIVE org AS (
    SELECT emp_id, first_name, manager_id, 1 AS lvl
    FROM employees WHERE manager_id IS NULL          -- anchor
  UNION ALL
    SELECT e.emp_id, e.first_name, e.manager_id, o.lvl + 1
    FROM employees e JOIN org o ON e.manager_id = o.emp_id   -- recursive
)
SELECT * FROM org ORDER BY lvl;
```

### Window functions
```sql
SELECT first_name, dept_id, salary,
  ROW_NUMBER()  OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rn,
  RANK()        OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rnk,
  DENSE_RANK()  OVER (PARTITION BY dept_id ORDER BY salary DESC) AS drnk,
  AVG(salary)   OVER (PARTITION BY dept_id)                      AS dept_avg,
  SUM(salary)   OVER (ORDER BY hire_date ROWS UNBOUNDED PRECEDING) AS running_total,
  LAG(salary)   OVER (ORDER BY hire_date)  AS prev_salary,
  LEAD(salary)  OVER (ORDER BY hire_date)  AS next_salary,
  NTILE(4)      OVER (ORDER BY salary)     AS quartile
FROM employees;
```
| Function | Ties behaviour on 100,100,90 |
|---|---|
| `ROW_NUMBER()` | 1, 2, 3 |
| `RANK()` | 1, 1, 3 (gap) |
| `DENSE_RANK()` | 1, 1, 2 (no gap) |

### ⭐ The classic: Nth highest salary — 4 ways
```sql
-- 1. LIMIT/OFFSET (simple, MySQL)
SELECT DISTINCT salary FROM employees ORDER BY salary DESC LIMIT 1 OFFSET 2;  -- 3rd

-- 2. DENSE_RANK (best, handles ties)
SELECT salary FROM (
  SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) r FROM employees
) t WHERE r = 3;

-- 3. Correlated subquery (portable)
SELECT DISTINCT e1.salary FROM employees e1
WHERE 3 - 1 = (SELECT COUNT(DISTINCT e2.salary) FROM employees e2 WHERE e2.salary > e1.salary);

-- 4. Per-department Nth highest
SELECT * FROM (
  SELECT *, DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) r FROM employees
) t WHERE r <= 3;
```

### Other classic queries
```sql
-- Duplicates
SELECT email, COUNT(*) c FROM employees GROUP BY email HAVING c > 1;

-- Delete duplicates keeping the lowest id
DELETE e1 FROM employees e1 JOIN employees e2
  ON e1.email = e2.email AND e1.emp_id > e2.emp_id;

-- Employees earning more than their manager
SELECT e.first_name FROM employees e JOIN employees m ON e.manager_id = m.emp_id
WHERE e.salary > m.salary;

-- Departments with no employees
SELECT d.* FROM departments d LEFT JOIN employees e ON d.dept_id=e.dept_id
WHERE e.emp_id IS NULL;

-- Second highest per department, top 3 overall, running totals → window functions above
```

---

## 12. Indexes ⭐⭐

An index is a separate sorted data structure (**B+Tree** in InnoDB) that turns an O(n) full table scan into an O(log n) lookup.

```sql
CREATE INDEX idx_lastname ON employees(last_name);
CREATE UNIQUE INDEX idx_email ON employees(email);
CREATE INDEX idx_dept_sal ON employees(dept_id, salary);   -- composite
CREATE FULLTEXT INDEX idx_bio ON employees(bio);
SHOW INDEX FROM employees;
DROP INDEX idx_lastname ON employees;
ANALYZE TABLE employees;    -- refresh optimizer statistics
```

### Index types
| Type | Notes |
|---|---|
| **Clustered** | InnoDB stores the *whole row* in the PK's B+Tree. **One per table.** If no PK, InnoDB uses the first non-null unique index, else a hidden rowid. |
| **Secondary / non-clustered** | Stores indexed columns + **the primary key**, so a non-covering lookup needs a second read into the clustered index (a *bookmark lookup*). |
| **Composite** | Multi-column; obeys the **leftmost prefix rule**. |
| **Covering** | The index alone answers the query (`Using index` in EXPLAIN) — no table access. |
| **Unique / Fulltext / Spatial / Hash** | Hash only in MEMORY engine. |

### Leftmost prefix rule ⭐
With `INDEX(a, b, c)`, the index can serve:
- ✅ `WHERE a=?` · `WHERE a=? AND b=?` · `WHERE a=? AND b=? AND c=?`
- ❌ `WHERE b=?` · `WHERE c=?` · `WHERE b=? AND c=?`
- ⚠️ `WHERE a=? AND c=?` → only the `a` part is used

### When an index is NOT used (index invalidation)
```sql
WHERE YEAR(hire_date) = 2024        -- ❌ function on the column
WHERE hire_date BETWEEN '2024-01-01' AND '2024-12-31'  -- ✅ sargable rewrite

WHERE name LIKE '%pranav'           -- ❌ leading wildcard
WHERE name LIKE 'pranav%'           -- ✅

WHERE emp_id + 0 = 5                -- ❌ arithmetic on the column
WHERE varchar_col = 123             -- ❌ implicit type conversion
WHERE col != 'X'  /  NOT IN         -- ❌ usually a scan
WHERE a = 1 OR b = 2                -- ⚠️ needs an index on both, or UNION
-- Also: low selectivity (e.g. gender) → optimizer prefers a full scan
```

### Costs of indexes
Faster `SELECT`, **slower** `INSERT`/`UPDATE`/`DELETE` (every index must be maintained), plus disk space. Index what you filter, join and sort on — not everything.

### Reading `EXPLAIN` ⭐
```sql
EXPLAIN SELECT * FROM employees WHERE dept_id = 1;
EXPLAIN ANALYZE SELECT ...;    -- MySQL 8.0.18+, shows ACTUAL timings
```
| Column | What to look for |
|---|---|
| `type` | best→worst: `system` > `const` > `eq_ref` > `ref` > `range` > `index` > **`ALL`** (full scan — red flag) |
| `key` | which index was actually chosen (`NULL` = none) |
| `rows` | estimated rows examined — lower is better |
| `filtered` | % of rows surviving the condition |
| `Extra` | ✅ `Using index` (covering). ⚠️ `Using filesort`, `Using temporary`, `Using where` with a big `rows` |

---

## 13. Transactions & ACID ⭐⭐

```sql
START TRANSACTION;              -- or BEGIN;
  UPDATE accounts SET balance = balance - 1000 WHERE id = 1;
  SAVEPOINT after_debit;
  UPDATE accounts SET balance = balance + 1000 WHERE id = 2;
  -- ROLLBACK TO after_debit;
COMMIT;                         -- or ROLLBACK;

SET autocommit = 0;             -- MySQL autocommits each statement by default
```

### ACID
| Property | Meaning | How InnoDB does it |
|---|---|---|
| **Atomicity** | all-or-nothing | undo log |
| **Consistency** | constraints hold before & after | constraints + triggers |
| **Isolation** | concurrent txns don't interfere | locking + **MVCC** |
| **Durability** | committed data survives a crash | redo log (WAL) + `fsync` |

### Concurrency anomalies
| Anomaly | Description |
|---|---|
| **Dirty read** | reading another txn's **uncommitted** change |
| **Non-repeatable read** | same row read twice gives different values (another txn UPDATEd+committed) |
| **Phantom read** | same range query returns **new rows** (another txn INSERTed) |
| **Lost update** | two txns overwrite each other's write |

### Isolation levels ⭐
| Level | Dirty | Non-repeatable | Phantom |
|---|:---:|:---:|:---:|
| `READ UNCOMMITTED` | ✅ possible | ✅ | ✅ |
| `READ COMMITTED` | ❌ | ✅ | ✅ |
| **`REPEATABLE READ`** *(MySQL default)* | ❌ | ❌ | ❌ in InnoDB (gap locks / next-key locks) |
| `SERIALIZABLE` | ❌ | ❌ | ❌ |

```sql
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
SELECT @@transaction_isolation;
```
> Higher isolation = more correctness, less concurrency. Oracle/PostgreSQL default to `READ COMMITTED`; MySQL/InnoDB defaults to `REPEATABLE READ` and — unusually — prevents phantoms there via next-key locking.

### Locking
```sql
SELECT * FROM accounts WHERE id=1 FOR UPDATE;         -- exclusive (pessimistic write lock)
SELECT * FROM accounts WHERE id=1 FOR SHARE;          -- shared read lock
SELECT ... FOR UPDATE NOWAIT | SKIP LOCKED;           -- 8.0+
```
- **Optimistic locking** — a `@Version` column; on update `WHERE version = :v`; 0 rows updated → `OptimisticLockException`. Best for low contention (this is what JPA `@Version` does).
- **Pessimistic locking** — `SELECT ... FOR UPDATE`; blocks others. Best for high contention.
- **Deadlock** — two txns wait on each other's locks. InnoDB detects it and rolls back the cheaper victim (`Error 1213`). **Prevention:** always acquire locks in the same order, keep transactions short, use the lowest workable isolation level.
```sql
SHOW ENGINE INNODB STATUS;   -- shows the LATEST DETECTED DEADLOCK
```

---

## 14. Normalization ⭐⭐

Organising data to reduce **redundancy** and avoid **insert/update/delete anomalies**.

### Unnormalized
| student_id | name | courses | dept | dept_head |
|---|---|---|---|---|
| 1 | Pranav | Java, SQL | CSE | Dr. Rao |

**1NF — atomic values, no repeating groups**
| student_id | name | course | dept | dept_head |
|---|---|---|---|---|
| 1 | Pranav | Java | CSE | Dr. Rao |
| 1 | Pranav | SQL | CSE | Dr. Rao |

**2NF — 1NF + no partial dependency** (no non-key attribute depends on *part* of a composite PK). PK here is (student_id, course); `name` depends only on `student_id` → split.
```
students(student_id, name, dept, dept_head)
enrollments(student_id, course)
```

**3NF — 2NF + no transitive dependency** (non-key → non-key). `dept_head` depends on `dept`, not on `student_id` → split.
```
students(student_id, name, dept_id)
departments(dept_id, dept_name, dept_head)
enrollments(student_id, course_id)
```

**BCNF** — stricter 3NF: for every functional dependency `X → Y`, `X` must be a super key.
**4NF** — no multi-valued dependencies. **5NF** — no join dependencies.

> **Rule of thumb:** normalize to **3NF** for OLTP (transactional apps like the ones in this repo). **Denormalize** deliberately for OLAP/reporting/read-heavy paths — trading redundancy for fewer joins.

**Memory hook:** *"The key (1NF), the whole key (2NF), and nothing but the key (3NF) — so help me Codd."*

---

## 15. Views, Stored Procedures, Functions, Triggers

```sql
-- VIEW — a stored query (virtual table)
CREATE VIEW v_high_earners AS
SELECT e.emp_id, e.first_name, e.salary, d.dept_name
FROM employees e JOIN departments d ON e.dept_id = d.dept_id
WHERE e.salary > 80000;

SELECT * FROM v_high_earners;
CREATE OR REPLACE VIEW ...;  DROP VIEW v_high_earners;
```
Views give security (expose a subset), simplicity, and abstraction. **Updatable** only if simple (no aggregates/DISTINCT/GROUP BY/UNION). MySQL has **no materialized views** (emulate with a table + scheduled event).

```sql
-- STORED PROCEDURE
DELIMITER //
CREATE PROCEDURE raise_salary(IN p_dept INT, IN p_pct DECIMAL(5,2), OUT p_count INT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; RESIGNAL; END;

    START TRANSACTION;
      UPDATE employees SET salary = salary * (1 + p_pct/100) WHERE dept_id = p_dept;
      SET p_count = ROW_COUNT();
    COMMIT;
END //
DELIMITER ;

CALL raise_salary(1, 10, @n);  SELECT @n;
```

```sql
-- STORED FUNCTION — returns a single value, usable in SELECT
DELIMITER //
CREATE FUNCTION annual_salary(p_monthly DECIMAL(10,2))
RETURNS DECIMAL(12,2) DETERMINISTIC READS SQL DATA
BEGIN RETURN p_monthly * 12; END //
DELIMITER ;

SELECT first_name, annual_salary(salary) FROM employees;
```

| Procedure | Function |
|---|---|
| `CALL` it | used inside expressions |
| 0..n values via `OUT`/`INOUT` | exactly **one** `RETURN` value |
| Can do DML/DDL & transactions | restricted (must be deterministic-ish) |
| Cannot be used in `SELECT` | can |

```sql
-- TRIGGER — automatic response to a table event
DELIMITER //
CREATE TRIGGER trg_emp_audit
AFTER UPDATE ON employees
FOR EACH ROW
BEGIN
    IF OLD.salary <> NEW.salary THEN
        INSERT INTO salary_audit(emp_id, old_sal, new_sal, changed_at)
        VALUES (OLD.emp_id, OLD.salary, NEW.salary, NOW());
    END IF;
END //
DELIMITER ;
```
Timing: `BEFORE`/`AFTER` × `INSERT`/`UPDATE`/`DELETE`. `NEW` available in INSERT/UPDATE; `OLD` in UPDATE/DELETE. Triggers don't fire on `TRUNCATE` or cascading FK actions.

> **MySQL vs Oracle PL/SQL:** MySQL's stored-program language is much simpler — no packages, no `%TYPE`/`%ROWTYPE`, different cursor syntax, `DECLARE ... HANDLER` instead of `EXCEPTION WHEN`. See [07_PLSQL.md](07_PLSQL.md) for the Oracle side used in the Week 1 exercises.

---

## 16. Users, Privileges & Backup
```sql
CREATE USER 'app'@'localhost' IDENTIFIED BY 'StrongPass!23';
GRANT SELECT, INSERT, UPDATE, DELETE ON cognizant_fse.* TO 'app'@'localhost';
GRANT ALL PRIVILEGES ON cognizant_fse.* TO 'admin'@'%';
REVOKE DELETE ON cognizant_fse.* FROM 'app'@'localhost';
FLUSH PRIVILEGES;
SHOW GRANTS FOR 'app'@'localhost';
```
```bash
mysqldump -u root -p cognizant_fse > backup.sql              # logical backup
mysqldump -u root -p --all-databases > all.sql
mysql -u root -p cognizant_fse < backup.sql                  # restore
```
**Principle of least privilege:** the app user should never be `root` and never have `DROP`.

---

## 17. Performance tuning checklist
1. **Index** the columns in `WHERE`, `JOIN`, `ORDER BY`, `GROUP BY`.
2. `EXPLAIN` every slow query; hunt `type: ALL` and `Using filesort`/`Using temporary`.
3. `SELECT` only needed columns — **never `SELECT *`** in application code (breaks covering indexes, wastes network, breaks on schema change).
4. Keep queries **sargable** (no functions on indexed columns).
5. Use `EXISTS`/`JOIN` instead of `IN (subquery)` for large sets; `UNION ALL` over `UNION`.
6. Avoid `N+1` — batch/join instead of looping queries (see [11_Spring_Data_JPA_Hibernate.md](11_Spring_Data_JPA_Hibernate.md)).
7. Right-size data types (`INT` not `BIGINT`, `VARCHAR(50)` not `VARCHAR(255)`).
8. Batch inserts; avoid autocommit-per-row.
9. Enable the **slow query log**: `slow_query_log=1`, `long_query_time=1`.
10. Tune `innodb_buffer_pool_size` to ~70% of RAM on a dedicated DB server.
11. Use **connection pooling** (HikariCP is the Spring Boot default).
12. Partition or archive very large tables; consider read replicas.

---

## 18. Connecting from Java

### Raw JDBC (know this — it's what JPA wraps)
```java
String url = "jdbc:mysql://localhost:3306/cognizant_fse?useSSL=false&serverTimezone=UTC";
try (Connection con = DriverManager.getConnection(url, "root", "pass");
     PreparedStatement ps = con.prepareStatement(
         "SELECT emp_id, first_name, salary FROM employees WHERE dept_id = ?")) {
    ps.setInt(1, 10);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            System.out.println(rs.getInt("emp_id") + " " + rs.getString("first_name"));
        }
    }
}
```
**JDBC steps:** load driver (auto since JDBC 4.0 via SPI) → get `Connection` → create `Statement`/`PreparedStatement` → execute → process `ResultSet` → close (use try-with-resources).

⭐ **`Statement` vs `PreparedStatement` vs `CallableStatement`**
| | Statement | PreparedStatement | CallableStatement |
|---|---|---|---|
| SQL | built by concatenation | **parameterised (`?`)** | calls a stored procedure |
| SQL injection | ❌ vulnerable | ✅ **safe** | ✅ |
| Precompiled | ❌ | ✅ (server-side plan cache) | ✅ |
| Use | never in real code | **always** | `{call proc(?,?)}` |

**SQL injection** — never do this:
```java
"SELECT * FROM users WHERE name='" + input + "'"    // input: ' OR '1'='1
```
Always bind parameters. In JPA, `@Query` with `:named` params is safe; string-concatenated JPQL is not.

### Spring Boot configuration
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cognizant_fse?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.datasource.hikari.maximum-pool-size=10
```
```xml
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
  <scope>runtime</scope>
</dependency>
```

---

## ⭐ Interview Gold — MySQL Rapid Fire

| Q | A |
|---|---|
| SQL vs MySQL? | SQL is the *language*; MySQL is an RDBMS *product* that implements it. |
| DDL vs DML vs DCL vs TCL? | Define / manipulate / control access / manage transactions. |
| Primary key vs unique key? | PK: one per table, NOT NULL, clustered. Unique: many allowed, permits NULLs. |
| `DELETE` vs `TRUNCATE` vs `DROP`? | (table in §3) |
| `WHERE` vs `HAVING`? | Rows before grouping vs groups after grouping; `HAVING` can use aggregates. |
| `UNION` vs `UNION ALL`? | UNION removes duplicates (sort/hash cost); UNION ALL doesn't — faster. |
| `IN` vs `EXISTS`? | `EXISTS` short-circuits on the first match, better for large correlated subqueries; `IN` is fine for small static lists. `NOT IN` breaks with NULLs. |
| Types of joins? | INNER, LEFT, RIGHT, FULL (not in MySQL), CROSS, SELF, NATURAL. |
| What is a self join? | Joining a table to itself with aliases — e.g. employee↔manager. |
| What is an index; downside? | A sorted lookup structure (B+Tree); costs write performance and storage. |
| Clustered vs non-clustered? | Clustered stores rows in index order (one per table, the PK in InnoDB); secondary stores key + PK pointer. |
| Why might an index not be used? | Function on the column, leading `%` wildcard, type mismatch, low selectivity, `OR` across columns, small table. |
| ACID? | Atomicity, Consistency, Isolation, Durability. |
| Isolation levels & anomalies? | (table in §13) |
| What is a deadlock; how to avoid? | Circular lock wait; consistent lock ordering, short transactions, lower isolation, retry logic. |
| Normalization & 3NF? | Reduce redundancy/anomalies; 3NF = no partial and no transitive dependencies. |
| When to denormalize? | Read-heavy reporting where join cost dominates; accept controlled redundancy. |
| View vs table? | A view is a stored query with no data of its own (MySQL has no materialized views). |
| Procedure vs function? | `CALL` vs expression-usable; OUT params vs a single return value. |
| Trigger use cases? | Auditing, derived columns, enforcing complex rules. Overuse hides logic — prefer app-level. |
| `CHAR` vs `VARCHAR`? | Fixed vs variable length; CHAR pads, faster for fixed-width codes. |
| `DATETIME` vs `TIMESTAMP`? | TIMESTAMP is UTC-normalised, 4 bytes, 2038 limit, auto-update capable. |
| How do you find the 2nd highest salary? | `DENSE_RANK()` window function, or `LIMIT 1 OFFSET 1` on DISTINCT ordered salaries. |
| How do you find/remove duplicates? | `GROUP BY … HAVING COUNT(*)>1`; self-join `DELETE` keeping `MIN(id)`. |
| What is a CTE? | A named temporary result set (`WITH`) — readable, reusable, supports recursion. |
| `RANK` vs `DENSE_RANK` vs `ROW_NUMBER`? | gaps / no gaps / always unique. |
| How do you prevent SQL injection? | `PreparedStatement` / bind parameters, least-privilege DB user, input validation, ORM. |
| How do you optimise a slow query? | `EXPLAIN` → add/fix index → make it sargable → select fewer columns → rewrite subquery as join → check statistics. |

---

## 📚 References
- MySQL 8.0 Reference Manual: https://dev.mysql.com/doc/refman/8.0/en/
- MySQL — Optimization: https://dev.mysql.com/doc/refman/8.0/en/optimization.html
- MySQL — InnoDB Locking & Transaction Model: https://dev.mysql.com/doc/refman/8.0/en/innodb-locking-transaction-model.html
- MySQL — Window Functions: https://dev.mysql.com/doc/refman/8.0/en/window-functions.html
- MySQL Connector/J: https://dev.mysql.com/doc/connector-j/en/
- Use The Index, Luke (best free SQL indexing resource): https://use-the-index-luke.com/
- Baeldung — Spring Boot with MySQL: https://www.baeldung.com/spring-boot-mysql
- Oracle JDBC Tutorial: https://docs.oracle.com/javase/tutorial/jdbc/
- W3Schools SQL (quick syntax lookup): https://www.w3schools.com/sql/
- SQLZoo / LeetCode Database (practice): https://sqlzoo.net/ · https://leetcode.com/studyplan/top-sql-50/
