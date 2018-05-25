package mycontroller.common;

public class Logger {

    private static boolean PRINT_INFO = true;
    private static boolean PRINT_WARNING = true;


    public static void printWarning(String sender, String message) {
        if (PRINT_WARNING) {
            System.err.printf("[%s] %s\n", sender, message);
        }
    }

    public static void printInfo(String sender, String message) {
        if (PRINT_INFO) {
            System.out.printf("[%s] %s\n", sender, message);
        }
    }

}
