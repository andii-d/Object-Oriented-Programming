import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable-style race result record for one typist.
 *
 * The leaderboard manager enriches this with race points and newly unlocked
 * badges after the engine produces base metrics.
 */
public class RaceResult {
    private final String name;
    private final String symbol;
    private final int finishPosition;
    private final double wpm;
    private final double accuracyPercent;
    private final int burnoutCount;
    private final double startingAccuracy;
    private final double finalAccuracy;
    private int racePoints;
    private List<String> newBadges;

    /**
     * Constructs a race result.
     */
    public RaceResult(
            String name,
            String symbol,
            int finishPosition,
            double wpm,
            double accuracyPercent,
            int burnoutCount,
            double startingAccuracy,
            double finalAccuracy
    ) {
        this.name = name;
        this.symbol = symbol;
        this.finishPosition = finishPosition;
        this.wpm = wpm;
        this.accuracyPercent = accuracyPercent;
        this.burnoutCount = burnoutCount;
        this.startingAccuracy = startingAccuracy;
        this.finalAccuracy = finalAccuracy;
        this.racePoints = 0;
        this.newBadges = new ArrayList<>();
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
     * @return finish position (1 = winner)
     */
    public int getFinishPosition() {
        return finishPosition;
    }

    /**
     * @return words per minute for this race
     */
    public double getWpm() {
        return wpm;
    }

    /**
     * @return typing accuracy percentage
     */
    public double getAccuracyPercent() {
        return accuracyPercent;
    }

    /**
     * @return number of burnout events
     */
    public int getBurnoutCount() {
        return burnoutCount;
    }

    /**
     * @return accuracy at race start
     */
    public double getStartingAccuracy() {
        return startingAccuracy;
    }

    /**
     * @return accuracy after race updates
     */
    public double getFinalAccuracy() {
        return finalAccuracy;
    }

    /**
     * @return finalAccuracy - startingAccuracy
     */
    public double getAccuracyDelta() {
        return finalAccuracy - startingAccuracy;
    }

    /**
     * @return points awarded for this race
     */
    public int getRacePoints() {
        return racePoints;
    }

    /**
     * Sets points awarded for this race.
     */
    public void setRacePoints(int racePoints) {
        this.racePoints = racePoints;
    }

    /**
     * Replaces the list of newly earned badges for this race.
     */
    public void setNewBadges(List<String> newBadges) {
        this.newBadges = new ArrayList<>(newBadges);
    }

    /**
     * @return immutable list of badges newly earned in this race
     */
    public List<String> getNewBadges() {
        return Collections.unmodifiableList(newBadges);
    }
}
