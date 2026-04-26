import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Core simulation engine for the Swing typing race.
 *
 * This class keeps the Part 1 mechanics:
 * - forward typing progress
 * - mistypes causing slide-back
 * - temporary burnout and recovery
 * - winner gets a small accuracy boost
 *
 * Part 2 additions (from setup options) are applied on top.
 */
public class TypingRaceEngine {
    public static final double TURN_SECONDS = 0.35;

    private static final int BASE_SLIDE_BACK = 2;
    private static final double BASE_BURNOUT_CHANCE = 0.05;
    private static final int MAX_TURNS = 3000;

    private final RaceConfig config;
    private final List<GuiTypist> typists;
    private final List<GuiTypist> finishOrder;
    private final Map<String, Integer> burnoutDurationReductionByName;
    private final Random random;
    private int turn;
    private boolean finished;

    /**
     * Creates a new race from the setup configuration.
     *
     * @param config race setup choices
     * @param leaderboardManager leaderboard data used for optional rank impact
     */
    public TypingRaceEngine(RaceConfig config, LeaderboardManager leaderboardManager) {
        this(config, leaderboardManager, null);
    }

    /**
     * Creates a new race from the setup configuration with optional Option B upgrades.
     *
     * @param config race setup choices
     * @param leaderboardManager leaderboard data used for optional rank impact
     * @param sponsorPrizeManager sponsor and upgrade data (nullable)
     */
    public TypingRaceEngine(
            RaceConfig config,
            LeaderboardManager leaderboardManager,
            SponsorPrizeManager sponsorPrizeManager
    ) {
        this.config = config;
        this.typists = new ArrayList<>();
        this.finishOrder = new ArrayList<>();
        this.burnoutDurationReductionByName = new HashMap<>();
        this.random = new Random();
        this.turn = 0;
        this.finished = false;

        for (int i = 0; i < config.getSeatCount(); i++) {
            TypistSetup setup = config.getTypists().get(i);
            double accuracy = setup.calculateBaseAccuracy();
            if (config.isNightShiftEnabled()) {
                accuracy -= 0.05;
            }
            if (config.isRankImpactEnabled()) {
                accuracy += leaderboardManager.getRankAdjustment(setup.getName());
            }
            if (sponsorPrizeManager != null) {
                accuracy += sponsorPrizeManager.getAccuracyUpgradeBonus(setup.getName());
                burnoutDurationReductionByName.put(
                        setup.getName(),
                        sponsorPrizeManager.getBurnoutDurationReduction(setup.getName())
                );
            } else {
                burnoutDurationReductionByName.put(setup.getName(), 0);
            }
            typists.add(new GuiTypist(setup, clamp(accuracy)));
        }
    }

    public String getPassage() {
        return config.getPassage();
    }

    public int getTurn() {
        return turn;
    }

    public int getPassageLength() {
        return config.getPassage().length();
    }

    public boolean isFinished() {
        return finished;
    }

    public List<GuiTypist> getTypists() {
        return typists;
    }

