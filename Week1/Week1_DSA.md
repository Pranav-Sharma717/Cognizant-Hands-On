# Cognizant Java FSE Phase 2 - Week 1 Assignment

## Exercise 1: Inventory Management System

### Code

```java
import java.util.HashMap;
import java.util.Map;

class Product {
    int productId;
    String productName;
    int quantity;
    double price;

    Product(int productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String toString() {
        return productId + " - " + productName + " - Qty: " + quantity + " - Price: " + price;
    }
}

public class InventoryManagement {
    private static Map<Integer, Product> inventory = new HashMap<>();

    public static void addProduct(Product product) {
        inventory.put(product.productId, product);
    }

    public static void updateProduct(int productId, int quantity, double price) {
        Product product = inventory.get(productId);
        if (product != null) {
            product.quantity = quantity;
            product.price = price;
        }
    }

    public static void deleteProduct(int productId) {
        inventory.remove(productId);
    }

    public static void displayProducts() {
        for (Product product : inventory.values()) {
            System.out.println(product);
        }
    }

    public static void main(String[] args) {
        addProduct(new Product(101, "Laptop", 10, 55000));
        addProduct(new Product(102, "Mouse", 50, 600));
        addProduct(new Product(103, "Keyboard", 30, 1200));

        System.out.println("Initial Inventory:");
        displayProducts();

        updateProduct(102, 45, 650);
        deleteProduct(103);

        System.out.println("\nUpdated Inventory:");
        displayProducts();
    }
}
```

### Output

```text
Initial Inventory:
101 - Laptop - Qty: 10 - Price: 55000.0
102 - Mouse - Qty: 50 - Price: 600.0
103 - Keyboard - Qty: 30 - Price: 1200.0

Updated Inventory:
101 - Laptop - Qty: 10 - Price: 55000.0
102 - Mouse - Qty: 45 - Price: 650.0
```

## Exercise 2: E-commerce Platform Search Function

### Code

```java
import java.util.Arrays;
import java.util.Comparator;

class Product {
    int productId;
    String productName;
    String category;

    Product(int productId, String productName, String category) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
    }

    public String toString() {
        return productId + " - " + productName + " - " + category;
    }
}

public class EcommerceSearch {
    public static Product linearSearch(Product[] products, String name) {
        for (Product product : products) {
            if (product.productName.equalsIgnoreCase(name)) {
                return product;
            }
        }
        return null;
    }

    public static Product binarySearch(Product[] products, String name) {
        int left = 0;
        int right = products.length - 1;

        while (left <= right) {
            int mid = (left + right) / 2;
            int result = products[mid].productName.compareToIgnoreCase(name);

            if (result == 0) {
                return products[mid];
            } else if (result < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Product[] products = {
            new Product(201, "Phone", "Electronics"),
            new Product(202, "Shoes", "Fashion"),
            new Product(203, "Bag", "Accessories"),
            new Product(204, "Watch", "Fashion")
        };

        Product linearResult = linearSearch(products, "Bag");

        Arrays.sort(products, Comparator.comparing(product -> product.productName));
        Product binaryResult = binarySearch(products, "Watch");

        System.out.println("Linear Search Result: " + linearResult);
        System.out.println("Binary Search Result: " + binaryResult);
    }
}
```

### Output

```text
Linear Search Result: 203 - Bag - Accessories
Binary Search Result: 204 - Watch - Fashion
```

## Exercise 3: Sorting Customer Orders

### Code

