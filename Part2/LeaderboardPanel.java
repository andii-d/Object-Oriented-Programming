import java.awt.BorderLayout;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Initial leaderboard view (table-only slice).
 */
public class LeaderboardPanel extends JPanel {
    private final DefaultTableModel model;

    public LeaderboardPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(new Object[]{"Rank", "Name", "Points", "Best WPM", "Races"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
    }

    public void refresh(LeaderboardManager manager) {
        model.setRowCount(0);
        for (LeaderboardManager.LeaderboardRow row : manager.getLeaderboardRows()) {
            model.addRow(new Object[]{
                    row.getRank(),
                    row.getName(),
                    row.getPoints(),
                    format(row.getBestWpm()),
                    row.getRaces()
            });
        }
    }

    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
