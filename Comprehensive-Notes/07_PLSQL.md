# 07 — PL/SQL Programming (Oracle)

> Maps to [`Week1/Week1_PLSQL.md`](../Week1/Week1_PLSQL.md) — 7 exercises × 3 scenarios each.
> **Reference:** [Oracle PL/SQL Language Reference](https://docs.oracle.com/en/database/oracle/oracle-database/21/lnpls/)

---

## 1. What is PL/SQL?

**P**rocedural **L**anguage extension to **SQL** — Oracle's proprietary procedural language that embeds SQL inside block-structured code with variables, loops, conditionals, exception handling and modularity.

**Why it exists / why it's still used**
| Benefit | Explanation |
|---|---|
| **Reduced network traffic** | Send one `CALL` instead of 50 round-trip statements |
| **Performance** | Runs inside the database engine; no context switching per statement |
| **Modularity** | Procedures, functions, packages — reusable named units |
| **Security** | Grant `EXECUTE` on a procedure without granting table access |
| **Portability** | Runs anywhere Oracle runs |
| **Error handling** | Structured `EXCEPTION` blocks |

| | SQL | PL/SQL |
|---|---|---|
| Type | declarative (what) | procedural (how) |
| Execution | one statement at a time | a whole block at once |
| Control flow | none | IF, LOOP, CASE |
| Variables | no | yes |
| Error handling | no | `EXCEPTION` section |
| Can contain the other? | no | **yes** — SQL is embedded in PL/SQL |

---

## 2. Block structure ⭐

```plsql
DECLARE                              -- OPTIONAL: variables, cursors, exceptions, types
    v_salary   NUMBER(10,2);
    v_name     VARCHAR2(50) := 'Pranav';
    c_tax      CONSTANT NUMBER := 0.1;
    v_emp_id   employees.emp_id%TYPE;        -- anchored to the column's type
    v_emp_row  employees%ROWTYPE;            -- a whole row
BEGIN                                -- MANDATORY: executable statements
    SELECT salary INTO v_salary FROM employees WHERE emp_id = 101;
    DBMS_OUTPUT.PUT_LINE('Salary: ' || v_salary);
EXCEPTION                            -- OPTIONAL: error handling
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Employee not found');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;                                 -- MANDATORY
/
```
> `SET SERVEROUTPUT ON;` is required for `DBMS_OUTPUT` to appear.
> `%TYPE` / `%ROWTYPE` are **anchored declarations** — if the column type changes, your code adapts automatically. Always prefer them.

**Block types**
| Type | Stored? | Named? |
|---|---|---|
| **Anonymous** | ❌ compiled each run | ❌ |
| **Procedure** | ✅ in the data dictionary | ✅ |
| **Function** | ✅ | ✅ (returns a value) |
| **Package** | ✅ | ✅ (spec + body) |
| **Trigger** | ✅ | ✅ (event-driven) |

**Data types:** `NUMBER(p,s)`, `VARCHAR2(n)`, `CHAR(n)`, `DATE`, `TIMESTAMP`, `BOOLEAN` (PL/SQL only — not a SQL column type), `CLOB`/`BLOB`, `PLS_INTEGER`/`BINARY_INTEGER` (fastest for loop counters), `RECORD`, collections (`TABLE`, `VARRAY`, associative arrays).

---

## 3. Exercise 1 — Control Structures

### IF / ELSIF / ELSE
```plsql
IF v_salary > 100000 THEN
    v_grade := 'A';
ELSIF v_salary > 50000 THEN
    v_grade := 'B';
ELSE
    v_grade := 'C';
END IF;
```

### CASE
```plsql
-- simple CASE
CASE v_dept
    WHEN 'IT'    THEN v_bonus := 5000;
    WHEN 'SALES' THEN v_bonus := 7000;
    ELSE              v_bonus := 2000;
END CASE;

-- searched CASE
CASE
    WHEN v_salary > 100000 THEN v_grade := 'A';
    WHEN v_salary >  50000 THEN v_grade := 'B';
    ELSE                        v_grade := 'C';
END CASE;

-- CASE as an expression
v_grade := CASE WHEN v_salary > 50000 THEN 'High' ELSE 'Low' END;
```

### Loops
```plsql
-- BASIC LOOP — always runs at least once, needs an explicit EXIT
LOOP
    v_i := v_i + 1;
    EXIT WHEN v_i > 10;
END LOOP;

-- WHILE LOOP — condition checked first (may run zero times)
WHILE v_i <= 10 LOOP
    DBMS_OUTPUT.PUT_LINE(v_i);
    v_i := v_i + 1;
END LOOP;

-- FOR LOOP — counter is implicitly declared, read-only, and scoped to the loop
FOR i IN 1..10 LOOP
    DBMS_OUTPUT.PUT_LINE(i);
END LOOP;

FOR i IN REVERSE 1..10 LOOP ... END LOOP;

-- CURSOR FOR LOOP — implicit open/fetch/close ⭐ cleanest way to iterate rows
FOR rec IN (SELECT emp_id, salary FROM employees WHERE dept_id = 10) LOOP
    DBMS_OUTPUT.PUT_LINE(rec.emp_id || ': ' || rec.salary);
END LOOP;

-- CONTINUE (11g+) and labelled loops
<<outer>>
FOR i IN 1..5 LOOP
    CONTINUE outer WHEN i = 3;
END LOOP outer;
```

### Worked scenario — apply a 1% discount to loans for customers over 60
```plsql
BEGIN
    FOR c IN (SELECT customer_id, dob FROM customers) LOOP
        IF MONTHS_BETWEEN(SYSDATE, c.dob)/12 > 60 THEN
            UPDATE loans SET interest_rate = interest_rate - 1
            WHERE customer_id = c.customer_id;
        END IF;
    END LOOP;
    COMMIT;
END;
/
```

---

## 4. Exercise 2 — Exception Handling ⭐

```plsql
BEGIN
    ...
EXCEPTION
    WHEN NO_DATA_FOUND THEN      ...
    WHEN TOO_MANY_ROWS THEN      ...
    WHEN ZERO_DIVIDE THEN        ...
    WHEN DUP_VAL_ON_INDEX THEN   ...
    WHEN OTHERS THEN             -- must be LAST; catch-all
        DBMS_OUTPUT.PUT_LINE(SQLCODE || ': ' || SQLERRM);
        ROLLBACK;
        RAISE;                   -- re-raise so the caller knows
END;
```

**Three exception categories**
| Type | Example | Declaration |
|---|---|---|
| **Predefined** | `NO_DATA_FOUND`, `ZERO_DIVIDE`, `TOO_MANY_ROWS`, `DUP_VAL_ON_INDEX`, `INVALID_CURSOR`, `VALUE_ERROR`, `INVALID_NUMBER`, `CURSOR_ALREADY_OPEN` | none needed |
| **Non-predefined** (an ORA- error with no name) | ORA-02292 (child record found) | `PRAGMA EXCEPTION_INIT` |
| **User-defined** | business rules | `DECLARE e EXCEPTION;` + `RAISE e;` |

```plsql
DECLARE
    e_insufficient_funds EXCEPTION;                   -- user-defined
    e_child_found        EXCEPTION;
    PRAGMA EXCEPTION_INIT(e_child_found, -2292);      -- bind to an ORA- code
    v_balance NUMBER;
BEGIN
    SELECT balance INTO v_balance FROM accounts WHERE acc_id = 1;
    IF v_balance < 1000 THEN
        RAISE e_insufficient_funds;
    END IF;
EXCEPTION
    WHEN e_insufficient_funds THEN
        DBMS_OUTPUT.PUT_LINE('Insufficient balance');
    WHEN e_child_found THEN
        DBMS_OUTPUT.PUT_LINE('Cannot delete: child rows exist');
END;
/

-- RAISE_APPLICATION_ERROR: return a custom error to the CALLING APPLICATION
-- error numbers must be in -20000 .. -20999
RAISE_APPLICATION_ERROR(-20001, 'Insufficient balance for account ' || v_acc);
```

**Key facts**
- `SQLCODE` — the Oracle error number; `SQLERRM` — the message.
- `WHEN OTHERS` must come last, and should log/re-raise, never silently swallow.
- An exception in the `DECLARE` section propagates to the **enclosing** block, not the local handler.
- Once handled, execution continues **after** the block — it does not resume where it failed.

---

## 5. Exercise 3 — Stored Procedures

```plsql
CREATE OR REPLACE PROCEDURE process_monthly_interest (
    p_acc_id   IN     NUMBER,                -- IN    (default) read-only
    p_rate     IN     NUMBER DEFAULT 0.05,
    p_new_bal     OUT NUMBER,                -- OUT   write-only, returned to caller
    p_note     IN OUT VARCHAR2               -- IN OUT both
) IS
    v_bal NUMBER;
BEGIN
    SELECT balance INTO v_bal FROM accounts WHERE acc_id = p_acc_id FOR UPDATE;
    p_new_bal := v_bal * (1 + p_rate);
    UPDATE accounts SET balance = p_new_bal WHERE acc_id = p_acc_id;
    p_note := 'Interest applied';
    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20002, 'Account not found: ' || p_acc_id);
    WHEN OTHERS THEN
        ROLLBACK; RAISE;
END process_monthly_interest;
/

-- calling
DECLARE
    v_bal NUMBER; v_note VARCHAR2(100) := '';
BEGIN
    process_monthly_interest(101, 0.06, v_bal, v_note);
    DBMS_OUTPUT.PUT_LINE(v_bal || ' - ' || v_note);
END;
/

EXEC process_monthly_interest(101, 0.06, :bal, :note);      -- SQL*Plus
-- named notation (clearest with many params)
process_monthly_interest(p_acc_id => 101, p_new_bal => v_bal, p_note => v_note);
```

---

## 6. Exercise 4 — Functions

```plsql
CREATE OR REPLACE FUNCTION calculate_age (p_dob IN DATE)
RETURN NUMBER
IS
    v_age NUMBER;
BEGIN
    v_age := TRUNC(MONTHS_BETWEEN(SYSDATE, p_dob) / 12);
    RETURN v_age;
EXCEPTION
    WHEN OTHERS THEN RETURN NULL;
END calculate_age;
/

SELECT name, calculate_age(dob) AS age FROM customers;   -- usable in SQL ✅
```
**A function usable in a SQL statement must not** modify database state (no DML) or commit/rollback — otherwise you get `ORA-14551: cannot perform a DML operation inside a query`.

⭐ **Procedure vs Function**
| | Procedure | Function |
|---|---|---|
| Returns | 0..n values via `OUT` params | exactly **one** value via `RETURN` |
| `RETURN` clause | not allowed in the header | **mandatory** |
| Callable from SQL | ❌ | ✅ (if it's read-only) |
| Called as | a statement (`EXEC`, `CALL`) | part of an expression |
| Typical use | perform an action / DML | compute and return a value |

---

## 7. Exercise 5 — Triggers ⭐

```plsql
CREATE OR REPLACE TRIGGER trg_audit_salary
AFTER UPDATE OF salary ON employees
FOR EACH ROW                                  -- ROW-LEVEL (omit → statement-level)
WHEN (OLD.salary <> NEW.salary)               -- optional guard
DECLARE
    PRAGMA AUTONOMOUS_TRANSACTION;            -- lets the audit commit independently
BEGIN
    INSERT INTO salary_audit(emp_id, old_sal, new_sal, changed_by, changed_on)
    VALUES (:OLD.emp_id, :OLD.salary, :NEW.salary, USER, SYSDATE);
    COMMIT;
END;
/

-- BEFORE INSERT: derive/validate values
CREATE OR REPLACE TRIGGER trg_emp_bi
BEFORE INSERT ON employees
FOR EACH ROW
BEGIN
    :NEW.created_on := SYSDATE;
    :NEW.email := LOWER(:NEW.email);
    IF :NEW.salary <= 0 THEN
        RAISE_APPLICATION_ERROR(-20010, 'Salary must be positive');
    END IF;
END;
/

-- COMPOUND TRIGGER (11g+) — the standard fix for the mutating-table error
CREATE OR REPLACE TRIGGER trg_compound
FOR UPDATE ON employees COMPOUND TRIGGER
    BEFORE STATEMENT IS BEGIN NULL; END BEFORE STATEMENT;
    AFTER  EACH ROW  IS BEGIN NULL; END AFTER EACH ROW;
    AFTER  STATEMENT IS BEGIN NULL; END AFTER STATEMENT;
END trg_compound;
/
```

**Classification**
- Timing: `BEFORE` | `AFTER` | `INSTEAD OF` (only on views)
- Event: `INSERT` | `UPDATE [OF col]` | `DELETE` (combinable with `OR`)
- Level: `FOR EACH ROW` (row-level, `:OLD`/`:NEW` available) vs statement-level (fires once)
- Also: DDL triggers, database event triggers (`AFTER LOGON ON DATABASE`)

| Event | `:OLD` | `:NEW` |
|---|---|---|
| INSERT | NULL | ✅ new values |
| UPDATE | ✅ old | ✅ new |
| DELETE | ✅ old | NULL |

⚠️ **Mutating table error (ORA-04091)** — a row-level trigger cannot query or modify the table it fires on. Fix with a compound trigger, an autonomous transaction, or (better) move the logic into a procedure.
**Uses:** auditing, derived columns, complex validation, enforcing business rules, maintaining denormalised copies.
**Caution:** triggers hide logic and complicate debugging. Prefer explicit application/service-layer logic where you can.

---

## 8. Exercise 6 — Cursors ⭐

A cursor is a pointer to the context area holding a query's result set.

### Implicit cursor (`SQL%`) — created automatically for every SQL statement
```plsql
UPDATE employees SET salary = salary * 1.1 WHERE dept_id = 10;
DBMS_OUTPUT.PUT_LINE(SQL%ROWCOUNT || ' rows updated');
IF SQL%FOUND THEN ... END IF;
```
Attributes: `%FOUND`, `%NOTFOUND`, `%ROWCOUNT`, `%ISOPEN` (always FALSE for implicit).

### Explicit cursor — you control it, for multi-row processing
```plsql
DECLARE
    CURSOR c_emp IS
        SELECT emp_id, name, salary FROM employees WHERE dept_id = 10;
    v_emp c_emp%ROWTYPE;
BEGIN
    OPEN c_emp;
    LOOP
        FETCH c_emp INTO v_emp;
        EXIT WHEN c_emp%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE(v_emp.name || ': ' || v_emp.salary);
    END LOOP;
    CLOSE c_emp;
END;
/
```
**Lifecycle:** DECLARE → OPEN → FETCH (loop) → CLOSE. Forgetting `CLOSE` leaks cursors (`ORA-01000: maximum open cursors exceeded`).

### Cursor FOR loop — ✅ preferred (implicit open/fetch/close/exit)
```plsql
FOR rec IN (SELECT emp_id, name FROM employees WHERE dept_id = 10) LOOP
    DBMS_OUTPUT.PUT_LINE(rec.name);
END LOOP;
```

### Parameterised cursor
```plsql
CURSOR c_emp(p_dept NUMBER) IS SELECT * FROM employees WHERE dept_id = p_dept;
OPEN c_emp(10);
```

### `FOR UPDATE` / `WHERE CURRENT OF` — row-by-row updates with locking
```plsql
DECLARE
    CURSOR c IS SELECT emp_id, salary FROM employees FOR UPDATE OF salary;
BEGIN
    FOR r IN c LOOP
        UPDATE employees SET salary = r.salary * 1.1 WHERE CURRENT OF c;
    END LOOP;
    COMMIT;
END;
/
```

### `REF CURSOR` — a cursor variable, returnable to Java/JDBC
```plsql
TYPE t_ref IS REF CURSOR;
PROCEDURE get_emps(p_cur OUT SYS_REFCURSOR) IS
BEGIN OPEN p_cur FOR SELECT * FROM employees; END;
```

### ⭐ BULK COLLECT / FORALL — the performance answer
Row-by-row PL/SQL loops cause a context switch per row. Bulk operations batch them:
```plsql
DECLARE
    TYPE t_ids IS TABLE OF employees.emp_id%TYPE;
    v_ids t_ids;
BEGIN
    SELECT emp_id BULK COLLECT INTO v_ids FROM employees WHERE dept_id = 10;

    FORALL i IN v_ids.FIRST .. v_ids.LAST
        UPDATE employees SET salary = salary * 1.1 WHERE emp_id = v_ids(i);
    COMMIT;
END;
/
```
Use `LIMIT` in a fetch loop for very large sets to bound PGA memory. *Mentioning BULK COLLECT/FORALL marks you as beyond-basics.*

---

## 9. Exercise 7 — Packages ⭐

A **package** groups related procedures, functions, variables, cursors, types and exceptions into one schema object. Two parts:

```plsql
-- SPECIFICATION — the public interface (what clients see)
CREATE OR REPLACE PACKAGE emp_pkg IS
    g_company CONSTANT VARCHAR2(50) := 'Cognizant';        -- public constant

    PROCEDURE hire_employee(p_name VARCHAR2, p_sal NUMBER, p_dept NUMBER);
    FUNCTION  get_annual_salary(p_emp_id NUMBER) RETURN NUMBER;
    FUNCTION  count_by_dept(p_dept NUMBER) RETURN NUMBER;

    e_invalid_salary EXCEPTION;                             -- public exception
END emp_pkg;
/

-- BODY — the implementation (hidden)
CREATE OR REPLACE PACKAGE BODY emp_pkg IS

    v_hire_count NUMBER := 0;                               -- PRIVATE package state
    FUNCTION validate(p_sal NUMBER) RETURN BOOLEAN IS       -- PRIVATE helper
    BEGIN RETURN p_sal > 0; END;

    PROCEDURE hire_employee(p_name VARCHAR2, p_sal NUMBER, p_dept NUMBER) IS
    BEGIN
        IF NOT validate(p_sal) THEN RAISE e_invalid_salary; END IF;
        INSERT INTO employees(name, salary, dept_id) VALUES (p_name, p_sal, p_dept);
        v_hire_count := v_hire_count + 1;
        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN ROLLBACK; RAISE;
    END hire_employee;

    FUNCTION get_annual_salary(p_emp_id NUMBER) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT salary * 12 INTO v FROM employees WHERE emp_id = p_emp_id;
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN 0;
    END get_annual_salary;

    FUNCTION count_by_dept(p_dept NUMBER) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v FROM employees WHERE dept_id = p_dept;
        RETURN v;
    END count_by_dept;

BEGIN                                                       -- initialisation section
    v_hire_count := 0;                                      -- runs once per session
END emp_pkg;
/

-- usage
EXEC emp_pkg.hire_employee('Pranav', 60000, 10);
SELECT emp_pkg.get_annual_salary(101) FROM dual;
```

**Why packages ⭐**
1. **Encapsulation** — declare in the body only → private
2. **Modularity / organisation** — related logic in one unit
3. **Performance** — the whole package is loaded into memory on first call
4. **Overloading** — same name, different parameter lists (not possible with standalone procedures)
5. **Session state** — package variables persist for the session
6. **Avoids recompilation cascades** — changing the body doesn't invalidate dependents; changing the spec does
7. Enables **forward declarations** and mutually recursive routines

**Useful Oracle-supplied packages:** `DBMS_OUTPUT`, `DBMS_SQL`, `DBMS_SCHEDULER`, `DBMS_LOB`, `UTL_FILE`, `DBMS_STATS`, `UTL_HTTP`.

---

## 10. Records & Collections
```plsql
-- RECORD
TYPE emp_rec IS RECORD (id NUMBER, name VARCHAR2(50), sal NUMBER);
v_emp emp_rec;

-- ASSOCIATIVE ARRAY (index-by table) — sparse, PL/SQL only
TYPE t_map IS TABLE OF NUMBER INDEX BY VARCHAR2(50);
v_map t_map;
v_map('pranav') := 60000;

-- NESTED TABLE — dense, can be a column type
TYPE t_nums IS TABLE OF NUMBER;
v_nums t_nums := t_nums(1,2,3);
v_nums.EXTEND; v_nums(4) := 4;

-- VARRAY — fixed maximum size, ordered
TYPE t_varr IS VARRAY(10) OF VARCHAR2(20);
```
Methods: `COUNT`, `FIRST`, `LAST`, `NEXT`, `PRIOR`, `EXISTS(i)`, `EXTEND`, `TRIM`, `DELETE`, `LIMIT`.

---

## 11. Transactions in PL/SQL
```plsql
BEGIN
    UPDATE accounts SET balance = balance - 1000 WHERE acc_id = 1;
    SAVEPOINT after_debit;
    UPDATE accounts SET balance = balance + 1000 WHERE acc_id = 2;
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN ROLLBACK TO after_debit; RAISE;
END;
/
```
`PRAGMA AUTONOMOUS_TRANSACTION` — the routine gets its own independent transaction (commits without affecting the caller). Essential for audit logging that must survive a rollback.

---

## 12. Oracle PL/SQL vs MySQL stored programs

| | Oracle PL/SQL | MySQL |
|---|---|---|
| Packages | ✅ | ❌ |
| `%TYPE` / `%ROWTYPE` | ✅ | ❌ |
| Exception handling | `EXCEPTION WHEN ... THEN` | `DECLARE ... HANDLER FOR` |
| Cursor FOR loop | ✅ | ❌ (manual open/fetch/close) |
| `BULK COLLECT` / `FORALL` | ✅ | ❌ |
| Output | `DBMS_OUTPUT.PUT_LINE` | `SELECT` |
| Delimiter | `/` | `DELIMITER //` |
| Sequences | `seq.NEXTVAL` | `AUTO_INCREMENT` (sequences from 8.0 in limited form) |
| Autonomous txn | ✅ | ❌ |
| Row limiting | `ROWNUM` / `FETCH FIRST n ROWS` | `LIMIT` |
| Null-safe / string concat | `NVL`, `\|\|` | `IFNULL`, `CONCAT()` |

See [03_MySQL.md §15](03_MySQL.md) for the MySQL equivalents.

---

## 13. Calling PL/SQL from Java (the FSE link)
```java
// Procedure with an OUT parameter
try (CallableStatement cs = con.prepareCall("{call process_monthly_interest(?,?,?,?)}")) {
    cs.setInt(1, 101);
    cs.setDouble(2, 0.06);
    cs.registerOutParameter(3, Types.NUMERIC);
    cs.registerOutParameter(4, Types.VARCHAR);
    cs.execute();
    double newBal = cs.getDouble(3);
}
```
```java
// Spring Data JPA
@Entity
@NamedStoredProcedureQuery(
    name = "Employee.annualSalary",
    procedureName = "get_annual_salary",
    parameters = {
        @StoredProcedureParameter(mode = ParameterMode.IN,  name = "p_emp_id", type = Long.class),
        @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_result", type = Double.class)})
public class Employee { }

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Procedure(name = "Employee.annualSalary")
    Double annualSalary(@Param("p_emp_id") Long id);
}
```

---

## ⭐ Interview Gold — PL/SQL Rapid Fire

| Q | A |
|---|---|
| What is PL/SQL and why use it? | Oracle's procedural extension to SQL — reduces network round trips, adds control flow, error handling, and modularity. |
| PL/SQL block sections? | `DECLARE` (optional), `BEGIN` (mandatory), `EXCEPTION` (optional), `END`. |
| `%TYPE` vs `%ROWTYPE`? | Anchors a variable to a **column's** type vs a whole **row's** structure. |
| Procedure vs Function? | 0..n OUT values vs one RETURN value; only a read-only function is callable from SQL. |
| What is a package and why use it? | Spec + body grouping related code — encapsulation, overloading, session state, performance, fewer recompilations. |
| Implicit vs explicit cursor? | Auto-created for every SQL statement (`SQL%ROWCOUNT`) vs declared by you for multi-row processing. |
| Cursor attributes? | `%FOUND`, `%NOTFOUND`, `%ROWCOUNT`, `%ISOPEN`. |
| Why a cursor FOR loop? | Implicit open/fetch/exit/close — shorter and leak-proof. |
| What is `BULK COLLECT`/`FORALL`? | Batch fetch/DML that eliminates per-row SQL↔PL/SQL context switches — big performance win. |
| Trigger types? | BEFORE/AFTER/INSTEAD OF × INSERT/UPDATE/DELETE, row- vs statement-level; plus DDL and database-event triggers. |
| `:OLD` and `:NEW`? | Row-level trigger pseudo-records for the pre- and post-change values. |
| Mutating table error? | A row-level trigger querying/modifying its own table (ORA-04091) — fix with a compound trigger or autonomous transaction. |
| Exception types? | Predefined, non-predefined (`PRAGMA EXCEPTION_INIT`), user-defined. |
| `RAISE` vs `RAISE_APPLICATION_ERROR`? | Re-raise/raise a PL/SQL exception vs return a custom numbered error (−20000..−20999) to the client application. |
| `SQLCODE` / `SQLERRM`? | Current error number / message inside a handler. |
| What does `PRAGMA AUTONOMOUS_TRANSACTION` do? | Runs the block in its own independent transaction that can commit regardless of the caller. |
| `DELETE` vs `TRUNCATE` in Oracle? | DML/rollbackable/fires triggers vs DDL/implicit commit/resets the high-water mark. |
| Can a function do DML? | Only if it's not called from a SQL statement; otherwise ORA-14551. |

---

## 📚 References
- Oracle PL/SQL Language Reference: https://docs.oracle.com/en/database/oracle/oracle-database/21/lnpls/
- Oracle Database SQL Language Reference: https://docs.oracle.com/en/database/oracle/oracle-database/21/sqlrf/
- Oracle Live SQL (free browser sandbox — practise here): https://livesql.oracle.com/
- Oracle-Base (excellent PL/SQL articles): https://oracle-base.com/articles/
- Ask TOM: https://asktom.oracle.com/
- *Oracle PL/SQL Programming*, Steven Feuerstein — the definitive book
