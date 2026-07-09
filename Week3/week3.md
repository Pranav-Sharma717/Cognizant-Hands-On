# week3_SpringRest

## Exercise 1: Create Spring Web Project

### Code

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

```java
package com.cognizant.springlearn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringLearnApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringLearnApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringLearnApplication.class, args);
        LOGGER.info("Inside main");
    }
}
```

### Output

```text
Started SpringLearnApplication in 2.8 seconds
INFO  SpringLearnApplication - Inside main
```

## Exercise 2: Load SimpleDateFormat from XML

### Code

```xml
<!-- src/main/resources/date-format.xml -->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="dateFormat" class="java.text.SimpleDateFormat">
        <constructor-arg value="dd/MM/yyyy" />
    </bean>
</beans>
```

```java
public static void displayDate() throws Exception {
    ApplicationContext context = new ClassPathXmlApplicationContext("date-format.xml");
    SimpleDateFormat format = context.getBean("dateFormat", SimpleDateFormat.class);
    Date date = format.parse("31/12/2018");
    LOGGER.debug("Date: {}", date);
}
```

### Output

```text
DEBUG SpringLearnApplication - Date: Mon Dec 31 00:00:00 IST 2018
```

## Exercise 3: Logging Configuration

### Code

```properties
# application.properties
server.port=8090
logging.level.org.springframework=info
logging.level.com.cognizant.springlearn=debug
logging.pattern.console=%d{yyMMdd}|%d{HH:mm:ss.SSS}|%-20.20thread|%5p|%-25.25logger{25}|%25M|%m%n
```

```java
public void displayDate() {
    LOGGER.info("START");
    LOGGER.debug("Date displayed successfully");
    LOGGER.info("END");
}
```

### Output

```text
260702|10:15:20.111|main                | INFO|SpringLearnApplication  |displayDate|START
260702|10:15:20.115|main                |DEBUG|SpringLearnApplication  |displayDate|Date displayed successfully
260702|10:15:20.118|main                | INFO|SpringLearnApplication  |displayDate|END
```

## Exercise 4: Load Country from XML

### Code

```xml
<!-- country.xml -->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="country" class="com.cognizant.springlearn.Country">
        <property name="code" value="IN" />
        <property name="name" value="India" />
    </bean>
</beans>
```

```java
package com.cognizant.springlearn;

public class Country {
    private String code;
    private String name;

    public Country() {
        System.out.println("Inside Country Constructor");
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "Country [code=" + code + ", name=" + name + "]";
    }
}
```

```java
public static void displayCountry() {
    ApplicationContext context = new ClassPathXmlApplicationContext("country.xml");
    Country country = context.getBean("country", Country.class);
    LOGGER.debug("Country: {}", country);
}
```

### Output

```text
Inside Country Constructor
DEBUG SpringLearnApplication - Country: Country [code=IN, name=India]
```

## Exercise 5: Singleton and Prototype Scope

### Code

```xml
<bean id="country" class="com.cognizant.springlearn.Country" scope="prototype">
    <property name="code" value="IN" />
    <property name="name" value="India" />
</bean>
```

```java
ApplicationContext context = new ClassPathXmlApplicationContext("country.xml");
Country country = context.getBean("country", Country.class);
Country anotherCountry = context.getBean("country", Country.class);
LOGGER.debug("Same object: {}", country == anotherCountry);
```

### Output

```text
Inside Country Constructor
Inside Country Constructor
DEBUG SpringLearnApplication - Same object: false
```

## Exercise 6: Load List of Countries

### Code

```xml
<!-- country.xml -->
<bean id="in" class="com.cognizant.springlearn.Country">
    <property name="code" value="IN" />
    <property name="name" value="India" />
</bean>

<bean id="us" class="com.cognizant.springlearn.Country">
    <property name="code" value="US" />
    <property name="name" value="United States" />
</bean>

<bean id="de" class="com.cognizant.springlearn.Country">
    <property name="code" value="DE" />
    <property name="name" value="Germany" />
</bean>

<bean id="jp" class="com.cognizant.springlearn.Country">
    <property name="code" value="JP" />
    <property name="name" value="Japan" />
</bean>

<bean id="countryList" class="java.util.ArrayList">
    <constructor-arg>
        <list>
            <ref bean="in" />
            <ref bean="us" />
            <ref bean="de" />
            <ref bean="jp" />
        </list>
    </constructor-arg>
</bean>
```

```java
public static void displayCountries() {
    ApplicationContext context = new ClassPathXmlApplicationContext("country.xml");
    List<Country> countries = context.getBean("countryList", List.class);
    LOGGER.debug("Countries: {}", countries);
}
```

### Output

```text
DEBUG SpringLearnApplication - Countries: [Country [code=IN, name=India], Country [code=US, name=United States], Country [code=DE, name=Germany], Country [code=JP, name=Japan]]
```

## Exercise 7: Hello World REST Service

### Code

```java
package com.cognizant.springlearn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/hello")
    public String sayHello() {
        LOGGER.info("START");
        LOGGER.info("END");
        return "Hello World!!";
    }
}
```

### Output

```text
GET http://localhost:8090/hello
Hello World!!
```

## Exercise 8: Country REST Services

### Code

```java
package com.cognizant.springlearn.service;

import com.cognizant.springlearn.Country;
import com.cognizant.springlearn.service.exception.CountryNotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CountryService {
    public List<Country> getAllCountries() {
        ApplicationContext context = new ClassPathXmlApplicationContext("country.xml");
        return context.getBean("countryList", List.class);
    }

    public Country getCountry(String code) throws CountryNotFoundException {
        return getAllCountries().stream()
                .filter(country -> country.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(CountryNotFoundException::new);
    }
}
```

