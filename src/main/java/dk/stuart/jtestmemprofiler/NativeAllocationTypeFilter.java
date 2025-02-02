package dk.stuart.jtestmemprofiler;

import java.util.*;

class NativeAllocationTypeFilter implements NativeFilter {
	private long nativeHandle;

	public NativeAllocationTypeFilter(Set<Class<?>> allocationFilter) {
		nativeHandle = init(allocationFilter);
	}

	private static native long init(Object allocationFilter);

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
