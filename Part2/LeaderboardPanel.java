import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/**
 * Option A leaderboard view panel.
 *
 * Contains:
 * - cumulative ranking table
 * - side-by-side metric comparison
 * - per-typist historical race log
 */
public class LeaderboardPanel extends JPanel {
    private final DefaultTableModel leaderboardModel;
    private final JTable leaderboardTable;
    private final JComboBox<String> compareACombo;
    private final JComboBox<String> compareBCombo;
    private final JComboBox<String> metricCombo;
    private final JLabel compareResultLabel;
    private final JComboBox<String> historyCombo;
    private final JTextArea historyArea;
    private LeaderboardManager manager;

    /**
     * Builds leaderboard UI components.
     */
    public LeaderboardPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        leaderboardModel = new DefaultTableModel(
                new Object[]{"Rank", "Name", "Title", "Points", "Best WPM", "Races", "Avg Accuracy %", "Burnouts", "Badges"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        leaderboardTable = new JTable(leaderboardModel);
        add(new JScrollPane(leaderboardTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));

        JPanel comparePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        compareACombo = new JComboBox<>();
        compareBCombo = new JComboBox<>();
        metricCombo = new JComboBox<>(new String[]{"Points", "Best WPM", "Average Accuracy %", "Total Burnouts"});
        JButton compareButton = new JButton("Compare");
        compareResultLabel = new JLabel("Pick two typists to compare.");
        compareButton.addActionListener(e -> compareTypists());
        comparePanel.add(new JLabel("Compare"));
        comparePanel.add(compareACombo);
        comparePanel.add(new JLabel("vs"));
        comparePanel.add(compareBCombo);
        comparePanel.add(metricCombo);
        comparePanel.add(compareButton);
        comparePanel.add(compareResultLabel);
        bottom.add(comparePanel, BorderLayout.NORTH);

        JPanel historyPanel = new JPanel(new BorderLayout(4, 4));
        JPanel historyTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        historyCombo = new JComboBox<>();
        historyCombo.addActionListener(e -> refreshHistoryArea());
        historyTop.add(new JLabel("History for"));
        historyTop.add(historyCombo);
        historyPanel.add(historyTop, BorderLayout.NORTH);

        historyArea = new JTextArea(6, 50);
        historyArea.setEditable(false);
        historyPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        bottom.add(historyPanel, BorderLayout.CENTER);

        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Reloads all leaderboard widgets from latest manager data.
     *
     * @param manager leaderboard service
     */
    public void refresh(LeaderboardManager manager) {
        this.manager = manager;
        leaderboardModel.setRowCount(0);
        for (LeaderboardManager.LeaderboardRow row : manager.getLeaderboardRows()) {
            leaderboardModel.addRow(new Object[]{
                    row.getRank(),
                    row.getName(),
                    row.getTitle(),
                    row.getPoints(),
                    format(row.getBestWpm()),
                    row.getRaces(),
                    format(row.getAvgAccuracy()),
                    row.getTotalBurnouts(),
                    row.getBadges().isEmpty() ? "-" : row.getBadges()
            });
        }
        refillTypistSelectors(manager.getTypistNames());
        refreshHistoryArea();
    }

    /**
     * Rebuilds typist selector combo boxes.
     */
    private void refillTypistSelectors(Set<String> names) {
        compareACombo.removeAllItems();
        compareBCombo.removeAllItems();
        historyCombo.removeAllItems();
        for (String name : names) {
            compareACombo.addItem(name);
            compareBCombo.addItem(name);
            historyCombo.addItem(name);
        }
    }

    /**
     * Compares selected typists on selected metric.
     */
    private void compareTypists() {
        if (manager == null || compareACombo.getSelectedItem() == null || compareBCombo.getSelectedItem() == null) {
            compareResultLabel.setText("Need at least two typists.");
            return;
        }
        String first = (String) compareACombo.getSelectedItem();
        String second = (String) compareBCombo.getSelectedItem();
        String metric = (String) metricCombo.getSelectedItem();

        double firstValue = manager.getMetricValue(first, metric);
        double secondValue = manager.getMetricValue(second, metric);
        compareResultLabel.setText(first + ": " + format(firstValue) + " | " + second + ": " + format(secondValue));
    }

    /**
     * Shows race history for currently selected typist.
     */
    private void refreshHistoryArea() {
        historyArea.setText("");
        if (manager == null || historyCombo.getSelectedItem() == null) {
            return;
        }
        String name = (String) historyCombo.getSelectedItem();
        List<LeaderboardManager.RaceHistory> history = manager.getHistory(name);
        if (history.isEmpty()) {
            historyArea.setText("No races recorded yet for " + name + ".");
            return;
        }
        StringBuilder builder = new StringBuilder();
        int raceNumber = 1;
        Double previousWpm = null;
        Double previousAccuracy = null;
        for (LeaderboardManager.RaceHistory item : history) {
            builder.append("Race ").append(raceNumber++).append(": ")
                    .append("Pos ").append(item.getFinishPosition())
                    .append(", WPM ").append(format(item.getWpm())).append(trend(item.getWpm(), previousWpm))
                    .append(", Accuracy ").append(format(item.getAccuracyPercent())).append("%")
                    .append(trend(item.getAccuracyPercent(), previousAccuracy))
                    .append(", Burnouts ").append(item.getBurnoutCount())
                    .append(", Points ").append(item.getPoints())
                    .append('\n');
            previousWpm = item.getWpm();
            previousAccuracy = item.getAccuracyPercent();
        }
        historyArea.setText(builder.toString());
    }

    /**
     * Formats decimals consistently for display.
     */
    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    /**
     * Displays metric direction versus the previous race to make trends obvious.
     */
    private String trend(double current, Double previous) {
        if (previous == null) {
            return " (baseline)";
        }
        double delta = current - previous;
        if (Math.abs(delta) < 0.01) {
            return " (flat)";
        }
        String direction = delta > 0 ? "up " : "down ";
        return " (" + direction + String.format(Locale.US, "%+.2f", delta) + ")";
    }
}
