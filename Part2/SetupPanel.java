import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

/**
 * Early setup screen scaffold.
 * Additional controls (modifiers + full typist customisation) are added later.
 */
public class SetupPanel extends JPanel {
    public interface RaceStartListener {
        void onRaceStart(RaceConfig config);
    }

    private final RaceStartListener listener;
    private final JComboBox<String> passagePresetCombo;
    private final JTextArea customPassageArea;
    private final JSpinner seatCountSpinner;

    public SetupPanel(RaceStartListener listener) {
        this.listener = listener;
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

        add(form, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton startButton = new JButton("Start Race");
        startButton.addActionListener(e -> startRace());
        bottom.add(startButton);
        add(bottom, BorderLayout.SOUTH);
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
        List<TypistSetup> typists = buildDefaultTypists(seatCount);

        RaceConfig config = new RaceConfig(
                passage,
                seatCount,
                false,
                false,
                false,
                false,
                typists
        );
        listener.onRaceStart(config);
    }

    private List<TypistSetup> buildDefaultTypists(int seatCount) {
        String[] symbols = {"①", "②", "③", "④", "⑤", "⑥"};
        Color[] colors = {
                new Color(60, 90, 210),
                new Color(210, 50, 50),
                new Color(40, 150, 60),
                new Color(160, 60, 190),
                new Color(210, 120, 20),
                new Color(35, 150, 180)
        };

        List<TypistSetup> typists = new ArrayList<>();
        for (int i = 0; i < seatCount; i++) {
            typists.add(new TypistSetup(
                    "Typist" + (i + 1),
                    symbols[i],
                    colors[i],
                    TypingStyle.TOUCH_TYPIST,
                    KeyboardType.MEMBRANE,
                    false,
                    false,
                    false
            ));
        }
        return typists;
    }
}