```java
class Order {
    int orderId;
    String customerName;
    double totalPrice;

    Order(int orderId, String customerName, double totalPrice) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.totalPrice = totalPrice;
    }

    public String toString() {
        return orderId + " - " + customerName + " - " + totalPrice;
    }
}

public class SortingOrders {
    public static void bubbleSort(Order[] orders) {
        for (int i = 0; i < orders.length - 1; i++) {
            for (int j = 0; j < orders.length - i - 1; j++) {
                if (orders[j].totalPrice > orders[j + 1].totalPrice) {
                    Order temp = orders[j];
                    orders[j] = orders[j + 1];
                    orders[j + 1] = temp;
                }
            }
        }
    }

    public static void quickSort(Order[] orders, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(orders, low, high);
            quickSort(orders, low, pivotIndex - 1);
            quickSort(orders, pivotIndex + 1, high);
        }
    }

    private static int partition(Order[] orders, int low, int high) {
        double pivot = orders[high].totalPrice;
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (orders[j].totalPrice <= pivot) {
                i++;
                Order temp = orders[i];
                orders[i] = orders[j];
                orders[j] = temp;
            }
        }

        Order temp = orders[i + 1];
        orders[i + 1] = orders[high];
        orders[high] = temp;

        return i + 1;
    }

    public static void displayOrders(Order[] orders) {
        for (Order order : orders) {
            System.out.println(order);
        }
    }

    public static void main(String[] args) {
        Order[] orders = {
            new Order(301, "Amit", 4500),
            new Order(302, "Sneha", 1200),
            new Order(303, "Rahul", 7800),
            new Order(304, "Priya", 3000)
        };

        bubbleSort(orders);
        System.out.println("Orders after Bubble Sort:");
        displayOrders(orders);

        Order[] moreOrders = {
            new Order(305, "Neha", 6500),
            new Order(306, "Karan", 2500),
            new Order(307, "Ravi", 9500),
            new Order(308, "Anu", 1500)
        };

        quickSort(moreOrders, 0, moreOrders.length - 1);
        System.out.println("\nOrders after Quick Sort:");
        displayOrders(moreOrders);
    }
}
```

### Output

```text
Orders after Bubble Sort:
302 - Sneha - 1200.0
304 - Priya - 3000.0
301 - Amit - 4500.0
303 - Rahul - 7800.0

Orders after Quick Sort:
308 - Anu - 1500.0
306 - Karan - 2500.0
305 - Neha - 6500.0
307 - Ravi - 9500.0
```

## Exercise 4: Employee Management System

### Code

```java
class Employee {
    int employeeId;
    String name;
    String position;
    double salary;

    Employee(int employeeId, String name, String position, double salary) {
        this.employeeId = employeeId;
        this.name = name;
        this.position = position;
        this.salary = salary;
    }

    public String toString() {
        return employeeId + " - " + name + " - " + position + " - " + salary;
    }
}

public class EmployeeManagement {
    private static Employee[] employees = new Employee[10];
    private static int count = 0;

    public static void addEmployee(Employee employee) {
        if (count < employees.length) {
            employees[count++] = employee;
        }
    }

    public static Employee searchEmployee(int employeeId) {
        for (int i = 0; i < count; i++) {
            if (employees[i].employeeId == employeeId) {
                return employees[i];
            }
        }
        return null;
    }

    public static void deleteEmployee(int employeeId) {
        for (int i = 0; i < count; i++) {
            if (employees[i].employeeId == employeeId) {
                for (int j = i; j < count - 1; j++) {
                    employees[j] = employees[j + 1];
                }
                employees[--count] = null;
                break;
            }
        }
    }

    public static void displayEmployees() {
        for (int i = 0; i < count; i++) {
            System.out.println(employees[i]);
        }
    }

    public static void main(String[] args) {
        addEmployee(new Employee(401, "Arjun", "Developer", 45000));
        addEmployee(new Employee(402, "Meera", "Tester", 38000));
        addEmployee(new Employee(403, "Vikram", "Manager", 70000));

        System.out.println("Employee Records:");
        displayEmployees();

        System.out.println("\nSearch Result: " + searchEmployee(402));

        deleteEmployee(401);
        System.out.println("\nAfter Deletion:");
        displayEmployees();
    }
}
```

### Output

```text
Employee Records:
401 - Arjun - Developer - 45000.0
402 - Meera - Tester - 38000.0
403 - Vikram - Manager - 70000.0

Search Result: 402 - Meera - Tester - 38000.0

After Deletion:
402 - Meera - Tester - 38000.0
403 - Vikram - Manager - 70000.0
```

## Exercise 5: Task Management System

### Code

