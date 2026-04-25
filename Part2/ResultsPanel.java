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
    private final DefaultTableModel model;
    private final Runnable newRaceCallback;

    public ResultsPanel(Runnable newRaceCallback) {
        this.newRaceCallback = newRaceCallback;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        winnerLabel = new JLabel("Run a race to see results.");
        add(winnerLabel, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Pos", "Typist", "WPM", "Accuracy %"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton newRaceButton = new JButton("Configure Next Race");
        newRaceButton.addActionListener(e -> this.newRaceCallback.run());
        bottom.add(newRaceButton);
        add(bottom, BorderLayout.SOUTH);
    }

    public void showResults(List<RaceResult> results) {
        model.setRowCount(0);
        if (results.isEmpty()) {
            winnerLabel.setText("No results available.");
            return;
        }

        RaceResult winner = results.get(0);
        winnerLabel.setText("Winner: " + winner.getName() + " (" + winner.getSymbol() + ")");

        for (RaceResult result : results) {
            model.addRow(new Object[]{
                    result.getFinishPosition(),
                    result.getName(),
                    format(result.getWpm()),
                    format(result.getAccuracyPercent())
            });
        }
    }

    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
