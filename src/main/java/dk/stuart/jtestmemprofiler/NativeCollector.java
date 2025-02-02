package dk.stuart.jtestmemprofiler;

import java.io.*;

interface NativeCollector extends Closeable {
	long getNativeHandle();

	@Override
	void close();
}
