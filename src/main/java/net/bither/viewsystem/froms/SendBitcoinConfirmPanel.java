package net.bither.viewsystem.froms;

import net.bither.bitherj.core.Tx;
import net.bither.bitherj.utils.UnitUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SendBitcoinConfirmPanel extends WizardPanel {

    public static interface SendConfirmListener {
        public void onConfirm(Tx request);

        public void onCancel();
    }


    private String address;
    private String changeAddress;
    private Tx tx;
    private SendConfirmListener listener;

    public SendBitcoinConfirmPanel(SendConfirmListener listener,
                                   String toAddress, String changeAddress, Tx tx) {
        super(MessageKey.SEND_CONFIRM, AwesomeIcon.CHECK, true);
        this.listener = listener;
        this.tx = tx;
        this.address = toAddress;
        this.changeAddress = changeAddress;
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

    }

    @Override
    public void initialiseContent(JPanel panel) {
        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[][][][][][][]" // Row constraints
        ));
        panel.add(getToAddressPanel(), "push,wrap");
        if (!Utils.isEmpty(changeAddress) && tx.amountSentToAddress(changeAddress) > 0) {
            panel.add(getChangePanel(), "push,wrap");
        }
        panel.add(getFeePanel(), "push");


    }

    private JPanel getToAddressPanel() {
        JPanel panel = Panels.newPanel(
                new MigLayout(
                        Panels.migXLayout(),
                        "[][][][]", // Columns
                        "[][]" // Rows
                ));
        panel.add(Labels.newValueLabel(LocaliserUtils.getString("send.confirm.address")), "push");
        panel.add(Labels.newValueLabel(address), "push,wrap");
        panel.add(Labels.newValueLabel(LocaliserUtils.getString("send.confirm.amount")), "push");
        long to = tx.amountSentToAddress(address);
        panel.add(Labels.newValueLabel(UnitUtil.formatValue(to, UnitUtil.BitcoinUnit.BTC))
                , "push,wrap");

        return panel;

    }

    private JPanel getChangePanel() {
        JPanel panel = Panels.newPanel(
                new MigLayout(
                        Panels.migXLayout(),
                        "[][][][]", // Columns
                        "[][]" // Rows
                ));
        panel.add(Labels.newValueLabel(LocaliserUtils.getString("send_confirm_change_to_label")), "push");
        panel.add(Labels.newValueLabel(changeAddress), "push,wrap");
        panel.add(Labels.newValueLabel(LocaliserUtils.getString("sign_transaction_change_amount_label")), "push");
        long to = tx.amountSentToAddress(changeAddress);
        panel.add(Labels.newValueLabel(UnitUtil.formatValue(to, UnitUtil.BitcoinUnit.BTC))
                , "push,wrap");

        return panel;
    }

    private JPanel getFeePanel() {
        JPanel panel = Panels.newPanel(
                new MigLayout(
                        Panels.migXLayout(),
                        "[][][][]", // Columns
                        "[][]" // Rows
                ));
        panel.add(Labels.newValueLabel(LocaliserUtils.getString("send.confirm.fee")), "push");
        long to = tx.getFee();
        panel.add(Labels.newValueLabel(UnitUtil.formatValue(to, UnitUtil.BitcoinUnit.BTC))
                , "push,wrap");

        return panel;
    }

    private void onOK() {
        //sendTx();
        onCancel();
        if (listener != null) {
            listener.onConfirm(tx);
        }

    }

    @Override
    protected void onCancel() {
        super.onCancel();
        if (listener != null) {
            listener.onCancel();
        }
    }
}
