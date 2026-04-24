import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Early simulation engine slice.
 * Burnout/mistype refinements and richer metrics are added in later commits.
 */
public class TypingRaceEngine {
    public static final double TURN_SECONDS = 0.35;

    private final RaceConfig config;
    private final List<GuiTypist> typists;
    private final Random random;
    private int turn;
    private boolean finished;

    public TypingRaceEngine(RaceConfig config) {
        this.config = config;
        this.typists = new ArrayList<>();
        this.random = new Random();
        this.turn = 0;
        this.finished = false;

        for (int i = 0; i < config.getSeatCount(); i++) {
            TypistSetup setup = config.getTypists().get(i);
            typists.add(new GuiTypist(setup, setup.calculateBaseAccuracy()));
        }
    }

    public String getPassage() {
        return config.getPassage();
    }

    public int getPassageLength() {
        return config.getPassage().length();
    }

    public int getTurn() {
        return turn;
    }

    public boolean isFinished() {
        return finished;
    }

    public List<GuiTypist> getTypists() {
        return typists;
    }

    public void advanceOneTurn() {
        if (finished) {
            return;
        }
        turn++;

        for (GuiTypist typist : typists) {
            if (typist.isFinished()) {
                continue;
            }

            boolean typedCorrectly = random.nextDouble() < typist.getCurrentAccuracy();
            typist.registerKeystroke(typedCorrectly);
            if (typedCorrectly) {
                typist.typeCharacter();
            }

            if (typist.getProgress() >= getPassageLength()) {
                typist.markFinished(turn);
                finished = true;
                break;
            }
        }
    }
}
