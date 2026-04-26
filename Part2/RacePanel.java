import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

/**
 * Animated race panel with one lane per typist.
 */
public class RacePanel extends JPanel {
    public interface RaceFinishedListener {
        void onRaceFinished(TypingRaceEngine engine);
    }

    private final JLabel headerLabel;
    private final JButton pauseResumeButton;
    private final JPanel lanesContainer;
    private final Map<GuiTypist, JTextPane> laneTextByTypist;
    private final Map<GuiTypist, JLabel> laneStatusByTypist;

    private TypingRaceEngine engine;
    private RaceFinishedListener raceFinishedListener;
    private Timer timer;

    public RacePanel() {
        this.laneTextByTypist = new HashMap<>();
        this.laneStatusByTypist = new HashMap<>();
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        headerLabel = new JLabel("Race not started");
        add(headerLabel, BorderLayout.NORTH);

        lanesContainer = new JPanel();
        lanesContainer.setLayout(new GridLayout(0, 1, 8, 8));
        add(new JScrollPane(lanesContainer), BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pauseResumeButton = new JButton("Pause");
        pauseResumeButton.setEnabled(false);
        pauseResumeButton.addActionListener(e -> togglePause());
        controls.add(pauseResumeButton);
        add(controls, BorderLayout.SOUTH);
    }

    public void startRace(TypingRaceEngine engine, RaceFinishedListener listener) {
        stopTimerIfNeeded();
        this.engine = engine;
        this.raceFinishedListener = listener;
        this.laneTextByTypist.clear();
        this.laneStatusByTypist.clear();
        this.lanesContainer.removeAll();
        this.pauseResumeButton.setEnabled(true);
        this.pauseResumeButton.setText("Pause");

        String passage = engine.getPassage();
        for (GuiTypist typist : engine.getTypists()) {
            JPanel lane = new JPanel(new BorderLayout(5, 5));
            lane.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

            JLabel laneTitle = new JLabel(typist.getSymbol() + " " + typist.getName());
            laneTitle.setFont(laneTitle.getFont().deriveFont(Font.BOLD, 13f));
            lane.add(laneTitle, BorderLayout.NORTH);

            JTextPane passagePane = new JTextPane();
            passagePane.setEditable(false);
            passagePane.setText(passage);
            passagePane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            lane.add(new JScrollPane(passagePane), BorderLayout.CENTER);

            JLabel statusLabel = new JLabel();
            lane.add(statusLabel, BorderLayout.SOUTH);

            lanesContainer.add(lane);
            laneTextByTypist.put(typist, passagePane);
            laneStatusByTypist.put(typist, statusLabel);
        }

        updateView();
        revalidate();
        repaint();

        timer = new Timer((int) (TypingRaceEngine.TURN_SECONDS * 1000), e -> onTick());
        timer.start();
    }

    private void onTick() {
        if (engine == null) {
            return;
        }
        engine.advanceOneTurn();
        updateView();
        if (engine.isFinished()) {
            stopTimerIfNeeded();
            pauseResumeButton.setEnabled(false);
            if (raceFinishedListener != null) {
                raceFinishedListener.onRaceFinished(engine);
            }
        }
    }

    private void updateView() {
        if (engine == null) {
            return;
        }
        headerLabel.setText("Turn " + engine.getTurn() + " | Passage length: " + engine.getPassageLength() + " chars");
        String passage = engine.getPassage();
        for (GuiTypist typist : engine.getTypists()) {
            JTextPane pane = laneTextByTypist.get(typist);
            JLabel status = laneStatusByTypist.get(typist);
            if (pane == null || status == null) {
                continue;
            }
            pane.setText(passage);
            applyHighlights(pane, typist, passage.length());
            status.setText(buildStatus(typist, passage.length()));
        }
    }

    private void applyHighlights(JTextPane pane, GuiTypist typist, int passageLength) {
        int progress = Math.max(0, Math.min(typist.getProgress(), passageLength));
        Highlighter highlighter = pane.getHighlighter();
        highlighter.removeAllHighlights();
        try {
            if (progress > 0) {
                highlighter.addHighlight(
                        0,
                        progress,
                        new DefaultHighlighter.DefaultHighlightPainter(lighten(typist.getColor()))
                );
            }
            if (progress < passageLength) {
                highlighter.addHighlight(
                        progress,
                        progress + 1,
                        new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 240, 120))
                );
            }
        } catch (BadLocationException ignored) {
        }
    }

    private String buildStatus(GuiTypist typist, int passageLength) {
        if (typist.isFinished()) {
            return "Finished! Progress " + passageLength + "/" + passageLength;
        }
        if (typist.isBurntOut()) {
            return "BURNT OUT (" + typist.getBurnoutTurnsRemaining() + " turns) | Progress "
                    + typist.getProgress() + "/" + passageLength;
        }
        if (typist.hasJustMistyped()) {
            return "Just mistyped [<] | Progress " + typist.getProgress() + "/" + passageLength;
        }
        return "Typing... Progress " + typist.getProgress() + "/" + passageLength;
    }

    private Color lighten(Color base) {
        return new Color(
                Math.min(255, base.getRed() + 120),
                Math.min(255, base.getGreen() + 120),
                Math.min(255, base.getBlue() + 120)
        );
    }

    private void togglePause() {
        if (timer == null) {
            return;
        }
        if (timer.isRunning()) {
            timer.stop();
            pauseResumeButton.setText("Resume");
        } else {
            timer.start();
            pauseResumeButton.setText("Pause");
        }
    }

    private void stopTimerIfNeeded() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
}
