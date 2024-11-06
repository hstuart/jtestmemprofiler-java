# JTestMemProfiler

A Java unit test memory allocation profiler library.

JTestMemProfiler allows you to run individual tests in serial and record the
total allocation profiler, or per-type allocation profile of a segment of your
test code, e.g. to monitor business critical code to keep memory allocation
overhead low or non-existent.

This library depends on an underlying native agent written in C++ that interfaces
with JVMTI that must be loaded in as part of the test suite runner in order to
facilitate tracking of and asserting on memory allocation profiles.

## Installation

The native agent and this library must be included as dependencies. As an example,
using Gradle with JUnit, the following can be used:

```kotlin
plugins {
    id("com.google.osdetector") version("1.7.3")
    id("java")
}

repositories {
    mavenCentral()
}

val agent = configurations.create("agent")

dependencies {
    agent("dk.stuart:jtestmemprofiler-native-agent:1.0.1") {
        this.artifact {
            this.classifier = "${osdetector.classifier}-jdk${JavaVersion.current()}"
            this.extension = when (osdetector.os) {
                "windows" -> "dll"
                "linux" -> "so"
                "osx" -> "dylib"
                else -> {
                    throw RuntimeException("Unsupported OS ${osdetector.os}")
                }
            }
        }
    }

    testImplementation("dk.stuart.jtestmemprofiler:jtestmemprofiler:1.0.1")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-agentpath:${agent.singleFile}")
}
```

## Usage

Note that depending on proper warmup approaches ensuring that all relevant types are
fully loaded, a profile capture _may_ include other types being instantiated, e.g.
by the underlying test framework or other threads operating in the system under test.
It is thus advisable to use this library either to report metrics to another system
(e.g., prometheus, InfluxDB, SQL database, etc.), or to assert based on approximate
numbers or specific types in order to not create flaky tests.

Using the memory allocation profiler can be accomplished in a test like this:

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
    }).build()){
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
