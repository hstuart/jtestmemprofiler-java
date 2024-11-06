package dk.stuart.jtestmemprofiler;

import java.util.*;
import java.util.function.*;

/**
 * A builder of {@link Profiler}.
 * <p>
 * The intermediate setter-like methods change the state of the builder until built. Use of the
 * builder should at least set one of the collectors, for the remaining methods defaults will be
 * used if not invoked. A {@link ProfilerBuilder} is not safe for use by multiple threads without
 * external synchronization.
 * <p>
 * Only a single collector method can be invoked on a builder, as well as a single filter, as that
 * is the only thing supported by the underlying JVMTI agent.
 */
@SuppressWarnings("unused")
public class ProfilerBuilder {
	private NativeCollector nativeCollector = null;
	private NativeFilter nativeFilter = null;
	private Consumer<HashMap<String, Long>> perTypeCollectorCallback = null;
	private Consumer<Long> totalsCollectorCallback = null;
	private int sampleRate = 0;
	private boolean enableImmediately = true;

	/**
	 * Set the profiler to collect allocation totals by allocation type.
	 * @param callback Callback that gets passed the allocation totals once the profiling is complete
	 * @return this builder
	 */
	public ProfilerBuilder withPerTypeCollector(Consumer<HashMap<String, Long>> callback) {
		if (nativeCollector != null) throw new IllegalStateException("Can only assign a single collector");
		nativeCollector = new NativePerTypeCollector();
		perTypeCollectorCallback = callback;
		return this;
	}

	/**
	 * Set the profiler to collect the total allocation during profiling.
	 * @param callback Callback that gets passed the allocation total once the profiling is complete
	 * @return this builder
	 */
	public ProfilerBuilder withTotalsCollector(Consumer<Long> callback) {
		if (nativeCollector != null) throw new IllegalStateException("Can only assign a single collector");
		nativeCollector = new NativeTotalsCollector();
		totalsCollectorCallback = callback;
		return this;
	}

	/**
	 * Set the profiler to only collect allocation information for the specified classes.
	 * @param classes Set of class instances to track allocation information for
	 * @return this builder
	 */
	public ProfilerBuilder withAllocationTypeFilter(Set<Class<?>> classes) {
		if (nativeFilter != null) throw new IllegalStateException("Can only assign a single filter");
		nativeFilter = new NativeAllocationTypeFilter(classes);
		return this;
	}

	/**
	 * Set the profiler to only collect allocation information for the specified threads.
	 * @param threads Set of threads to track allocation information for
	 * @return this builder
	 */
	public ProfilerBuilder withThreadIdFilter(Set<Thread> threads) {
		if (nativeFilter != null) throw new IllegalStateException("Can only assign a single filter");
		nativeFilter = new NativeThreadIdFilter(threads);
		return this;
	}

	/**
	 * Set the sample rate for tracking allocations (every sampleRate allocations will be recorded).
	 * @param sampleRate record every sampleRate bytes allocation (defaults to 0 that tracks everything)
	 * @return this builder
	 */
	public ProfilerBuilder withSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
		return this;
	}

	/**
	 * Set the profiler to be enabled immediately when the profiler is constructed
	 * @param enableImmediately true if enabled immediately, false otherwise (defaults to true)
	 * @return this builder
	 */
	public ProfilerBuilder withEnableImmediately(boolean enableImmediately) {
		this.enableImmediately = enableImmediately;
		return this;
	}

	/**
	 * Constructs a profiler instance based on the values set on this builder.
	 * @return Profiler instance
	 */
	public Profiler build() {
		return new Profiler(nativeCollector, nativeFilter, sampleRate, perTypeCollectorCallback, totalsCollectorCallback, enableImmediately);
	}
}
