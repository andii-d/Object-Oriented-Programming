import java.awt.Color;

/**
 * Immutable setup model for one GUI typist.
 *
 * This stores all customisation choices from the setup screen and exposes
 * helper methods to convert those choices into simulation values.
 */
public class TypistSetup {
    private final String name;
    private final String symbol;
    private final Color color;
    private final TypingStyle typingStyle;
    private final KeyboardType keyboardType;
    private final boolean wristSupport;
    private final boolean energyDrink;
    private final boolean noiseCancellingHeadphones;

    /**
     * Creates one typist setup entry.
     *
     * @param name typist display name
     * @param symbol symbol or emoji for race display
     * @param color lane/highlight color
     * @param typingStyle selected typing style
     * @param keyboardType selected keyboard type
     * @param wristSupport true if wrist support accessory is enabled
     * @param energyDrink true if energy drink accessory is enabled
     * @param noiseCancellingHeadphones true if headphones accessory is enabled
     */
    public TypistSetup(
            String name,
            String symbol,
            Color color,
            TypingStyle typingStyle,
            KeyboardType keyboardType,
            boolean wristSupport,
            boolean energyDrink,
            boolean noiseCancellingHeadphones
    ) {
        this.name = (name == null || name.trim().isEmpty()) ? "Typist" : name.trim();
        this.symbol = (symbol == null || symbol.trim().isEmpty()) ? "⌨" : symbol.trim();
        this.color = color == null ? Color.BLUE : color;
        this.typingStyle = typingStyle == null ? TypingStyle.TOUCH_TYPIST : typingStyle;
        this.keyboardType = keyboardType == null ? KeyboardType.MEMBRANE : keyboardType;
        this.wristSupport = wristSupport;
        this.energyDrink = energyDrink;
        this.noiseCancellingHeadphones = noiseCancellingHeadphones;
    }

    /**
     * @return typist display name
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
     * @return selected typing style
     */
    public TypingStyle getTypingStyle() {
        return typingStyle;
    }

    /**
     * @return selected keyboard type
     */
    public KeyboardType getKeyboardType() {
        return keyboardType;
    }

    /**
     * @return true if wrist support is enabled
     */
    public boolean hasWristSupport() {
        return wristSupport;
    }

    /**
     * @return true if energy drink is enabled
     */
    public boolean hasEnergyDrink() {
        return energyDrink;
    }

    /**
     * @return true if noise-cancelling headphones are enabled
     */
    public boolean hasNoiseCancellingHeadphones() {
        return noiseCancellingHeadphones;
    }

    /**
     * Calculates base accuracy from style + keyboard with clamping.
     *
     * @return base accuracy in range [0.0, 1.0]
     */
    public double calculateBaseAccuracy() {
        return clamp(0.65 + typingStyle.getAccuracyBonus() + keyboardType.getAccuracyBonus());
    }

    /**
     * Wrist support shortens burnout from 3 turns to 2 turns.
     *
     * @return burnout duration in turns
     */
    public int getBurnoutDuration() {
        return wristSupport ? 2 : 3;
    }

    /**
     * @return additive burnout-risk modifier from selected style + keyboard
     */
    public double getBurnoutRiskBonus() {
        return typingStyle.getBurnoutRiskBonus() + keyboardType.getBurnoutRiskBonus();
    }

    /**
     * Returns mistype multiplier after accessories.
     *
     * Lower value means fewer mistypes.
     */
    public double getMistypeMultiplier() {
        double multiplier = keyboardType.getMistypeMultiplier();
        if (noiseCancellingHeadphones) {
            multiplier *= 0.80;
        }
        return multiplier;
    }

    /**
     * Human-readable summary shown on the setup screen.
     */
    public String getImpactSummary() {
        return String.format(
                "Base accuracy %.2f | Burnout risk %+.2f | Burnout turns %d%s%s",
                calculateBaseAccuracy(),
                getBurnoutRiskBonus(),
                getBurnoutDuration(),
                energyDrink ? " | Energy Drink enabled" : "",
                noiseCancellingHeadphones ? " | Noise-cancelling enabled" : ""
        );
    }

    /**
     * Utility clamp used for accuracy values.
     */
    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}

/**
 * Typing style preset.
 */
enum TypingStyle {
    TOUCH_TYPIST("Touch Typist", 0.15, 0.02),
    HUNT_AND_PECK("Hunt & Peck", -0.12, -0.01),
    PHONE_THUMBS("Phone Thumbs", -0.06, 0.01),
    VOICE_TO_TEXT("Voice-to-Text", 0.05, 0.03);

    private final String label;
    private final double accuracyBonus;
    private final double burnoutRiskBonus;

    /**
     * @param label label shown in setup UI
     * @param accuracyBonus additive accuracy modifier
     * @param burnoutRiskBonus additive burnout-risk modifier
     */
    TypingStyle(String label, double accuracyBonus, double burnoutRiskBonus) {
        this.label = label;
        this.accuracyBonus = accuracyBonus;
        this.burnoutRiskBonus = burnoutRiskBonus;
    }

    /**
     * @return style accuracy modifier
     */
    public double getAccuracyBonus() {
        return accuracyBonus;
    }

    /**
     * @return style burnout-risk modifier
     */
    public double getBurnoutRiskBonus() {
        return burnoutRiskBonus;
    }

    @Override
    public String toString() {
        return label;
    }
}

/**
 * Keyboard type preset.
 */
enum KeyboardType {
    MECHANICAL("Mechanical", 0.05, 0.95, 0.01),
    MEMBRANE("Membrane", 0.00, 1.00, 0.00),
    TOUCHSCREEN("Touchscreen", -0.08, 1.20, 0.00),
    STENOGRAPHY("Stenography", 0.10, 0.90, 0.03);

    private final String label;
    private final double accuracyBonus;
    private final double mistypeMultiplier;
    private final double burnoutRiskBonus;

    /**
     * @param label label shown in setup UI
     * @param accuracyBonus additive accuracy modifier
     * @param mistypeMultiplier multiplicative mistype modifier
     * @param burnoutRiskBonus additive burnout-risk modifier
     */
    KeyboardType(String label, double accuracyBonus, double mistypeMultiplier, double burnoutRiskBonus) {
        this.label = label;
        this.accuracyBonus = accuracyBonus;
        this.mistypeMultiplier = mistypeMultiplier;
        this.burnoutRiskBonus = burnoutRiskBonus;
    }

    /**
     * @return keyboard accuracy modifier
     */
    public double getAccuracyBonus() {
        return accuracyBonus;
    }

    /**
     * @return keyboard mistype multiplier
     */
    public double getMistypeMultiplier() {
        return mistypeMultiplier;
    }

    /**
     * @return keyboard burnout-risk modifier
     */
    public double getBurnoutRiskBonus() {
        return burnoutRiskBonus;
    }

    @Override
    public String toString() {
        return label;
    }
}
