package dk.stuart.jtestmemprofiler;

import java.util.*;

class NativeThreadIdFilter implements NativeFilter {
	private long nativeHandle;

	public NativeThreadIdFilter(Set<Thread> threads) {
		nativeHandle = init(threads);
	}

	private static native long init(Object threads);

	private static native void cleanup(long nativeHandle);

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
}
