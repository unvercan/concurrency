Race Condition:

- A race condition occurs when multiple threads access and modify shared data concurrently.
- It can lead to unpredictable results due to simultaneous modifications.
- The final outcome depends on thread scheduling (the timing of thread execution).

Solution to Race Condition:

1) Ensure only one thread can modify the value at a time:

- Use the synchronized keyword or synchronized blocks.
- Implement a lock mechanism (ReentrantLock, etc.).

2) Ensure atomic operations:

- Use atomic variables (AtomicInteger, AtomicBoolean, etc.)
