/*
 * Created by JFormDesigner on Fri Feb 13 18:11:31 MSK 2015
 */

package monakhv.samlib.desk.gui;

import java.awt.*;
import java.awt.event.*;

import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.Tag;
import monakhv.samlib.desk.Main;
import monakhv.samlib.desk.data.DataExportImport;
import monakhv.samlib.desk.data.Settings;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.BookController;
import monakhv.samlib.desk.sql.DaoController;
import monakhv.samlib.db.TagController;
import monakhv.samlib.desk.workers.CheckUpdateWorker;
import monakhv.samlib.desk.workers.LoadBookWorker;
import monakhv.samlib.desk.workers.ReadAuthorWorker;
import monakhv.samlib.log.Log;
import monakhv.samlib.service.AuthorService;
import monakhv.samlib.service.GuiUpdate;

/**
 * @author Dmitry Monakhov
 */
public class MainForm extends JFrame implements GuiUpdate{
    private static final String DEBUG_TAG="MainForm";
    private ResourceBundle bndl = ResourceBundle.getBundle("samlibDesk");
    private final DefaultListModel<Author> authorsModel;
    private final AuthorService service;

    private final SQLController sql;
    private final Settings settings;
    private BookList bkList;
    //private String selection=null;
    private String sortOrder=SQLController.COL_isnew + " DESC, " + SQLController.COL_NAME;
    private Author selectedAuthor;
    private TagComboItem selectedTag= TagComboItem.ALL;
    private List<Author> authorList;
    private AuthorTagsDialog authorTags;

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
        authorTags = new AuthorTagsDialog(this,DaoController.getInstance(sql),this);


        initComponents();


