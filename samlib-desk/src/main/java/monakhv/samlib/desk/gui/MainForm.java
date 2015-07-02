/*
 * Created by JFormDesigner on Fri Feb 13 18:11:31 MSK 2015
 */

package monakhv.samlib.desk.gui;

import java.awt.*;
import java.awt.event.*;

import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.Tag;
import monakhv.samlib.desk.Main;
import monakhv.samlib.desk.data.Settings;
import monakhv.samlib.desk.service.ServiceOperation;
import monakhv.samlib.desk.sql.AuthorController;
import monakhv.samlib.desk.sql.BookController;
import monakhv.samlib.desk.sql.TagController;
import monakhv.samlib.log.Log;

/**
 * @author Dmitry Monakhov
 */
public class MainForm extends JFrame {
    private static final String DEBUG_TAG="MainForm";
    private ResourceBundle bndl = ResourceBundle.getBundle("samlibDesk");
    private final DefaultListModel<Author> authorsModel;
    //private final DefaultListModel<Book> booksModel;

    private final SQLController sql;
    private Settings settings;
    private BookList bkList;
    //private String selection=null;
    private String sortOrder=SQLController.COL_NAME;
    private Author selectedAuthor;
    private ComboItem selectedTag=ComboItem.ALL;
    private List<Author> authorList;

    /**
     * Spercial container for Combo Box  wiget
     */

