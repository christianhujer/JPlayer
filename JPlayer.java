import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.awt.SystemTray.getSystemTray;
import static java.awt.Toolkit.getDefaultToolkit;

public class JPlayer {

    public static void main(final String... args) throws AWTException {
        new JPlayer(args);
    }

    private static final String DEFAULT_URI = "http://streamer.psyradio.org:8010/";

    private Thread thread;
    private final TrayIcon trayIcon;
    private String title;
    private volatile Process mplayer;
    private String uri = DEFAULT_URI;

    public JPlayer(final String... args) throws AWTException {
        trayIcon = createAndInstallTrayIcon();
        if (args.length > 0) uri = args[0];
        start();
    }

    private TrayIcon createAndInstallTrayIcon() throws AWTException {
        final TrayIcon trayIcon = new TrayIcon(getDefaultToolkit().createImage(getClass().getResource("MPlayer_w" + getSystemTray().getTrayIconSize().width + ".png")));
        getSystemTray().add(trayIcon);
        trayIcon.setPopupMenu(createPopupMenu());
        return trayIcon;
    }

    private PopupMenu createPopupMenu() {
        final PopupMenu popupMenu = new PopupMenu("JPlayer");
        popupMenu.add(createMenuItem("pause", this::pause));
        popupMenu.add(createMenuItem("continue", this::cont));
	popupMenu.add(createMenuItem("restart", this::restart));
        popupMenu.add(createMenuItem("quit", this::stop));
        return popupMenu;
    }

    private MenuItem createMenuItem(final String title, final ActionListener al) {
        final MenuItem menuItem = new MenuItem(title);
        menuItem.addActionListener(al);
        return menuItem;
    }

    public void start() {
        if (thread != null) return;
        thread = new Thread(this::play);
        thread.start();
    }

    public void pause(final ActionEvent e) {
        if (thread != null) thread.interrupt();
        if (mplayer != null) mplayer.destroy();
    }

    public void cont(final ActionEvent e) {
        start();
    }

    public void stop(final ActionEvent e) {
        pause(e);
        getSystemTray().remove(trayIcon);
    }

    public void restart(final ActionEvent e) {
        stop(e);
        try {
            Restarter.restart();
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void play() {
        try {
            tryKeepPlaying(uri);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        thread = null;
    }

    public void tryKeepPlaying(final String uri) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder("mplayer", "--quiet", uri);
        while (!thread.isInterrupted())
            runMplayer(processBuilder);
    }

    public void runMplayer(final ProcessBuilder processBuilder) throws IOException {
        mplayer = processBuilder.start();
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(mplayer.getInputStream()))) {
            in.lines().filter(line -> line.matches("^ICY Info.*")).forEach(this::setIcy);
        } catch (final UncheckedIOException ignore) {
        }
    }

    public void setTitle(final String title) {
        this.title = title;
        trayIcon.setToolTip("Track: " + title);
        displayTitle();
    }

    public void displayTitle() {
        trayIcon.displayMessage("Track", title, TrayIcon.MessageType.INFO);
    }

    public void setIcy(final String icy) {
        final String title = getTitle(icy);
        if (title == null) return;
        setTitle(title);
    }

    public String getTitle(final String icy) {
        final Pattern pattern = Pattern.compile("\\bStreamTitle='([^']*)'");
        final Matcher m = pattern.matcher(icy);
        if (m.find()) return m.group(1);
        return null;
    }

}
