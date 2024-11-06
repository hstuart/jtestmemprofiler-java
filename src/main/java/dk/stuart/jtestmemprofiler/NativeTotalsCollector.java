package dk.stuart.jtestmemprofiler;

class NativeTotalsCollector implements NativeCollector {
	private long nativeHandle;

	public NativeTotalsCollector() {
		nativeHandle = init();
	}

	private static native long init();

	private static native void cleanup(long nativeHandle);

	private static native long get(long nativeHandle);

	@Override
	public long getNativeHandle() {
		return nativeHandle;
	}

	@Override
	public void close() {
		var handle = nativeHandle;
		nativeHandle = 0;
		cleanup(handle);
	}

	public long getAllocationTotal() {
		if (nativeHandle == 0) return -1L;

		return get(nativeHandle);
	}
}