    public MainForm( Settings settings ) {
        this.settings=settings;
        SQLController sql1;

        try {
            sql1 = SQLController.getInstance( settings.getDataDirectoryPath()  );
        } catch (Exception e) {
            Log.e(DEBUG_TAG,"Error SQL init");
            sql1 =null;
        }
        sql = sql1;
        authorsModel = new DefaultListModel<>();
//        booksModel = new DefaultListModel<>();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                Main.exit(1);
            }
        });
        setTitle(bndl.getString("MainForm.Title.text"));


        initComponents();


        addSortedAuthorList();
        jAuthorList.setModel(authorsModel);
        jAuthorList.setCellRenderer(new AuthorRenderer());
        jAuthorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);




        cBTags.addItem(ComboItem.ALL);
        cBTags.addItem(ComboItem.NEW);
        TagController tagCtl = new TagController(sql);
        for (Tag tag : tagCtl.getAll()){
            cBTags.addItem(new ComboItem(tag));
        }


        //TODO: we can move the constant 20 to settings
        bookScrolPanel.getVerticalScrollBar().setUnitIncrement(20);

        bkList = new BookList(bookPanel, new BookList.CallBack(){

            @Override
            public void bookClick(MouseEvent e, Book book) {
                makeBookClick(e, book);
            }
        });

        jAuthorList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                JList lsm = (JList) e.getSource();


                if (lsm.isSelectionEmpty()|| !e.getValueIsAdjusting() ) {
                    return;
                }
                if (lsm.getMinSelectionIndex() != lsm.getMaxSelectionIndex()) {
                    Log.i(DEBUG_TAG, "selection " + lsm.getMinSelectionIndex() + " - " + lsm.getMaxSelectionIndex());
                    return;
                }
                selectedAuthor=authorsModel.get(lsm.getMaxSelectionIndex());
                Log.i(DEBUG_TAG, "selection " +selectedAuthor.getName()+"  "+e.getValueIsAdjusting());
                loadBookList(selectedAuthor);
            }
        });

    }



    /**
     * Construct Author list
     */
    private void addSortedAuthorList() {
        AuthorController ctl = new AuthorController(sql);
         authorsModel.removeAllElements();



        if (selectedTag.equals(ComboItem.ALL)){
            authorList=ctl.getAll(sortOrder);
        }
        else if (selectedTag.equals(ComboItem.NEW)){
            authorList=ctl.getAllNew(sortOrder);
        }
        else {
            authorList=ctl.getAll(sortOrder,selectedTag.getTag());
        }

        for (Author a : authorList ){
            authorsModel.addElement(a);

//            Log.i(DEBUG_TAG,"Author: "+a.getName()+"- books - "+a.getBooks().size());
//            for (Book book : a.getBooks()){
//                Log.i(DEBUG_TAG,"               ---- "+book.getTitle());
//            }
        }

    }

    /**
     * Construct Book List
     * @param a author
     */
    private void loadBookList(Author a){
        BookController ctl = new BookController(sql);

        if (a != null){
            bkList.load(ctl.getAll(a, SQLController.COL_BOOK_DATE));
        }


        //bookPanel.setComponentPopupMenu(bookPopup);
        redraw();



    }
    private void redraw(){
        this.getContentPane().validate();
        this.getContentPane().repaint();
    }


    private void menuItemExitActionPerformed(ActionEvent e) {
        Main.exit(0);
    }

    private void reFreshActionPerformed(ActionEvent e) {
        bookPanel.revalidate();
        this.repaint();
    }

    private void buttonUpdateActionPerformed(ActionEvent e) {
        //buttonUpdate.setEnabled(false);
        ServiceOperation ops = new ServiceOperation(settings);

        ops.update(authorList);
    }

    private void menuItemSettingsActionPerformed(ActionEvent e) {
        SettingsForm.show(this, settings);
    }

    private void cBTagsActionPerformed(ActionEvent e) {

        JComboBox cb = (JComboBox) e.getSource();
        selectedTag= (ComboItem) cb.getSelectedItem();
        Log.d(DEBUG_TAG,"Tag: "+selectedTag.toString());
        addSortedAuthorList();
        redraw();
    }

    private void jAuthorListMouseClicked(MouseEvent e) {

        int butNum = e.getButton();
        if (butNum ==1){//left mouse button clicks are ignored
            return;
        }
        int index = jAuthorList.locationToIndex(e.getPoint());
        if (index <0){
            return;
        }
        jAuthorList.setSelectedIndex(index);
        selectedAuthor=authorsModel.elementAt(index);
        authorPopup.setLabel(selectedAuthor.getName());
        authorPopup.show(e.getComponent(), e.getX(), e.getY());

    }

    private void makeBookClick(MouseEvent e, Book book) {
        bookPopup.show(e.getComponent(), e.getX(), e.getY());
        Log.i(DEBUG_TAG,"Book: "+book.getTitle());
    }

    private void menuAuthorMakeReadActionPerformed(ActionEvent e) {
        AuthorController ctl = new AuthorController(sql);
        ctl.markRead(selectedAuthor);
        loadBookList(selectedAuthor);
        addSortedAuthorList();
        redraw();
    }

    private void menuAuthorDeleteActionPerformed(ActionEvent e) {
        int  answer = JOptionPane.showConfirmDialog(
                this,
                bndl.getString("MainForm.confirmAuthorDelete"),
                selectedAuthor.getName(),
                JOptionPane.YES_NO_OPTION
        );
        if (answer==JOptionPane.YES_OPTION){
            ServiceOperation ops = new ServiceOperation( settings);
            ops.delete(selectedAuthor);
            selectedAuthor = null;
            addSortedAuthorList();
            redraw();
        }
    }

    private void menuToolsAddActionPerformed(ActionEvent e) {
        AddTextValue addAuthor= new AddTextValue(this,"http://samlib.ot.ru",bndl.getString("MainForm.AddAuthor.Title.text"),
                new AddTextValue.CallBack(){

                    @Override
                    public void okClick(String answer) {
                        Log.i(DEBUG_TAG, "got value: " + answer);
                        ServiceOperation operation = new ServiceOperation(settings);
                        operation.addAuthor(answer);
                        addSortedAuthorList();
                        redraw();
                    }
                });
        addAuthor.setVisible(true);


    }



    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("samlibDesk");
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        menuItemSettings = new JMenuItem();
        menuItemExit = new JMenuItem();
        menuTools = new JMenu();
        menuToolsAdd = new JMenuItem();
        panelMain = new JPanel();
        toolBar = new JPanel();
        buttonUpdate = new JButton();
        cBTags = new JComboBox<>();
        progressBar1 = new JProgressBar();
        reFresh = new JButton();
        scrollPane1 = new JScrollPane();
        jAuthorList = new JList();
        bookScrolPanel = new JScrollPane();
        bookPanel = new JPanel();
        bookPopup = new JPopupMenu();
        menuItem3 = new JMenuItem();
        menuItem4 = new JMenuItem();
        authorPopup = new JPopupMenu();
        menuAuthorMakeRead = new JMenuItem();
        menuAuthorDelete = new JMenuItem();

        //======== this ========
        setMinimumSize(new Dimension(20, 70));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== menuBar1 ========
        {

            //======== menu1 ========
            {
                menu1.setText("File");

                //---- menuItemSettings ----
                menuItemSettings.setText(bundle.getString("MainForm.menu.settings"));
                menuItemSettings.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        menuItemSettingsActionPerformed(e);
                    }
                });
                menu1.add(menuItemSettings);

                //---- menuItemExit ----
                menuItemExit.setText(bundle.getString("MainForm.menu.exit"));
                menuItemExit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        menuItemExitActionPerformed(e);
                    }
                });
                menu1.add(menuItemExit);
            }
            menuBar1.add(menu1);

            //======== menuTools ========
            {
                menuTools.setText(bundle.getString("MainForm.menuTools.text"));

                //---- menuToolsAdd ----
                menuToolsAdd.setText(bundle.getString("MainForm.menuToolsAdd.text"));
                menuToolsAdd.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        menuToolsAddActionPerformed(e);
                    }
                });
                menuTools.add(menuToolsAdd);
            }
            menuBar1.add(menuTools);
        }
        setJMenuBar(menuBar1);

        //======== panelMain ========
        {
            panelMain.setMinimumSize(new Dimension(800, 100));
            panelMain.setBorder(Borders.DLU4);
            panelMain.setLayout(new FormLayout(
                "[200dlu,default]:grow, 5dlu, [350dlu,default]:grow(0.8), default",
                "default, fill:[400dlu,default]:grow, $lgap, default"));

            //======== toolBar ========
            {
                toolBar.setLayout(new GridBagLayout());
                ((GridBagLayout)toolBar.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                ((GridBagLayout)toolBar.getLayout()).rowHeights = new int[] {0, 5, 0};
                ((GridBagLayout)toolBar.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout)toolBar.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                //---- buttonUpdate ----
                buttonUpdate.setText(bundle.getString("MainForm.buttonUpdate.text"));
                buttonUpdate.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        buttonUpdateActionPerformed(e);
                    }
                });
                toolBar.add(buttonUpdate, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- cBTags ----
                cBTags.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cBTagsActionPerformed(e);
                    }
                });
                toolBar.add(cBTags, new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
                toolBar.add(progressBar1, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- reFresh ----
                reFresh.setText("Refresh");
                reFresh.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        reFreshActionPerformed(e);
                    }
                });
                toolBar.add(reFresh, new GridBagConstraints(24, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
            }
            panelMain.add(toolBar, CC.xywh(1, 1, 3, 1));

            //======== scrollPane1 ========
            {
                scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                //---- jAuthorList ----
                jAuthorList.setComponentPopupMenu(null);
                jAuthorList.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        jAuthorListMouseClicked(e);
                    }
                });
                scrollPane1.setViewportView(jAuthorList);
            }
            panelMain.add(scrollPane1, CC.xy(1, 2));

            //======== bookScrolPanel ========
            {
                bookScrolPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                bookScrolPanel.setDoubleBuffered(true);
                bookScrolPanel.setAutoscrolls(true);
                bookScrolPanel.setComponentPopupMenu(null);

                //======== bookPanel ========
                {
                    bookPanel.setLayout(new GridBagLayout());
                    ((GridBagLayout)bookPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
                    ((GridBagLayout)bookPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                    ((GridBagLayout)bookPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)bookPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
                }
                bookScrolPanel.setViewportView(bookPanel);
            }
            panelMain.add(bookScrolPanel, CC.xy(3, 2));
        }
        contentPane.add(panelMain, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());

        //======== bookPopup ========
        {

            //---- menuItem3 ----
            menuItem3.setText("Booktext1");
            bookPopup.add(menuItem3);

            //---- menuItem4 ----
            menuItem4.setText("BookText2");
            bookPopup.add(menuItem4);
        }

        //======== authorPopup ========
        {

            //---- menuAuthorMakeRead ----
            menuAuthorMakeRead.setText(bundle.getString("MainForm.authorMenu.makeRead"));
            menuAuthorMakeRead.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    menuAuthorMakeReadActionPerformed(e);
                }
            });
            authorPopup.add(menuAuthorMakeRead);

            //---- menuAuthorDelete ----
            menuAuthorDelete.setText(bundle.getString("MainForm.menuAuthorDelete.text"));
            menuAuthorDelete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    menuAuthorDeleteActionPerformed(e);
                }
            });
            authorPopup.add(menuAuthorDelete);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem menuItemSettings;
    private JMenuItem menuItemExit;
    private JMenu menuTools;
    private JMenuItem menuToolsAdd;
    private JPanel panelMain;
    private JPanel toolBar;
    private JButton buttonUpdate;
    private JComboBox<ComboItem> cBTags;
    private JProgressBar progressBar1;
    private JButton reFresh;
    private JScrollPane scrollPane1;
    private JList jAuthorList;
    private JScrollPane bookScrolPanel;
    private JPanel bookPanel;
    private JPopupMenu bookPopup;
    private JMenuItem menuItem3;
    private JMenuItem menuItem4;
    private JPopupMenu authorPopup;
    private JMenuItem menuAuthorMakeRead;
    private JMenuItem menuAuthorDelete;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


}
