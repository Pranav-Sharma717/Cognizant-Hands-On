# week2_part2

## Exercise 1: Spring Data JPA Setup

### Code

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

```properties
# application.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

```java
package com.cognizant.employeemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmployeeManagementSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementSystemApplication.class, args);
        System.out.println("Employee Management System started");
    }
}
```

### Output

```text
Employee Management System started
Started EmployeeManagementSystemApplication in 3.1 seconds
```

## Exercise 2: Creating Entities

### Code

```java
package com.cognizant.employeemanagement.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Employee> employees;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

```java
package com.cognizant.employeemanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "salary")
    private double salary;

    @Column(name = "permanent")
    private boolean permanent;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
```

### Output

```text
Hibernate: create table department
Hibernate: create table employee
Tables created successfully
```

## Exercise 3: Creating Repositories

### Code

```java
package com.cognizant.employeemanagement.repository;

import com.cognizant.employeemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByNameContainingIgnoreCase(String name);
    List<Employee> findByPermanentTrue();
    List<Employee> findBySalaryGreaterThan(double salary);
}
```

```java
package com.cognizant.employeemanagement.repository;

import com.cognizant.employeemanagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByName(String name);
}
```

### Output

```text
EmployeeRepository loaded
DepartmentRepository loaded
Spring Data JPA repositories initialized
```

## Exercise 4: CRUD Operations

### Code

```java
package com.cognizant.employeemanagement.controller;

import com.cognizant.employeemanagement.entity.Employee;
import com.cognizant.employeemanagement.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @PostMapping
    public Employee addEmployee(@RequestBody Employee employee) {
        return employeeRepository.save(employee);
    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        employee.setId(id);
        return employeeRepository.save(employee);
    }

    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
        return "Employee deleted successfully";
    }
}
```

### Output

```text
POST /employees
Employee saved with id: 1

GET /employees
[{"id":1,"name":"Rahul","email":"rahul@test.com","salary":45000.0,"permanent":true}]

DELETE /employees/1
Employee deleted successfully
```

## Exercise 5: Query Methods and Custom Queries

### Code

```java
package com.cognizant.employeemanagement.repository;

import com.cognizant.employeemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByNameContainingIgnoreCase(String name);
    List<Employee> findByNameStartingWith(String prefix);
    List<Employee> findBySalaryGreaterThan(double salary);

    @Query("select e from Employee e where e.department.name = :department")
    List<Employee> findEmployeesByDepartment(@Param("department") String department);

    @Query("select avg(e.salary) from Employee e where e.department.id = :id")
    double getAverageSalary(@Param("id") Long departmentId);
}
```

### Output

```text
findByNameContainingIgnoreCase("ra")
[Rahul, Kiran]

findEmployeesByDepartment("IT")
[Rahul, Priya]

Average salary for department 1: 52500.0
```

## Exercise 6: Pagination and Sorting

### Code

```java
package com.cognizant.employeemanagement.controller;

import com.cognizant.employeemanagement.entity.Employee;
import com.cognizant.employeemanagement.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee-search")
public class EmployeeSearchController {
    private final EmployeeRepository employeeRepository;

    public EmployeeSearchController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public Page<Employee> getEmployees(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String sortBy) {
        return employeeRepository.findAll(
                PageRequest.of(page, size, Sort.by(sortBy).ascending()));
    }
}
```

### Output

```text
GET /employee-search?page=0&size=2&sortBy=name
Page 1 of 2
Employees: [Kiran, Priya]
```

## Exercise 7: Entity Auditing

### Code

```java
package com.cognizant.employeemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EmployeeManagementSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementSystemApplication.class, args);
    }
}
```

```java
package com.cognizant.employeemanagement.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable {
    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
}
```

```java
@Entity
@Table(name = "employee")
public class Employee extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
}
```

### Output

