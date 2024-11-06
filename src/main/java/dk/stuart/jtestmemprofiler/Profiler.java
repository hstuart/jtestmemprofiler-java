package dk.stuart.jtestmemprofiler;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * A JVMTI profiler instance frontend for a native agent JVMTI profiler.
 * <p>
 * The profiler is constructed through {@link ProfilerBuilder}.
 * <p>
 * The profiler is intended to be used as part of a test framework to calculate memory allocation profiles
 * for code under test.
 */
@SuppressWarnings("unused")
public class Profiler implements Closeable {
	private final NativeCollector collector;
	private final NativeFilter filter;
	private final Consumer<HashMap<String, Long>> perTypeCollectorCallback;
	private final Consumer<Long> totalsCollectorCallback;
	private final NativeProfiler profiler;
	private final int sampleRate;
	private boolean closed = false;

	Profiler(NativeCollector collector, NativeFilter filter, int sampleRate, Consumer<HashMap<String, Long>> perTypeCollectorCallback, Consumer<Long> totalsCollectorCallback, boolean enableImmediately) {
		this.collector = collector;
		this.filter = filter;
		this.sampleRate = sampleRate;
		this.perTypeCollectorCallback = perTypeCollectorCallback;
		this.totalsCollectorCallback = totalsCollectorCallback;
		profiler = new NativeProfiler();
		profiler.doSetCollector(collector);
		profiler.doSetFilter(filter);

		if (enableImmediately) {
			enable();
		}
	}

	/**
	 * Closes the profiler and releases any native resources in use.
	 *
	 * @throws IllegalStateException if a closed profiler is attempted to be closed again
	 */
	@Override
	public void close() {
		checkClosed();
		closed = true;

		profiler.doDisable();

		if (perTypeCollectorCallback != null)
			perTypeCollectorCallback.accept(((NativePerTypeCollector)collector).getAllocations());

		if (totalsCollectorCallback != null)
			totalsCollectorCallback.accept(((NativeTotalsCollector)collector).getAllocationTotal());

		collector.close();
		profiler.doSetCollector(null);

		if (filter != null) {
			filter.close();
			profiler.doSetFilter(null);
		}
	}

	private void checkClosed() {
		if (closed) throw new IllegalStateException("Profiler has already been closed. Construct a new profiler.");
	}

	/**
	 * Starts collecting allocation profiling information. If already enabled, no further effect.
	 *
	 * @throws IllegalStateException if a closed profiler is attempted to be closed again
	 */
	public void enable() {
		checkClosed();
		profiler.doEnable();
		profiler.doSetSampleRate(sampleRate);
	}

	/**
	 * Stops collecting allocation profiling information. If already disabled, no further effect.
	 *
	 * @throws IllegalStateException if a closed profiler is attempted to be closed again
	 */
	public void disable() {
		checkClosed();
		profiler.doDisable();
	}

	/**
	 * Gets a value indicating whether profiling is enabled.
	 *
	 * @return true if profiling is enabled, false otherwise
	 * @throws IllegalStateException if a closed profiler is attempted to be closed again
	 */
	public boolean isEnabled() {
		checkClosed();
		return profiler.doIsEnabled();
	}
}
