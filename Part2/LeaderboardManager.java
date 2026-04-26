import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Initial Option A leaderboard service.
 *
 * This slice tracks cumulative points and best WPM. Badges/history are added later.
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
        }
    }

    public int calculatePoints(RaceResult result, int seatCount) {
        int placementPoints = seatCount - (result.getFinishPosition() - 1);
        int wpmBonus = result.getWpm() >= 40 ? 1 : 0;
        int burnoutPenalty = -Math.min(2, result.getBurnoutCount());
        return Math.max(0, placementPoints + wpmBonus + burnoutPenalty);
    }

    public List<LeaderboardRow> getLeaderboardRows() {
        List<LeaderboardEntry> sorted = new ArrayList<>(entries.values());
        sorted.sort((left, right) -> {
            if (left.totalPoints != right.totalPoints) {
                return Integer.compare(right.totalPoints, left.totalPoints);
            }
            return Double.compare(right.bestWpm, left.bestWpm);
        });

        List<LeaderboardRow> rows = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            LeaderboardEntry entry = sorted.get(i);
            rows.add(new LeaderboardRow(i + 1, entry.name, entry.totalPoints, entry.bestWpm, entry.totalRaces));
        }
        return rows;
    }

    public static class LeaderboardRow {
        private final int rank;
        private final String name;
        private final int points;
        private final double bestWpm;
        private final int races;

        public LeaderboardRow(int rank, String name, int points, double bestWpm, int races) {
            this.rank = rank;
            this.name = name;
            this.points = points;
            this.bestWpm = bestWpm;
            this.races = races;
        }

        public int getRank() {
            return rank;
        }

        public String getName() {
            return name;
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
    }

    private static class LeaderboardEntry {
        private final String name;
        private int totalPoints;
        private int totalRaces;
        private double bestWpm;

        private LeaderboardEntry(String name) {
            this.name = name;
        }
    }
}
