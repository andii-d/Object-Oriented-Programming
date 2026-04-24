import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Entry point for the Part 2 GUI build.
 */
public class TypingRaceGUI {
    public static void main(String[] args) {
        startRaceGUI();
    }

    public static void startRaceGUI() {
        SwingUtilities.invokeLater(() -> {
            TypingRaceFrame frame = new TypingRaceFrame();
            frame.setVisible(true);
        });
    }
}

class TypingRaceFrame extends JFrame {
    private final JTabbedPane tabs;
    private final SetupPanel setupPanel;
    private final RacePanel racePanel;

    TypingRaceFrame() {
        super("Typing Race Simulator - Part 2");
        this.tabs = new JTabbedPane();
        this.setupPanel = new SetupPanel(this::startRace);
        this.racePanel = new RacePanel();

        tabs.addTab("Setup", setupPanel);
        tabs.addTab("Race", racePanel);

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void startRace(RaceConfig config) {
        racePanel.startRace(new TypingRaceEngine(config), () -> {
            // Results tab comes in a later slice.
        });
        tabs.setSelectedIndex(1);
    }
}
