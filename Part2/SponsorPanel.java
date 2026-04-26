import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Option B financial leaderboard and upgrade purchase panel.
 */
public class SponsorPanel extends JPanel {
    private final DefaultTableModel model;
    private final JComboBox<String> typistCombo;
    private final JComboBox<String> upgradeCombo;
    private final JLabel statusLabel;
    private SponsorPrizeManager manager;

    public SponsorPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(new Object[]{"Rank", "Typist", "Sponsor", "Total Coins", "Available", "Upgrades"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typistCombo = new JComboBox<>();
        upgradeCombo = new JComboBox<>(new String[]{
                "Precision Keys (+accuracy)",
                "Ergonomic Support (-burnout duration)"
        });
        JButton buyButton = new JButton("Purchase Upgrade");
        buyButton.addActionListener(e -> buySelectedUpgrade());
        statusLabel = new JLabel("Run races to earn coins, then buy upgrades.");
        controls.add(new JLabel("Typist"));
        controls.add(typistCombo);
        controls.add(upgradeCombo);
        controls.add(buyButton);
        controls.add(statusLabel);

        add(controls, BorderLayout.SOUTH);
    }

    public void refresh(SponsorPrizeManager manager) {
        this.manager = manager;
        model.setRowCount(0);

        for (SponsorPrizeManager.FinancialRow row : manager.getFinancialRows()) {
            model.addRow(new Object[]{
                    row.getRank(),
                    row.getName(),
                    row.getSponsor(),
                    row.getTotalCoins(),
                    row.getAvailableCoins(),
                    row.getUpgrades()
            });
        }

        List<String> names = manager.getKnownTypists();
        String previous = (String) typistCombo.getSelectedItem();
        typistCombo.removeAllItems();
        for (String name : names) {
            typistCombo.addItem(name);
        }
        if (previous != null) {
            typistCombo.setSelectedItem(previous);
        }
    }

    private void buySelectedUpgrade() {
        if (manager == null || typistCombo.getSelectedItem() == null || upgradeCombo.getSelectedItem() == null) {
            statusLabel.setText("No typist selected.");
            return;
        }

        String typist = (String) typistCombo.getSelectedItem();
        String upgrade = (String) upgradeCombo.getSelectedItem();
        boolean success;
        if (upgrade.startsWith("Precision")) {
            success = manager.purchasePrecisionKeys(typist);
            if (success) {
                statusLabel.setText("Purchased Precision Keys for " + typist + " (-" + manager.getPrecisionKeysCost() + " coins).");
            } else {
                statusLabel.setText("Cannot purchase Precision Keys for " + typist + ".");
            }
        } else {
            success = manager.purchaseErgoSupport(typist);
            if (success) {
                statusLabel.setText("Purchased Ergonomic Support for " + typist + " (-" + manager.getErgoSupportCost() + " coins).");
            } else {
                statusLabel.setText("Cannot purchase Ergonomic Support for " + typist + ".");
            }
        }

        refresh(manager);
    }
}