    /**
     * Advances the simulation by exactly one turn.
     *
     * Important rule: the race ends as soon as the first typist reaches
     * passageLength (>=), matching the Part 1 spec.
     */
    public void advanceOneTurn() {
        if (finished) {
            return;
        }
        turn++;

        for (GuiTypist typist : typists) {
            typist.setJustMistyped(false);
            if (typist.isFinished()) {
                continue;
            }

            if (typist.isBurntOut()) {
                typist.recoverFromBurnout();
                continue;
            }

            double effectiveAccuracy = computeEffectiveAccuracy(typist);
            double adjustedAccuracy = clamp(1.0 - ((1.0 - effectiveAccuracy) * typist.getSetup().getMistypeMultiplier()));
            boolean typedCorrectly = random.nextDouble() < adjustedAccuracy;
            typist.registerKeystroke(typedCorrectly);
            if (typedCorrectly) {
                typist.typeCharacter();
            } else {
                int slideBack = config.isAutocorrectEnabled()
                        ? Math.max(1, BASE_SLIDE_BACK / 2)
                        : BASE_SLIDE_BACK;
                typist.slideBack(slideBack);
                typist.addMistype();
                typist.setJustMistyped(true);
            }

            double burnoutChance = BASE_BURNOUT_CHANCE * effectiveAccuracy * effectiveAccuracy;
            burnoutChance += typist.getSetup().getBurnoutRiskBonus();
            if (config.isCaffeineModeEnabled() && turn > 10) {
                burnoutChance += 0.05;
            }
            burnoutChance = clampChance(burnoutChance);

            if (random.nextDouble() < burnoutChance) {
                typist.incrementBurnoutCount();
                int reduction = burnoutDurationReductionByName.getOrDefault(typist.getName(), 0);
                int burnoutDuration = Math.max(1, typist.getSetup().getBurnoutDuration() - reduction);
                typist.burnOut(burnoutDuration);
                typist.setCurrentAccuracy(typist.getCurrentAccuracy() - 0.02);
            }

            if (typist.getProgress() >= getPassageLength()) {
                typist.markFinished(turn);
                finishOrder.add(typist);
            }
        }

        // End immediately once we have a winner.
        if (!finishOrder.isEmpty()) {
            placeRemainingTypistsByProgress();
            finished = true;
            applyPostRaceAdjustments();
            return;
        }

        if (turn >= MAX_TURNS) {
            forceFinishByProgress();
            finished = true;
            applyPostRaceAdjustments();
        }
    }

    public List<RaceResult> buildResults() {
        List<RaceResult> results = new ArrayList<>();
        for (int i = 0; i < finishOrder.size(); i++) {
            GuiTypist typist = finishOrder.get(i);
            double minutes = Math.max(0.01, (typist.getFinishTurn() * TURN_SECONDS) / 60.0);
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

    /**
     * Computes the effective accuracy for this turn after modifiers.
     */
    private double computeEffectiveAccuracy(GuiTypist typist) {
        double effectiveAccuracy = typist.getCurrentAccuracy();
        if (config.isCaffeineModeEnabled() && turn <= 10) {
            effectiveAccuracy += 0.08;
        }

        if (typist.getSetup().hasEnergyDrink()) {
            if (typist.getProgress() < getPassageLength() / 2) {
                effectiveAccuracy += 0.06;
            } else {
                effectiveAccuracy -= 0.06;
            }
        }
        return clamp(effectiveAccuracy);
    }

    /**
     * If no winner appears before MAX_TURNS, force a finish by current progress.
     */
    private void forceFinishByProgress() {
        List<GuiTypist> ordered = new ArrayList<>(typists);
        ordered.sort(Comparator
                .comparingInt(GuiTypist::getProgress).reversed()
                .thenComparing(GuiTypist::getName));
        for (GuiTypist typist : ordered) {
            if (!typist.isFinished()) {
                typist.markFinished(turn);
            }
            if (!finishOrder.contains(typist)) {
                finishOrder.add(typist);
            }
        }
    }

    /**
     * Once a winner exists, assign the remaining positions by current progress.
     * This keeps results complete while still ending the race immediately.
     */
    private void placeRemainingTypistsByProgress() {
        List<GuiTypist> remaining = new ArrayList<>();
        for (GuiTypist typist : typists) {
            if (!finishOrder.contains(typist)) {
                remaining.add(typist);
            }
        }

        remaining.sort(Comparator
                .comparingInt(GuiTypist::getProgress).reversed()
                .thenComparing(GuiTypist::getName));

        for (GuiTypist typist : remaining) {
            typist.markFinished(turn);
            finishOrder.add(typist);
        }
    }

    /**
     * Applies post-race performance adjustment.
     * Winner gets +0.02 accuracy, clamped in GuiTypist.
     */
    private void applyPostRaceAdjustments() {
        if (!finishOrder.isEmpty()) {
            GuiTypist winner = finishOrder.get(0);
            winner.setCurrentAccuracy(winner.getCurrentAccuracy() + 0.02);
        }
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double clampChance(double value) {
        return Math.max(0.0, Math.min(0.70, value));
    }
}
