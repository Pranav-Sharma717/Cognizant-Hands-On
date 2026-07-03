# Week1_PLSQL

## Exercise 1: Control Structures

### Scenario 1 Code

```sql
BEGIN
    FOR customer_rec IN (
        SELECT c.CustomerID, c.Name
        FROM Customers c
        WHERE FLOOR(MONTHS_BETWEEN(SYSDATE, c.DOB) / 12) > 60
    ) LOOP
        UPDATE Loans
        SET InterestRate = InterestRate - 1
        WHERE CustomerID = customer_rec.CustomerID;

        DBMS_OUTPUT.PUT_LINE('Discount applied for ' || customer_rec.Name);
    END LOOP;

    COMMIT;
END;
/
```

### Scenario 1 Output

```text
Discount applied for Robert King
PL/SQL procedure successfully completed.
```

### Scenario 2 Code

```sql
BEGIN
    FOR customer_rec IN (
        SELECT CustomerID, Name
        FROM Customers
        WHERE Balance > 10000
    ) LOOP
        UPDATE Customers
        SET IsVIP = 'TRUE'
        WHERE CustomerID = customer_rec.CustomerID;

        DBMS_OUTPUT.PUT_LINE(customer_rec.Name || ' promoted to VIP');
    END LOOP;

    COMMIT;
END;
/
```

### Scenario 2 Output

```text
Robert King promoted to VIP
PL/SQL procedure successfully completed.
```

### Scenario 3 Code

```sql
BEGIN
    FOR loan_rec IN (
        SELECT c.Name, l.LoanID, l.EndDate
        FROM Loans l
        JOIN Customers c ON l.CustomerID = c.CustomerID
        WHERE l.EndDate BETWEEN SYSDATE AND SYSDATE + 30
    ) LOOP
        DBMS_OUTPUT.PUT_LINE(
            'Reminder: Loan ' || loan_rec.LoanID ||
            ' for ' || loan_rec.Name ||
            ' is due on ' || TO_CHAR(loan_rec.EndDate, 'DD-MON-YYYY')
        );
    END LOOP;
END;
/
```

### Scenario 3 Output

```text
Reminder: Loan 2 for Robert King is due on 14-JUL-2026
PL/SQL procedure successfully completed.
```

## Exercise 2: Error Handling

### Scenario 1 Code

```sql
CREATE OR REPLACE PROCEDURE SafeTransferFunds (
    p_from_account IN NUMBER,
    p_to_account IN NUMBER,
    p_amount IN NUMBER
) AS
    v_balance NUMBER;
BEGIN
    SELECT Balance
    INTO v_balance
    FROM Accounts
    WHERE AccountID = p_from_account;

    IF v_balance < p_amount THEN
        RAISE_APPLICATION_ERROR(-20001, 'Insufficient funds');
    END IF;

    UPDATE Accounts
    SET Balance = Balance - p_amount
    WHERE AccountID = p_from_account;

    UPDATE Accounts
    SET Balance = Balance + p_amount
    WHERE AccountID = p_to_account;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Transfer completed successfully');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        INSERT INTO ErrorLog (ErrorMessage)
        VALUES ('SafeTransferFunds Error: ' || SQLERRM);
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Transfer failed: ' || SQLERRM);
END;
/

EXEC SafeTransferFunds(1, 2, 300);
```

### Scenario 1 Output

```text
Procedure created.
Transfer completed successfully
PL/SQL procedure successfully completed.
```

### Scenario 2 Code

```sql
CREATE OR REPLACE PROCEDURE UpdateSalary (
    p_employee_id IN NUMBER,
    p_percentage IN NUMBER
) AS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM Employees
    WHERE EmployeeID = p_employee_id;

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Employee not found');
    END IF;

    UPDATE Employees
    SET Salary = Salary + (Salary * p_percentage / 100)
    WHERE EmployeeID = p_employee_id;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Salary updated successfully');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        INSERT INTO ErrorLog (ErrorMessage)
        VALUES ('UpdateSalary Error: ' || SQLERRM);
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Salary update failed: ' || SQLERRM);
END;
/

EXEC UpdateSalary(2, 10);
```

### Scenario 2 Output

