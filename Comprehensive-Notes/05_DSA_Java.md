# 05 — Data Structures & Algorithms in Java

> Maps directly to [`Week1/Week1_DSA.md`](../Week1/Week1_DSA.md) (7 exercises) and to the Cognizant coding round.
> **References:** [GeeksforGeeks DSA](https://www.geeksforgeeks.org/data-structures/), [Java Collections](https://docs.oracle.com/javase/tutorial/collections/)

---

## 1. Algorithm Analysis — Big-O ⭐

**Asymptotic notations**
- **O(f(n))** — upper bound (worst case) — the one you quote
- **Ω(f(n))** — lower bound (best case)
- **Θ(f(n))** — tight bound (average/exact growth)

**Growth order (best → worst)**
```
O(1) < O(log n) < O(n) < O(n log n) < O(n²) < O(n³) < O(2ⁿ) < O(n!)
```

| n | O(log n) | O(n) | O(n log n) | O(n²) | O(2ⁿ) |
|---|---|---|---|---|---|
| 10 | 3 | 10 | 33 | 100 | 1,024 |
| 1,000 | 10 | 1,000 | 10,000 | 1,000,000 | astronomical |
| 1,000,000 | 20 | 10⁶ | 2×10⁷ | 10¹² | ∞ |

**Rules:** drop constants (`O(2n)` → `O(n)`), drop lower terms (`O(n²+n)` → `O(n²)`), add sequential steps, multiply nested loops.

**Space complexity** = auxiliary space (excluding the input). Recursion costs O(depth) stack space.

⭐ **How to reason out loud:** *"Single loop over n → O(n). Nested loop → O(n²). Halving each step → O(log n). Sorting dominates at O(n log n). Using a HashMap trades O(n) space for O(1) lookup, turning an O(n²) nested search into O(n)."*

---

## 2. Arrays

Contiguous memory, fixed size, O(1) indexed access.
`address(i) = base + i × elementSize` — that's *why* access is O(1).

| Operation | Complexity |
|---|---|
| Access by index | **O(1)** |
| Search (unsorted) | O(n) |
| Search (sorted, binary) | O(log n) |
| Insert/Delete at end | O(1) |
| Insert/Delete at middle | O(n) (shifting) |

### Essential array patterns for coding rounds
```java
// TWO POINTERS — pair with target sum in a sorted array, O(n)
int i = 0, j = a.length - 1;
while (i < j) {
    int s = a[i] + a[j];
    if (s == target) return new int[]{i, j};
    else if (s < target) i++;
    else j--;
}

// SLIDING WINDOW — max sum of k consecutive elements, O(n)
int sum = 0;
for (int x = 0; x < k; x++) sum += a[x];
int best = sum;
for (int x = k; x < a.length; x++) { sum += a[x] - a[x-k]; best = Math.max(best, sum); }

// KADANE — maximum subarray sum, O(n)
int cur = a[0], max = a[0];
for (int x = 1; x < a.length; x++) { cur = Math.max(a[x], cur + a[x]); max = Math.max(max, cur); }

// PREFIX SUM — range sum queries in O(1) after O(n) prep
int[] pre = new int[n+1];
for (int x = 0; x < n; x++) pre[x+1] = pre[x] + a[x];
int rangeSum = pre[r+1] - pre[l];

// FREQUENCY MAP — duplicates / first non-repeating / anagram
Map<Integer,Integer> f = new HashMap<>();
for (int x : a) f.merge(x, 1, Integer::sum);

// ROTATE by k (reversal algorithm) — O(n) time, O(1) space
reverse(a, 0, n-1); reverse(a, 0, k-1); reverse(a, k, n-1);

// MISSING NUMBER 1..n
int missing = n*(n+1)/2 - Arrays.stream(a).sum();
// or XOR: x ^= a[i] ^ (i+1)
```

---

## 3. Searching ⭐ (Exercise 2 — E-commerce search)

### Linear Search — O(n)
```java
static int linearSearch(int[] a, int key) {
    for (int i = 0; i < a.length; i++) if (a[i] == key) return i;
    return -1;
}
```
Works on unsorted data, O(1) space, no preprocessing.

### Binary Search — O(log n) — **requires sorted data**
```java
static int binarySearch(int[] a, int key) {
    int lo = 0, hi = a.length - 1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;      // ⚠️ NOT (lo+hi)/2 — avoids int overflow
        if (a[mid] == key) return mid;
        if (a[mid] < key) lo = mid + 1;
        else              hi = mid - 1;
    }
    return -1;
}

// recursive
static int bs(int[] a, int lo, int hi, int key) {
    if (lo > hi) return -1;
    int mid = lo + (hi - lo) / 2;
    if (a[mid] == key) return mid;
    return a[mid] < key ? bs(a, mid+1, hi, key) : bs(a, lo, mid-1, key);
}
```
Built-ins: `Arrays.binarySearch(a, key)`, `Collections.binarySearch(list, key)`.

| | Linear | Binary |
|---|---|---|
| Data | any | **must be sorted** |
| Time | O(n) | **O(log n)** |
| Space | O(1) | O(1) iterative / O(log n) recursive |
| Best for | small / unsorted / linked lists | large sorted arrays |

⭐ **The Exercise 2 answer:** *"For an e-commerce product search, linear search is O(n) per query. If the catalogue is sorted by the search key, binary search gives O(log n) — for 1 million products that's 20 comparisons instead of 1,000,000. In a real system you'd index it (a hash index for exact match → O(1), a B-Tree for range queries), which is exactly what a database index does."*

---

## 4. Sorting ⭐ (Exercise 3 — Customer orders)

| Algorithm | Best | Average | Worst | Space | Stable | In-place |
|---|---|---|---|---|:---:|:---:|
| Bubble | O(n) | O(n²) | O(n²) | O(1) | ✅ | ✅ |
| Selection | O(n²) | O(n²) | O(n²) | O(1) | ❌ | ✅ |
| Insertion | O(n) | O(n²) | O(n²) | O(1) | ✅ | ✅ |
| **Merge** | O(n log n) | O(n log n) | **O(n log n)** | O(n) | ✅ | ❌ |
| **Quick** | O(n log n) | O(n log n) | O(n²) | O(log n) | ❌ | ✅ |
| Heap | O(n log n) | O(n log n) | O(n log n) | O(1) | ❌ | ✅ |
| Counting/Radix | O(n+k) | O(n+k) | O(n+k) | O(k) | ✅ | ❌ |

> **Stable** = equal elements keep their relative order. Matters when you sort by one key then another.

```java
// BUBBLE — optimised with an early-exit flag
static void bubble(int[] a) {
    for (int i = 0; i < a.length - 1; i++) {
        boolean swapped = false;
        for (int j = 0; j < a.length - 1 - i; j++)
            if (a[j] > a[j+1]) { int t=a[j]; a[j]=a[j+1]; a[j+1]=t; swapped = true; }
        if (!swapped) break;               // already sorted → O(n) best case
    }
}

// SELECTION — minimum number of swaps (n-1)
static void selection(int[] a) {
    for (int i = 0; i < a.length - 1; i++) {
        int min = i;
        for (int j = i+1; j < a.length; j++) if (a[j] < a[min]) min = j;
        int t=a[i]; a[i]=a[min]; a[min]=t;
    }
}

// INSERTION — great for small or nearly-sorted arrays
static void insertion(int[] a) {
    for (int i = 1; i < a.length; i++) {
        int key = a[i], j = i - 1;
        while (j >= 0 && a[j] > key) a[j+1] = a[j--];
        a[j+1] = key;
    }
}

// MERGE SORT — divide & conquer, guaranteed O(n log n), stable
static void mergeSort(int[] a, int l, int r) {
    if (l >= r) return;
    int m = l + (r - l) / 2;
    mergeSort(a, l, m); mergeSort(a, m+1, r);
    merge(a, l, m, r);
}
static void merge(int[] a, int l, int m, int r) {
    int[] L = Arrays.copyOfRange(a, l, m+1), R = Arrays.copyOfRange(a, m+1, r+1);
    int i=0, j=0, k=l;
    while (i<L.length && j<R.length) a[k++] = (L[i] <= R[j]) ? L[i++] : R[j++];
    while (i<L.length) a[k++] = L[i++];
    while (j<R.length) a[k++] = R[j++];
}

// QUICK SORT — Lomuto partition
static void quickSort(int[] a, int lo, int hi) {
    if (lo >= hi) return;
    int p = partition(a, lo, hi);
    quickSort(a, lo, p-1); quickSort(a, p+1, hi);
}
static int partition(int[] a, int lo, int hi) {
    int pivot = a[hi], i = lo - 1;
    for (int j = lo; j < hi; j++)
        if (a[j] < pivot) { i++; int t=a[i]; a[i]=a[j]; a[j]=t; }
    int t=a[i+1]; a[i+1]=a[hi]; a[hi]=t;
    return i + 1;
}
```

⭐ **Quick vs Merge (Exercise 3's core question)**
| | Quick Sort | Merge Sort |
|---|---|---|
| Worst case | O(n²) (already-sorted with bad pivot) | **O(n log n) guaranteed** |
| Space | O(log n) — **in-place** | O(n) extra |
| Stable | ❌ | ✅ |
| Cache locality | excellent | poorer |
| Use when | in-memory arrays, memory-constrained | linked lists, external sort, stability required |
| Mitigation | randomised / median-of-three pivot | — |

**What Java actually uses:** `Arrays.sort(int[])` → **dual-pivot quicksort** (fast, primitives don't need stability). `Arrays.sort(Object[])` / `Collections.sort` → **TimSort** (merge + insertion hybrid, stable, O(n) on nearly-sorted data). *This is a great answer to give.*

```java
// Sorting objects — what you'd actually write
orders.sort(Comparator.comparingDouble(Order::getTotal).reversed()
                      .thenComparing(Order::getCustomerName));
```

---

## 5. Recursion ⭐ (Exercise 7 — Financial Forecasting)

Every recursive function needs a **base case** and a **recursive case** that moves toward it.

```java
// Factorial — O(n)
static long fact(int n){ return n <= 1 ? 1 : n * fact(n-1); }

// Fibonacci — naive O(2ⁿ) ❌
static int fib(int n){ return n <= 1 ? n : fib(n-1) + fib(n-2); }

// Fibonacci — memoized O(n) ✅
static long fib(int n, long[] memo) {
    if (n <= 1) return n;
    if (memo[n] != 0) return memo[n];
    return memo[n] = fib(n-1, memo) + fib(n-2, memo);
}

// Fibonacci — iterative O(n) time, O(1) space ✅✅
static long fibIter(int n) {
    long a = 0, b = 1;
    for (int i = 2; i <= n; i++) { long c = a + b; a = b; b = c; }
    return n == 0 ? 0 : b;
}

// FINANCIAL FORECASTING — future value with compound growth (Exercise 7)
static double futureValue(double present, double rate, int years) {
    if (years == 0) return present;                    // base case
    return futureValue(present, rate, years - 1) * (1 + rate);
}
// Optimised: present * Math.pow(1 + rate, years)  → O(1)

// Tower of Hanoi — 2ⁿ − 1 moves
static void hanoi(int n, char from, char to, char aux) {
    if (n == 0) return;
    hanoi(n-1, from, aux, to);
    System.out.printf("Move disk %d: %c → %c%n", n, from, to);
    hanoi(n-1, aux, to, from);
}
```

| | Recursion | Iteration |
|---|---|---|
| Readability | elegant for trees/divide-and-conquer | explicit |
| Memory | O(depth) stack frames | O(1) |
| Risk | `StackOverflowError` | none |
| Speed | slower (call overhead) | faster |

**Tail recursion** — the recursive call is the last operation. Many languages optimise this into a loop; **Java (HotSpot) does not**, so deep recursion still overflows.

⭐ **The Exercise 7 answer:** *"The recursive forecast is O(n) time and O(n) stack space, and recomputes nothing but still risks a StackOverflowError for large horizons. Memoization or an iterative loop makes it O(n)/O(1); and since compound growth is a closed form, `P × (1+r)ⁿ` via `Math.pow` is O(1)."*

---

## 6. Linked List

```java
class Node {
    int data; Node next;
    Node(int d){ data = d; }
}

class LinkedList {
    Node head;

    void insertAtEnd(int d) {
        Node n = new Node(d);
        if (head == null) { head = n; return; }
        Node c = head; while (c.next != null) c = c.next;
        c.next = n;                                        // O(n)
    }
    void insertAtHead(int d){ Node n = new Node(d); n.next = head; head = n; }  // O(1)

    void delete(int d) {
        if (head == null) return;
        if (head.data == d) { head = head.next; return; }
        Node c = head;
        while (c.next != null && c.next.data != d) c = c.next;
        if (c.next != null) c.next = c.next.next;
    }

    // REVERSE — the classic interview question, O(n)/O(1)
    Node reverse() {
        Node prev = null, cur = head;
        while (cur != null) { Node nx = cur.next; cur.next = prev; prev = cur; cur = nx; }
        return head = prev;
    }

    // CYCLE DETECTION — Floyd's tortoise & hare
    boolean hasCycle() {
        Node slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next; fast = fast.next.next;
            if (slow == fast) return true;
        }
        return false;
    }

    // MIDDLE element in one pass
    Node middle() {
        Node slow = head, fast = head;
        while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
        return slow;
    }
    // Nth from end: advance fast n steps, then move both
}
```

| | Array | Linked List |
|---|---|---|
| Memory | contiguous | scattered nodes + pointer overhead |
| Access | **O(1)** | O(n) |
| Insert/delete at head | O(n) | **O(1)** |
| Size | fixed (ArrayList amortised) | dynamic |
| Cache locality | excellent | poor |

Types: singly, doubly (`prev` pointer — `java.util.LinkedList` is doubly-linked), circular.

---

## 7. Stack & Queue ⭐ (Exercise 5 — Task Management uses a queue/list)

### Stack — LIFO
```java
Deque<Integer> stack = new ArrayDeque<>();   // ✅ preferred over legacy java.util.Stack
stack.push(1); stack.pop(); stack.peek(); stack.isEmpty();
```
**Uses:** function call stack, undo/redo, expression evaluation, balanced parentheses, DFS, back button.
```java
// Balanced brackets
static boolean balanced(String s) {
    Deque<Character> st = new ArrayDeque<>();
    Map<Character,Character> pairs = Map.of(')','(', ']','[', '}','{');
    for (char c : s.toCharArray()) {
        if (pairs.containsValue(c)) st.push(c);
        else if (pairs.containsKey(c) && (st.isEmpty() || st.pop() != pairs.get(c))) return false;
    }
    return st.isEmpty();
}
```

### Queue — FIFO
```java
Queue<Integer> q = new LinkedList<>();       // or ArrayDeque
q.offer(1); q.poll(); q.peek();
Deque<Integer> dq = new ArrayDeque<>();      // double-ended
PriorityQueue<Task> pq = new PriorityQueue<>(Comparator.comparingInt(Task::priority));
```
**Uses:** task scheduling (Exercise 5!), BFS, print/message queues, rate limiting, producer-consumer.

---

## 8. Trees ⭐ (Exercise 6 — Library Management search)

```java
class TreeNode {
    int val; TreeNode left, right;
    TreeNode(int v){ val = v; }
}
```

**Terminology:** root, parent, child, leaf, height (edges root→deepest leaf), depth, level, subtree, degree.

### Binary Search Tree — left < node < right
```java
TreeNode insert(TreeNode root, int v) {
    if (root == null) return new TreeNode(v);
    if (v < root.val)      root.left  = insert(root.left, v);
    else if (v > root.val) root.right = insert(root.right, v);
    return root;
}
TreeNode search(TreeNode root, int v) {
    if (root == null || root.val == v) return root;
    return v < root.val ? search(root.left, v) : search(root.right, v);
}
```
| Operation | Balanced | Skewed (worst) |
|---|---|---|
| search/insert/delete | **O(log n)** | O(n) |

**Self-balancing trees:** AVL (strictly balanced, more rotations, faster reads), Red-Black (looser, fewer rotations, faster writes — used by **`TreeMap`/`TreeSet`** and Linux/Java internals), B/B+ Tree (**database and filesystem indexes**, high fanout minimises disk reads).

### Traversals ⭐
```java
void inorder(TreeNode n){ if(n==null)return; inorder(n.left); visit(n); inorder(n.right); }   // L-N-R → SORTED for a BST
void preorder(TreeNode n){ if(n==null)return; visit(n); preorder(n.left); preorder(n.right); } // N-L-R → copy/serialize
void postorder(TreeNode n){ if(n==null)return; postorder(n.left); postorder(n.right); visit(n);} // L-R-N → delete/free

void levelOrder(TreeNode root) {                        // BFS with a queue
    if (root == null) return;
    Queue<TreeNode> q = new LinkedList<>();
    q.offer(root);
    while (!q.isEmpty()) {
        TreeNode n = q.poll();
        visit(n);
        if (n.left != null)  q.offer(n.left);
        if (n.right != null) q.offer(n.right);
    }
}
int height(TreeNode n){ return n == null ? 0 : 1 + Math.max(height(n.left), height(n.right)); }
```

**Heap** — a complete binary tree with the heap property (min-heap: parent ≤ children). Backed by an array: `parent(i)=(i-1)/2`, `left(i)=2i+1`, `right(i)=2i+2`. `PriorityQueue` is a min-heap. Insert/extract O(log n), peek O(1), build-heap O(n).

⭐ **The Exercise 6 answer:** *"Linear search through a book list is O(n). Sorting by title and binary searching gives O(log n). A BST/TreeMap gives O(log n) with dynamic inserts and in-order range scans; a HashMap gives O(1) exact lookups but no range queries. A real library system uses a database B-Tree index — the same trade-off at disk scale."*

---

## 9. Hashing ⭐ (Exercise 1 — Inventory, Exercise 4 — Employee)

A hash function maps a key to a bucket index → **O(1) average** insert/search/delete.

**Collision resolution:** separate chaining (Java's `HashMap`: linked list → Red-Black tree at 8 entries) vs open addressing (linear/quadratic probing, double hashing).

```java
// Exercise 1 — Inventory: HashMap<productId, Product> gives O(1) add/update/delete
Map<String, Product> inventory = new HashMap<>();
inventory.put(p.getId(), p);
inventory.get(id);
inventory.computeIfPresent(id, (k,v) -> { v.setQty(v.getQty()-1); return v; });
```
See [04_Java_Collections_Generics.md §4.1](04_Java_Collections_Generics.md) for full `HashMap` internals.

**Data-structure choice for the exercises**
| Exercise | Best structure | Why |
|---|---|---|
| 1 — Inventory | `HashMap<id, Product>` | O(1) add/update/delete by id |
| 2 — Product search | sorted array + binary search / index | O(log n) vs O(n) |
| 3 — Order sorting | `Comparator` + TimSort | stable, O(n log n) |
| 4 — Employee mgmt | array/`ArrayList` for indexed traversal, `HashMap` for id lookup | trade-off demo |
| 5 — Task mgmt | `LinkedList`/`Queue`, `PriorityQueue` for priority | O(1) head ops |
| 6 — Library | `TreeMap` / BST | O(log n) + ordered range queries |
| 7 — Forecasting | recursion + memoization | avoids exponential recompute |

---

## 10. Graphs (bonus — appears in harder rounds)

```java
Map<Integer, List<Integer>> adj = new HashMap<>();       // adjacency list, O(V+E) space

void bfs(int start) {
    Set<Integer> seen = new HashSet<>();
    Queue<Integer> q = new LinkedList<>();
    q.offer(start); seen.add(start);
    while (!q.isEmpty()) {
        int n = q.poll();
        for (int nb : adj.getOrDefault(n, List.of()))
            if (seen.add(nb)) q.offer(nb);
    }
}
void dfs(int n, Set<Integer> seen) {
    if (!seen.add(n)) return;
    for (int nb : adj.getOrDefault(n, List.of())) dfs(nb, seen);
}
```
| | BFS | DFS |
|---|---|---|
| Structure | Queue | Stack / recursion |
| Finds | **shortest path** (unweighted) | any path, cycle detection |
| Space | O(width) | O(depth) |
Both O(V+E) with an adjacency list.
Know the *names*: Dijkstra (shortest path, weighted, non-negative), Bellman-Ford (negative edges), Kruskal/Prim (MST), topological sort (dependency ordering — think Maven build order).

---

## 11. Dynamic Programming (bonus)

**When:** optimal substructure + overlapping subproblems.
**Two styles:** top-down memoization (recursion + cache) vs bottom-up tabulation (iterative).

```java
// 0/1 Knapsack — O(n·W)
static int knapsack(int[] wt, int[] val, int W) {
    int n = wt.length;
    int[][] dp = new int[n+1][W+1];
    for (int i = 1; i <= n; i++)
        for (int w = 0; w <= W; w++)
            dp[i][w] = (wt[i-1] <= w)
                ? Math.max(val[i-1] + dp[i-1][w-wt[i-1]], dp[i-1][w])
                : dp[i-1][w];
    return dp[n][W];
}

// Climbing stairs / Fibonacci-shaped — O(n)/O(1)
static int ways(int n){ int a=1,b=1; for(int i=2;i<=n;i++){int c=a+b;a=b;b=c;} return b; }
```
Other classics: LCS, LIS, coin change, edit distance, subset sum, matrix path sums.

---

## 12. Algorithm design paradigms
| Paradigm | Idea | Examples |
|---|---|---|
| Brute force | try everything | linear search |
| **Divide & conquer** | split → solve → combine | merge sort, quick sort, binary search |
| **Greedy** | best local choice each step | activity selection, Huffman, Dijkstra, Kruskal |
| **Dynamic programming** | memoize overlapping subproblems | knapsack, LCS |
| Backtracking | build + undo | N-Queens, Sudoku, permutations |
| Branch & bound | prune with bounds | TSP |

---

## ⭐ Interview Gold — DSA Rapid Fire

| Q | A |
|---|---|
| Time complexity of binary search? Requirement? | O(log n); the data **must be sorted**. |
| Best sorting algorithm? | No universal best — Merge for guaranteed O(n log n) + stability, Quick for in-place speed, Insertion for small/nearly-sorted. Java uses dual-pivot quicksort for primitives, TimSort for objects. |
| What does "stable sort" mean and when does it matter? | Equal elements keep relative order — matters for multi-key sorting (sort by name, then stably by department). |
| Array vs LinkedList? | O(1) access vs O(1) head insert; contiguous vs pointer-chased. |
| Stack vs Queue? | LIFO vs FIFO. |
| Where is a stack used in Java itself? | The JVM call stack, expression evaluation, recursion. |
| BFS vs DFS? | Queue/level-order/shortest path vs Stack/depth-first/less memory on wide graphs. |
| Why is HashMap O(1)? | Hash → direct bucket index; degrades to O(log n) if a bucket treeifies from collisions. |
| How would you find a cycle in a linked list? | Floyd's slow/fast pointers, O(n)/O(1). |
| Reverse a linked list? | Three pointers prev/cur/next, iterative, O(n)/O(1). |
| Recursion vs iteration? | Elegance + O(depth) stack vs O(1) space and speed; risk of StackOverflowError. |
| Why is naive Fibonacci bad? | O(2ⁿ) — recomputes the same subproblems; memoize for O(n). |
| BST worst case, and the fix? | O(n) when skewed; use a self-balancing tree (AVL / Red-Black). |
| Which Java class is a Red-Black tree? | `TreeMap` and `TreeSet`. |
| Which data structure backs a database index? | B/B+ Tree (high fanout, disk-friendly). |
| How do you detect duplicates in O(n)? | `HashSet.add()` returns false on a duplicate — O(n) time, O(n) space. |
| Space-time tradeoff example? | Using a `HashMap` for O(1) lookup turns an O(n²) nested loop into O(n), at O(n) extra space. |

---

## 📚 References
- GeeksforGeeks — DSA: https://www.geeksforgeeks.org/data-structures/
- Big-O Cheat Sheet: https://www.bigocheatsheet.com/
- Oracle — Collections Tutorial: https://docs.oracle.com/javase/tutorial/collections/
- `Arrays.sort` Javadoc (documents the algorithms used): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html
- Visualgo (animated data structures): https://visualgo.net/
- LeetCode Top Interview 150: https://leetcode.com/studyplan/top-interview-150/
- NeetCode roadmap: https://neetcode.io/roadmap
- *Introduction to Algorithms* (CLRS) — the reference text
