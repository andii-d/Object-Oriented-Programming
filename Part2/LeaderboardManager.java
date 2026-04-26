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
 * This slice expands beyond points to include titles, average accuracy,
 * total burnouts, and unlocked badges.
 */
public class LeaderboardManager {
    private final Map<String, LeaderboardEntry> entries = new LinkedHashMap<>();

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

    public List<LeaderboardRow> getLeaderboardRows() {
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

        List<LeaderboardRow> rows = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            LeaderboardEntry entry = sorted.get(i);
            rows.add(new LeaderboardRow(
                    i + 1,
                    entry.name,
                    entry.getTitle(),
                    entry.totalPoints,
                    entry.bestWpm,
                    entry.totalRaces,
                    entry.getAverageAccuracy(),
                    entry.totalBurnouts,
                    String.join(", ", entry.badges)
            ));
        }
        return rows;
    }

    public Set<String> getTypistNames() {
        return new LinkedHashSet<>(entries.keySet());
    }

    public List<RaceHistory> getHistory(String typistName) {
        LeaderboardEntry entry = entries.get(typistName);
        if (entry == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(entry.history);
    }

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

    public static class LeaderboardRow {
        private final int rank;
        private final String name;
        private final String title;
        private final int points;
        private final double bestWpm;
        private final int races;
        private final double avgAccuracy;
        private final int totalBurnouts;
        private final String badges;

        public LeaderboardRow(
                int rank,
                String name,
                String title,
                int points,
                double bestWpm,
                int races,
                double avgAccuracy,
                int totalBurnouts,
                String badges
        ) {
            this.rank = rank;
            this.name = name;
            this.title = title;
            this.points = points;
            this.bestWpm = bestWpm;
            this.races = races;
            this.avgAccuracy = avgAccuracy;
            this.totalBurnouts = totalBurnouts;
            this.badges = badges;
        }

        public int getRank() {
            return rank;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public int getPoints() {
            return points;
        }

        public double getBestWpm() {
            return bestWpm;
        }

        public int getRaces() {
            return races;
        }

        public double getAvgAccuracy() {
            return avgAccuracy;
        }

        public int getTotalBurnouts() {
            return totalBurnouts;
        }

        public String getBadges() {
            return badges;
        }
    }

    public static class RaceHistory {
        private final int finishPosition;
        private final double wpm;
        private final double accuracyPercent;
        private final int burnoutCount;
        private final int points;

        public RaceHistory(RaceResult result) {
            this.finishPosition = result.getFinishPosition();
            this.wpm = result.getWpm();
            this.accuracyPercent = result.getAccuracyPercent();
            this.burnoutCount = result.getBurnoutCount();
            this.points = result.getRacePoints();
        }

        public int getFinishPosition() {
            return finishPosition;
        }

        public double getWpm() {
            return wpm;
        }

        public double getAccuracyPercent() {
            return accuracyPercent;
        }

        public int getBurnoutCount() {
            return burnoutCount;
        }

        public int getPoints() {
            return points;
        }
    }

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

        private LeaderboardEntry(String name) {
            this.name = name;
            this.badges = new LinkedHashSet<>();
            this.history = new ArrayList<>();
        }

        private double getAverageAccuracy() {
            if (totalRaces == 0) {
                return 0.0;
            }
            return totalAccuracyPercent / totalRaces;
        }

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
