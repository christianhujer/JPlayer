import java.lang.ProcessBuilder;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class Restarter {
    public static void restart() throws IOException {
        new ProcessBuilder(getMyOwnCmdLine()).inheritIO().start();
    }

    public static String[] getMyOwnCmdLine() throws IOException {
        return readFirstLine("/proc/self/cmdline").split("\u0000");
    }

    public static String readFirstLine(final String filename) throws IOException {
        try (final BufferedReader in = new BufferedReader(new FileReader(filename))) {
            return in.readLine();
        }
    }
}
