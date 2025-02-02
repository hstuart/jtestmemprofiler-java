package dk.stuart.jtestmemprofiler;

import java.util.*;

class NativePerTypeCollector implements NativeCollector {
	long nativeHandle;

	public NativePerTypeCollector() {
		nativeHandle = init();
	}

	private static native long init();

	private static native void cleanup(long nativeHandle);

	private static native Object get(long nativeHandle);

	@Override
	public void close() {
		var handle = nativeHandle;
		nativeHandle = 0;
		cleanup(handle);
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Long> getAllocations() {
		return (HashMap<String, Long>)get(nativeHandle);
	}

	@Override
	public long getNativeHandle() {
		return nativeHandle;
	}
}
