# week4_microservices

## Part 1: Microservices Using Spring Boot 3

### Exercise 1: User and Order Management System

#### User Service

```xml
<!-- user-service/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

```properties
# user-service/src/main/resources/application.properties
spring.application.name=user-service
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/userdb
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

```java
package com.cognizant.userservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;

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
}
```

```java
package com.cognizant.userservice.repository;

import com.cognizant.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
```

```java
package com.cognizant.userservice.controller;

import com.cognizant.userservice.model.User;
import com.cognizant.userservice.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
```

#### Order Service with WebClient

```xml
<!-- order-service/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

```properties
# order-service/src/main/resources/application.properties
spring.application.name=order-service
server.port=8082
spring.datasource.url=jdbc:mysql://localhost:3306/orderdb
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
user.service.url=http://localhost:8081
```

```java
package com.cognizant.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
```

```java
package com.cognizant.orderservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String item;
    private Double amount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
```

```java
package com.cognizant.orderservice.dto;

public record UserDto(Long id, String name, String email) {
}
```

```java
package com.cognizant.orderservice.dto;

import com.cognizant.orderservice.model.CustomerOrder;

public record OrderResponse(CustomerOrder order, UserDto user) {
}
```

```java
package com.cognizant.orderservice.repository;

import com.cognizant.orderservice.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
}
```

```java
package com.cognizant.orderservice.controller;

import com.cognizant.orderservice.dto.OrderResponse;
import com.cognizant.orderservice.dto.UserDto;
import com.cognizant.orderservice.model.CustomerOrder;
import com.cognizant.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final WebClient webClient;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public OrderController(OrderRepository orderRepository, WebClient webClient) {
        this.orderRepository = orderRepository;
        this.webClient = webClient;
    }

    @PostMapping
    public CustomerOrder create(@RequestBody CustomerOrder order) {
        return orderRepository.save(order);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id) {
        CustomerOrder order = orderRepository.findById(id).orElseThrow();
        UserDto user = webClient.get()
                .uri(userServiceUrl + "/users/{id}", order.getUserId())
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
        return new OrderResponse(order, user);
    }
}
```

#### Output

```text
POST http://localhost:8081/users
Response: {"id":1,"name":"Sajjad","email":"sajjad@example.com"}

POST http://localhost:8082/orders
Response: {"id":1,"userId":1,"item":"Laptop","amount":55000.0}

GET http://localhost:8082/orders/1
Response: {"order":{"id":1,"userId":1,"item":"Laptop","amount":55000.0},"user":{"id":1,"name":"Sajjad","email":"sajjad@example.com"}}
```

### Exercise 2: Inventory Management System with Service Discovery

#### Eureka Discovery Server

```xml
<!-- discovery-server/pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

```java
package com.cognizant.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
```

```properties
server.port=8761
spring.application.name=discovery-server
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

#### Config Server

```xml
<!-- config-server/pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

```java
package com.cognizant.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

```properties
server.port=8888
spring.application.name=config-server
spring.cloud.config.server.git.uri=file:///D:/config-repo
```

#### Product Service

```properties
spring.application.name=product-service
server.port=8083
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

```java
package com.cognizant.productservice.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;

record Product(Long id, String name, int stock) {
}

@RestController
@RequestMapping("/products")
public class ProductController {
    @GetMapping
    public List<Product> products() {
        return List.of(
                new Product(1L, "Keyboard", 25),
                new Product(2L, "Mouse", 40)
        );
    }

    @GetMapping("/{id}")
    public Product product(@PathVariable Long id) {
        return new Product(id, "Keyboard", 25);
    }
}
```

#### Inventory Service

```properties
spring.application.name=inventory-service
server.port=8084
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

```java
package com.cognizant.inventoryservice.controller;

import org.springframework.web.bind.annotation.*;

record InventoryResponse(Long productId, int availableStock, String status) {
}

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    @GetMapping("/{productId}")
    public InventoryResponse stock(@PathVariable Long productId) {
        return new InventoryResponse(productId, 25, "IN_STOCK");
    }
}
```

#### Output

```text
http://localhost:8761
Registered applications: PRODUCT-SERVICE, INVENTORY-SERVICE

GET http://localhost:8084/inventory/1
Response: {"productId":1,"availableStock":25,"status":"IN_STOCK"}
```

### Exercise 3: API Gateway with Rate Limiting, Caching and Path Rewriting

```xml
<!-- api-gateway/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
    </dependency>
</dependencies>
```

```properties
spring.application.name=api-gateway
server.port=9090
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true
spring.data.redis.host=localhost
spring.data.redis.port=6379