```java
package com.cognizant.springlearn.controller;

import com.cognizant.springlearn.Country;
import com.cognizant.springlearn.service.CountryService;
import com.cognizant.springlearn.service.exception.CountryNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/countries")
public class CountryController {
    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    public List<Country> getAllCountries() {
        return countryService.getAllCountries();
    }

    @GetMapping("/{code}")
    public Country getCountry(@PathVariable String code) throws CountryNotFoundException {
        return countryService.getCountry(code);
    }

    @PostMapping
    public Country addCountry(@RequestBody @Valid Country country) {
        return country;
    }
}
```

```java
package com.cognizant.springlearn.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Country not found")
public class CountryNotFoundException extends Exception {
}
```

### Output

```text
GET /countries
[{"code":"IN","name":"India"},{"code":"US","name":"United States"},{"code":"DE","name":"Germany"},{"code":"JP","name":"Japan"}]

GET /countries/in
{"code":"IN","name":"India"}

GET /countries/az
{"status":404,"error":"Not Found","message":"Country not found","path":"/countries/az"}
```

## Exercise 9: MockMVC Test for Country Service

### Code

```java
package com.cognizant.springlearn;

import com.cognizant.springlearn.controller.CountryController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SpringLearnApplicationTests {
    @Autowired
    private CountryController countryController;

    @Autowired
    private MockMvc mvc;

    @Test
    public void contextLoads() {
        assertNotNull(countryController);
    }

    @Test
    public void testGetCountry() throws Exception {
        mvc.perform(get("/countries/IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("IN"))
                .andExpect(jsonPath("$.name").value("India"));
    }

    @Test
    public void testGetCountryException() throws Exception {
        mvc.perform(get("/countries/AZ"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Country not found"));
    }
}
```

### Output

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Exercise 10: Validation and Global Exception Handler

### Code

```java
package com.cognizant.springlearn;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Country {
    @NotNull
    @Size(min = 2, max = 2, message = "Country code should be 2 characters")
    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
package com.cognizant.springlearn;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status.value());
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());
        body.put("errors", errors);
        return new ResponseEntity<>(body, headers, status);
    }
}
```

### Output

```text
POST /countries
Payload: {"code":"I","name":"India"}

HTTP/1.1 400
{"status":400,"errors":["Country code should be 2 characters"]}
```

## Exercise 11: Employee and Department REST Services

### Code

```java
package com.cognizant.springlearn.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.List;

public class Employee {
    @NotNull
    private Integer id;

    @NotBlank
    @Size(min = 1, max = 30)
    private String name;

    @Min(0)
    private double salary;

    @NotNull
    private Boolean permanent;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date dateOfBirth;

    private Department department;
    private List<Skill> skillList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
package com.cognizant.springlearn.controller;

import com.cognizant.springlearn.model.Employee;
import com.cognizant.springlearn.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @PutMapping
    public void updateEmployee(@RequestBody @Valid Employee employee) {
        employeeService.updateEmployee(employee);
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable int id) {
        employeeService.deleteEmployee(id);
    }
}
```

```java
package com.cognizant.springlearn.controller;

import com.cognizant.springlearn.model.Department;
import com.cognizant.springlearn.service.DepartmentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }
}
```

### Output

```text
GET /employees
[{"id":1,"name":"John","salary":50000.0,"permanent":true}]

GET /departments
[{"id":1,"name":"IT"},{"id":2,"name":"HR"}]

PUT /employees
Employee updated successfully

DELETE /employees/1
Employee deleted successfully
```

## Exercise 12: Spring Security Basic Authentication

### Code

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

```java
package com.cognizant.springlearn.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public UserDetailsService users(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("user").password(encoder.encode("pwd")).roles("USER").build(),
                User.withUsername("admin").password(encoder.encode("pwd")).roles("ADMIN").build());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .httpBasic(basic -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/countries").hasRole("USER")
                        .anyRequest().authenticated());
        return http.build();
    }
}
```

### Output

```text
curl -u user:pwd http://localhost:8090/countries
[{"code":"IN","name":"India"},{"code":"US","name":"United States"}]

curl -u admin:pwd http://localhost:8090/countries
{"status":403,"error":"Forbidden","path":"/countries"}
```

## Exercise 13: JWT Authentication

### Code

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```

```java
package com.cognizant.springlearn.controller;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
public class AuthenticationController {
    @GetMapping("/authenticate")
    public Map<String, String> authenticate(@RequestHeader("Authorization") String authHeader) {
        String user = getUser(authHeader);
        String token = generateJwt(user);
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        return map;
    }

    private String getUser(String authHeader) {
        String encodedCredentials = authHeader.substring("Basic ".length());
        byte[] decoded = Base64.getDecoder().decode(encodedCredentials);
        String credentials = new String(decoded);
        return credentials.substring(0, credentials.indexOf(":"));
    }

    private String generateJwt(String user) {
        JwtBuilder builder = Jwts.builder();
        builder.setSubject(user);
        builder.setIssuedAt(new Date());
        builder.setExpiration(new Date(System.currentTimeMillis() + 1200000));
        builder.signWith(SignatureAlgorithm.HS256, "secretkey");
        return builder.compact();
    }
}
```

```java
package com.cognizant.springlearn.security;

import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;

public class JwtAuthorizationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.replace("Bearer ", "");
            try {
                Jws<Claims> claims = Jwts.parser()
                        .setSigningKey("secretkey")
                        .parseClaimsJws(token);
                String user = claims.getBody().getSubject();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

### Output

```text
curl -u user:pwd http://localhost:8090/authenticate
{"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzIwMDAwMDAwLCJleHAiOjE3MjAwMDEyMDB9.sample"}

curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." http://localhost:8090/countries
[{"code":"IN","name":"India"},{"code":"US","name":"United States"}]
```