```java
class Task {
    int taskId;
    String taskName;
    String status;
    Task next;

    Task(int taskId, String taskName, String status) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.status = status;
    }

    public String toString() {
        return taskId + " - " + taskName + " - " + status;
    }
}

public class TaskManagement {
    private static Task head;

    public static void addTask(Task task) {
        if (head == null) {
            head = task;
            return;
        }

        Task current = head;
        while (current.next != null) {
            current = current.next;
        }
        current.next = task;
    }

    public static Task searchTask(int taskId) {
        Task current = head;
        while (current != null) {
            if (current.taskId == taskId) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    public static void deleteTask(int taskId) {
        if (head == null) {
            return;
        }

        if (head.taskId == taskId) {
            head = head.next;
            return;
        }

        Task current = head;
        while (current.next != null && current.next.taskId != taskId) {
            current = current.next;
        }

        if (current.next != null) {
            current.next = current.next.next;
        }
    }

    public static void displayTasks() {
        Task current = head;
        while (current != null) {
            System.out.println(current);
            current = current.next;
        }
    }

    public static void main(String[] args) {
        addTask(new Task(501, "Complete Assignment", "Pending"));
        addTask(new Task(502, "Attend Meeting", "Completed"));
        addTask(new Task(503, "Submit Report", "In Progress"));

        System.out.println("Task List:");
        displayTasks();

        System.out.println("\nSearch Result: " + searchTask(503));

        deleteTask(502);
        System.out.println("\nAfter Deletion:");
        displayTasks();
    }
}
```

### Output

```text
Task List:
501 - Complete Assignment - Pending
502 - Attend Meeting - Completed
503 - Submit Report - In Progress

Search Result: 503 - Submit Report - In Progress

After Deletion:
501 - Complete Assignment - Pending
503 - Submit Report - In Progress
```

## Exercise 6: Library Management System

### Code

```java
import java.util.Arrays;
import java.util.Comparator;

class Book {
    int bookId;
    String title;
    String author;

    Book(int bookId, String title, String author) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
    }

    public String toString() {
        return bookId + " - " + title + " - " + author;
    }
}

public class LibraryManagement {
    public static Book linearSearch(Book[] books, String title) {
        for (Book book : books) {
            if (book.title.equalsIgnoreCase(title)) {
                return book;
            }
        }
        return null;
    }

    public static Book binarySearch(Book[] books, String title) {
        int left = 0;
        int right = books.length - 1;

        while (left <= right) {
            int mid = (left + right) / 2;
            int result = books[mid].title.compareToIgnoreCase(title);

            if (result == 0) {
                return books[mid];
            } else if (result < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Book[] books = {
            new Book(601, "Java Basics", "James Gosling"),
            new Book(602, "Data Structures", "Mark Allen"),
            new Book(603, "Clean Code", "Robert Martin"),
            new Book(604, "Algorithms", "Thomas Cormen")
        };

        Book linearResult = linearSearch(books, "Clean Code");

        Arrays.sort(books, Comparator.comparing(book -> book.title));
        Book binaryResult = binarySearch(books, "Java Basics");

        System.out.println("Linear Search Result: " + linearResult);
        System.out.println("Binary Search Result: " + binaryResult);
    }
}
```

### Output

```text
Linear Search Result: 603 - Clean Code - Robert Martin
Binary Search Result: 601 - Java Basics - James Gosling
```

## Exercise 7: Financial Forecasting

### Code

```java
public class FinancialForecasting {
    public static double calculateFutureValue(double currentValue, double growthRate, int years) {
        if (years == 0) {
            return currentValue;
        }
        return calculateFutureValue(currentValue, growthRate, years - 1) * (1 + growthRate);
    }

    public static void main(String[] args) {
        double currentValue = 10000;
        double growthRate = 0.08;
        int years = 5;

        double futureValue = calculateFutureValue(currentValue, growthRate, years);

        System.out.println("Current Value: " + currentValue);
        System.out.println("Growth Rate: " + (growthRate * 100) + "%");
        System.out.println("Years: " + years);
        System.out.printf("Future Value: %.2f%n", futureValue);
    }
}
```

### Output

```text
Current Value: 10000.0
Growth Rate: 8.0%
Years: 5
Future Value: 14693.28
```
