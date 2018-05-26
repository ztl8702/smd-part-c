/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.common;

/**
 * Class to print out logging statements for debugging and info
 */

public class Logger {

    // different levels of severity
    // INFO is verbose 
    // WARNING is very important messages
    // DEBUG is somewhere in between.

    private static boolean PRINT_INFO = false;
    private static boolean PRINT_DEBUG = false;
    private static boolean PRINT_WARNING = true;

    /**
     * Prints a warning message
     */
    public static void printWarning(String sender, String message) {
        if (PRINT_WARNING) {
            System.err.printf("WARNING [%s] %s\n", sender, message);
        }
    }

    /**
     * Prints an information message
     */
    public static void printInfo(String sender, String message) {
        if (PRINT_INFO) {
            System.out.printf("[%s] %s\n", sender, message);
        }
    }

    /**
     * Prints a debug message
     */
    public static void printDebug(String sender, String message) {
        if (PRINT_DEBUG) {
            System.err.printf("[%s] %s\n", sender, message);
        }
    }

}
