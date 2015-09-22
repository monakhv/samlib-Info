/*
 * Created by JFormDesigner on Fri Feb 27 19:05:55 MSK 2015
 */

package monakhv.samlib.desk.gui;

import java.util.*;
import monakhv.samlib.desk.data.Settings;
import monakhv.samlib.http.HttpClientController;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * @author Dmitry Monakhov
 */
public class SettingsForm extends JPanel {
    private JDialog settingDialog;
    private Settings settings;
    private ResourceBundle bundle;
    private HashMap<String,ComboItem> bookLifeTime;
    public SettingsForm(JFrame frame, Settings settings) {
        bookLifeTime = new HashMap<>();
        this.settings=settings;
        initComponents();

        bundle = ResourceBundle.getBundle("samlibDesk");
        loadData(settings);
        settingDialog=new JDialog(frame,"Настройки",true);
        Container content = settingDialog.getContentPane();
        content.add(this);
        settingDialog.pack();
    }

    private void loadData(Settings settings) {
        cbProxy.setSelected(settings.isProxyUse());
        tfProxyHost.setText(settings.getProxyHost());
        tfProxyUser.setText(settings.getProxyUser());
        tfProxyPort.setText(settings.getProxyStrPort());
        tfProxyPassword.setText(settings.getProxyPassword());
        cbProxyActionPerformed();
        initComoBookLifeTime();
        cBBookAutoLoadFlag.setSelected(settings.getAutoLoadFlag());
        ckBLimitBookLifeFlag.setSelected(settings.getLimitBookLifeTimeFlag());
        cbBookFileType.setSelectedItem(settings.getFileType().name());
        cBBookLifiLimit.setSelectedItem(bookLifeTime.get(settings.getBookLifeTime()));

        ckBLimitBookLifeFlagActionPerformed();
    }
    private void saveData() {
        settings.setProxyUse(cbProxy.isSelected());
        settings.setProxyHost(tfProxyHost.getText());
        settings.setProxyStrPort(tfProxyPort.getText());
        settings.setProxyUser(tfProxyUser.getText());
        settings.setProxyPassword(tfProxyPassword.getText());
        settings.saveProperties();
        HttpClientController.getInstance(settings).setProxy(settings.getProxy());
        settings.setAutoLoadFlag(cBBookAutoLoadFlag.isSelected());
        settings.setLimitBookLifeTimeFlag(ckBLimitBookLifeFlag.isSelected());
        settings.setFileType((String) cbBookFileType.getSelectedItem());

        ComboItem ci = (ComboItem) cBBookLifiLimit.getSelectedItem();
        settings.setBookLifeTime(ci.getData());

    }
    private void initComoBookLifeTime(){
        String [] vv = {"week","month","half-year","year"};

        for (String v : vv){
            String title = bundle.getString("SettingsForm.interval.title."+v);
            String data = bundle.getString("SettingsForm.interval.data."+v);
            ComboItem ci = new ComboItem(title,data);
            bookLifeTime.put(data,ci);
            cBBookLifiLimit.addItem(ci);
        }

    }
    private void open(){
        //jPFpassword.requestFocus();
        settingDialog.setVisible(true);
    }
    private void close(){
        settingDialog.setVisible(false);
    }

    private void buttonOKActionPerformed(ActionEvent e) {
        saveData();
        close();
    }

    private void buttonCancelActionPerformed(ActionEvent e) {
        close();
    }

    public static void show(JFrame frame,Settings settings){
        SettingsForm sf=new SettingsForm(frame,settings);
        sf.open();
    }

    private void cbProxyActionPerformed() {

        boolean enable = cbProxy.isSelected();
        tfProxyHost.setEnabled(enable);
        tfProxyUser.setEnabled(enable);
        tfProxyPort.setEnabled(enable);
        tfProxyPassword.setEnabled(enable);

    }

    private void ckBLimitBookLifeFlagActionPerformed() {

        boolean enable = ckBLimitBookLifeFlag.isSelected();
        cBBookLifiLimit.setEnabled(enable);
    }
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("samlibDesk");
        tabbedPane1 = new JTabbedPane();
        panel4 = new JPanel();
        cBBookAutoLoadFlag = new JCheckBox();
        cbBookFileType = new JComboBox<>();
        label1 = new JLabel();
        ckBLimitBookLifeFlag = new JCheckBox();
        cBBookLifiLimit = new JComboBox();
        label6 = new JLabel();
        panel3 = new JPanel();
        panelProxy = new JPanel();
        cbProxy = new JCheckBox();
        label2 = new JLabel();
        tfProxyHost = new JTextField();
        label4 = new JLabel();
        tfProxyPort = new JTextField();
        label5 = new JLabel();
        tfProxyUser = new JTextField();
        label3 = new JLabel();
        tfProxyPassword = new JPasswordField();
        panel2 = new JPanel();
        buttonOK = new JButton();
        buttonCancel = new JButton();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

