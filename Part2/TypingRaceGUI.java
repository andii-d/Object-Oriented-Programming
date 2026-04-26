import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Entry point for the Part 2 Swing version.
 *
 * The spec asks for a startRaceGUI() method as the launch method, so both
 * main() and external callers route through that method.
 */
public class TypingRaceGUI {
    /**
     * Standard Java entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        startRaceGUI();
    }

    /**
     * Starts the Swing UI on the Event Dispatch Thread.
     */
    public static void startRaceGUI() {
        SwingUtilities.invokeLater(() -> {
            TypingRaceFrame frame = new TypingRaceFrame();
            frame.setVisible(true);
        });
    }
}

/**
 * Main application window for the GUI typing race simulator.
 *
 * Tabs:
 * 1) Setup
 * 2) Race animation
 * 3) Results
 * 4) Leaderboard (Option A)
 * 5) Sponsorship (Option B)
 */
class TypingRaceFrame extends JFrame {
    private final LeaderboardManager leaderboardManager;
    private final SponsorPrizeManager sponsorPrizeManager;
    private final SetupPanel setupPanel;
    private final RacePanel racePanel;
    private final ResultsPanel resultsPanel;
    private final LeaderboardPanel leaderboardPanel;
    private final SponsorPanel sponsorPanel;
    private final JTabbedPane tabs;

    private RaceConfig currentConfig;

    /**
     * Builds all panels and wires navigation flow between them.
     */
    TypingRaceFrame() {
        super("Typing Race Simulator - Part 2 (Swing, Option A + Option B)");
        this.leaderboardManager = new LeaderboardManager();
        this.sponsorPrizeManager = new SponsorPrizeManager();
        this.tabs = new JTabbedPane();

        this.setupPanel = new SetupPanel(this::startRace);
        this.racePanel = new RacePanel();
        this.resultsPanel = new ResultsPanel(() -> tabs.setSelectedIndex(0));
        this.leaderboardPanel = new LeaderboardPanel();
        this.sponsorPanel = new SponsorPanel();

        tabs.addTab("Setup", setupPanel);
        tabs.addTab("Race", racePanel);
        tabs.addTab("Results", resultsPanel);
        tabs.addTab("Leaderboard", leaderboardPanel);
        tabs.addTab("Sponsorship", sponsorPanel);

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        setSize(1250, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Creates a new race engine from setup data and switches to the race tab.
     *
     * @param config race configuration from SetupPanel
     */
    private void startRace(RaceConfig config) {
        this.currentConfig = config;
        sponsorPrizeManager.assignSponsors(config.getTypists());
        TypingRaceEngine engine = new TypingRaceEngine(config, leaderboardManager, sponsorPrizeManager);
        racePanel.startRace(engine, this::finishRace);
        tabs.setSelectedIndex(1);
    }

    /**
     * Finalises one completed race:
     * - builds per-race metrics
     * - updates cumulative Option A leaderboard data
     * - refreshes results/leaderboard views
     *
     * @param engine completed engine containing final race state
     */
    private void finishRace(TypingRaceEngine engine) {
        List<RaceResult> results = engine.buildResults();
        leaderboardManager.applyRaceResults(results, currentConfig.getSeatCount());
        sponsorPrizeManager.applyRaceResults(results, currentConfig.getSeatCount());
        resultsPanel.showResults(results);
        leaderboardPanel.refresh(leaderboardManager);
        sponsorPanel.refresh(sponsorPrizeManager);
        tabs.setSelectedIndex(2);
    }
}
