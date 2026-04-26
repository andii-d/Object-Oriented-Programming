import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Setup screen for configuring and starting a race.
 *
 * Covers:
 * - passage selection/custom passage
 * - seat count (2-6)
 * - global modifiers
 * - per-typist customisation (style, keyboard, symbol/emoji, accessories)
 */
public class SetupPanel extends JPanel {
    /**
     * Listener used to pass a validated RaceConfig to the main frame.
     */
    public interface RaceStartListener {
        void onRaceStart(RaceConfig config);
    }

    private final RaceStartListener listener;
    private final JComboBox<String> passagePresetCombo;
    private final JTextArea customPassageArea;
    private final JSpinner seatCountSpinner;
    private final JCheckBox autocorrectCheckBox;
    private final JCheckBox caffeineCheckBox;
    private final JCheckBox nightShiftCheckBox;
    private final JCheckBox rankImpactCheckBox;
    private final List<TypistRow> typistRows;
    private final Map<String, String> presetPassages;

    /**
     * Builds the setup UI and wires events.
     *
     * @param listener callback fired when Start Race is pressed successfully
     */
    public SetupPanel(RaceStartListener listener) {
        this.listener = listener;
        this.typistRows = new ArrayList<>();
        this.presetPassages = buildPresetPassages();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("Passage:"), gbc);
        passagePresetCombo = new JComboBox<>(new String[]{"Short", "Medium", "Long", "Custom"});
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        topPanel.add(passagePresetCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        topPanel.add(new JLabel("Custom Passage:"), gbc);
        customPassageArea = new JTextArea(3, 40);
        customPassageArea.setLineWrap(true);
        customPassageArea.setWrapStyleWord(true);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        topPanel.add(new JScrollPane(customPassageArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        topPanel.add(new JLabel("Seat Count (2-6):"), gbc);
        seatCountSpinner = new JSpinner(new SpinnerNumberModel(3, 2, 6, 1));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        topPanel.add(seatCountSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        topPanel.add(new JLabel("Difficulty Modifiers:"), gbc);

        JPanel modifiersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        autocorrectCheckBox = new JCheckBox("Autocorrect (halves slide-back)");
        caffeineCheckBox = new JCheckBox("Caffeine Mode (+speed first 10 turns, more burnout after)");
        nightShiftCheckBox = new JCheckBox("Night Shift (global accuracy reduction)");
        rankImpactCheckBox = new JCheckBox("Rank Impact (optional)");
        modifiersPanel.add(autocorrectCheckBox);
        modifiersPanel.add(caffeineCheckBox);
        modifiersPanel.add(nightShiftCheckBox);
        modifiersPanel.add(rankImpactCheckBox);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        topPanel.add(modifiersPanel, gbc);

        JTextArea impactLegend = new JTextArea(
                "Typing Style, Keyboard, and Accessories change accuracy/mistypes/burnout.\n"
                        + "Wrist Support: shorter burnout. Energy Drink: stronger first half, weaker second half.\n"
                        + "Noise-Cancelling Headphones: fewer mistypes."
        );
        impactLegend.setEditable(false);
        impactLegend.setOpaque(false);
        impactLegend.setLineWrap(true);
        impactLegend.setWrapStyleWord(true);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        topPanel.add(impactLegend, gbc);

        add(topPanel, BorderLayout.NORTH);

        JPanel rowContainer = new JPanel();
        rowContainer.setLayout(new BoxLayout(rowContainer, BoxLayout.Y_AXIS));
        for (int i = 0; i < 6; i++) {
            TypistRow row = new TypistRow(i + 1);
            typistRows.add(row);
            rowContainer.add(row);
        }
        add(new JScrollPane(rowContainer), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton startButton = new JButton("Start Race");
        bottom.add(startButton);
        add(bottom, BorderLayout.SOUTH);

        seatCountSpinner.addChangeListener(e -> updateEnabledRows());
        passagePresetCombo.addActionListener(e -> updateCustomPassageState());
        updateCustomPassageState();
        updateEnabledRows();

        startButton.addActionListener(e -> startRace());
    }

    /**
     * Creates predefined passage options.
     */
    private Map<String, String> buildPresetPassages() {
        Map<String, String> presets = new LinkedHashMap<>();
        presets.put("Short", "The quick brown fox jumps over the lazy dog.");
        presets.put("Medium", "Typing races reward rhythm, precision, and calm under pressure. Keep your focus and let your fingers flow.");
        presets.put("Long", "In a typing race, tiny errors add up quickly. Consistency beats chaos: stay accurate, recover from mistakes, and build momentum until the finish line.");
        return presets;
    }

    /**
     * Enables custom passage box only when "Custom" is selected.
     */
    private void updateCustomPassageState() {
        boolean customSelected = "Custom".equals(passagePresetCombo.getSelectedItem());
        customPassageArea.setEnabled(customSelected);
    }

    /**
     * Enables only as many typist rows as selected seat count.
     */
    private void updateEnabledRows() {
        int enabledSeats = (int) seatCountSpinner.getValue();
        for (int i = 0; i < typistRows.size(); i++) {
            typistRows.get(i).setRowEnabled(i < enabledSeats);
        }
    }

    /**
     * Validates all setup input and starts the race if valid.
     */
    private void startRace() {
        int seatCount = (int) seatCountSpinner.getValue();
        String selectedPassageType = (String) passagePresetCombo.getSelectedItem();
        String passage;
        if ("Custom".equals(selectedPassageType)) {
            passage = customPassageArea.getText().trim();
            if (passage.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a custom passage.");
                return;
            }
        } else {
            passage = presetPassages.get(selectedPassageType);
        }

        List<TypistSetup> setups = new ArrayList<>();
        Set<String> usedSymbols = new HashSet<>();
        Set<String> usedNames = new HashSet<>();
        for (int i = 0; i < seatCount; i++) {
            TypistRow row = typistRows.get(i);
            TypistSetup setup = row.toSetup();

            if (setup.getName().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Typist " + (i + 1) + " needs a name.");
                return;
            }
            String normalizedName = setup.getName().trim().toLowerCase(Locale.ROOT);
            if (usedNames.contains(normalizedName)) {
                JOptionPane.showMessageDialog(this, "Each typist must have a unique name.");
                return;
            }
            usedNames.add(normalizedName);
            if (usedSymbols.contains(setup.getSymbol())) {
                JOptionPane.showMessageDialog(this, "Each typist must have a unique symbol/emoji.");
                return;
            }
            usedSymbols.add(setup.getSymbol());
            setups.add(setup);
        }

        RaceConfig config = new RaceConfig(
                passage,
                seatCount,
                autocorrectCheckBox.isSelected(),
                caffeineCheckBox.isSelected(),
                nightShiftCheckBox.isSelected(),
                rankImpactCheckBox.isSelected(),
                setups
        );
        listener.onRaceStart(config);
    }

    /**
     * One UI row representing one typist seat configuration.
     */
    private static class TypistRow extends JPanel {
        private static final String[] SYMBOL_DEFAULTS = {"①", "②", "③", "④", "⑤", "⑥"};

        private final JTextField nameField;
        private final JTextField symbolField;
        private final JComboBox<String> colorCombo;
        private final JComboBox<TypingStyle> styleCombo;
        private final JComboBox<KeyboardType> keyboardCombo;
        private final JComboBox<String> sponsorCombo;
        private final JCheckBox wristSupportBox;
        private final JCheckBox energyDrinkBox;
        private final JCheckBox noiseCancellingBox;
        private final JLabel summaryLabel;

        /**
         * Builds controls for one seat row.
         */
        TypistRow(int index) {
            setBorder(BorderFactory.createTitledBorder("Seat " + index + " Typist"));
            setLayout(new GridLayout(3, 1, 0, 4));

            JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            nameField = new JTextField("Typist" + index, 10);
            symbolField = new JTextField(SYMBOL_DEFAULTS[index - 1], 3);
            colorCombo = new JComboBox<>(new String[]{"Blue", "Red", "Green", "Magenta", "Orange", "Cyan"});
            row1.add(new JLabel("Name"));
            row1.add(nameField);
            row1.add(new JLabel("Symbol/Emoji"));
            row1.add(symbolField);
            row1.add(new JLabel("Color"));
            row1.add(colorCombo);
            add(row1);

            JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            styleCombo = new JComboBox<>(TypingStyle.values());
            keyboardCombo = new JComboBox<>(KeyboardType.values());
            sponsorCombo = new JComboBox<>(SponsorPrizeManager.getAvailableSponsors().toArray(new String[0]));
            wristSupportBox = new JCheckBox("Wrist Support");
            energyDrinkBox = new JCheckBox("Energy Drink");
            noiseCancellingBox = new JCheckBox("Noise-Cancelling Headphones");
            row2.add(new JLabel("Style"));
            row2.add(styleCombo);
            row2.add(new JLabel("Keyboard"));
            row2.add(keyboardCombo);
            row2.add(new JLabel("Sponsor"));
            row2.add(sponsorCombo);
            row2.add(wristSupportBox);
            row2.add(energyDrinkBox);
            row2.add(noiseCancellingBox);
            add(row2);

            JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            summaryLabel = new JLabel();
            row3.add(summaryLabel);
            add(row3);

            Runnable updateSummary = this::refreshSummary;
            styleCombo.addActionListener(e -> updateSummary.run());
            keyboardCombo.addActionListener(e -> updateSummary.run());
            wristSupportBox.addActionListener(e -> updateSummary.run());
            energyDrinkBox.addActionListener(e -> updateSummary.run());
            noiseCancellingBox.addActionListener(e -> updateSummary.run());
            sponsorCombo.addActionListener(e -> updateSummary.run());
            nameField.getDocument().addDocumentListener(simpleDocumentListener(updateSummary));
            refreshSummary();
        }

        /**
         * Converts UI values into a TypistSetup object.
         */
        TypistSetup toSetup() {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                name = "Typist";
            }
            String symbol = symbolField.getText().trim();
            if (symbol.isEmpty()) {
                symbol = "⌨";
            }
            Color color = parseColor((String) colorCombo.getSelectedItem());
            TypingStyle style = (TypingStyle) styleCombo.getSelectedItem();
            KeyboardType keyboardType = (KeyboardType) keyboardCombo.getSelectedItem();

            return new TypistSetup(
                    name,
                    symbol,
                    color,
                    style,
                    keyboardType,
                    wristSupportBox.isSelected(),
                    energyDrinkBox.isSelected(),
                    noiseCancellingBox.isSelected(),
                    (String) sponsorCombo.getSelectedItem()
            );
        }

        /**
         * Enables or disables all controls in this row.
         */
        void setRowEnabled(boolean enabled) {
            setEnabled(enabled);
            nameField.setEnabled(enabled);
            symbolField.setEnabled(enabled);
            colorCombo.setEnabled(enabled);
            styleCombo.setEnabled(enabled);
            keyboardCombo.setEnabled(enabled);
            sponsorCombo.setEnabled(enabled);
            wristSupportBox.setEnabled(enabled);
            energyDrinkBox.setEnabled(enabled);
            noiseCancellingBox.setEnabled(enabled);
            summaryLabel.setEnabled(enabled);
            setVisible(true);
        }

        /**
         * Recomputes and displays the attribute impact summary text.
         */
        private void refreshSummary() {
            TypistSetup setup = toSetup();
            summaryLabel.setText(setup.getImpactSummary());
        }

        /**
         * Maps color names from combo box to RGB values.
         */
        private static Color parseColor(String colorName) {
            if ("Red".equals(colorName)) {
                return new Color(210, 50, 50);
            }
            if ("Green".equals(colorName)) {
                return new Color(40, 150, 60);
            }
            if ("Magenta".equals(colorName)) {
                return new Color(160, 60, 190);
            }
            if ("Orange".equals(colorName)) {
                return new Color(210, 120, 20);
            }
            if ("Cyan".equals(colorName)) {
                return new Color(35, 150, 180);
            }
            return new Color(60, 90, 210);
        }

        /**
         * Small helper to trigger summary refresh on text changes.
         */
        private static DocumentListener simpleDocumentListener(Runnable runnable) {
            return new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    runnable.run();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    runnable.run();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    runnable.run();
                }
            };
        }
    }
}
