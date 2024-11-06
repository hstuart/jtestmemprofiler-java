package dk.stuart.jtestmemprofiler;

/**
 * {@link ProfilerException} translates the underlying JVMTI error codes to readable text and is used to
 * communicate errors from the native JVMTI agent to the consuming Java code.
 */
public class ProfilerException extends RuntimeException {
	/**
	 * Constructs a new profiler exception with the specified JVMTI error code.
	 * @param errorCode The JVMTI error code
	 */
	public ProfilerException(int errorCode) {
		super(fromErrorCode(errorCode));
	}

	private static String fromErrorCode(int errorCode) {
		return switch (errorCode) {
			case 10 -> "Invalid thread";
			case 11 -> "Invalid thread group";
			case 12 -> "Invalid priority";
			case 13 -> "Thread not suspended";
			case 14 -> "Thread suspended";
			case 15 -> "Thread not alive";
			case 20 -> "Invalid object";
			case 21 -> "Invalid class";
			case 22 -> "Class not prepared";
			case 23 -> "Invalid method ID";
			case 24 -> "Invalid location";
			case 25 -> "Invalid field ID";
			case 26 -> "Invalid module";
			case 31 -> "No more frames";
			case 32 -> "Opaque frame";
			case 34 -> "Type mismatch";
			case 35 -> "Invalid slot";
			case 40 -> "Duplicate";
			case 41 -> "Not found";
			case 50 -> "Invalid monitor";
			case 51 -> "Not monitor owner";
			case 52 -> "Interrupt";
			case 60 -> "Invalid class format";
			case 61 -> "Circular class definition";
			case 62 -> "Fails verification";
			case 63 -> "Unsupported redefinition method added";
			case 64 -> "Unsupported redefinition schema changed";
			case 65 -> "Invalid type state";
			case 66 -> "Unsupported redefinition hierarchy changed";
			case 67 -> "Unsupported redefinition method deleted";
			case 68 -> "Unsupported version";
			case 69 -> "Names don't match";
			case 70 -> "Unsupported redefinition class modifiers changed";
			case 71 -> "Unsupported redefinition method modifiers changed";
			case 72 -> "Unsupported redefinition class attribute changed";
			case 79 -> "Unmodifiable class";
			case 80 -> "Unmodifiable module";
			case 98 -> "Not available";
			case 99 -> "Must possess capability";
			case 100 -> "Null pointer";
			case 101 -> "Absent information";
			case 102 -> "Invalid event type";
			case 103 -> "Illegal argument";
			case 104 -> "Native method";
			case 106 -> "Class loader unsupported";
			case 110 -> "Out of memory";
			case 111 -> "Access denied";
			case 112 -> "Wrong phase";
			case 113 -> "Internal";
			case 115 -> "Unattached thread";
			case 116 -> "Invalid environment";
			default -> String.format("Unknown '%d'", errorCode);
		};
	}
}
