/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.common;

/**
 * Class to print out logging statements for debugging and info
 */

public class Logger {

    private static boolean PRINT_INFO = true;
    private static boolean PRINT_DEBUG = true;
    private static boolean PRINT_WARNING = true;


    public static void printWarning(String sender, String message) {
        if (PRINT_WARNING) {
            System.err.printf("WARNING [%s] %s\n", sender, message);
        }
    }

    public static void printInfo(String sender, String message) {
        if (PRINT_INFO) {
            System.out.printf("[%s] %s\n", sender, message);
        }
    }

    public static void printDebug(String sender, String message) {
        if (PRINT_DEBUG) {
            System.err.printf("[%s] %s\n", sender, message);
        }
    }

}
