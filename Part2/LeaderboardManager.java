import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Option A leaderboard service.
 *
 * Responsibilities:
 * - compute points per race
 * - maintain cumulative ranking state
 * - assign titles and milestone badges
 * - expose table/comparison/history views for the UI
 */
public class LeaderboardManager {
    private final Map<String, LeaderboardEntry> entries = new LinkedHashMap<>();

    /**
     * Applies one race worth of results to cumulative leaderboard state.
     *
     * @param results ordered race results (position 1 at index 0)
     * @param seatCount number of seats in that race
     */
    public void applyRaceResults(List<RaceResult> results, int seatCount) {
        for (RaceResult result : results) {
            int points = calculatePoints(result, seatCount);
            result.setRacePoints(points);

            LeaderboardEntry entry = entries.computeIfAbsent(result.getName(), LeaderboardEntry::new);
            entry.totalPoints += points;
            entry.totalRaces++;
            entry.bestWpm = Math.max(entry.bestWpm, result.getWpm());
            entry.totalBurnouts += result.getBurnoutCount();
            entry.totalAccuracyPercent += result.getAccuracyPercent();
            entry.history.add(new RaceHistory(result));

            if (result.getFinishPosition() == 1) {
                entry.consecutiveWins++;
            } else {
                entry.consecutiveWins = 0;
            }

            if (result.getBurnoutCount() == 0) {
                entry.noBurnoutStreak++;
            } else {
                entry.noBurnoutStreak = 0;
            }

            List<String> unlocked = new ArrayList<>();
            if (entry.consecutiveWins >= 3 && entry.badges.add("Speed Demon")) {
                unlocked.add("Speed Demon");
            }
            if (entry.noBurnoutStreak >= 5 && entry.badges.add("Iron Fingers")) {
                unlocked.add("Iron Fingers");
            }
            result.setNewBadges(unlocked);
        }
    }

    /**
     * Option A scoring algorithm:
     * - placement points
     * - WPM bonus
     * - burnout penalty (capped)
     * - floor at zero
     */
    public int calculatePoints(RaceResult result, int seatCount) {
        int placementPoints = seatCount - (result.getFinishPosition() - 1);
        int wpmBonus;
        if (result.getWpm() >= 60) {
            wpmBonus = 2;
        } else if (result.getWpm() >= 40) {
            wpmBonus = 1;
        } else {
            wpmBonus = 0;
        }
        int burnoutPenalty = -Math.min(3, result.getBurnoutCount());
        return Math.max(0, placementPoints + wpmBonus + burnoutPenalty);
    }

