public class OutputHelpers {
    // ANSI escape codes for styling
    public static final String RESET = "\033[0m";
    public static final String GREEN = "\033[0;32m";
    public static final String BLUE = "\033[0;34m";
    public static final String CYAN = "\033[0;36m";
    public static final String YELLOW = "\033[0;33m";
    public static final String RED = "\033[0;31m";
    public static final String BOLD = "\033[1m";

    public static void printProgress(String task) {
        System.out.println(GREEN + task + "..." + RESET);
    }
}