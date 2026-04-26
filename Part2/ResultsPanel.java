import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Early results panel showing winner + core metrics.
 */
public class ResultsPanel extends JPanel {
    private final JLabel winnerLabel;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final Runnable newRaceCallback;

    public ResultsPanel(Runnable newRaceCallback) {
        this.newRaceCallback = newRaceCallback;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        winnerLabel = new JLabel("Run a race to see results.");
        add(winnerLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Pos", "Typist", "WPM", "Accuracy %", "Burnouts", "Acc Change", "Points", "New Badges"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton newRaceButton = new JButton("Configure Next Race");
        newRaceButton.addActionListener(e -> this.newRaceCallback.run());
        bottom.add(newRaceButton);
        add(bottom, BorderLayout.SOUTH);
    }

    public void showResults(List<RaceResult> results) {
        tableModel.setRowCount(0);
        if (results.isEmpty()) {
            winnerLabel.setText("No results available.");
            return;
        }

        RaceResult winner = results.get(0);
        winnerLabel.setText(
                "Winner: " + winner.getName() + " (" + winner.getSymbol() + ")"
                        + " | Final accuracy " + format(winner.getFinalAccuracy())
                        + " (" + signed(winner.getAccuracyDelta()) + ")"
        );

        for (RaceResult result : results) {
            tableModel.addRow(new Object[]{
                    result.getFinishPosition(),
                    result.getName(),
                    format(result.getWpm()),
                    format(result.getAccuracyPercent()),
                    result.getBurnoutCount(),
                    signed(result.getAccuracyDelta()),
                    result.getRacePoints(),
                    result.getNewBadges().isEmpty() ? "-" : String.join(", ", result.getNewBadges())
            });
        }
    }

    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String signed(double value) {
        return String.format(Locale.US, "%+.2f", value);
    }
}