```text
Procedure created.
Salary updated successfully
PL/SQL procedure successfully completed.
```

### Scenario 3 Code

```sql
CREATE OR REPLACE PROCEDURE AddNewCustomer (
    p_customer_id IN NUMBER,
    p_name IN VARCHAR2,
    p_dob IN DATE,
    p_balance IN NUMBER
) AS
BEGIN
    INSERT INTO Customers (CustomerID, Name, DOB, Balance, IsVIP, LastModified)
    VALUES (p_customer_id, p_name, p_dob, p_balance, 'FALSE', SYSDATE);

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Customer added successfully');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        ROLLBACK;
        INSERT INTO ErrorLog (ErrorMessage)
        VALUES ('AddNewCustomer Error: Customer ID already exists');
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Customer insertion failed: Customer ID already exists');
    WHEN OTHERS THEN
        ROLLBACK;
        INSERT INTO ErrorLog (ErrorMessage)
        VALUES ('AddNewCustomer Error: ' || SQLERRM);
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Customer insertion failed: ' || SQLERRM);
END;
/

EXEC AddNewCustomer(4, 'Riya Sharma', TO_DATE('1998-11-02', 'YYYY-MM-DD'), 4500);
```

### Scenario 3 Output

```text
Procedure created.
Customer added successfully
PL/SQL procedure successfully completed.
```

## Exercise 3: Stored Procedures

### Scenario 1 Code

```sql
CREATE OR REPLACE PROCEDURE ProcessMonthlyInterest AS
BEGIN
    UPDATE Accounts
    SET Balance = Balance + (Balance * 0.01)
    WHERE AccountType = 'Savings';

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Monthly interest processed for savings accounts');
END;
/

EXEC ProcessMonthlyInterest;
```

### Scenario 1 Output

```text
Procedure created.
Monthly interest processed for savings accounts
PL/SQL procedure successfully completed.
```

### Scenario 2 Code

```sql
CREATE OR REPLACE PROCEDURE UpdateEmployeeBonus (
    p_department IN VARCHAR2,
    p_bonus_percentage IN NUMBER
) AS
BEGIN
    UPDATE Employees
    SET Salary = Salary + (Salary * p_bonus_percentage / 100)
    WHERE Department = p_department;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Bonus updated for department: ' || p_department);
END;
/

EXEC UpdateEmployeeBonus('IT', 5);
```

### Scenario 2 Output

```text
Procedure created.
Bonus updated for department: IT
PL/SQL procedure successfully completed.
```

### Scenario 3 Code

```sql
CREATE OR REPLACE PROCEDURE TransferFunds (
    p_from_account IN NUMBER,
    p_to_account IN NUMBER,
    p_amount IN NUMBER
) AS
    v_balance NUMBER;
BEGIN
    SELECT Balance
    INTO v_balance
    FROM Accounts
    WHERE AccountID = p_from_account;

    IF v_balance >= p_amount THEN
        UPDATE Accounts
        SET Balance = Balance - p_amount
        WHERE AccountID = p_from_account;

        UPDATE Accounts
        SET Balance = Balance + p_amount
        WHERE AccountID = p_to_account;

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Funds transferred successfully');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Insufficient balance');
    END IF;
END;
/

EXEC TransferFunds(2, 1, 200);
```

### Scenario 3 Output

```text
Procedure created.
Funds transferred successfully
PL/SQL procedure successfully completed.
```

## Exercise 4: Functions

### Scenario 1 Code

```sql
CREATE OR REPLACE FUNCTION CalculateAge (
    p_dob IN DATE
) RETURN NUMBER AS
BEGIN
    RETURN FLOOR(MONTHS_BETWEEN(SYSDATE, p_dob) / 12);
END;
/

SELECT CalculateAge(TO_DATE('1985-05-15', 'YYYY-MM-DD')) AS Age
FROM dual;
```

### Scenario 1 Output

```text
Function created.

AGE
---
41
```

### Scenario 2 Code

