package dk.stuart.jtestmemprofiler;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("ThrowableNotThrown")
public class ProfilerTest {
	@Test
	void profile_perTypeCollector_recordsAllocations() {
		HashMap<String, Long> allocations = new HashMap<>();

		try (var ignored = new ProfilerBuilder().withPerTypeCollector(allocations::putAll).build()) {
			var ignored2 = new byte[10];
			var ignored3 = new char[20];
			var ignored4 = new Object[40];
			var ignored5 = new RuntimeException("hello");
		}

		assertThat(allocations).containsKeys("[B", "[C", "[Ljava/lang/Object;", "Ljava/lang/RuntimeException;");
		assertThat(allocations.get("[B")).isGreaterThan(10);
		assertThat(allocations.get("[C")).isGreaterThan(20);
		assertThat(allocations.get("[Ljava/lang/Object;")).isGreaterThan(40);
		assertThat(allocations.get("Ljava/lang/RuntimeException;")).isGreaterThan(0);
	}

	@Test
	void profile_totalsCollector_recordsAllocations() {
		long[] totals = {0};

		try (var ignored = new ProfilerBuilder().withTotalsCollector(alloc -> totals[0] = alloc).build()) {
			var ignored2 = new byte[10];
			var ignored3 = new char[20];
		}

		assertThat(totals[0]).isGreaterThan(30);
	}

	@Test
	void profile_perTypeCollectorFilteredTypes_recordsAllocations() {
		HashMap<String, Long> allocations = new HashMap<>();

		try (var ignored = new ProfilerBuilder().withPerTypeCollector(allocations::putAll).withAllocationTypeFilter(Set.of(byte[].class, Object[].class)).build()) {
			var ignored2 = new byte[10];
			var ignored3 = new char[20];
			var ignored4 = new Object[40];
			var ignored5 = new RuntimeException("hello");
		}

		assertThat(allocations).containsKeys("[B", "[Ljava/lang/Object;");
		assertThat(allocations).doesNotContainKeys("[C", "Ljava/lang/RuntimeException;");
		assertThat(allocations.get("[B")).isGreaterThan(10);
		assertThat(allocations.get("[Ljava/lang/Object;")).isGreaterThan(40);
	}

	@Test
	void profiler_perTypeCollectorFilteredThread_recordsAllocations() {
		HashMap<String, Long> allocations = new HashMap<>();

		var t1 = new Thread(() -> {
			var ignored = new byte[10];
		});
		var t2 = new Thread(() -> {
			var ignored = new char[10];
		});

		try (var ignored = new ProfilerBuilder().withPerTypeCollector(allocations::putAll).withThreadIdFilter(Set.of(t1)).build()) {
			t1.start();
			t2.start();
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		assertThat(allocations).containsKeys("[B");
		assertThat(allocations).doesNotContainKeys("[C");
		assertThat(allocations.get("[B")).isGreaterThan(10);
	}
}
