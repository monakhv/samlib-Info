/*
 * Created by JFormDesigner on Tue Feb 17 18:22:43 MSK 2015
 */

package monakhv.samlib.desk.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.log.Log;

/**
 * @author Dmitry Monakhov
 */
public class BookRow extends JPanel {
    private static final String DEBUG_TAG="BookRow";

    private final static ImageIcon GREEN_ICON = new ImageIcon(AuthorRenderer.class.getResource("/pics/16x16/bullet_green.png"));
    private final static ImageIcon BLACK_ICON = new ImageIcon(AuthorRenderer.class.getResource("/pics/16x16/bullet_black.png"));

    private Book book;


    public BookRow(Book book) {
        initComponents();
        scrollPane1.removeMouseWheelListener(scrollPane1.getMouseWheelListeners()[0]);
        load(book);
    }


    private void load (Book book){
        this.book = book;
        title.setText("<html>"+book.getTitle()+"</html>");
        description.setContentType("text/html");
        Log.d(DEBUG_TAG, "description: " + book.getDescription() + "<");
        try {
            description.setText("<html>"+book.getDescription()+"</html>");
        }
        catch (Exception e ){
            description.setContentType("text/plain");
            description.setText(book.getDescription());
        }

        size.setText(book.getSize()+"K");
        form.setText(book.getForm());
        if (book.isIsNew()){
            newIcon.setIcon(GREEN_ICON);
        }
        else {
            newIcon.setIcon(BLACK_ICON);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        newIcon = new JLabel();
        panel1 = new JPanel();
        title = new JLabel();
        panel2 = new JPanel();
        size = new JLabel();
        form = new JLabel();
        scrollPane1 = new JScrollPane();
        description = new JTextPane();

        //======== this ========
        setBorder(new EtchedBorder(EtchedBorder.RAISED));
        setLayout(new FormLayout(
            "pref, $lcgap, [pref,250dlu]:grow",
            "top:pref:grow"));
        add(newIcon, CC.xy(1, 1));

        //======== panel1 ========
        {
            panel1.setLayout(new GridBagLayout());
            ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 52, 0};
            ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
            ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

            //---- title ----
            title.setText("Titlle is here");
            title.setFont(title.getFont().deriveFont(title.getFont().getSize() + 6f));
            panel1.add(title, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //======== panel2 ========
            {
                panel2.setLayout(new GridBagLayout());
                ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 10, 0};
                ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0};
                ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                //---- size ----
                size.setText("text");
                panel2.add(size, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- form ----
                form.setText("text");
                panel2.add(form, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 0, 5), 0, 0));
            }
            panel1.add(panel2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== scrollPane1 ========
            {
                scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane1.setBorder(null);
                scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                scrollPane1.setWheelScrollingEnabled(false);

                //---- description ----
                description.setContentType("text/html");
                description.setEditable(false);
                scrollPane1.setViewportView(description);
            }
            panel1.add(scrollPane1, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        add(panel1, CC.xy(3, 1));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel newIcon;
    private JPanel panel1;
    private JLabel title;
    private JPanel panel2;
    private JLabel size;
    private JLabel form;
    private JScrollPane scrollPane1;
    private JTextPane description;


    // JFormDesigner - End of variables declaration  //GEN-END:variables



    public void setCallBackClickListener(final BookList.CallBack callBackClickListener) {

        panel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                callBackClickListener.bookClick(e, book);
            }
        });
        panel2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                callBackClickListener.bookClick(e, book);
            }
        });
        description.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                callBackClickListener.bookClick(e, book);
            }
        });

    }
}