spring.cloud.gateway.routes[0].id=customer-route
spring.cloud.gateway.routes[0].uri=lb://customer-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/customers/**
spring.cloud.gateway.routes[0].filters[0]=RewritePath=/api/customers/(?<segment>.*), /customers/${segment}
spring.cloud.gateway.routes[0].filters[1].name=RequestRateLimiter
spring.cloud.gateway.routes[0].filters[1].args.redis-rate-limiter.replenishRate=5
spring.cloud.gateway.routes[0].filters[1].args.redis-rate-limiter.burstCapacity=10
spring.cloud.gateway.routes[0].filters[1].args.key-resolver=#{@ipKeyResolver}

spring.cloud.gateway.routes[1].id=billing-route
spring.cloud.gateway.routes[1].uri=lb://billing-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/billing/**
spring.cloud.gateway.routes[1].filters[0]=RewritePath=/api/billing/(?<segment>.*), /billing/${segment}
```

```java
package com.cognizant.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }
}
```

```java
package com.cognizant.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLogFilter implements GlobalFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        LOGGER.info("Gateway request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());
        return chain.filter(exchange);
    }
}
```

#### Output

```text
GET http://localhost:9090/api/customers/1
Gateway forwards to lb://customer-service/customers/1

When too many requests are sent:
HTTP/1.1 429 Too Many Requests
```

### Exercise 4: Resilient Payment Service with Circuit Breaker

```xml
<!-- payment-service/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot3</artifactId>
    </dependency>
</dependencies>
```

```properties
spring.application.name=payment-service
server.port=8085

resilience4j.circuitbreaker.instances.paymentGateway.sliding-window-size=5
resilience4j.circuitbreaker.instances.paymentGateway.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.paymentGateway.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.instances.paymentGateway.permitted-number-of-calls-in-half-open-state=2
```

```java
package com.cognizant.paymentservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "paymentFallback")
    public String processPayment(String accountNumber, double amount) {
        throw new RuntimeException("Third-party payment gateway timeout");
    }

    public String paymentFallback(String accountNumber, double amount, Throwable throwable) {
        LOGGER.warn("Fallback executed for account {} because {}", accountNumber, throwable.getMessage());
        return "Payment request accepted and will be retried later";
    }
}
```

```java
package com.cognizant.paymentservice.controller;

import com.cognizant.paymentservice.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public String pay(@RequestParam String accountNumber, @RequestParam double amount) {
        return paymentService.processPayment(accountNumber, amount);
    }
}
```

#### Output

```text
POST http://localhost:8085/payments?accountNumber=00987987973432&amount=500
Response: Payment request accepted and will be retried later

WARN PaymentService - Fallback executed for account 00987987973432 because Third-party payment gateway timeout
```

## Part 2: Bank Microservices Hands-on

### Exercise 1: Account Microservice

```xml
<!-- account/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
</dependencies>
```

```java
package com.cognizant.account.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

record Account(String number, String type, int balance) {
}

@RestController
public class AccountController {
    @GetMapping("/accounts/{number}")
    public Account getAccount(@PathVariable String number) {
        return new Account(number, "savings", 234343);
    }
}
```

#### Output

```text
GET http://localhost:8080/accounts/00987987973432
Response: {"number":"00987987973432","type":"savings","balance":234343}
```

### Exercise 2: Loan Microservice

```properties
# loan/src/main/resources/application.properties
spring.application.name=loan-service
server.port=8081
```

```java
package com.cognizant.loan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

record Loan(String number, String type, int loan, int emi, int tenure) {
}

@RestController
public class LoanController {
    @GetMapping("/loans/{number}")
    public Loan getLoan(@PathVariable String number) {
        return new Loan(number, "car", 400000, 3258, 18);
    }
}
```

#### Output

```text
GET http://localhost:8081/loans/H00987987972342
Response: {"number":"H00987987972342","type":"car","loan":400000,"emi":3258,"tenure":18}
```

### Exercise 3: Eureka Discovery Server

```xml
<!-- eureka-discovery-server/pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

```java
package com.cognizant.eurekadiscoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaDiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaDiscoveryServerApplication.class, args);
    }
}
```

```properties
server.port=8761
spring.application.name=eureka-discovery-server
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
logging.level.com.netflix.eureka=OFF
logging.level.com.netflix.discovery=OFF
```

#### Register Account and Loan with Eureka

```xml
<!-- Add to account and loan pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

```properties
# account/src/main/resources/application.properties
spring.application.name=account-service
server.port=8080
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

