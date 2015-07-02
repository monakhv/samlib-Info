/*
 * Created by JFormDesigner on Thu Jul 02 17:51:43 MSK 2015
 */

package monakhv.samlib.desk.gui;

import java.awt.event.*;
import monakhv.samlib.db.DaoBuilder;
import monakhv.samlib.db.TagController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Tag;
import monakhv.samlib.db.entity.Tag2Author;
import monakhv.samlib.log.Log;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Dmitry Monakhov
 */
public class AuthorTagsDialog extends JDialog {
    private static final String DEBUG_TAG="AuthorTagsDialog";
    private HashMap<String,JCheckBox> allCbs;

    private TagController tagCtl;
    private DaoBuilder sql;

    public AuthorTagsDialog(Frame owner,DaoBuilder sql) {
        super(owner);
        initComponents();
        this.sql = sql;
        allCbs=new HashMap<>();


        tagCtl = new TagController(sql);

        panel1.removeAll();
        for (Tag tag: tagCtl.getAll()){
            JCheckBox cb = new JCheckBox();
            allCbs.put(tag.getUcName(), cb);
            cb.setName(tag.getName());
            JLabel label = new JLabel();
            label.setText(tag.getName());
            panel1.add(cb);
            panel1.add(label);

        }

        panel1.revalidate();
        pack();


    }

    public AuthorTagsDialog(Dialog owner) {
        super(owner);
        initComponents();
    }

    void setPanel(Author author){
        for (JCheckBox cb : allCbs.values()){
            cb.setSelected(false);
        }

        for (Tag2Author t2a : author.getTag2Authors()){
            int tagId = t2a.getTag().getId();
            Tag tag = tagCtl.getById(tagId);
            allCbs.get(tag.getUcName()).setSelected(true);
            Log.i(DEBUG_TAG, "Author: " + author.getName() + " - " + tag.getName());
        }
        panel1.revalidate();


    }

    private void okButtonActionPerformed(ActionEvent e) {
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent e) {
        setVisible(false);
    }
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        panel1 = new JPanel();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {0, 0};
                ((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 0};
                ((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                ((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

                //======== panel1 ========
                {
                    panel1.setLayout(new GridLayout(0, 2));
                }
                contentPanel.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0};

                //---- okButton ----
                okButton.setText("OK");
                okButton.setMaximumSize(new Dimension(65, 31));
                okButton.setMinimumSize(new Dimension(65, 31));
                okButton.setPreferredSize(new Dimension(65, 31));
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        okButtonActionPerformed(e);
                    }
                });
                buttonBar.add(okButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cancelButtonActionPerformed(e);
                    }
                });
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JPanel panel1;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
