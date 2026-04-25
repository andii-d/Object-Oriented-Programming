import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
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

/**
 * Early setup screen scaffold.
 * Additional controls (modifiers + full typist customisation) are added later.
 */
public class SetupPanel extends JPanel {
    private static final String[] SYMBOL_DEFAULTS = {"①", "②", "③", "④", "⑤", "⑥"};
    private static final Color[] SEAT_COLORS = {
            new Color(60, 90, 210),
            new Color(210, 50, 50),
            new Color(40, 150, 60),
            new Color(160, 60, 190),
            new Color(210, 120, 20),
            new Color(35, 150, 180)
    };

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
    private final List<TypistRow> typistRows;

    public SetupPanel(RaceStartListener listener) {
        this.listener = listener;
        this.typistRows = new ArrayList<>();
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Passage"), gbc);
        passagePresetCombo = new JComboBox<>(new String[]{"Short", "Medium", "Long", "Custom"});
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(passagePresetCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        form.add(new JLabel("Custom Passage"), gbc);
        customPassageArea = new JTextArea(3, 40);
        customPassageArea.setLineWrap(true);
        customPassageArea.setWrapStyleWord(true);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(new JScrollPane(customPassageArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        form.add(new JLabel("Seat Count (2-6)"), gbc);
        seatCountSpinner = new JSpinner(new SpinnerNumberModel(3, 2, 6, 1));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(seatCountSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        form.add(new JLabel("Modifiers"), gbc);
        JPanel modifiersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        autocorrectCheckBox = new JCheckBox("Autocorrect");
        caffeineCheckBox = new JCheckBox("Caffeine Mode");
        nightShiftCheckBox = new JCheckBox("Night Shift");
        modifiersPanel.add(autocorrectCheckBox);
        modifiersPanel.add(caffeineCheckBox);
        modifiersPanel.add(nightShiftCheckBox);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(modifiersPanel, gbc);

        add(form, BorderLayout.NORTH);

        JPanel typistsContainer = new JPanel();
        typistsContainer.setLayout(new BoxLayout(typistsContainer, BoxLayout.Y_AXIS));
        for (int i = 0; i < 6; i++) {
            TypistRow row = new TypistRow(i + 1);
            typistRows.add(row);
            typistsContainer.add(row);
        }
        add(new JScrollPane(typistsContainer), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton startButton = new JButton("Start Race");
        startButton.addActionListener(e -> startRace());
        bottom.add(startButton);
        add(bottom, BorderLayout.SOUTH);

        seatCountSpinner.addChangeListener(e -> updateEnabledRows());
        updateEnabledRows();
    }

    private void startRace() {
        String passage;
        String preset = (String) passagePresetCombo.getSelectedItem();
        if ("Custom".equals(preset)) {
            passage = customPassageArea.getText().trim();
            if (passage.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a custom passage.");
                return;
            }
        } else if ("Medium".equals(preset)) {
            passage = "Typing races reward rhythm, precision, and calm under pressure.";
        } else if ("Long".equals(preset)) {
            passage = "In a typing race, consistency beats chaos. Stay accurate, recover from mistakes, and keep momentum.";
        } else {
            passage = "The quick brown fox jumps over the lazy dog.";
        }

        int seatCount = (int) seatCountSpinner.getValue();
        List<TypistSetup> typists = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        Set<String> usedSymbols = new HashSet<>();
        for (int i = 0; i < seatCount; i++) {
            TypistSetup setup = typistRows.get(i).toSetup();
            String name = setup.getName().trim();
            String symbol = setup.getSymbol();
            if (usedNames.contains(name.toLowerCase())) {
                JOptionPane.showMessageDialog(this, "Typist names must be unique.");
                return;
            }
            if (usedSymbols.contains(symbol)) {
                JOptionPane.showMessageDialog(this, "Typist symbols must be unique.");
                return;
            }
            usedNames.add(name.toLowerCase());
            usedSymbols.add(symbol);
            typists.add(setup);
        }

        RaceConfig config = new RaceConfig(
                passage,
                seatCount,
                autocorrectCheckBox.isSelected(),
                caffeineCheckBox.isSelected(),
                nightShiftCheckBox.isSelected(),
                false,
                typists
        );
        listener.onRaceStart(config);
    }

    private void updateEnabledRows() {
        int enabledRows = (int) seatCountSpinner.getValue();
        for (int i = 0; i < typistRows.size(); i++) {
            typistRows.get(i).setRowEnabled(i < enabledRows);
        }
    }

    private static class TypistRow extends JPanel {
        private final int seatIndex;
        private final JTextField nameField;
        private final JTextField symbolField;
        private final JComboBox<TypingStyle> styleCombo;
        private final JComboBox<KeyboardType> keyboardCombo;
        private final JCheckBox wristSupportBox;
        private final JCheckBox energyDrinkBox;
        private final JCheckBox noiseCancellingBox;

        TypistRow(int seatNumber) {
            this.seatIndex = seatNumber - 1;
            setBorder(BorderFactory.createTitledBorder("Seat " + seatNumber));
            setLayout(new GridLayout(2, 1, 0, 4));

            JPanel lineOne = new JPanel(new FlowLayout(FlowLayout.LEFT));
            nameField = new JTextField("Typist" + seatNumber, 10);
            symbolField = new JTextField(SYMBOL_DEFAULTS[seatIndex], 3);
            styleCombo = new JComboBox<>(TypingStyle.values());
            keyboardCombo = new JComboBox<>(KeyboardType.values());
            lineOne.add(new JLabel("Name"));
            lineOne.add(nameField);
            lineOne.add(new JLabel("Symbol"));
            lineOne.add(symbolField);
            lineOne.add(new JLabel("Style"));
            lineOne.add(styleCombo);
            lineOne.add(new JLabel("Keyboard"));
            lineOne.add(keyboardCombo);
            add(lineOne);

            JPanel lineTwo = new JPanel(new FlowLayout(FlowLayout.LEFT));
            wristSupportBox = new JCheckBox("Wrist Support");
            energyDrinkBox = new JCheckBox("Energy Drink");
            noiseCancellingBox = new JCheckBox("Noise-Cancelling");
            lineTwo.add(wristSupportBox);
            lineTwo.add(energyDrinkBox);
            lineTwo.add(noiseCancellingBox);
            add(lineTwo);
        }

        TypistSetup toSetup() {
            String symbol = symbolField.getText().trim();
            if (symbol.isEmpty()) {
                symbol = SYMBOL_DEFAULTS[seatIndex];
            }
            return new TypistSetup(
                    nameField.getText().trim(),
                    symbol,
                    SEAT_COLORS[seatIndex],
                    (TypingStyle) styleCombo.getSelectedItem(),
                    (KeyboardType) keyboardCombo.getSelectedItem(),
                    wristSupportBox.isSelected(),
                    energyDrinkBox.isSelected(),
                    noiseCancellingBox.isSelected()
            );
        }

        void setRowEnabled(boolean enabled) {
            setEnabled(enabled);
            nameField.setEnabled(enabled);
            symbolField.setEnabled(enabled);
            styleCombo.setEnabled(enabled);
            keyboardCombo.setEnabled(enabled);
            wristSupportBox.setEnabled(enabled);
            energyDrinkBox.setEnabled(enabled);
            noiseCancellingBox.setEnabled(enabled);
        }
    }
}
