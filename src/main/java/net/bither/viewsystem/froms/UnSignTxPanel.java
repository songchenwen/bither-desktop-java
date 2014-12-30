package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.BitherUI;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.qrcode.QRCodeEnodeUtil;
import net.bither.bitherj.utils.GenericUtils;
import net.bither.bitherj.utils.UnitUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.fonts.AwesomeDecorator;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.qrcode.GenerateUnsignedTxPanel;
import net.bither.qrcode.IReadQRCode;
import net.bither.qrcode.IScanQRCode;
import net.bither.qrcode.SelectQRCodePanel;
import net.bither.runnable.CommitTransactionThread;
import net.bither.runnable.CompleteTransactionRunnable;
import net.bither.runnable.RunnableListener;
import net.bither.utils.InputParser;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.TransactionsUtil;
import net.bither.viewsystem.TextBoxes;
import net.bither.viewsystem.action.PasteAddressAction;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;

public class UnSignTxPanel extends WizardPanel implements IScanQRCode {

    private JTextField tfAddress;
    private JTextField tfAmt;
    private String bitcoinAddress;
    private Tx tx;
    private boolean needConfirm = true;

    public UnSignTxPanel() {
        super(MessageKey.UNSIGNED, AwesomeIcon.FA_BANK,false);
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
                "[]10[][][][][]" // Row constraints
        ));
        panel.add(newEnterAddressPanel(), "push,wrap");
        panel.add(newAmountPanel(), "push,wrap");
        validateValues();

    }

    public JPanel newAmountPanel() {

        JPanel panel = Panels.newPanel(new MigLayout(
                Panels.migXLayout(),
                "[][][][]", // Columns
                "[]" // Rows
        ));

        //panel.add(Labels.newAmount(), "span 4,grow,push,wrap");
        JLabel label = Labels.newBitcoinSymbol();
        label.setText("");
        AwesomeDecorator.applyIcon(AwesomeIcon.FA_BTC, label, true, BitherUI.NORMAL_ICON_SIZE);
        tfAmt = TextBoxes.newAmount(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateUI();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateUI();

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateUI();
            }

            private void updateUI() {
                validateValues();
            }
        });
        panel.add(label, "shrink");

        panel.add(tfAmt, "grow,push,wrap");

        return panel;

    }

    public JPanel newEnterAddressPanel() {

        JPanel panel = Panels.newPanel(
                new MigLayout(
                        Panels.migXLayout(),
                        "[][][][]", // Columns
                        "[]" // Rows
                ));


        tfAddress = TextBoxes.newEnterAddress(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateUI();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateUI();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateUI();
            }

            private void updateUI() {
                validateValues();
            }
        });


        panel.add(Labels.newBitcoinAddress());

        panel.add(tfAddress, "growx," + BitherUI.COMBO_BOX_WIDTH_MIG + ",push");
        panel.add(Buttons.newPasteButton(new PasteAddressAction(tfAddress)), "shrink");

        panel.add(getQRCodeButton(), "shrink");


        return panel;

    }

    private JButton getQRCodeButton() {
        JButton button = Buttons.newFromCameraIconButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onQRcode();
            }
        });
        return button;
    }

    private void onQRcode() {

        SelectQRCodePanel qrCodePanel = new SelectQRCodePanel(new IScanQRCode() {

            public void handleResult(String result, final IReadQRCode readQRCode) {

                new InputParser.StringInputParser(result) {
                    @Override
                    protected void bitcoinRequest(final String address, final String addressLabel,
                                                  final long amount, final String bluetoothMac) {
                        tfAddress.setText(address);
                        if (amount > 0) {
                            tfAmt.setText(UnitUtil.formatValue(amount, UnitUtil.BitcoinUnit.BTC));

                        }
                        tfAmt.requestFocus();
                        validateValues();
                    }

                    @Override
                    protected void error(final String messageResId, final Object... messageArgs) {
                        readQRCode.reTry(LocaliserUtils.getString("scan.watch.only.address.error"));

                    }
                }.parse();


            }
        }, true);
        qrCodePanel.showPanel();

    }

    private void validateValues() {
        boolean isValidAmounts = false;
        String amtString = tfAmt.getText().trim();
        long btc = 0;
        if (!Utils.isEmpty(amtString)) {
            btc = GenericUtils.toNanoCoins(amtString, 0).longValue();
        }
        if (btc > 0) {
            isValidAmounts = true;
        } else {
        }
        boolean isValidAddress = Utils.validBicoinAddress(tfAddress.getText().trim());
        setOkEnabled(isValidAddress && isValidAmounts);
    }

    private void onOK() {
        bitcoinAddress = tfAddress.getText().trim();
        if (Utils.validBicoinAddress(bitcoinAddress)) {
            String amtString = tfAmt.getText().trim();
            long btc = GenericUtils.toNanoCoins(amtString, 0).longValue();
            try {
                CompleteTransactionRunnable completeTransactionRunnable = new CompleteTransactionRunnable(
                        Bither.getActionAddress(), btc, bitcoinAddress, null);
                completeTransactionRunnable.setRunnableListener(completeTransactionListener);
                new Thread(completeTransactionRunnable).start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    SendBitcoinConfirmPanel.SendConfirmListener sendConfirmListener = new SendBitcoinConfirmPanel.SendConfirmListener() {
        @Override
        public void onConfirm(Tx request) {

            String qrCodeString = QRCodeEnodeUtil.getPresignTxString(request, null, LocaliserUtils.getString("address.cannot.be.parsed"));
            GenerateUnsignedTxPanel generateUnsignedTxPanel = new GenerateUnsignedTxPanel(UnSignTxPanel.this, qrCodeString);
            generateUnsignedTxPanel.showPanel();

        }

        @Override
        public void onCancel() {

        }
    };

    RunnableListener completeTransactionListener = new RunnableListener() {
        @Override
        public void prepare() {
        }

        @Override
        public void success(Object obj) {
            if (obj != null && obj instanceof Tx) {
                tx = (Tx) obj;
                if (needConfirm) {
                    SendBitcoinConfirmPanel confirmPanel = new SendBitcoinConfirmPanel
                            (sendConfirmListener, bitcoinAddress, null, tx);
                    confirmPanel.showPanel();
                } else {
                    sendConfirmListener.onConfirm(tx);
                }
            } else {
                new MessageDialog(LocaliserUtils.getString("password.wrong")).showMsg();
            }

        }

        @Override
        public void error(int errorCode, String errorMsg) {
            new MessageDialog(errorMsg).showMsg();

        }
    };

    @Override
    public void handleResult(String result, IReadQRCode readQRCode) {
        boolean success;
        try {
            success = TransactionsUtil.signTransaction(tx, result);
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        if (success) {
            readQRCode.close();
            sendTx(tx);
        } else {
            readQRCode.reTry(" qr code error");
        }

    }

    private void sendTx(Tx tx) {

        try {
            CommitTransactionThread commitTransactionThread =
                    new CommitTransactionThread(Bither.getActionAddress(), tx
                            , false, commitTransactionListener);
            commitTransactionThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    CommitTransactionThread.CommitTransactionListener commitTransactionListener = new CommitTransactionThread.CommitTransactionListener() {
        @Override
        public void onCommitTransactionSuccess(Tx tx) {
            onCancel();

        }

        @Override
        public void onCommitTransactionFailed() {

        }
    };
}