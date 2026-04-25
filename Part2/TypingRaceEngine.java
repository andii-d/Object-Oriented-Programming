import java.util.ArrayList;
import java.util.Comparator;
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

    /**
     * Builds a basic ordered results list for the Results tab.
     */
    public List<RaceResult> buildResults() {
        List<GuiTypist> ordered = new ArrayList<>(typists);
        ordered.sort(Comparator
                .comparingInt(GuiTypist::getProgress).reversed()
                .thenComparing(GuiTypist::getName));

        List<RaceResult> results = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            GuiTypist typist = ordered.get(i);
            int finishTurn = typist.getFinishTurn() > 0 ? typist.getFinishTurn() : Math.max(1, turn);
            double minutes = Math.max(0.01, (finishTurn * TURN_SECONDS) / 60.0);
            double wordsTyped = Math.min(typist.getProgress(), getPassageLength()) / 5.0;
            double wpm = wordsTyped / minutes;
            double accuracyPercent = typist.getTotalKeystrokes() == 0
                    ? 100.0
                    : (100.0 * typist.getCorrectKeystrokes() / typist.getTotalKeystrokes());

            results.add(new RaceResult(
                    typist.getName(),
                    typist.getSymbol(),
                    i + 1,
                    wpm,
                    accuracyPercent,
                    typist.getBurnoutCount(),
                    typist.getStartingAccuracy(),
                    typist.getCurrentAccuracy()
            ));
        }
        return results;
    }
}