        //======== tabbedPane1 ========
        {

            //======== panel4 ========
            {
                panel4.setLayout(new GridBagLayout());
                ((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
                ((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                ((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

                //---- cBBookAutoLoadFlag ----
                cBBookAutoLoadFlag.setText(bundle.getString("SettingsForm.cBBookAutoLoadFlag.text"));
                panel4.add(cBBookAutoLoadFlag, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- cbBookFileType ----
                cbBookFileType.setModel(new DefaultComboBoxModel<>(new String[] {
                    "HTML",
                    "FB2"
                }));
                panel4.add(cbBookFileType, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label1 ----
                label1.setText(bundle.getString("SettingsForm.label1.text"));
                panel4.add(label1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- ckBLimitBookLifeFlag ----
                ckBLimitBookLifeFlag.setText(bundle.getString("SettingsForm.ckBLimitBookLifeFlag.text"));
                ckBLimitBookLifeFlag.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ckBLimitBookLifeFlagActionPerformed();
                    }
                });
                panel4.add(ckBLimitBookLifeFlag, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
                panel4.add(cBBookLifiLimit, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- label6 ----
                label6.setText(bundle.getString("SettingsForm.label6.text"));
                panel4.add(label6, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            tabbedPane1.addTab(bundle.getString("SettingsForm.panel4.tab.title"), panel4);

            //======== panel3 ========
            {
                panel3.setLayout(new GridBagLayout());
                ((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                ((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

                //======== panelProxy ========
                {
                    panelProxy.setBorder(null);
                    panelProxy.setLayout(new GridBagLayout());
                    ((GridBagLayout)panelProxy.getLayout()).columnWidths = new int[] {10, 200, 0};
                    ((GridBagLayout)panelProxy.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
                    ((GridBagLayout)panelProxy.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
                    ((GridBagLayout)panelProxy.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                    //---- cbProxy ----
                    cbProxy.setText(bundle.getString("SettingsForm.cbProxy.text"));
                    cbProxy.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            cbProxyActionPerformed();
                        }
                    });
                    panelProxy.add(cbProxy, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                    //---- label2 ----
                    label2.setText(bundle.getString("SettingsForm.label2.text"));
                    panelProxy.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                        new Insets(0, 0, 5, 5), 0, 0));
                    panelProxy.add(tfProxyHost, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                    //---- label4 ----
                    label4.setText(bundle.getString("SettingsForm.label4.text"));
                    panelProxy.add(label4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- tfProxyPort ----
                    tfProxyPort.setText(bundle.getString("SettingsForm.tfProxyPort.text"));
                    panelProxy.add(tfProxyPort, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                    //---- label5 ----
                    label5.setText(bundle.getString("SettingsForm.label5.text"));
                    panelProxy.add(label5, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                        new Insets(0, 0, 5, 5), 0, 0));
                    panelProxy.add(tfProxyUser, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                    //---- label3 ----
                    label3.setText(bundle.getString("SettingsForm.label3.text"));
                    panelProxy.add(label3, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                        new Insets(0, 0, 0, 5), 0, 0));
                    panelProxy.add(tfProxyPassword, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                panel3.add(panelProxy, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            }
            tabbedPane1.addTab(bundle.getString("SettingsForm.panel3.tab.title"), panel3);
        }
        add(tabbedPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //======== panel2 ========
        {
            panel2.setLayout(new GridBagLayout());
            ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
            ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

            //---- buttonOK ----
            buttonOK.setText(bundle.getString("SettingsForm.buttonOK.text"));
            buttonOK.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonOKActionPerformed(e);
                    buttonOKActionPerformed(e);
                }
            });
            panel2.add(buttonOK, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- buttonCancel ----
            buttonCancel.setText(bundle.getString("SettingsForm.buttonCancel.text"));
            buttonCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonCancelActionPerformed(e);
                }
            });
            panel2.add(buttonCancel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        add(panel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JTabbedPane tabbedPane1;
    private JPanel panel4;
    private JCheckBox cBBookAutoLoadFlag;
    private JComboBox<String> cbBookFileType;
    private JLabel label1;
    private JCheckBox ckBLimitBookLifeFlag;
    private JComboBox cBBookLifiLimit;
    private JLabel label6;
    private JPanel panel3;
    private JPanel panelProxy;
    private JCheckBox cbProxy;
    private JLabel label2;
    private JTextField tfProxyHost;
    private JLabel label4;
    private JTextField tfProxyPort;
    private JLabel label5;
    private JTextField tfProxyUser;
    private JLabel label3;
    private JPasswordField tfProxyPassword;
    private JPanel panel2;
    private JButton buttonOK;
    private JButton buttonCancel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