```sql
CREATE OR REPLACE FUNCTION CalculateMonthlyInstallment (
    p_loan_amount IN NUMBER,
    p_interest_rate IN NUMBER,
    p_years IN NUMBER
) RETURN NUMBER AS
    v_monthly_rate NUMBER;
    v_months NUMBER;
    v_emi NUMBER;
BEGIN
    v_monthly_rate := p_interest_rate / (12 * 100);
    v_months := p_years * 12;

    v_emi := p_loan_amount * v_monthly_rate * POWER(1 + v_monthly_rate, v_months)
             / (POWER(1 + v_monthly_rate, v_months) - 1);

    RETURN ROUND(v_emi, 2);
END;
/

SELECT CalculateMonthlyInstallment(5000, 5, 5) AS MonthlyInstallment
FROM dual;
```

### Scenario 2 Output

```text
Function created.

MONTHLYINSTALLMENT
------------------
94.36
```

### Scenario 3 Code

```sql
CREATE OR REPLACE FUNCTION HasSufficientBalance (
    p_account_id IN NUMBER,
    p_amount IN NUMBER
) RETURN BOOLEAN AS
    v_balance NUMBER;
BEGIN
    SELECT Balance
    INTO v_balance
    FROM Accounts
    WHERE AccountID = p_account_id;

    RETURN v_balance >= p_amount;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN FALSE;
END;
/

DECLARE
    v_result BOOLEAN;
BEGIN
    v_result := HasSufficientBalance(1, 500);

    IF v_result THEN
        DBMS_OUTPUT.PUT_LINE('Sufficient balance available');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Insufficient balance');
    END IF;
END;
/
```

### Scenario 3 Output

```text
Function created.
Sufficient balance available
PL/SQL procedure successfully completed.
```

## Exercise 5: Triggers

### Scenario 1 Code

```sql
CREATE OR REPLACE TRIGGER UpdateCustomerLastModified
BEFORE UPDATE ON Customers
FOR EACH ROW
BEGIN
    :NEW.LastModified := SYSDATE;
END;
/

UPDATE Customers
SET Balance = Balance + 500
WHERE CustomerID = 1;

COMMIT;
```

### Scenario 1 Output

```text
Trigger created.
1 row updated.
Commit complete.
```

### Scenario 2 Code

```sql
CREATE OR REPLACE TRIGGER LogTransaction
AFTER INSERT ON Transactions
FOR EACH ROW
BEGIN
    INSERT INTO AuditLog (TransactionID, AccountID, Amount, TransactionType)
    VALUES (:NEW.TransactionID, :NEW.AccountID, :NEW.Amount, :NEW.TransactionType);
END;
/

INSERT INTO Transactions
VALUES (3, 1, SYSDATE, 400, 'Deposit');

COMMIT;
```

### Scenario 2 Output

```text
Trigger created.
1 row inserted.
Commit complete.
```

### Scenario 3 Code

```sql
CREATE OR REPLACE TRIGGER CheckTransactionRules
BEFORE INSERT ON Transactions
FOR EACH ROW
DECLARE
    v_balance NUMBER;
BEGIN
    IF :NEW.TransactionType = 'Withdrawal' THEN
        SELECT Balance
        INTO v_balance
        FROM Accounts
        WHERE AccountID = :NEW.AccountID;

        IF :NEW.Amount > v_balance THEN
            RAISE_APPLICATION_ERROR(-20003, 'Withdrawal amount exceeds account balance');
        END IF;
    ELSIF :NEW.TransactionType = 'Deposit' THEN
        IF :NEW.Amount <= 0 THEN
            RAISE_APPLICATION_ERROR(-20004, 'Deposit amount must be positive');
        END IF;
    END IF;
END;
/

INSERT INTO Transactions
VALUES (4, 2, SYSDATE, 100, 'Withdrawal');

COMMIT;
```

### Scenario 3 Output

```text
Trigger created.
1 row inserted.
Commit complete.
```

## Exercise 6: Cursors

### Scenario 1 Code

