import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Option B sponsor and prize service.
 *
 * Responsibilities:
 * - assign sponsor deals to typists
 * - compute race earnings (base + speed + sponsor bonus - burnout penalty)
 * - track cumulative and available coins
 * - apply purchased upgrades to future races
 * - provide a financial leaderboard model for the GUI
 */
public class SponsorPrizeManager {
    private static final String NO_SPONSOR = "None";

    private static final String KEY_CORP = "KeyCorp";
    private static final String VELOCITY_KEYS = "VelocityKeys";
    private static final String STEADY_HANDS = "SteadyHands";

    private static final int PRECISION_KEYS_COST = 220;
    private static final int ERGO_SUPPORT_COST = 180;
    private static final int MAX_UPGRADE_LEVEL = 3;

    private final Map<String, Wallet> wallets = new LinkedHashMap<>();

    public static List<String> getAvailableSponsors() {
        return Arrays.asList(
                NO_SPONSOR,
                KEY_CORP + " (+50 if no burnout)",
                VELOCITY_KEYS + " (+40 if WPM >= 55)",
                STEADY_HANDS + " (+30 if top 3 finish)"
        );
    }

    /**
     * Registers sponsor choices from setup before each race.
     */
    public void assignSponsors(List<TypistSetup> typists) {
        for (TypistSetup typist : typists) {
            Wallet wallet = wallets.computeIfAbsent(typist.getName(), Wallet::new);
            wallet.sponsorCode = normalizeSponsor(typist.getSponsorName());
            wallet.sponsorLabel = typist.getSponsorName();
        }
    }

    /**
     * Applies one race of Option B earnings logic to all results.
     */
    public void applyRaceResults(List<RaceResult> results, int seatCount) {
        for (RaceResult result : results) {
            Wallet wallet = wallets.computeIfAbsent(result.getName(), Wallet::new);
            if (wallet.sponsorCode == null) {
                wallet.sponsorCode = NO_SPONSOR;
                wallet.sponsorLabel = NO_SPONSOR;
            }

            int base = calculateBaseEarnings(result, seatCount);
            int speedBonus = calculateSpeedBonus(result);
            int burnoutPenalty = calculateBurnoutPenalty(result);
            int sponsorBonus = calculateSponsorBonus(wallet.sponsorCode, result);
            int raceCoins = Math.max(0, base + speedBonus + sponsorBonus - burnoutPenalty);

            wallet.totalCoinsEarned += raceCoins;
            wallet.availableCoins += raceCoins;

            result.setRaceEarnings(raceCoins);
            result.setSponsorBonus(sponsorBonus);
        }
    }

    public boolean purchasePrecisionKeys(String typistName) {
        Wallet wallet = wallets.get(typistName);
        if (wallet == null || wallet.availableCoins < PRECISION_KEYS_COST || wallet.precisionKeysLevel >= MAX_UPGRADE_LEVEL) {
            return false;
        }
        wallet.availableCoins -= PRECISION_KEYS_COST;
        wallet.precisionKeysLevel++;
        return true;
    }

    public boolean purchaseErgoSupport(String typistName) {
        Wallet wallet = wallets.get(typistName);
        if (wallet == null || wallet.availableCoins < ERGO_SUPPORT_COST || wallet.ergoSupportLevel >= MAX_UPGRADE_LEVEL) {
            return false;
        }
        wallet.availableCoins -= ERGO_SUPPORT_COST;
        wallet.ergoSupportLevel++;
        return true;
    }

    /**
     * Accuracy improvement from purchased precision upgrades.
     */
    public double getAccuracyUpgradeBonus(String typistName) {
        Wallet wallet = wallets.get(typistName);
        if (wallet == null) {
            return 0.0;
        }
        return wallet.precisionKeysLevel * 0.02;
    }

    /**
     * Burnout turn reduction from purchased ergonomic support.
     */
    public int getBurnoutDurationReduction(String typistName) {
        Wallet wallet = wallets.get(typistName);
        if (wallet == null) {
            return 0;
        }
        return Math.min(1, wallet.ergoSupportLevel);
    }

