# JTestMemProfiler

A Java Memory Allocation Profiling Library.

JTestMemProfiler is a specialized library designed for profiling memory allocations during unit testing in Java
applications. It enables you to execute individual tests sequentially while capturing comprehensive data on overall
memory usage or specific types of allocations within targeted segments of your code. This can be particularly beneficial
for monitoring and optimizing memory allocation overhead in critical sections of your application, ensuring it remains
minimal or non-existent.

The library relies on a native agent developed in C++ that interfaces with the Java Virtual Machine Tool Interface (
JVMTI). To leverage its profiling capabilities, this agent must be integrated into the test suite runner. This
integration is essential for tracking memory allocations and validating allocation profiles during your tests.

## Installation

To set up JTestMemProfiler in your project, both the native agent and this Java library need to be added as
dependencies. For instance, when using Gradle alongside JUnit, you can include them in your `build.gradle.kts` file like so:

```kotlin
plugins {
    id("java")
}

repositories {
    mavenCentral()
}

val agent = configurations.create("agent")

dependencies {
    agent("dk.stuart:jtestmemprofiler-native-agent:1.0.1") {
        this.artifact {
            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                this.classifier = "windows-x86_64-jdk${JavaVersion.current()}"
                this.extension = "dll"
            } else if (Os.isFamily(Os.FAMILY_MAC)) {
                this.classifier = "osx-x86_64-${JavaVersion.current()}"
                this.extension = "dylib"
            } else if (Os.isFamily(Os.FAMILY_UNIX)) {
                this.classifier = "linux-x86_64-${JavaVersion.current()}"
                this.extension = "so"
            } else {
                throw RuntimeException("unsupported operating system")
            }
        }
    }

    testImplementation("dk.stuart:jtestmemprofiler:1.0.1")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-agentpath:${agent.singleFile}")
}
```

## Usage

_Note that depending on proper warmup approaches ensuring that all relevant types are
fully loaded, a profile capture _may_ include other types being instantiated, e.g.
by the underlying test framework or other threads operating in the system under test.

It is advisable to use this library either to report metrics to another system
(e.g., prometheus, InfluxDB, SQL database, etc.), or to assert based on approximate
numbers or specific types in order to not create flaky tests.

To utilize the memory allocation profiler in your tests, you can implement it as follows:

```java
HashMap<String, Long> allocations = new HashMap<>();

try (var ignored = new ProfilerBuilder().withPerTypeCollector(allocations::putAll).build()){
	var ignored2 = new byte[10];
}
```

Types are written into the `allocations` map using their `.class.getName()` method, so
`new byte[...]` will be written as `byte.class.getName()` as key or `[B`.

As an alternative, a total allocation amount can be computed like this:

```java
long[] totals = {0};

try (var ignored = new ProfilerBuilder().withTotalsCollector(alloc -> {
        totals[0] = alloc;
    }).build()) {
    var ignored2 = new byte[10];
}
```

For each of the collectors, it is also possible to filter allocations either by types or
by threads, e.g.:

```java
HashMap<String, Long> allocations = new HashMap<>();

try (var ignored = new ProfilerBuilder()
        .withPerTypeCollector(allocations::putAll)
        .withAllocationTypeFilter(Set.of(byte[].class, Object[].class)).build()) {
    var ignored2 = new byte[10];
}
```

```java
try (var ignored = new ProfilerBuilder()
        .withPerTypeCollector(allocations::putAll)
        .withThreadIdFilter(Set.of(Thread.currentThread())).build()) {
	var ignored2 = new byte[10];
}
```

Finally, you can get all stack traces where allocation occurred in your profiling like this:

```java
TrieNode[] allocations = { null };

try (var ignored = new ProfilerBuilder()
        .withCallTreeCollector(trieNode -> allocations[0] = trieNode).build()) {
    var ignored2 = new byte[10];
}
```

Note that the trie structure will start at the entry-point of the thread and/or program being profiled. As such, the
order of information in the trie is inverse to the order you will see in a stacktrace.