```sql
DECLARE
    CURSOR GenerateMonthlyStatements IS
        SELECT c.Name, t.TransactionID, t.Amount, t.TransactionType, t.TransactionDate
        FROM Customers c
        JOIN Accounts a ON c.CustomerID = a.CustomerID
        JOIN Transactions t ON a.AccountID = t.AccountID
        WHERE EXTRACT(MONTH FROM t.TransactionDate) = EXTRACT(MONTH FROM SYSDATE)
        AND EXTRACT(YEAR FROM t.TransactionDate) = EXTRACT(YEAR FROM SYSDATE);
BEGIN
    FOR statement_rec IN GenerateMonthlyStatements LOOP
        DBMS_OUTPUT.PUT_LINE(
            statement_rec.Name || ' - Transaction ' ||
            statement_rec.TransactionID || ' - ' ||
            statement_rec.TransactionType || ' - ' ||
            statement_rec.Amount
        );
    END LOOP;
END;
/
```

### Scenario 1 Output

```text
John Doe - Transaction 1 - Deposit - 200
Jane Smith - Transaction 2 - Withdrawal - 300
John Doe - Transaction 3 - Deposit - 400
Jane Smith - Transaction 4 - Withdrawal - 100
PL/SQL procedure successfully completed.
```

### Scenario 2 Code

```sql
DECLARE
    CURSOR ApplyAnnualFee IS
        SELECT AccountID, Balance
        FROM Accounts;

    v_fee NUMBER := 100;
BEGIN
    FOR account_rec IN ApplyAnnualFee LOOP
        UPDATE Accounts
        SET Balance = Balance - v_fee
        WHERE AccountID = account_rec.AccountID;

        DBMS_OUTPUT.PUT_LINE('Annual fee applied to account ' || account_rec.AccountID);
    END LOOP;

    COMMIT;
END;
/
```

### Scenario 2 Output

```text
Annual fee applied to account 1
Annual fee applied to account 2
Annual fee applied to account 3
PL/SQL procedure successfully completed.
```

### Scenario 3 Code

```sql
DECLARE
    CURSOR UpdateLoanInterestRates IS
        SELECT LoanID, InterestRate
        FROM Loans;
BEGIN
    FOR loan_rec IN UpdateLoanInterestRates LOOP
        UPDATE Loans
        SET InterestRate = InterestRate + 0.5
        WHERE LoanID = loan_rec.LoanID;

        DBMS_OUTPUT.PUT_LINE('Interest rate updated for loan ' || loan_rec.LoanID);
    END LOOP;

    COMMIT;
END;
/
```

### Scenario 3 Output

```text
Interest rate updated for loan 1
Interest rate updated for loan 2
PL/SQL procedure successfully completed.
```

## Exercise 7: Packages

### Scenario 1 Code

```sql
CREATE OR REPLACE PACKAGE CustomerManagement AS
    PROCEDURE AddCustomer(p_customer_id NUMBER, p_name VARCHAR2, p_dob DATE, p_balance NUMBER);
    PROCEDURE UpdateCustomerDetails(p_customer_id NUMBER, p_name VARCHAR2, p_balance NUMBER);
    FUNCTION GetCustomerBalance(p_customer_id NUMBER) RETURN NUMBER;
END CustomerManagement;
/

CREATE OR REPLACE PACKAGE BODY CustomerManagement AS
    PROCEDURE AddCustomer(p_customer_id NUMBER, p_name VARCHAR2, p_dob DATE, p_balance NUMBER) AS
    BEGIN
        INSERT INTO Customers (CustomerID, Name, DOB, Balance, IsVIP, LastModified)
        VALUES (p_customer_id, p_name, p_dob, p_balance, 'FALSE', SYSDATE);
        COMMIT;
    END;

    PROCEDURE UpdateCustomerDetails(p_customer_id NUMBER, p_name VARCHAR2, p_balance NUMBER) AS
    BEGIN
        UPDATE Customers
        SET Name = p_name, Balance = p_balance
        WHERE CustomerID = p_customer_id;
        COMMIT;
    END;

    FUNCTION GetCustomerBalance(p_customer_id NUMBER) RETURN NUMBER AS
        v_balance NUMBER;
    BEGIN
        SELECT Balance INTO v_balance
        FROM Customers
        WHERE CustomerID = p_customer_id;

        RETURN v_balance;
    END;
END CustomerManagement;
/

BEGIN
    CustomerManagement.AddCustomer(5, 'Ankit Verma', TO_DATE('1996-01-10', 'YYYY-MM-DD'), 9000);
    DBMS_OUTPUT.PUT_LINE('Balance: ' || CustomerManagement.GetCustomerBalance(5));
END;
/
```

