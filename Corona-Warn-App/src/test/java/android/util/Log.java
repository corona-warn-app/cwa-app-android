package android.util;

public class Log {

    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;

    public static int d(String tag, String msg) {
        System.out.println("D: " + tag + ": " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        System.out.println("I/: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        System.out.println("W/: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        return e(tag, msg, null);
    }

    public static int e(String tag, String msg, Throwable error) {
        if (error != null) {
            System.out.println("E/: " + tag + ": " + msg + "Error: " + error);
        } else {
            System.out.println("E/: " + tag + ": " + msg);
        }
        return 0;
    }
}