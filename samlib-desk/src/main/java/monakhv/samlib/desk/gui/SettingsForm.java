/*
 * Created by JFormDesigner on Fri Feb 27 19:05:55 MSK 2015
 */

package monakhv.samlib.desk.gui;

import monakhv.samlib.desk.data.Settings;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Dmitry Monakhov
 */
public class SettingsForm extends JPanel {
    private JDialog settingDialog;
    private Settings settings;
    public SettingsForm(JFrame frame, Settings settings) {
        this.settings=settings;
        initComponents();

        loadData(settings);
        settingDialog=new JDialog(frame,"Настройки",true);
        Container content = settingDialog.getContentPane();
        content.add(this);
        settingDialog.pack();
    }

    private void loadData(Settings settings) {

    }
    private void saveData(Settings settings) {

    }
    private void open(){
        //jPFpassword.requestFocus();
        settingDialog.setVisible(true);
    }
    private void close(){
        settingDialog.setVisible(false);
    }

    private void buttonOKActionPerformed(ActionEvent e) {
        saveData(settings);
        close();
    }

    private void buttonCancelActionPerformed(ActionEvent e) {
        close();
    }

    public static void show(JFrame frame,Settings settings){
        SettingsForm sf=new SettingsForm(frame,settings);
        sf.open();
    }
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel1 = new JPanel();
        cbProxy = new JCheckBox();
        label2 = new JLabel();
        tfProxyHost = new JTextField();
        label4 = new JLabel();
        tfProxyPort = new JTextField();
        label5 = new JLabel();
        tfProxyUser = new JTextField();
        label3 = new JLabel();
        tfProxyPassword = new JTextField();
        panel2 = new JPanel();
        buttonOK = new JButton();
        buttonCancel = new JButton();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

        //======== panel1 ========
        {
            panel1.setBorder(new TitledBorder("Proxy"));
            panel1.setLayout(new GridBagLayout());
            ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 200, 0};
            ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
            ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
            ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- cbProxy ----
            cbProxy.setText("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u043f\u0440\u043e\u043a\u0441\u0438");
            panel1.add(cbProxy, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label2 ----
            label2.setText("\u0425\u043e\u0441\u0442");
            panel1.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));
            panel1.add(tfProxyHost, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u041f\u043e\u0440\u0442");
            panel1.add(label4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- tfProxyPort ----
            tfProxyPort.setText("3128");
            panel1.add(tfProxyPort, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label5 ----
            label5.setText("\u041f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044c");
            panel1.add(label5, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));
            panel1.add(tfProxyUser, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label3 ----
            label3.setText("\u041f\u0430\u0440\u043e\u043b\u044c");
            panel1.add(label3, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));
            panel1.add(tfProxyPassword, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
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
            buttonOK.setText("OK");
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
            buttonCancel.setText("Cancel");
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
        add(panel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel1;
    private JCheckBox cbProxy;
    private JLabel label2;
    private JTextField tfProxyHost;
    private JLabel label4;
    private JTextField tfProxyPort;
    private JLabel label5;
    private JTextField tfProxyUser;
    private JLabel label3;
    private JTextField tfProxyPassword;
    private JPanel panel2;
    private JButton buttonOK;
    private JButton buttonCancel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
