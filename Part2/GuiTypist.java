import java.awt.Color;

/**
 * Runtime race state for one typist in the GUI simulation.
 *
 * This mirrors the Part 1 Typist concept (progress, burnout, accuracy) and
 * also stores extra stats needed for Part 2 analytics.
 */
public class GuiTypist {
    private final TypistSetup setup;
    private final String name;
    private final String symbol;
    private final Color color;

    private final double startingAccuracy;
    private double currentAccuracy;

    private int progress;
    private boolean burntOut;
    private int burnoutTurnsRemaining;
    private int burnoutCount;
    private int correctKeystrokes;
    private int totalKeystrokes;
    private int mistypeCount;
    private int finishTurn;
    private boolean finished;
    private boolean justMistyped;

    /**
     * Creates a runtime typist state from setup data.
     *
     * @param setup static setup configuration
     * @param startingAccuracy initial accuracy for this race
     */
    public GuiTypist(TypistSetup setup, double startingAccuracy) {
        this.setup = setup;
        this.name = setup.getName();
        this.symbol = setup.getSymbol();
        this.color = setup.getColor();
        this.startingAccuracy = clamp(startingAccuracy);
        this.currentAccuracy = clamp(startingAccuracy);
        this.progress = 0;
        this.burntOut = false;
        this.burnoutTurnsRemaining = 0;
        this.burnoutCount = 0;
        this.correctKeystrokes = 0;
        this.totalKeystrokes = 0;
        this.mistypeCount = 0;
        this.finishTurn = -1;
        this.finished = false;
        this.justMistyped = false;
    }

    /**
     * @return the immutable setup record for this typist
     */
    public TypistSetup getSetup() {
        return setup;
    }

    /**
     * @return typist name
     */
    public String getName() {
        return name;
    }

    /**
     * @return typist symbol/emoji
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return typist lane color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return initial race accuracy
     */
    public double getStartingAccuracy() {
        return startingAccuracy;
    }

    /**
     * @return current race accuracy
     */
    public double getCurrentAccuracy() {
        return currentAccuracy;
    }

    /**
     * Sets current race accuracy, clamped to [0.0, 1.0].
     */
    public void setCurrentAccuracy(double currentAccuracy) {
        this.currentAccuracy = clamp(currentAccuracy);
    }

    /**
     * @return current passage progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * @return true if this typist is currently burnt out
     */
    public boolean isBurntOut() {
        return burntOut;
    }

    /**
     * @return burnout turns remaining
     */
    public int getBurnoutTurnsRemaining() {
        return burnoutTurnsRemaining;
    }

    /**
     * @return number of burnout events in this race
     */
    public int getBurnoutCount() {
        return burnoutCount;
    }

    /**
     * Increments burnout event counter by one.
     */
    public void incrementBurnoutCount() {
        burnoutCount++;
    }

    /**
     * @return total number of correct keystrokes
     */
    public int getCorrectKeystrokes() {
        return correctKeystrokes;
    }

    /**
     * @return total number of attempted keystrokes
     */
    public int getTotalKeystrokes() {
        return totalKeystrokes;
    }

    /**
     * @return total number of mistypes
     */
    public int getMistypeCount() {
        return mistypeCount;
    }

    /**
     * @return turn number when this typist was assigned a finish position
     */
    public int getFinishTurn() {
        return finishTurn;
    }

    /**
     * @return true if typist has finished/been placed
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Marks typist as finished at the given turn.
     */
    public void markFinished(int turn) {
        this.finishTurn = turn;
        this.finished = true;
    }

    /**
     * @return true if this typist mistyped in the current turn
     */
    public boolean hasJustMistyped() {
        return justMistyped;
    }

    /**
     * Updates the per-turn mistype marker.
     */
    public void setJustMistyped(boolean justMistyped) {
        this.justMistyped = justMistyped;
    }

    /**
     * Records one typing attempt for accuracy statistics.
     *
     * @param wasCorrect true when keystroke was correct
     */
    public void registerKeystroke(boolean wasCorrect) {
        totalKeystrokes++;
        if (wasCorrect) {
            correctKeystrokes++;
        }
    }

    /**
     * Increments mistype counter by one.
     */
    public void addMistype() {
        mistypeCount++;
    }

    /**
     * Moves typist forward by one character when active.
     */
    public void typeCharacter() {
        if (!burntOut && !finished) {
            progress++;
        }
    }

    /**
     * Slides typist backward by amount, clamped to zero.
     */
    public void slideBack(int amount) {
        if (amount > 0) {
            progress = Math.max(0, progress - amount);
        }
    }

    /**
     * Activates burnout for the requested number of turns.
     */
    public void burnOut(int turns) {
        if (turns > 0 && !finished) {
            burntOut = true;
            burnoutTurnsRemaining = turns;
        }
    }

    /**
     * Recovers one burnout turn; clears burnout at zero.
     */
    public void recoverFromBurnout() {
        if (burntOut) {
            burnoutTurnsRemaining--;
            if (burnoutTurnsRemaining <= 0) {
                burnoutTurnsRemaining = 0;
                burntOut = false;
            }
        }
    }

    /**
     * Utility clamp for accuracy values.
     */
    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
