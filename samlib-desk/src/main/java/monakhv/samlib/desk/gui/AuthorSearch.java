/*
 * Created by JFormDesigner on Mon Jul 20 16:01:24 MSK 2015
 */

package monakhv.samlib.desk.gui;


import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.AuthorCard;
import monakhv.samlib.desk.data.Settings;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;
import monakhv.samlib.service.SamlibService;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Dmitry Monakhov
 */
public class AuthorSearch extends JDialog {
    private static final String DEBUG_TAG="AuthorSearch";
    private List<AuthorCard> authorCards;
    private final Settings settings;
    private final AuthorCardListModel model;
    private final Frame frame;
    public AuthorSearch(Frame owner,Settings settings) {
        super(owner);
        initComponents();
        this.settings=settings;
        authorCards = new ArrayList<>();
        model = new AuthorCardListModel();
        this.frame = owner;
        jlAuthorList.setCellRenderer(new AuthorCardRenderer());
        jlAuthorList.setModel(model);

    }


    private void cancelButtonActionPerformed() {
        this.setVisible(false);
    }

    private void buttonSearchActionPerformed() {
        String pat = tFsearch.getText();

        if (pat == null){
            return;
        }
        if (pat.equalsIgnoreCase("")){
            return;
        }
        Log.i(DEBUG_TAG,"Run search pattern: "+pat);
        SearchWorker worker = new SearchWorker( pat);
        frame.setCursor(Cursor.WAIT_CURSOR);
        worker.execute();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("samlibDesk");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        panel1 = new JPanel();
        tFsearch = new JTextField();
        buttonSearch = new JButton();
        scrollPane1 = new JScrollPane();
        jlAuthorList = new JList();
        buttonBar = new JPanel();
        cancelButton = new JButton();

        //======== this ========
        setTitle(bundle.getString("AuthorSearch.this.title"));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {400, 0};
                ((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 600, 0};
                ((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                ((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

                //======== panel1 ========
                {
                    panel1.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
                    ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                    ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
                    ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
                    panel1.add(tFsearch, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- buttonSearch ----
                    buttonSearch.setText(bundle.getString("AuthorSearch.buttonSearch.text"));
                    buttonSearch.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            buttonSearchActionPerformed();
                        }
                    });
                    panel1.add(buttonSearch, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));
                }
                contentPanel.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //======== scrollPane1 ========
                {
                    scrollPane1.setViewportView(jlAuthorList);
                }
                contentPanel.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- cancelButton ----
                cancelButton.setText(bundle.getString("AuthorSearch.cancelButton.text"));
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cancelButtonActionPerformed();
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
    private JTextField tFsearch;
    private JButton buttonSearch;
    private JScrollPane scrollPane1;
    private JList jlAuthorList;
    private JPanel buttonBar;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    @Override
    public String toString() {
        return "AuthorSearch";
    }

    private class SearchWorker extends SwingWorker<Void,Void>{
        private String pattern;

        public SearchWorker(String pattern) {
            this.pattern = pattern;
        }

        @Override
        protected Void doInBackground() throws Exception {
            authorCards=SamlibService.makeSearch(pattern, HttpClientController.getInstance(settings));
            return null;
        }

        @Override
        protected void done() {

            model.update(authorCards);

            frame.setCursor(Cursor.DEFAULT_CURSOR);
        }
    }

    private class AuthorCardListModel  extends AbstractListModel<AuthorCard> {
        private List<AuthorCard> data;

        public AuthorCardListModel(){
            data = new ArrayList<>();
        }
        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public AuthorCard getElementAt(int index) {
            return data.get(index);
        }
        public void update(List<AuthorCard> data){
            this.data=data;
            fireContentsChanged(this,0,data.size()-1);
        }
    }
    private class AuthorCardRenderer extends DefaultListCellRenderer {
        private final AuthorCardRow pan ;


        public AuthorCardRenderer() {
            pan = new AuthorCardRow();
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean hasFocus) {


            if (value != null) {
                if (value instanceof AuthorCard) {
                    final AuthorCard a = (AuthorCard) value;
                    pan.load(a);
                }

            }

            //pan.setSelected(isSelected,list);

            pan.setEnabled(list.isEnabled());
            return pan;


        }

    }
}
