package dk.stuart.jtestmemprofiler;

public class NativeCallTreeCollector implements NativeCollector {
    long nativeHandle;

    public NativeCallTreeCollector() {
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

    public TrieNode getAllocations() {
        return (TrieNode) get(nativeHandle);
    }

    @Override
    public long getNativeHandle() {
        return nativeHandle;
    }
}
