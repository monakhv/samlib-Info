/*
 * Created by JFormDesigner on Fri Feb 13 18:11:31 MSK 2015
 */

package monakhv.samlib.desk.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.desk.Main;
import monakhv.samlib.desk.data.Settings;
import monakhv.samlib.desk.sql.AuthorController;
import monakhv.samlib.desk.sql.BookController;
import monakhv.samlib.log.Log;

/**
 * @author Dmitry Monakhov
 */
public class MainForm extends JFrame {
    private static final String DEBUG_TAG="MainForm";
    private final DefaultListModel<Author> authorsModel;
    private final DefaultListModel<Book> booksModel;

    private final SQLController sql;
    private Settings settings;

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
        booksModel = new DefaultListModel<>();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                Main.exit(1);
            }
        });

        initComponents();


        addSortedAuthorList();
        jAuthorList.setModel(authorsModel);
        jAuthorList.setCellRenderer(new AuthorRenderer());
        jAuthorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jBookList.setModel(booksModel);
        jBookList.setCellRenderer(new BookRenderer());
        jBookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
                Author a =authorsModel.get(lsm.getMaxSelectionIndex());
                Log.i(DEBUG_TAG, "selection " +a.getName()+"  "+e.getValueIsAdjusting());
                loadBookList(a);


            }
        });
    }

    private void addSortedAuthorList() {
        AuthorController ctl = new AuthorController(sql);
         authorsModel.removeAllElements();


        for (Author a : ctl.getAll(null, SQLController.COL_NAME) ){
            authorsModel.addElement(a);
        }

    }
    private void loadBookList(Author a){
        BookController ctl = new BookController(sql);
        booksModel.removeAllElements();


        for (Book book : ctl.getAll(a,null) ){
            booksModel.addElement(book);
        }

    }


    private void menuItemExitActionPerformed(ActionEvent e) {
        Main.exit(0);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        menuItem1 = new JMenuItem();
        menuItemExit = new JMenuItem();
        panelMain = new JPanel();
        scrollPane1 = new JScrollPane();
        jAuthorList = new JList();
        scrollPane2 = new JScrollPane();
        jBookList = new JList();

        //======== this ========
        setMinimumSize(new Dimension(20, 70));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== menuBar1 ========
        {

            //======== menu1 ========
            {
                menu1.setText("File");

                //---- menuItem1 ----
                menuItem1.setText("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438");
                menu1.add(menuItem1);

                //---- menuItemExit ----
                menuItemExit.setText("Exit");
                menuItemExit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        menuItemExitActionPerformed(e);
                    }
                });
                menu1.add(menuItemExit);
            }
            menuBar1.add(menu1);
        }
        setJMenuBar(menuBar1);

        //======== panelMain ========
        {
            panelMain.setMinimumSize(new Dimension(800, 100));
            panelMain.setBorder(Borders.DLU4);
            panelMain.setLayout(new FormLayout(
                "[200dlu,default]:grow, 5dlu, [400dlu,default]:grow(0.8), default",
                "fill:[400dlu,default]:grow"));

            //======== scrollPane1 ========
            {
                scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane1.setViewportView(jAuthorList);
            }
            panelMain.add(scrollPane1, CC.xy(1, 1));

            //======== scrollPane2 ========
            {
                scrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane2.setAutoscrolls(true);
                scrollPane2.setViewportView(jBookList);
            }
            panelMain.add(scrollPane2, CC.xy(3, 1));
        }
        contentPane.add(panelMain, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem menuItem1;
    private JMenuItem menuItemExit;
    private JPanel panelMain;
    private JScrollPane scrollPane1;
    private JList jAuthorList;
    private JScrollPane scrollPane2;
    private JList jBookList;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