        addSortedAuthorList();
        jAuthorList.setModel(authorsModel);
        jAuthorList.setCellRenderer(new AuthorRenderer());
        jAuthorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);




        createTagSelector();


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
                Log.i(DEBUG_TAG, "selection " +selectedAuthor.getName()+"  "+e.getValueIsAdjusting()+" - "+selectedAuthor.getTag2Authors().size());
                loadBookList(selectedAuthor);
            }
        });

        service = new AuthorService(DaoController.getInstance(sql),this,settings );

    }

    private void createTagSelector(){
        cBTags.removeAllItems();
        cBTags.addItem(TagComboItem.ALL);
        cBTags.addItem(TagComboItem.NEW);
        TagController tagCtl = new TagController(DaoController.getInstance(sql));
        for (Tag tag : tagCtl.getAll()){
            cBTags.addItem(new TagComboItem(tag));
        }
    }



    /**
     * Construct Author list
     */
    private void addSortedAuthorList() {
        AuthorController ctl = new AuthorController(DaoController.getInstance(sql));
         authorsModel.removeAllElements();

        authorList=ctl.getAll(selectedTag.getId(),sortOrder);



        for (Author a : authorList ){
            authorsModel.addElement(a);


        }

    }

    /**
     * Construct Book List
     * @param a author
     */
    private void loadBookList(Author a){
        BookController ctl = new BookController(DaoController.getInstance(sql));

        if (a != null){
            bkList.load(ctl.getAll(a, SQLController.COL_BOOK_ISNEW + " DESC, " + SQLController.COL_BOOK_DATE + " DESC"));
        }


        //bookPanel.setComponentPopupMenu(bookPopup);
        //redraw();



    }
    private void redraw(){
        this.getContentPane().validate();
        this.getContentPane().repaint();
    }


    private void menuItemExitActionPerformed(ActionEvent e) {
        Main.exit(0);
    }


    private void buttonUpdateActionPerformed(ActionEvent e) {
        if (authorList.isEmpty()){
            return;
        }
        buttonUpdate.setEnabled(false);
        progressBar1.setStringPainted(true);
        progressBar1.setMinimum(0);
        progressBar1.setMaximum(authorList.size());
        progressBar1.setValue(0);
        CheckUpdateWorker worker = new CheckUpdateWorker(service,authorList);
        worker.execute();

    }

    private void menuItemSettingsActionPerformed(ActionEvent e) {
        SettingsForm.show(this, settings);
    }

    private void cBTagsActionPerformed(ActionEvent e) {

        JComboBox cb = (JComboBox) e.getSource();
        selectedTag= (TagComboItem) cb.getSelectedItem();
        if (selectedTag == null){
            return;
        }
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
        Log.i(DEBUG_TAG, "Book: " + book.getTitle() + "  - " + e.getButton());
        if (e.getButton() == 1 ){
            book.setFileType(settings.getFileType());
            DataExportImport dd = new DataExportImport(settings);

            if (dd.needUpdateFile(book)){
                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                LoadBookWorker worker = new LoadBookWorker(service,book.getId());
                worker.execute();

            }
            else {
                showBook(book);
            }

        }
        else {
            bookPopup.show(e.getComponent(), e.getX(), e.getY());
        }


    }

    private void menuAuthorMakeReadActionPerformed(ActionEvent e) {
        ReadAuthorWorker worker = new ReadAuthorWorker(service,selectedAuthor);
        worker.execute();
    }

    private void menuAuthorDeleteActionPerformed(ActionEvent e) {
        int  answer = JOptionPane.showConfirmDialog(
                this,
                bndl.getString("MainForm.confirmAuthorDelete"),
                selectedAuthor.getName(),
                JOptionPane.YES_NO_OPTION
        );
        if (answer==JOptionPane.YES_OPTION){
            service.makeAuthorDel(selectedAuthor.getId());
            selectedAuthor = null;

        }
    }

    private void menuToolsAddActionPerformed(ActionEvent e) {
        AddTextValue addAuthor= new AddTextValue(this,"http://samlib.ot.ru",bndl.getString("MainForm.AddAuthor.Title.text"),
                new AddTextValue.CallBack(){

                    @Override
                    public void okClick(String answer) {
                        Log.i(DEBUG_TAG, "got value: " + answer);
                        ArrayList<String> urls = new ArrayList<>();
                        urls.add(answer);
                        service.makeAuthorAdd(urls);

                    }
                });
        addAuthor.setVisible(true);


    }

    private void menuAuthorTagsActionPerformed(ActionEvent e) {
        authorTags.setPanel(selectedAuthor);
        authorTags.setVisible(true);

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
        scrollPane1 = new JScrollPane();
        jAuthorList = new JList();
        panel1 = new JPanel();
        lbProgress = new JLabel();
        bookScrolPanel = new JScrollPane();
        bookPanel = new JPanel();
        bookPopup = new JPopupMenu();
        menuItem4 = new JMenuItem();
        menuItem3 = new JMenuItem();
        authorPopup = new JPopupMenu();
        menuAuthorTags = new JMenuItem();
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

            //======== panel1 ========
            {
                panel1.setBorder(new EtchedBorder());
                panel1.setLayout(new GridBagLayout());
                ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

                //---- lbProgress ----
                lbProgress.setText(bundle.getString("MainForm.lbProgress.text"));
                panel1.add(lbProgress, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
            }
            panelMain.add(panel1, CC.xywh(1, 4, 3, 1));

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

            //---- menuItem4 ----
            menuItem4.setText("BookText2");
            bookPopup.add(menuItem4);

            //---- menuItem3 ----
            menuItem3.setText("Booktext1");
            bookPopup.add(menuItem3);
        }

        //======== authorPopup ========
        {

            //---- menuAuthorTags ----
            menuAuthorTags.setText(bundle.getString("MainForm.menuAuthorTags.text"));
            menuAuthorTags.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    menuAuthorTagsActionPerformed(e);
                }
            });
            authorPopup.add(menuAuthorTags);

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
    private JComboBox<TagComboItem> cBTags;
    private JProgressBar progressBar1;
    private JScrollPane scrollPane1;
    private JList jAuthorList;
    private JPanel panel1;
    private JLabel lbProgress;
    private JScrollPane bookScrolPanel;
    private JPanel bookPanel;
    private JPopupMenu bookPopup;
    private JMenuItem menuItem4;
    private JMenuItem menuItem3;
    private JPopupMenu authorPopup;
    private JMenuItem menuAuthorTags;
    private JMenuItem menuAuthorMakeRead;
    private JMenuItem menuAuthorDelete;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    @Override
    public void makeUpdateAuthors() {
        Log.d(DEBUG_TAG,"makeUpdateAuthors");
        addSortedAuthorList();
        redraw();
    }

    @Override
    public void makeUpdateBooks() {
        loadBookList(selectedAuthor);
        redraw();
    }

    @Override
    public void makeUpdateTagList() {
        createTagSelector();
        redraw();
    }

    @Override
    public void finishBookLoad(boolean result, AbstractSettings.FileType ft, long book_id) {
        this.setCursor(Cursor.DEFAULT_CURSOR);
        if (result){
            AuthorController ctl = new AuthorController(DaoController.getInstance(sql));
            showBook(ctl.getBookController().getById(book_id));
        }
        else {
            showError("Book Load Error");
        }

    }

    @Override
    public void sendAuthorUpdateProgress(int total, int iCurrent, String name) {
        progressBar1.setValue(iCurrent);
        showError(name);
    }

    @Override
    public void finishUpdate(boolean result, List<Author> updatedAuthors) {
        buttonUpdate.setEnabled(true);
        progressBar1.setValue(0);
        if (! result){
            showError("Error update authors");
        }


    }

    @Override
    public void sendResult(String action, int numberOfAdded, int numberOfDeleted, int doubleAdd, int totalToAdd, long author_id) {

    }

    /**
     * Show book
     * @param book book to read
     */
    private void showBook(Book book){
        Log.i(DEBUG_TAG, "Display book: " + settings.getBookFile(book,book.getFileType()).getAbsolutePath());
        try {
            //TODO: put reader into setting for different File type
            Runtime.getRuntime().exec("/usr/bin/firefox "+settings.getBookFile(book,book.getFileType()).getAbsolutePath());
        } catch (IOException e) {
            Log.e(DEBUG_TAG,"Error Open Book");
        }
    }

    /**
     * Show error
     * @param msg message to display
     */
    private void showError(String msg){
        Log.e(DEBUG_TAG,msg);
        lbProgress.setText(msg);
    }



}
