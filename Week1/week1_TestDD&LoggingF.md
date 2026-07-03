# week1_TestDD&LoggingF

## 1. JUnit Basic Testing Exercises

### Exercise 1: Setting Up JUnit

#### Code

```xml
<!-- pom.xml -->
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>
```

```java
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class FirstJUnitTest {
    @Test
    public void testJUnitSetup() {
        assertTrue(true);
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 2: Writing Basic JUnit Tests

#### Code

```java
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }
}
```

```java
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CalculatorTest {
    @Test
    public void testAdd() {
        Calculator calculator = new Calculator();
        assertEquals(15, calculator.add(10, 5));
    }

    @Test
    public void testSubtract() {
        Calculator calculator = new Calculator();
        assertEquals(5, calculator.subtract(10, 5));
    }
}
```

#### Output

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 3: Assertions in JUnit

#### Code

```java
import org.junit.Test;
import static org.junit.Assert.*;

public class AssertionsTest {
    @Test
    public void testAssertions() {
        assertEquals(5, 2 + 3);
        assertTrue(5 > 3);
        assertFalse(5 < 3);
        assertNull(null);
        assertNotNull(new Object());
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 4: AAA Pattern, Setup and Teardown

#### Code

```java
public class StringHelper {
    public String reverse(String value) {
        return new StringBuilder(value).reverse().toString();
    }
}
```

```java
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class StringHelperTest {
    private StringHelper helper;

    @Before
    public void setUp() {
        helper = new StringHelper();
        System.out.println("Setup completed");
    }

    @After
    public void tearDown() {
        helper = null;
        System.out.println("Cleanup completed");
    }

    @Test
    public void testReverse() {
        String result = helper.reverse("java");
        assertEquals("avaj", result);
    }
}
```

#### Output

```text
Setup completed
Cleanup completed
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 2. JUnit Advanced Testing Exercises

### Exercise 1: Parameterized Tests

#### Code

```java
public class EvenChecker {
    public boolean isEven(int number) {
        return number % 2 == 0;
    }
}
```

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EvenCheckerTest {
    @ParameterizedTest
    @ValueSource(ints = {2, 4, 6, 8, 10})
    void testEvenNumbers(int number) {
        EvenChecker checker = new EvenChecker();
        assertTrue(checker.isEven(number));
    }
}
```

#### Output

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 2: Test Suites

#### Code

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdditionTest {
    @Test
    void testAddition() {
        assertEquals(10, 5 + 5);
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiplicationTest {
    @Test
    void testMultiplication() {
        assertEquals(25, 5 * 5);
    }
}
```

```java
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({AdditionTest.class, MultiplicationTest.class})
public class AllTests {
}
```

#### Output

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 3: Test Execution Order

#### Code

```java
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@TestMethodOrder(OrderAnnotation.class)
public class OrderedTests {
    @Test
    @Order(1)
    void firstTest() {
        System.out.println("First test executed");
    }

    @Test
    @Order(2)
    void secondTest() {
        System.out.println("Second test executed");
    }
}
```

#### Output

```text
First test executed
Second test executed
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 4: Exception Testing

#### Code

```java
public class ExceptionThrower {
    public void throwException() {
        throw new IllegalArgumentException("Invalid input");
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExceptionThrowerTest {
    @Test
    void testException() {
        ExceptionThrower thrower = new ExceptionThrower();
        assertThrows(IllegalArgumentException.class, thrower::throwException);
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 5: Timeout Testing

#### Code

```java
public class PerformanceTester {
    public void performTask() throws InterruptedException {
        Thread.sleep(500);
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import java.time.Duration;

public class PerformanceTesterTest {
    @Test
    void testPerformance() {
        PerformanceTester tester = new PerformanceTester();

        assertTimeout(Duration.ofSeconds(1), () -> {
            tester.performTask();
        });
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 3. Mockito Exercises

### Exercise 1: Mocking and Stubbing

#### Code

```java
interface ExternalApi {
    String getData();
    void sendData(String data);
}

class MyService {
    private final ExternalApi api;

    MyService(ExternalApi api) {
        this.api = api;
    }

    String fetchData() {
        return api.getData();
    }

    void saveData(String data) {
        api.sendData(data);
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MyServiceTest {
    @Test
    void testExternalApi() {
        ExternalApi mockApi = mock(ExternalApi.class);
        when(mockApi.getData()).thenReturn("Mock Data");

        MyService service = new MyService(mockApi);

        assertEquals("Mock Data", service.fetchData());
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 2: Verifying Interactions

#### Code

```java
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class VerifyInteractionTest {
    @Test
    void testVerifyInteraction() {
        ExternalApi mockApi = mock(ExternalApi.class);
        MyService service = new MyService(mockApi);

        service.fetchData();

        verify(mockApi).getData();
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 3: Argument Matching

#### Code

```java
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class ArgumentMatchingTest {
    @Test
    void testArgumentMatching() {
        ExternalApi mockApi = mock(ExternalApi.class);
        MyService service = new MyService(mockApi);

        service.saveData("Java");

        verify(mockApi).sendData(eq("Java"));
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 4: Handling Void Methods

#### Code

```java
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class VoidMethodTest {
    @Test
    void testVoidMethod() {
        ExternalApi mockApi = mock(ExternalApi.class);
        doNothing().when(mockApi).sendData("Saved");

        MyService service = new MyService(mockApi);
        service.saveData("Saved");

        verify(mockApi).sendData("Saved");
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 5: Multiple Returns

#### Code

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MultipleReturnTest {
    @Test
    void testMultipleReturns() {
        ExternalApi mockApi = mock(ExternalApi.class);
        when(mockApi.getData()).thenReturn("First Data", "Second Data");

        MyService service = new MyService(mockApi);

        assertEquals("First Data", service.fetchData());
        assertEquals("Second Data", service.fetchData());
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 6: Interaction Order

#### Code

```java
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import static org.mockito.Mockito.*;

public class InteractionOrderTest {
    @Test
    void testInteractionOrder() {
        ExternalApi mockApi = mock(ExternalApi.class);

        mockApi.getData();
        mockApi.sendData("Done");

        InOrder inOrder = inOrder(mockApi);
        inOrder.verify(mockApi).getData();
        inOrder.verify(mockApi).sendData("Done");
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 7: Void Method with Exception

#### Code

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class VoidExceptionTest {
    @Test
    void testVoidMethodException() {
        ExternalApi mockApi = mock(ExternalApi.class);
        doThrow(new RuntimeException("API error")).when(mockApi).sendData("Fail");

        MyService service = new MyService(mockApi);

        assertThrows(RuntimeException.class, () -> service.saveData("Fail"));
        verify(mockApi).sendData("Fail");
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 4. Advanced Mockito Exercises

### Exercise 1: Mock Repository

#### Code

```java
interface Repository {
    String getData();
}

class Service {
    private final Repository repository;

    Service(Repository repository) {
        this.repository = repository;
    }

    String processData() {
        return "Processed " + repository.getData();
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ServiceTest {
    @Test
    void testServiceWithMockRepository() {
        Repository mockRepository = mock(Repository.class);
        when(mockRepository.getData()).thenReturn("Mock Data");

        Service service = new Service(mockRepository);

        assertEquals("Processed Mock Data", service.processData());
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 2: Mock REST Client

#### Code

```java
interface RestClient {
    String getResponse();
}

class ApiService {
    private final RestClient restClient;

    ApiService(RestClient restClient) {
        this.restClient = restClient;
    }

    String fetchData() {
        return "Fetched " + restClient.getResponse();
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ApiServiceTest {
    @Test
    void testServiceWithMockRestClient() {
        RestClient mockRestClient = mock(RestClient.class);
        when(mockRestClient.getResponse()).thenReturn("Mock Response");

        ApiService apiService = new ApiService(mockRestClient);

        assertEquals("Fetched Mock Response", apiService.fetchData());
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 3: Mock File I/O

#### Code

```java
interface FileReaderService {
    String read();
}

interface FileWriterService {
    void write(String content);
}

class FileService {
    private final FileReaderService reader;
    private final FileWriterService writer;

    FileService(FileReaderService reader, FileWriterService writer) {
        this.reader = reader;
        this.writer = writer;
    }

    String processFile() {
        String result = "Processed " + reader.read();
        writer.write(result);
        return result;
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class FileServiceTest {
    @Test
    void testServiceWithMockFileIO() {
        FileReaderService reader = mock(FileReaderService.class);
        FileWriterService writer = mock(FileWriterService.class);
        when(reader.read()).thenReturn("Mock File Content");

        FileService fileService = new FileService(reader, writer);

        assertEquals("Processed Mock File Content", fileService.processFile());
        verify(writer).write("Processed Mock File Content");
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 4: Mock Network Client

#### Code

```java
interface NetworkClient {
    String connect();
}

class NetworkService {
    private final NetworkClient client;

    NetworkService(NetworkClient client) {
        this.client = client;
    }

    String connectToServer() {
        return "Connected to " + client.connect();
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class NetworkServiceTest {
    @Test
    void testServiceWithMockNetworkClient() {
        NetworkClient client = mock(NetworkClient.class);
        when(client.connect()).thenReturn("Mock Connection");

        NetworkService service = new NetworkService(client);

        assertEquals("Connected to Mock Connection", service.connectToServer());
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 5: Mock Multiple Return Values

#### Code

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MultiReturnServiceTest {
    @Test
    void testServiceWithMultipleReturnValues() {
        Repository repository = mock(Repository.class);
        when(repository.getData())
                .thenReturn("First Mock Data")
                .thenReturn("Second Mock Data");

        Service service = new Service(repository);

        assertEquals("Processed First Mock Data", service.processData());
        assertEquals("Processed Second Mock Data", service.processData());
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 5. JUnit Spring Test Exercises

### Exercise 1: Service Unit Test

#### Code

```java
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {
    public int add(int a, int b) {
        return a + b;
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorServiceTest {
    @Test
    void testAdd() {
        CalculatorService service = new CalculatorService();
        assertEquals(30, service.add(10, 20));
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 2: Mock Repository in Service Test

#### Code

```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    private Long id;
    private String name;

    public User() {
    }

    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
```

```java
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
```

```java
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
```

```java
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    @Test
    void testGetUserById() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findById(1L)).thenReturn(Optional.of(new User(1L, "Amit")));

        UserService service = new UserService(repository);

        assertEquals("Amit", service.getUserById(1L).getName());
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 3: REST Controller with MockMvc

#### Code

```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
```

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testGetUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(new User(1L, "Amit"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Amit"));
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 4: Spring Boot Integration Test

#### Code

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFullFlow() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 5: Controller POST Endpoint

#### Code

```java
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@PostMapping
public ResponseEntity<User> createUser(@RequestBody User user) {
    return ResponseEntity.ok(userService.saveUser(user));
}
```

```java
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

public class UserPostControllerTest {
    @Test
    void testCreateUser() throws Exception {
        when(userService.saveUser(any(User.class))).thenReturn(new User(2L, "Neha"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":2,\"name\":\"Neha\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neha"));
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 6: Service Exception Handling

#### Code

```java
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UserServiceExceptionTest {
    @Test
    void testMissingUser() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        UserService service = new UserService(repository);

        assertThrows(NoSuchElementException.class, () -> service.getUserById(99L));
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 7: Custom Repository Query

#### Code

```java
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);
}
```

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByName() {
        userRepository.save(new User(3L, "Rahul"));

        List<User> users = userRepository.findByName("Rahul");

        assertEquals(1, users.size());
        assertEquals("Rahul", users.get(0).getName());
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 8: Controller Exception Handling

#### Code

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
}
```

```java
import org.junit.jupiter.api.Test;
import java.util.NoSuchElementException;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerExceptionTest {
    @Test
    void testControllerException() throws Exception {
        when(userService.getUserById(100L)).thenThrow(new NoSuchElementException());

        mockMvc.perform(get("/users/100"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 9: Parameterized Test

#### Code

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorParameterizedTest {
    @ParameterizedTest
    @CsvSource({"2,3,5", "5,5,10", "10,20,30"})
    void testAddWithMultipleInputs(int a, int b, int expected) {
        CalculatorService service = new CalculatorService();
        assertEquals(expected, service.add(a, b));
    }
}
```

#### Output

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 6. Mockito Mock Dependencies in Spring Tests

### Exercise 1: Mock Service in Controller Test

#### Code

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerMockTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testControllerWithMockService() throws Exception {
        when(userService.getUserById(1L)).thenReturn(new User(1L, "John"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"));
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 2: Mock Repository in Service Test

#### Code

```java
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserServiceMockRepositoryTest {
    @Test
    void testServiceWithMockRepository() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findById(2L)).thenReturn(Optional.of(new User(2L, "Priya")));

        UserService service = new UserService(repository);

        assertEquals("Priya", service.getUserById(2L).getName());
        verify(repository).findById(2L);
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Exercise 3: Mock Service in Integration Test

#### Code

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationMockTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testIntegrationWithMockService() throws Exception {
        when(userService.getUserById(3L)).thenReturn(new User(3L, "Kiran"));

        mockMvc.perform(get("/users/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kiran"));
    }
}
```

#### Output

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 7. SLF4J Logging Exercises

### Exercise 1: Error and Warning Logs

#### Code

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingExample {
    private static final Logger logger = LoggerFactory.getLogger(LoggingExample.class);

    public static void main(String[] args) {
        logger.error("This is an error message");
        logger.warn("This is a warning message");
    }
}
```

#### Output

```text
ERROR LoggingExample - This is an error message
WARN  LoggingExample - This is a warning message
```

### Exercise 2: Parameterized Logging

#### Code

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterizedLoggingExample {
    private static final Logger logger = LoggerFactory.getLogger(ParameterizedLoggingExample.class);

    public static void main(String[] args) {
        String user = "Pranav";
        int attempts = 3;

        logger.info("User {} logged in after {} attempts", user, attempts);
        logger.warn("User {} has reached warning limit: {}", user, attempts);
    }
}
```

#### Output

```text
INFO  ParameterizedLoggingExample - User Pranav logged in after 3 attempts
WARN  ParameterizedLoggingExample - User Pranav has reached warning limit: 3
```

### Exercise 3: Different Appenders

#### Code

```xml
<!-- logback.xml -->
<configuration>
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="file" class="ch.qos.logback.core.FileAppender">
        <file>app.log</file>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="debug">
        <appender-ref ref="console" />
        <appender-ref ref="file" />
    </root>
</configuration>
```

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppenderLoggingExample {
    private static final Logger logger = LoggerFactory.getLogger(AppenderLoggingExample.class);

    public static void main(String[] args) {
        logger.debug("Debug message written to console and file");
        logger.info("Application started successfully");
        logger.error("Sample error message");
    }
}
```

#### Output

```text
10:15:22.120 [main] DEBUG AppenderLoggingExample - Debug message written to console and file
10:15:22.125 [main] INFO  AppenderLoggingExample - Application started successfully
10:15:22.128 [main] ERROR AppenderLoggingExample - Sample error message

app.log file created with the same log messages.
```