    public int getPrecisionKeysCost() {
        return PRECISION_KEYS_COST;
    }

    public int getErgoSupportCost() {
        return ERGO_SUPPORT_COST;
    }

    public List<String> getKnownTypists() {
        return new ArrayList<>(wallets.keySet());
    }

    public List<FinancialRow> getFinancialRows() {
        List<Wallet> sorted = new ArrayList<>(wallets.values());
        sorted.sort((left, right) -> {
            if (left.totalCoinsEarned != right.totalCoinsEarned) {
                return Integer.compare(right.totalCoinsEarned, left.totalCoinsEarned);
            }
            return left.name.compareTo(right.name);
        });

        List<FinancialRow> rows = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Wallet wallet = sorted.get(i);
            rows.add(new FinancialRow(
                    i + 1,
                    wallet.name,
                    wallet.sponsorLabel,
                    wallet.totalCoinsEarned,
                    wallet.availableCoins,
                    "Precision " + wallet.precisionKeysLevel + " | Ergo " + wallet.ergoSupportLevel
            ));
        }
        return rows;
    }

    private int calculateBaseEarnings(RaceResult result, int seatCount) {
        int placeValue = seatCount - (result.getFinishPosition() - 1);
        return Math.max(0, placeValue * 100);
    }

    private int calculateSpeedBonus(RaceResult result) {
        if (result.getWpm() >= 60) {
            return 60;
        }
        if (result.getWpm() >= 45) {
            return 30;
        }
        return 0;
    }

    private int calculateBurnoutPenalty(RaceResult result) {
        return result.getBurnoutCount() * 25;
    }

    private int calculateSponsorBonus(String sponsorCode, RaceResult result) {
        switch (sponsorCode) {
            case KEY_CORP:
                return result.getBurnoutCount() == 0 ? 50 : 0;
            case VELOCITY_KEYS:
                return result.getWpm() >= 55 ? 40 : 0;
            case STEADY_HANDS:
                return result.getFinishPosition() <= 3 ? 30 : 0;
            default:
                return 0;
        }
    }

    private String normalizeSponsor(String label) {
        if (label == null || label.trim().isEmpty() || NO_SPONSOR.equals(label)) {
            return NO_SPONSOR;
        }
        if (label.startsWith(KEY_CORP)) {
            return KEY_CORP;
        }
        if (label.startsWith(VELOCITY_KEYS)) {
            return VELOCITY_KEYS;
        }
        if (label.startsWith(STEADY_HANDS)) {
            return STEADY_HANDS;
        }
        return NO_SPONSOR;
    }

    public static class FinancialRow {
        private final int rank;
        private final String name;
        private final String sponsor;
        private final int totalCoins;
        private final int availableCoins;
        private final String upgrades;

        public FinancialRow(int rank, String name, String sponsor, int totalCoins, int availableCoins, String upgrades) {
            this.rank = rank;
            this.name = name;
            this.sponsor = sponsor;
            this.totalCoins = totalCoins;
            this.availableCoins = availableCoins;
            this.upgrades = upgrades;
        }

        public int getRank() {
            return rank;
        }

        public String getName() {
            return name;
        }

        public String getSponsor() {
            return sponsor;
        }

        public int getTotalCoins() {
            return totalCoins;
        }

        public int getAvailableCoins() {
            return availableCoins;
        }

        public String getUpgrades() {
            return upgrades;
        }
    }

    private static class Wallet {
        private final String name;
        private String sponsorCode;
        private String sponsorLabel;
        private int totalCoinsEarned;
        private int availableCoins;
        private int precisionKeysLevel;
        private int ergoSupportLevel;

        private Wallet(String name) {
            this.name = name;
            this.sponsorCode = NO_SPONSOR;
            this.sponsorLabel = NO_SPONSOR;
            this.totalCoinsEarned = 0;
            this.availableCoins = 0;
            this.precisionKeysLevel = 0;
            this.ergoSupportLevel = 0;
        }
    }
}