    /**
     * Builds sorted leaderboard rows for table display.
     */
    public List<LeaderboardRow> getLeaderboardRows() {
        List<LeaderboardEntry> sorted = getSortedEntries();
        List<LeaderboardRow> rows = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            LeaderboardEntry entry = sorted.get(i);
            rows.add(new LeaderboardRow(
                    i + 1,
                    entry.name,
                    entry.totalPoints,
                    entry.bestWpm,
                    entry.totalRaces,
                    entry.getAverageAccuracy(),
                    entry.totalBurnouts,
                    entry.getTitle(),
                    String.join(", ", entry.badges)
            ));
        }
        return rows;
    }

    /**
     * @return typist names currently present in leaderboard data
     */
    public Set<String> getTypistNames() {
        return new LinkedHashSet<>(entries.keySet());
    }

    /**
     * @return immutable race history for one typist
     */
    public List<RaceHistory> getHistory(String typistName) {
        LeaderboardEntry entry = entries.get(typistName);
        if (entry == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(entry.history);
    }

    /**
     * Reads a comparison metric by name for the comparison panel.
     */
    public double getMetricValue(String typistName, String metric) {
        LeaderboardEntry entry = entries.get(typistName);
        if (entry == null) {
            return 0.0;
        }
        switch (metric) {
            case "Points":
                return entry.totalPoints;
            case "Best WPM":
                return entry.bestWpm;
            case "Average Accuracy %":
                return entry.getAverageAccuracy();
            case "Total Burnouts":
                return entry.totalBurnouts;
            default:
                return 0.0;
        }
    }

    /**
     * Optional rank-impact adjustment used when that modifier is enabled.
     */
    public double getRankAdjustment(String typistName) {
        List<LeaderboardEntry> sorted = getSortedEntries();
        if (sorted.isEmpty()) {
            return 0.0;
        }
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).name.equals(typistName)) {
                int rank = i + 1;
                if (rank == 1) {
                    return -0.02;
                }
                if (rank <= 3) {
                    return -0.01;
                }
                if (rank == sorted.size()) {
                    return 0.02;
                }
                return 0.01;
            }
        }
        return 0.0;
    }

    /**
     * Sorts leaderboard entries by points, then best WPM, then name.
     */
    private List<LeaderboardEntry> getSortedEntries() {
        List<LeaderboardEntry> sorted = new ArrayList<>(entries.values());
        sorted.sort((left, right) -> {
            if (left.totalPoints != right.totalPoints) {
                return Integer.compare(right.totalPoints, left.totalPoints);
            }
            if (Double.compare(left.bestWpm, right.bestWpm) != 0) {
                return Double.compare(right.bestWpm, left.bestWpm);
            }
            return left.name.compareTo(right.name);
        });
        return sorted;
    }

    /**
     * Flat row model for leaderboard table rendering.
     */
    public static class LeaderboardRow {
        private final int rank;
        private final String name;
        private final int points;
        private final double bestWpm;
        private final int races;
        private final double avgAccuracy;
        private final int totalBurnouts;
        private final String title;
        private final String badges;

        /**
         * Creates one leaderboard table row.
         */
        public LeaderboardRow(
                int rank,
                String name,
                int points,
                double bestWpm,
                int races,
                double avgAccuracy,
                int totalBurnouts,
                String title,
                String badges
        ) {
            this.rank = rank;
            this.name = name;
            this.points = points;
            this.bestWpm = bestWpm;
            this.races = races;
            this.avgAccuracy = avgAccuracy;
            this.totalBurnouts = totalBurnouts;
            this.title = title;
            this.badges = badges;
        }

        /**
         * @return ranking position (1 is highest)
         */
        public int getRank() {
            return rank;
        }

        /**
         * @return typist name
         */
        public String getName() {
            return name;
        }

        /**
         * @return cumulative points
         */
        public int getPoints() {
            return points;
        }

        /**
         * @return personal best WPM
         */
        public double getBestWpm() {
            return bestWpm;
        }

        /**
         * @return number of races included in this entry
         */
        public int getRaces() {
            return races;
        }

        /**
         * @return average accuracy percentage across races
         */
        public double getAvgAccuracy() {
            return avgAccuracy;
        }

        /**
         * @return cumulative burnout count
         */
        public int getTotalBurnouts() {
            return totalBurnouts;
        }

        /**
         * @return title inferred from cumulative points
         */
        public String getTitle() {
            return title;
        }

        /**
         * @return comma-separated badge names
         */
        public String getBadges() {
            return badges;
        }
    }

    /**
     * Historical snapshot of one typist's result in one race.
     */
    public static class RaceHistory {
        private final int finishPosition;
        private final double wpm;
        private final double accuracyPercent;
        private final int burnoutCount;
        private final int points;

        /**
         * Creates one history item from a race result.
         */
        public RaceHistory(RaceResult result) {
            this.finishPosition = result.getFinishPosition();
            this.wpm = result.getWpm();
            this.accuracyPercent = result.getAccuracyPercent();
            this.burnoutCount = result.getBurnoutCount();
            this.points = result.getRacePoints();
        }

        /**
         * @return finish position in that race
         */
        public int getFinishPosition() {
            return finishPosition;
        }

        /**
         * @return WPM in that race
         */
        public double getWpm() {
            return wpm;
        }

        /**
         * @return accuracy percentage in that race
         */
        public double getAccuracyPercent() {
            return accuracyPercent;
        }

        /**
         * @return burnout count in that race
         */
        public int getBurnoutCount() {
            return burnoutCount;
        }

        /**
         * @return points awarded in that race
         */
        public int getPoints() {
            return points;
        }
    }

    /**
     * Internal mutable aggregate state for one typist.
     */
    private static class LeaderboardEntry {
        private final String name;
        private int totalPoints;
        private int totalRaces;
        private double bestWpm;
        private int totalBurnouts;
        private double totalAccuracyPercent;
        private int consecutiveWins;
        private int noBurnoutStreak;
        private final Set<String> badges;
        private final List<RaceHistory> history;

        /**
         * Creates an empty leaderboard entry.
         */
        private LeaderboardEntry(String name) {
            this.name = name;
            this.totalPoints = 0;
            this.totalRaces = 0;
            this.bestWpm = 0.0;
            this.totalBurnouts = 0;
            this.totalAccuracyPercent = 0.0;
            this.consecutiveWins = 0;
            this.noBurnoutStreak = 0;
            this.badges = new LinkedHashSet<>();
            this.history = new ArrayList<>();
        }

        /**
         * @return average accuracy across all races
         */
        private double getAverageAccuracy() {
            if (totalRaces == 0) {
                return 0.0;
            }
            return totalAccuracyPercent / totalRaces;
        }

        /**
         * Derives a lightweight title from cumulative points.
         */
        private String getTitle() {
            if (totalPoints >= 40) {
                return "Legend";
            }
            if (totalPoints >= 25) {
                return "Speed Star";
            }
            if (totalPoints >= 12) {
                return "Rising Typist";
            }
            return "Contender";
        }
    }
}
