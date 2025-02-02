package dk.stuart.jtestmemprofiler;

class NativeProfiler {
	private static native int disable();

	private static native int enable();

	private static native boolean isEnabled();

	private static native void setCollector(long nativeHandle);

	private static native void setFilter(long nativeHandle);

	private static native int setSampleRate(int sampleRate);

	public void doEnable() {
		var error = enable();
		if (error != 0) throw new ProfilerException(error);
	}

	public void doDisable() {
		var error = disable();
		if (error != 0) throw new ProfilerException(error);
	}

	public boolean doIsEnabled() {
		return isEnabled();
	}

	public void doSetCollector(NativeCollector collector) {
		setCollector(collector == null ? 0L : collector.getNativeHandle());
	}

	public void doSetFilter(NativeFilter filter) {
		setFilter(filter == null ? 0L : filter.getNativeHandle());
	}

	public void doSetSampleRate(int sampleRate) {
		var error = setSampleRate(sampleRate);
		if (error != 0) throw new ProfilerException(error);
	}
}