### Scenario 1 Output

```text
Package created.
Package body created.
Balance: 9000
PL/SQL procedure successfully completed.
```

### Scenario 2 Code

```sql
CREATE OR REPLACE PACKAGE EmployeeManagement AS
    PROCEDURE HireEmployee(p_employee_id NUMBER, p_name VARCHAR2, p_position VARCHAR2, p_salary NUMBER, p_department VARCHAR2);
    PROCEDURE UpdateEmployeeDetails(p_employee_id NUMBER, p_position VARCHAR2, p_salary NUMBER);
    FUNCTION CalculateAnnualSalary(p_employee_id NUMBER) RETURN NUMBER;
END EmployeeManagement;
/

CREATE OR REPLACE PACKAGE BODY EmployeeManagement AS
    PROCEDURE HireEmployee(p_employee_id NUMBER, p_name VARCHAR2, p_position VARCHAR2, p_salary NUMBER, p_department VARCHAR2) AS
    BEGIN
        INSERT INTO Employees (EmployeeID, Name, Position, Salary, Department, HireDate)
        VALUES (p_employee_id, p_name, p_position, p_salary, p_department, SYSDATE);
        COMMIT;
    END;

    PROCEDURE UpdateEmployeeDetails(p_employee_id NUMBER, p_position VARCHAR2, p_salary NUMBER) AS
    BEGIN
        UPDATE Employees
        SET Position = p_position, Salary = p_salary
        WHERE EmployeeID = p_employee_id;
        COMMIT;
    END;

    FUNCTION CalculateAnnualSalary(p_employee_id NUMBER) RETURN NUMBER AS
        v_salary NUMBER;
    BEGIN
        SELECT Salary INTO v_salary
        FROM Employees
        WHERE EmployeeID = p_employee_id;

        RETURN v_salary * 12;
    END;
END EmployeeManagement;
/

BEGIN
    EmployeeManagement.HireEmployee(3, 'Kiran Rao', 'Analyst', 50000, 'Finance');
    DBMS_OUTPUT.PUT_LINE('Annual Salary: ' || EmployeeManagement.CalculateAnnualSalary(3));
END;
/
```

### Scenario 2 Output

```text
Package created.
Package body created.
Annual Salary: 600000
PL/SQL procedure successfully completed.
```

### Scenario 3 Code

```sql
CREATE OR REPLACE PACKAGE AccountOperations AS
    PROCEDURE OpenAccount(p_account_id NUMBER, p_customer_id NUMBER, p_account_type VARCHAR2, p_balance NUMBER);
    PROCEDURE CloseAccount(p_account_id NUMBER);
    FUNCTION GetTotalBalance(p_customer_id NUMBER) RETURN NUMBER;
END AccountOperations;
/

CREATE OR REPLACE PACKAGE BODY AccountOperations AS
    PROCEDURE OpenAccount(p_account_id NUMBER, p_customer_id NUMBER, p_account_type VARCHAR2, p_balance NUMBER) AS
    BEGIN
        INSERT INTO Accounts (AccountID, CustomerID, AccountType, Balance, LastModified)
        VALUES (p_account_id, p_customer_id, p_account_type, p_balance, SYSDATE);
        COMMIT;
    END;

    PROCEDURE CloseAccount(p_account_id NUMBER) AS
    BEGIN
        DELETE FROM Accounts
        WHERE AccountID = p_account_id;
        COMMIT;
    END;

    FUNCTION GetTotalBalance(p_customer_id NUMBER) RETURN NUMBER AS
        v_total NUMBER;
    BEGIN
        SELECT NVL(SUM(Balance), 0)
        INTO v_total
        FROM Accounts
        WHERE CustomerID = p_customer_id;

        RETURN v_total;
    END;
END AccountOperations;
/

BEGIN
    AccountOperations.OpenAccount(4, 5, 'Savings', 3000);
    DBMS_OUTPUT.PUT_LINE('Total Balance: ' || AccountOperations.GetTotalBalance(5));
END;
/
```

### Scenario 3 Output

```text
Package created.
Package body created.
Total Balance: 3000
PL/SQL procedure successfully completed.
```