```properties
# loan/src/main/resources/application.properties
spring.application.name=loan-service
server.port=8081
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

```java
@SpringBootApplication
@EnableDiscoveryClient
public class AccountApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}
```

#### Output

```text
http://localhost:8761
Instances currently registered with Eureka:
ACCOUNT-SERVICE
LOAN-SERVICE
```

### Exercise 4: Greet Service, API Gateway and Global Filter

#### Greet Service

```properties
# greet-service/src/main/resources/application.properties
spring.application.name=greet-service
server.port=8082
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

```java
package com.cognizant.greetservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetController {
    @GetMapping("/greet")
    public String greet() {
        return "Hello World";
    }
}
```

#### API Gateway

```xml
<!-- api-gateway/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

```properties
# api-gateway/src/main/resources/application.properties
spring.application.name=api-gateway
server.port=9090
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true
```

```java
package com.cognizant.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LogFilter implements GlobalFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        LOGGER.info("Request received by API Gateway: {}", exchange.getRequest().getURI());
        return chain.filter(exchange);
    }
}
```

#### Output

```text
GET http://localhost:9090/greet-service/greet
Response: Hello World

API Gateway console:
Request received by API Gateway: http://localhost:9090/greet-service/greet
```

## Part 3: Centralized Authentication and SSO

### Exercise 1: OAuth 2.1 / OIDC Login

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
</dependencies>
```

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_CLIENT_ID
            client-secret: YOUR_CLIENT_SECRET
            scope: openid, profile, email
```

```java
package com.cognizant.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .build();
    }
}
```

```java
package com.cognizant.security.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {
    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OidcUser user) {
        return user.getClaims();
    }
}
```

#### Output

```text
GET http://localhost:8080/user
Redirects to OIDC login page.
After login, user profile claims are returned.
```

### Exercise 2: Resource Server

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://issuer.example.com
```

```java
package com.cognizant.resourceserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ResourceServerConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .build();
    }
}
```

```java
package com.cognizant.resourceserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecureController {
    @GetMapping("/secure")
    public String secure() {
        return "This is a secure endpoint";
    }
}
```

#### Output

```text
GET http://localhost:8080/secure without token -> 401 Unauthorized
GET http://localhost:8080/secure with Bearer token -> This is a secure endpoint
```

### Exercise 3: JWT Based Secure Communication

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

```properties
jwt.secret=replace-with-a-long-secret-key-of-at-least-32-characters
```

```java
package com.cognizant.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;

    public String createToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }
}
```

#### Output

```text
Generated JWT can be passed as:
Authorization: Bearer <token>
Protected endpoints accept valid JWT and reject missing/invalid JWT.
```

## Part 4: Edge Services, Load Balancing and Resilience

### Exercise 1: Edge Service Routing and Filtering

```properties
spring.application.name=edge-service
server.port=9090
spring.cloud.gateway.routes[0].id=example-route
spring.cloud.gateway.routes[0].uri=http://localhost:8080
spring.cloud.gateway.routes[0].predicates[0]=Path=/example/**
```

```java
@Component
public class LoggingFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("Request: " + exchange.getRequest().getURI());
        return chain.filter(exchange);
    }
}
```

#### Output

```text
GET http://localhost:9090/example/test
Console: Request: http://localhost:9090/example/test
```

### Exercise 2: Load Balancing in API Gateway

```properties
spring.cloud.gateway.routes[0].id=load-balanced-route
spring.cloud.gateway.routes[0].uri=lb://example-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/loadbalanced/**
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

```java
@Configuration
public class LoadBalancerConfiguration {
    @Bean
    ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory factory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RandomLoadBalancer(
                factory.getLazyProvider(name, ServiceInstanceListSupplier.class),
                name
        );
    }
}
```

#### Output

```text
GET http://localhost:9090/loadbalanced/test
Gateway resolves one available EXAMPLE-SERVICE instance through Eureka and forwards the request.
```

### Exercise 3: Resilience Pattern in API Gateway

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
```

```properties
spring.cloud.gateway.routes[0].id=resilient-route
spring.cloud.gateway.routes[0].uri=lb://example-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/resilient/**
spring.cloud.gateway.routes[0].filters[0].name=CircuitBreaker
spring.cloud.gateway.routes[0].filters[0].args.name=exampleCircuitBreaker
spring.cloud.gateway.routes[0].filters[0].args.fallbackUri=forward:/fallback

resilience4j.circuitbreaker.instances.exampleCircuitBreaker.sliding-window-size=10
resilience4j.circuitbreaker.instances.exampleCircuitBreaker.failure-rate-threshold=50
```

```java
@RestController
public class FallbackController {
    @GetMapping("/fallback")
    public String fallback() {
        return "Service is temporarily unavailable. Please try again later.";
    }
}
```

#### Output

```text
When target service is down:
GET http://localhost:9090/resilient/test
Response: Service is temporarily unavailable. Please try again later.
```
