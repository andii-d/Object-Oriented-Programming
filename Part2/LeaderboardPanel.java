import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Locale;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Initial leaderboard view (table-only slice).
 */
public class LeaderboardPanel extends JPanel {
    private final DefaultTableModel model;
    private final JComboBox<String> compareACombo;
    private final JComboBox<String> compareBCombo;
    private final JComboBox<String> metricCombo;
    private final JLabel compareResultLabel;
    private LeaderboardManager manager;

    public LeaderboardPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(
                new Object[]{"Rank", "Name", "Title", "Points", "Best WPM", "Races", "Avg Accuracy %", "Burnouts", "Badges"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

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
        add(comparePanel, BorderLayout.SOUTH);
    }

    public void refresh(LeaderboardManager manager) {
        this.manager = manager;
        model.setRowCount(0);
        for (LeaderboardManager.LeaderboardRow row : manager.getLeaderboardRows()) {
            model.addRow(new Object[]{
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
    }

    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private void refillTypistSelectors(Set<String> names) {
        compareACombo.removeAllItems();
        compareBCombo.removeAllItems();
        for (String name : names) {
            compareACombo.addItem(name);
            compareBCombo.addItem(name);
        }
    }

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
}
