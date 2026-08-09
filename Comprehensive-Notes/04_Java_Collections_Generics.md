# 04 — Java Collections Framework & Generics

> Guaranteed interview territory. If you know `HashMap` internals cold, you clear most fresher Java rounds.
> **References:** [Collections Tutorial](https://docs.oracle.com/javase/tutorial/collections/), [java.util Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/package-summary.html)

---

## 1. The hierarchy

```
                        Iterable<E>
                             │
                       Collection<E>
        ┌────────────────────┼────────────────────┐
      List<E>              Set<E>               Queue<E>
        │                    │                     │
   ArrayList            HashSet ──► LinkedHashSet  ├─ PriorityQueue
   LinkedList           TreeSet (SortedSet/Navigable)  ├─ ArrayDeque (Deque)
   Vector → Stack       EnumSet                    └─ LinkedList
   CopyOnWriteArrayList CopyOnWriteArraySet           LinkedBlockingQueue

    Map<K,V>   ← NOT a Collection (no Iterable)
        ├─ HashMap ──► LinkedHashMap
        ├─ TreeMap (SortedMap / NavigableMap)
        ├─ Hashtable ──► Properties
        ├─ ConcurrentHashMap
        ├─ EnumMap
        └─ WeakHashMap / IdentityHashMap
```

⭐ **`Map` is not a `Collection`** — it stores key→value *pairs*, not single elements, so it doesn't extend `Collection`/`Iterable`. You iterate via `entrySet()`, `keySet()`, or `values()`.

### `Collection` core methods
```java
add(e) addAll(c) remove(o) removeAll(c) removeIf(pred) retainAll(c)
contains(o) containsAll(c) size() isEmpty() clear() iterator() stream() toArray()
```

---

## 2. List — ordered, indexed, allows duplicates

### 2.1 ArrayList vs LinkedList vs Vector ⭐⭐

| | `ArrayList` | `LinkedList` | `Vector` |
|---|---|---|---|
| Backing | resizable **array** | **doubly-linked list** | resizable array |
| `get(i)` | **O(1)** | O(n) | O(1) |
| `add(e)` at end | O(1) amortised | O(1) | O(1) |
| `add(0, e)` / `remove(0)` | O(n) (shift) | **O(1)** | O(n) |
| Memory | compact | +2 pointers per node | compact |
| Thread-safe | ❌ | ❌ | ✅ (synchronized, legacy) |
| Growth | ×1.5 (`oldCap + oldCap>>1`) | n/a | ×2 |
| Implements | `RandomAccess` | `Deque` too | `RandomAccess` |

> **Practical answer:** use `ArrayList` ~95% of the time. `LinkedList` only when you're constantly inserting/removing at the head — and even then `ArrayDeque` is usually better. `Vector`/`Stack` are legacy; use `Collections.synchronizedList` or `CopyOnWriteArrayList`.

```java
List<String> list = new ArrayList<>();
list.add("A"); list.add(0, "B");          // append / insert
list.get(0); list.set(0, "C");
list.remove(0);            // by INDEX
list.remove("C");          // by OBJECT — ⚠️ for List<Integer>, remove(1) removes INDEX 1!
list.indexOf("A"); list.contains("A"); list.subList(0, 2);
list.sort(Comparator.naturalOrder());
Collections.sort(list); Collections.reverse(list); Collections.shuffle(list);
list.removeIf(s -> s.startsWith("X"));
```
⭐ `list.remove(1)` vs `list.remove(Integer.valueOf(1))` — the first removes index 1, the second removes the *value* 1. Classic trick question.

**ArrayList internals:** default capacity 10 (lazily allocated on first add). On overflow, `grow()` creates a new array of `1.5×` size and `Arrays.copyOf`s. `ensureCapacity(n)` avoids repeated copies if you know the size.

**Immutable lists**
```java
List<String> a = List.of("x","y");                        // Java 9, truly immutable
List<String> b = Collections.unmodifiableList(orig);      // unmodifiable VIEW of a mutable list
List<String> c = Arrays.asList("x","y");                  // fixed-size view backed by the array
```

---

## 3. Set — no duplicates

| | `HashSet` | `LinkedHashSet` | `TreeSet` |
|---|---|---|---|
| Backing | `HashMap` | `LinkedHashMap` | `TreeMap` (Red-Black tree) |
| Order | **none** | **insertion order** | **sorted** |
| add/remove/contains | O(1) | O(1) | **O(log n)** |
| `null` allowed | ✅ one | ✅ one | ❌ (NPE on compare) |
| Requires | `equals`+`hashCode` | same | `Comparable`/`Comparator` |

```java
Set<String> hs = new HashSet<>();
Set<String> ls = new LinkedHashSet<>();
NavigableSet<Integer> ts = new TreeSet<>(Comparator.reverseOrder());
ts.first(); ts.last(); ts.headSet(5); ts.tailSet(5); ts.subSet(2,8);
ts.floor(5); ts.ceiling(5); ts.lower(5); ts.higher(5); ts.pollFirst();
```
⭐ `HashSet.add()` internally calls `map.put(e, PRESENT)` and returns `true` only if the key was new — that's how duplicate rejection works, which is why **`equals`/`hashCode` must be correct**.

---

## 4. Map — key→value

| | `HashMap` | `LinkedHashMap` | `TreeMap` | `Hashtable` | `ConcurrentHashMap` |
|---|---|---|---|---|---|
| Order | none | insertion (or **access**) | sorted by key | none | none |
| null key | ✅ one | ✅ one | ❌ | ❌ | ❌ |
| null value | ✅ | ✅ | ✅ | ❌ | ❌ |
| Thread-safe | ❌ | ❌ | ❌ | ✅ (whole-map lock) | ✅ (**bucket-level CAS + sync**) |
| get/put | O(1) | O(1) | O(log n) | O(1) | O(1) |
| Since | 1.2 | 1.4 | 1.2 | 1.0 (legacy) | 1.5 |

### 4.1 HashMap internals ⭐⭐⭐ (the deep question)

```
     hash(key) → index = (n-1) & hash        table = Node[] (array of buckets)

     [0] → null
     [1] → Node(k1,v1) → Node(k9,v9)         ← collision chain (LinkedList)
     [2] → null
     [3] → TreeNode(...)                     ← treeified bucket (Red-Black tree)
```

**Mechanics**
1. `hash(key)` = `h = key.hashCode(); h ^ (h >>> 16)` — **spreading**: XORs the high 16 bits into the low bits so that high-bit differences still affect the (small) bucket index.
2. Index = `(n - 1) & hash`, where `n` (capacity) is always a power of 2 — so `&` replaces the expensive `%`.
3. **Default capacity 16, load factor 0.75.** When `size > capacity × loadFactor` (i.e. 12), it **resizes to 2×** and **rehashes** everything.
4. **Collision:** entries in the same bucket form a linked list.
5. **Treeify (Java 8+):** when a bucket has **≥ 8** entries *and* table capacity ≥ **64**, the chain converts to a **Red-Black tree** → worst case O(n) becomes **O(log n)**. It untreeifies below **6** on resize. This mitigates hash-collision DoS.
6. **`put` flow:** compute hash → find bucket → if empty, insert → else walk the chain comparing `hash == hash && (k == key || key.equals(k))` → replace value if equal, else append.

**Why load factor 0.75?** A time/space trade-off — Poisson analysis in the JDK source shows bucket-collision probability stays low while space waste stays modest.

**What if `hashCode()` always returns the same value?** All entries land in one bucket → degrades to a linked list (O(n)) or a tree (O(log n) since Java 8).

**Why must keys be immutable?** If a key's field changes after insertion, its `hashCode` changes, so lookup computes a different bucket → the entry becomes unreachable (a memory leak). Use `String`, wrappers, or immutable value objects as keys.

### 4.2 Map API — including the Java 8 additions
```java
Map<String,Integer> m = new HashMap<>();
m.put("a", 1); m.get("a"); m.remove("a"); m.containsKey("a"); m.containsValue(1);
m.getOrDefault("z", 0);
m.putIfAbsent("a", 1);
m.computeIfAbsent("list", k -> new ArrayList<>()).add(x);   // multimap idiom
m.computeIfPresent("a", (k,v) -> v + 1);
m.compute("a", (k,v) -> v == null ? 1 : v + 1);
m.merge("a", 1, Integer::sum);                              // frequency-count idiom ⭐
m.forEach((k,v) -> System.out.println(k + "=" + v));
m.replaceAll((k,v) -> v * 2);

for (Map.Entry<String,Integer> e : m.entrySet())            // preferred iteration
    System.out.println(e.getKey() + " " + e.getValue());
```

**Sort a map by value ⭐**
```java
LinkedHashMap<String,Integer> sorted = m.entrySet().stream()
    .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                              (a,b) -> a, LinkedHashMap::new));
```

### 4.3 LRU cache with LinkedHashMap ⭐ (a favourite)
```java
class LRUCache<K,V> extends LinkedHashMap<K,V> {
    private final int capacity;
    LRUCache(int capacity) {
        super(capacity, 0.75f, true);   // accessOrder = true
        this.capacity = capacity;
    }
    @Override protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > capacity;
    }
}
```

### 4.4 HashMap vs Hashtable vs ConcurrentHashMap ⭐
- **Hashtable** — legacy, every method `synchronized` on the whole object → one thread at a time. No nulls.
- **ConcurrentHashMap** — Java 8 uses **CAS on empty bins + `synchronized` on the first node of a bin**, so different buckets are updated concurrently. (Pre-Java-8 it used 16 segment locks.) Iterators are **weakly consistent** — no `ConcurrentModificationException`. `size()` is an estimate. No nulls (so `get()==null` unambiguously means "absent").
- **`Collections.synchronizedMap(m)`** — a wrapper locking every call on a single mutex; you must still manually synchronize during iteration.

---

## 5. Queue & Deque

```java
Queue<Integer> q = new LinkedList<>();
q.offer(1);      // add, returns false if full   (add() throws)
q.poll();        // retrieve+remove, null if empty (remove() throws)
q.peek();        // retrieve, null if empty       (element() throws)

Deque<Integer> stack = new ArrayDeque<>();   // ✅ preferred over legacy Stack
stack.push(1); stack.pop(); stack.peek();
Deque<Integer> dq = new ArrayDeque<>();
dq.addFirst(1); dq.addLast(2); dq.pollFirst(); dq.pollLast();

PriorityQueue<Integer> minHeap = new PriorityQueue<>();                    // natural order
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
PriorityQueue<Task> byPrio = new PriorityQueue<>(Comparator.comparingInt(Task::prio));
minHeap.offer(5); minHeap.poll();   // O(log n) insert/extract, O(1) peek
```
> `PriorityQueue` is a **binary heap** — it is **not fully sorted**; only `peek()`/`poll()` respect priority. Iteration order is arbitrary.
> `ArrayDeque` beats `Stack` (synchronized legacy, extends Vector) and `LinkedList` (pointer overhead) for both stack and queue use.

**Blocking queues** (producer-consumer): `ArrayBlockingQueue`, `LinkedBlockingQueue`, `PriorityBlockingQueue`, `SynchronousQueue`, `DelayQueue` — `put()`/`take()` block.

---

## 6. Iterator, ListIterator & fail-fast ⭐

```java
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String s = it.next();
    if (s.equals("x")) it.remove();     // ✅ the ONLY safe removal during iteration
}
```

**ConcurrentModificationException** — collections track a `modCount`; the iterator caches `expectedModCount` at creation and compares on every `next()`. Structurally modifying the collection directly during a for-each throws CME.
```java
for (String s : list) if (s.equals("x")) list.remove(s);   // ❌ CME
list.removeIf(s -> s.equals("x"));                         // ✅ Java 8, cleanest
```

| Fail-fast | Fail-safe |
|---|---|
| `ArrayList`, `HashMap`, `HashSet` … | `CopyOnWriteArrayList`, `ConcurrentHashMap` |
| Throws CME | No exception |
| Iterates the live collection | Iterates a snapshot / weakly-consistent view |
| No memory overhead | Copy overhead (COW) / may miss recent updates |

`ListIterator` adds `hasPrevious()`, `previous()`, `add()`, `set()` and bidirectional traversal (List only).

---

## 7. Comparable vs Comparator ⭐⭐

```java
// COMPARABLE — natural ordering, defined INSIDE the class, ONE ordering
class Employee implements Comparable<Employee> {
    @Override public int compareTo(Employee o) { return Integer.compare(this.id, o.id); }
}
Collections.sort(list);          // uses compareTo

// COMPARATOR — external, MANY orderings
Comparator<Employee> bySalary = Comparator.comparingDouble(Employee::getSalary);
Comparator<Employee> byNameThenSalaryDesc =
    Comparator.comparing(Employee::getName)
              .thenComparing(Employee::getSalary, Comparator.reverseOrder());
Comparator<Employee> nullSafe =
    Comparator.nullsFirst(Comparator.comparing(Employee::getName));

list.sort(bySalary);
list.sort(bySalary.reversed());
```

| | `Comparable` | `Comparator` |
|---|---|---|
| Package | `java.lang` | `java.util` |
| Method | `compareTo(T o)` | `compare(T a, T b)` |
| Where | inside the class | separate class / lambda |
| Orderings | one (natural) | many |
| Modifies the class | yes | no |

**Contract:** return negative / zero / positive. Must be *consistent with equals* ideally (`compareTo(b)==0` ⟺ `equals(b)`) — `TreeSet`/`TreeMap` use `compareTo`, **not** `equals`, so an inconsistent comparator silently drops "duplicates".
⚠️ Never implement `compare` as `a.getAge() - b.getAge()` — integer overflow. Use `Integer.compare`.

---

## 8. Generics ⭐

### Why
Compile-time type safety + no casts. Before generics:
```java
List l = new ArrayList();
l.add("s");
Integer i = (Integer) l.get(0);   // compiles, ClassCastException at RUNTIME
```

### Generic class / method / interface
```java
class Box<T> {
    private T value;
    void set(T v){ value = v; }
    T get(){ return value; }
}

class Pair<K, V> {
    private final K key; private final V value;
    Pair(K k, V v){ key = k; value = v; }
}

// generic method — <T> before the return type
public static <T extends Comparable<T>> T max(List<T> list) {
    T best = list.get(0);
    for (T t : list) if (t.compareTo(best) > 0) best = t;
    return best;
}
```
Convention: `T` type, `E` element, `K` key, `V` value, `N` number, `R` result, `?` wildcard.

### Bounded types
```java
<T extends Number>                    // upper bound: T is Number or a subclass
<T extends Comparable<T> & Cloneable> // multiple bounds (class first, then interfaces)
```

### Wildcards & PECS ⭐⭐
```java
List<?>                    // unknown type — read as Object, can only add null
List<? extends Number>     // "producer" — you can READ Numbers, can't add (except null)
List<? super Integer>      // "consumer" — you can ADD Integers, reads give Object
```
> **PECS — Producer Extends, Consumer Super** (Effective Java Item 31).
```java
// copies FROM src (producer) TO dest (consumer)
static <T> void copy(List<? extends T> src, List<? super T> dest) {
    for (T t : src) dest.add(t);
}
Collections.copy(dest, src);   // exactly this signature in the JDK
```
Why can't you `add` to `List<? extends Number>`? It might actually be a `List<Integer>`; adding a `Double` would corrupt it — so the compiler forbids all adds.

### Type erasure ⭐
Generics are a **compile-time** feature. The compiler checks types, inserts casts, then **erases** type parameters (to their bound, or `Object`). At runtime `List<String>` and `List<Integer>` are both just `List`.

**Consequences:**
```java
new T();                        // ❌ can't instantiate a type parameter
new T[10];                      // ❌ can't create a generic array
list instanceof List<String>    // ❌ illegal — use List<?>
static T field;                 // ❌ no static members of a type parameter
void m(List<String> l){} void m(List<Integer> l){}   // ❌ same erasure — clash
catch (MyException<T> e)        // ❌ can't catch/throw a generic exception type
List<int>                       // ❌ primitives not allowed — use List<Integer>
```
```java
List<String> a = new ArrayList<>();
List<Integer> b = new ArrayList<>();
a.getClass() == b.getClass();   // true! — erasure
```

**Generics are invariant:** `List<String>` is **not** a `List<Object>` (unlike arrays, which are covariant and therefore unsafe).

**Heap pollution & `@SafeVarargs`:** a generic varargs parameter creates an unchecked array; annotate with `@SafeVarargs` if you only read from it.

---

## 9. `Collections` utility class
```java
Collections.sort(list); Collections.sort(list, cmp);
Collections.reverse(list); Collections.shuffle(list); Collections.swap(list,0,1);
Collections.max(list); Collections.min(list); Collections.frequency(list, x);
Collections.binarySearch(sortedList, key);
Collections.emptyList(); Collections.singletonList(x); Collections.nCopies(3,"x");
Collections.unmodifiableList(list);
Collections.synchronizedList(list); Collections.synchronizedMap(map);
Collections.disjoint(a, b);  Collections.addAll(list, "a","b");
```

---

## 10. Choosing the right collection

| Need | Use |
|---|---|
| Indexed access, mostly reads | `ArrayList` |
| Frequent head insert/remove | `ArrayDeque` (or `LinkedList`) |
| Unique elements, fast lookup | `HashSet` |
| Unique + insertion order | `LinkedHashSet` |
| Unique + sorted / range queries | `TreeSet` |
| Key→value, fast lookup | `HashMap` |
| Key→value + insertion/access order, LRU | `LinkedHashMap` |
| Key→value sorted / `floorKey`/`ceilingKey` | `TreeMap` |
| Enum keys | `EnumMap` / `EnumSet` (fastest) |
| Thread-safe map | `ConcurrentHashMap` |
| Read-mostly thread-safe list | `CopyOnWriteArrayList` |
| Stack / Queue | `ArrayDeque` |
| Priority processing | `PriorityQueue` |
| Producer-consumer | `LinkedBlockingQueue` |

---

## ⭐ Interview Gold — Collections Rapid Fire

| Q | A |
|---|---|
| Array vs ArrayList? | Fixed size vs dynamic; primitives allowed vs objects only; `length` vs `size()`; no generics vs generic. |
| ArrayList vs LinkedList? | Array vs doubly-linked; O(1) vs O(n) random access; O(n) vs O(1) head insert. |
| How does `ArrayList` grow? | New array of 1.5× capacity + `Arrays.copyOf`. |
| HashMap vs HashTable? | Sync, nulls, legacy, iterator type (see §4.4). |
| How does HashMap work internally? | hash spreading → `(n-1)&hash` bucket → chain → treeify at 8 with capacity ≥64 → resize at load factor 0.75. |
| Default capacity & load factor? | 16 and 0.75 → resizes at 12 entries. |
| Why is capacity a power of 2? | Lets `(n-1) & hash` replace modulo, and keeps the bit-mask distribution even. |
| What happens on hash collision? | Chained in the bucket; treeified to a Red-Black tree at 8 entries. |
| Can `HashMap` have a null key? | Yes, exactly one — it goes to bucket 0. `Hashtable`/`ConcurrentHashMap` cannot. |
| Why override `equals` and `hashCode` for map keys? | Otherwise identity semantics put equal-by-value objects in different buckets → lookups fail. |
| HashSet vs TreeSet? | O(1) unordered vs O(log n) sorted; TreeSet needs Comparable/Comparator and rejects null. |
| ConcurrentHashMap vs synchronizedMap? | Bucket-level locking/CAS + weakly-consistent iterators vs single global lock + manual iteration sync. |
| Fail-fast vs fail-safe? | modCount check → CME vs snapshot/weakly-consistent → no CME. |
| How to avoid CME? | `Iterator.remove()`, `removeIf()`, iterate a copy, or use a concurrent collection. |
| Comparable vs Comparator? | Natural single ordering inside the class vs multiple external orderings. |
| What is type erasure? | Generic type info is removed at compile time; runtime sees raw types. |
| PECS? | Producer Extends, Consumer Super. |
| Can you create `new T[]`? | No — erasure means the runtime type is unknown; use `(T[]) new Object[n]` with `@SuppressWarnings`, or `Array.newInstance`. |
| Why is `Map` not a `Collection`? | It stores pairs, not elements; different contract. |
| Iterator vs ListIterator vs Enumeration? | Forward+remove / bidirectional+add+set (List only) / legacy read-only. |
| How would you build an LRU cache? | `LinkedHashMap` with `accessOrder=true` and `removeEldestEntry` (see §4.3). |

---

## 📚 References
- Oracle — Collections Framework Tutorial: https://docs.oracle.com/javase/tutorial/collections/
- Oracle — Generics Tutorial: https://docs.oracle.com/javase/tutorial/java/generics/
- java.util Javadoc: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/package-summary.html
- Baeldung — Java HashMap Guide: https://www.baeldung.com/java-hashmap
- Baeldung — ConcurrentHashMap: https://www.baeldung.com/java-concurrent-map
- OpenJDK `HashMap` source (read the class Javadoc — it explains treeification): https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/HashMap.java
- *Effective Java* Items 26–33 (generics)
