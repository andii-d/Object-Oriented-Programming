import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Minimal animated race view.
 * Lane-by-lane passage highlighting will be added in a later commit.
 */
public class RacePanel extends JPanel {
    public interface RaceFinishedListener {
        void onRaceFinished(TypingRaceEngine engine);
    }

    private final JLabel statusLabel;
    private final JButton pauseResumeButton;
    private TypingRaceEngine engine;
    private RaceFinishedListener finishedListener;
    private Timer timer;

    public RacePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Race not started");
        add(statusLabel, BorderLayout.CENTER);

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
        this.finishedListener = listener;
        this.pauseResumeButton.setEnabled(true);
        this.pauseResumeButton.setText("Pause");
        updateStatus();

        timer = new Timer((int) (TypingRaceEngine.TURN_SECONDS * 1000), e -> onTick());
        timer.start();
    }

    private void onTick() {
        if (engine == null) {
            return;
        }
        engine.advanceOneTurn();
        updateStatus();
        if (engine.isFinished()) {
            stopTimerIfNeeded();
            pauseResumeButton.setEnabled(false);
            if (finishedListener != null) {
                finishedListener.onRaceFinished(engine);
            }
        }
    }

    private void updateStatus() {
        if (engine == null) {
            statusLabel.setText("Race not started");
            return;
        }
        statusLabel.setText("Turn " + engine.getTurn() + " | Passage length: " + engine.getPassageLength() + " chars");
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
