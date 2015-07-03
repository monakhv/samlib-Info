/*
 * Created by JFormDesigner on Thu Jul 02 17:51:43 MSK 2015
 */

package monakhv.samlib.desk.gui;

import java.awt.event.*;

import monakhv.samlib.db.AuthorController;
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
    private final static ImageIcon DELETE_ICON = new ImageIcon(AuthorRenderer.class.getResource("/pics/16x16/delete.png"));
    private final static ImageIcon EDIT_ICON = new ImageIcon(AuthorRenderer.class.getResource("/pics/16x16/keyboardpencil.png"));

    private ResourceBundle bundle = ResourceBundle.getBundle("samlibDesk");
    private HashMap<String,JCheckBox> allCbs;

    private TagController tagCtl;
    private DaoBuilder sql;
    private Author author;
    private GuiCallBack callBack;

    public AuthorTagsDialog(Frame owner,DaoBuilder sql,GuiCallBack callBack) {
        super(owner);
        initComponents();
        this.sql = sql;
        this.callBack = callBack;
        allCbs=new HashMap<>();


        tagCtl = new TagController(sql);

        makePanel();


    }
    private void makePanel(){
        panel1.removeAll();
        for (final Tag tag: tagCtl.getAll()){
            JCheckBox cb = new JCheckBox();
            allCbs.put(tag.getUcName(), cb);
            cb.setName(tag.getName());
            JLabel label = new JLabel();
            label.setText(tag.getName());


            JButton deleteButton = new JButton(DELETE_ICON);
            JButton editButton = new JButton(EDIT_ICON);


            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    startDelete(tag);
                }
            });

            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    startEdit(tag);
                }
            });
            cb.setText(tag.getName());
            panel1.add(cb);


            JPanel pan = new JPanel(new GridLayout(1,2));
            pan.add(deleteButton);
            pan.add(editButton);
            panel1.add(pan);

        }

        panel1.revalidate();
        pack();

    }


    void setPanel(Author author){
        tFAddTagName.setText("");
        this.author = author;
        setTitle(author.getName());
        for (JCheckBox cb : allCbs.values()){
            cb.setSelected(false);
        }

        for (Tag2Author t2a : author.getTag2Authors()){
            int tagId = t2a.getTag().getId();
            Tag tag = tagCtl.getById(tagId);
            allCbs.get(tag.getUcName()).setSelected(true);
            Log.i(DEBUG_TAG, "Author: " + author.getName() + " - " + tag.getName());
        }
        redraw();


    }
    private void redraw(){
        this.getContentPane().validate();
        this.getContentPane().repaint();
    }

    private void okButtonActionPerformed(ActionEvent e) {
        List<Tag> tags = new ArrayList<>();
        for (JCheckBox cb : allCbs.values()){
            if (cb.isSelected()){
                tags.add(tagCtl.getByName(cb.getName()));
            }
        }
        AuthorController aSQL = new AuthorController(sql);
        aSQL.syncTags(author,tags);
        callBack.authorRedraw();
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent e) {
        setVisible(false);
    }

    private void buttonAddTagActionPerformed(ActionEvent e) {
        String tname =  tFAddTagName.getText();
        tFAddTagName.setText("");
        tagCtl.insert(new Tag(tname));
        makePanel();
        setPanel(author);;

    }
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("samlibDesk");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        panel1 = new JPanel();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        panel2 = new JPanel();
        tFAddTagName = new JTextField();
        buttonAddTag = new JButton();

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
                    panel1.setLayout(new GridLayout(0, 2, 2, 2));
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

            //======== panel2 ========
            {
                panel2.setLayout(new GridBagLayout());
                ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
                ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
                panel2.add(tFAddTagName, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- buttonAddTag ----
                buttonAddTag.setText(bundle.getString("AuthorTagsDialog.AddButton.text"));
                buttonAddTag.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        buttonAddTagActionPerformed(e);
                    }
                });
                panel2.add(buttonAddTag, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
            }
            dialogPane.add(panel2, BorderLayout.NORTH);
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
    private JPanel panel2;
    private JTextField tFAddTagName;
    private JButton buttonAddTag;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    private void startDelete(Tag tag){
        int  answer = JOptionPane.showConfirmDialog(
                this,
                bundle.getString("AuthorTagsDialog.Confirm.text")+" \""+tag.getName()+"\"?",
                bundle.getString("AuthorTagsDialog.Confirm.Title"),
                JOptionPane.YES_NO_OPTION
        );
        if (answer==JOptionPane.YES_OPTION){
            tagCtl.delete(tag);
            AuthorController aSQL = new AuthorController(sql);
            author=aSQL.getById(author.getId());
            makePanel();
            setPanel(author);
        }
    }
    private void startEdit(final Tag tag){
        AddTextValue editTag= new AddTextValue(
                this,
                bundle.getString("AuthorTagsDialog.EditTag.text"),
                bundle.getString("AuthorTagsDialog.EditTag.title"),
                new AddTextValue.CallBack(){

                    @Override
                    public void okClick(String answer) {
                        tag.setName(answer);
                        tagCtl.update(tag);
                        makePanel();
                        setPanel(author);
                    }
                },tag.getName());


        editTag.setVisible(true);

    }
}
