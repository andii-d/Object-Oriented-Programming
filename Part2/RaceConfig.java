import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable race configuration built from the setup screen.
 */
public class RaceConfig {
    private final String passage;
    private final int seatCount;
    private final boolean autocorrectEnabled;
    private final boolean caffeineModeEnabled;
    private final boolean nightShiftEnabled;
    private final boolean rankImpactEnabled;
    private final List<TypistSetup> typists;

    /**
     * Creates a race configuration.
     *
     * @param passage race passage text
     * @param seatCount number of typists (2-6)
     * @param autocorrectEnabled true if autocorrect modifier is enabled
     * @param caffeineModeEnabled true if caffeine modifier is enabled
     * @param nightShiftEnabled true if night shift modifier is enabled
     * @param rankImpactEnabled true if rank impact modifier is enabled
     * @param typists typist setup list
     */
    public RaceConfig(
            String passage,
            int seatCount,
            boolean autocorrectEnabled,
            boolean caffeineModeEnabled,
            boolean nightShiftEnabled,
            boolean rankImpactEnabled,
            List<TypistSetup> typists
    ) {
        if (seatCount < 2 || seatCount > 6) {
            throw new IllegalArgumentException("Seat count must be between 2 and 6.");
        }
        if (typists == null) {
            throw new IllegalArgumentException("Typist setup list cannot be null.");
        }
        if (typists.size() < seatCount) {
            throw new IllegalArgumentException("Typist setup list must include all active seats.");
        }
        for (int i = 0; i < seatCount; i++) {
            if (typists.get(i) == null) {
                throw new IllegalArgumentException("Typist setup cannot contain null entries.");
            }
        }
        String safePassage = (passage == null || passage.trim().isEmpty())
                ? "The quick brown fox jumps over the lazy dog."
                : passage.trim();

        this.passage = safePassage;
        this.seatCount = seatCount;
        this.autocorrectEnabled = autocorrectEnabled;
        this.caffeineModeEnabled = caffeineModeEnabled;
        this.nightShiftEnabled = nightShiftEnabled;
        this.rankImpactEnabled = rankImpactEnabled;
        this.typists = Collections.unmodifiableList(new ArrayList<>(typists));
    }

    /**
     * @return passage text used for this race
     */
    public String getPassage() {
        return passage;
    }

    /**
     * @return number of competitors in this race
     */
    public int getSeatCount() {
        return seatCount;
    }

    /**
     * @return true if autocorrect is enabled
     */
    public boolean isAutocorrectEnabled() {
        return autocorrectEnabled;
    }

    /**
     * @return true if caffeine mode is enabled
     */
    public boolean isCaffeineModeEnabled() {
        return caffeineModeEnabled;
    }

    /**
     * @return true if night shift is enabled
     */
    public boolean isNightShiftEnabled() {
        return nightShiftEnabled;
    }

    /**
     * @return true if rank impact is enabled
     */
    public boolean isRankImpactEnabled() {
        return rankImpactEnabled;
    }

    /**
     * @return immutable typist setup list
     */
    public List<TypistSetup> getTypists() {
        return typists;
    }
}
