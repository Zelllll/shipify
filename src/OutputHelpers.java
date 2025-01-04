public class OutputHelpers {
    // ANSI escape codes for styling
    public static final String RESET = "\033[0m";
    public static final String GREEN = "\033[0;32m";
    public static final String BLUE = "\033[0;34m";
    public static final String CYAN = "\033[0;36m";
    public static final String YELLOW = "\033[0;33m";
    public static final String RED = "\033[0;31m";
    public static final String BOLD = "\033[1m";

//    public static void main(String[] args) {
//        System.out.println(YELLOW + "Input directory verified: " + CYAN + "C:\\Users\\Elijah\\CLionProjects\\ddpc2\\patch_assets" + RESET);
//        System.out.println(YELLOW + "Output directory verified: " + CYAN + "C:\\Users\\Elijah\\CLionProjects\\ddpc2\\patch_assets\\out" + RESET);
//        System.out.println(BLUE + "Found " + BOLD + "97 files" + RESET + BLUE + " in input directory." + RESET);
//        printProgress("Building scenes", GREEN);
//        printProgress("Building objects", GREEN);
//        printProgress("Building miscellaneous files", GREEN);
//        printProgress("Building text", GREEN);
//        printProgress("Building audio", GREEN);
//        printProgress("Building entrance table", GREEN);
//        printProgress("Building entrance cutscene table", GREEN);
//        System.out.println(RED + "Withdrawing $1,000..." + RESET);
//        System.out.println(GREEN + BOLD + "Success!" + RESET + " Output generated in: " + CYAN + "C:\\Users\\Elijah\\CLionProjects\\ddpc2\\patch_assets\\out" + RESET);
//    }

    public static void printProgress(String task) {
        System.out.println(GREEN + task + "..." + RESET);
    }
}