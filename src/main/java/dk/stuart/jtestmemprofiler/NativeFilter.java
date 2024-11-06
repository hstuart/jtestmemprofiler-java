package dk.stuart.jtestmemprofiler;

import java.io.*;

interface NativeFilter extends Closeable {
	long getNativeHandle();

	@Override
	void close();
}