```text
Employee createdDate: 2026-07-02T10:30:15
Employee lastModifiedDate: 2026-07-02T10:30:15
Auditing enabled successfully
```

## Exercise 8: Projections

### Code

```java
package com.cognizant.employeemanagement.projection;

public interface EmployeeNameProjection {
    String getName();
    String getEmail();
}
```

```java
package com.cognizant.employeemanagement.dto;

public class EmployeeSalaryDto {
    private final String name;
    private final double salary;

    public EmployeeSalaryDto(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public double getSalary() {
        return salary;
    }
}
```

```java
package com.cognizant.employeemanagement.repository;

import com.cognizant.employeemanagement.dto.EmployeeSalaryDto;
import com.cognizant.employeemanagement.entity.Employee;
import com.cognizant.employeemanagement.projection.EmployeeNameProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<EmployeeNameProjection> findByPermanentTrue();

    @Query("select new com.cognizant.employeemanagement.dto.EmployeeSalaryDto(e.name, e.salary) from Employee e")
    List<EmployeeSalaryDto> getEmployeeSalaryDetails();
}
```

### Output

```text
Interface Projection:
Rahul - rahul@test.com

Class Projection:
Rahul - 45000.0
Priya - 60000.0
```

## Exercise 9: Data Source Configuration

### Code

```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/employeedb
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

```java
package com.cognizant.employeemanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
}
```

### Output

```text
HikariPool-1 - Start completed.
Connected to database: employeedb
Hibernate dialect: MySQLDialect
```

## Exercise 10: Hibernate Specific Features

### Code

```java
package com.cognizant.employeemanagement.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.BatchSize;

@Entity
@DynamicUpdate
@BatchSize(size = 10)
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double salary;
}
```

```properties
# application.properties
spring.jpa.properties.hibernate.jdbc.batch_size=30
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.show-sql=true
```

```java
package com.cognizant.employeemanagement.service;

import com.cognizant.employeemanagement.entity.Employee;
import com.cognizant.employeemanagement.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmployeeBatchService {
    private final EmployeeRepository employeeRepository;

    public EmployeeBatchService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public void saveEmployees(List<Employee> employees) {
        employeeRepository.saveAll(employees);
    }
}
```

### Output

```text
Hibernate batch insert started
30 employees inserted in batch
Hibernate batch processing completed
```

## Additional Hands-on: Country Query Methods

### Code

```java
package com.cognizant.ormlearn.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "country")
public class Country {
    @Id
    @Column(name = "co_code")
    private String code;

    @Column(name = "co_name")
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return code + " - " + name;
    }
}
```

```java
package com.cognizant.ormlearn.repository;

import com.cognizant.ormlearn.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CountryRepository extends JpaRepository<Country, String> {
    List<Country> findByNameContainingIgnoreCase(String text);
    List<Country> findByNameContainingIgnoreCaseOrderByNameAsc(String text);
    List<Country> findByNameStartingWith(String alphabet);
}
```

### Output

```text
Countries containing "ou":
BV - Bouvet Island
DJ - Djibouti
LU - Luxembourg
ZA - South Africa

Countries starting with Z:
ZM - Zambia
ZW - Zimbabwe
```

## Additional Hands-on: HQL and Native Queries

### Code

```java
package com.cognizant.ormlearn.repository;

import com.cognizant.ormlearn.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    @Query("select e from Employee e left join fetch e.department left join fetch e.skillList where e.permanent = true")
    List<Employee> getAllPermanentEmployees();

    @Query("select avg(e.salary) from Employee e where e.department.id = :id")
    double getAverageSalary(@Param("id") int departmentId);

    @Query(value = "select * from employee", nativeQuery = true)
    List<Employee> getAllEmployeesNative();
}
```

### Output

```text
Permanent Employees:
John - IT - [Java, SQL]
Mary - HR - [Communication]

Average salary for department 2: 57500.0

Native Query Employees:
John, Mary, Ravi
```
