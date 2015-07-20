/*
 * Created by JFormDesigner on Mon Jul 20 17:19:00 MSK 2015
 */

package monakhv.samlib.desk.gui;

import javax.swing.border.*;
import monakhv.samlib.db.entity.AuthorCard;

import java.awt.*;
import javax.swing.*;

/**
 * @author Dmitry Monakhov
 */
public class AuthorCardRow extends JPanel {
    public AuthorCardRow() {
        initComponents();
    }
    public void load(AuthorCard authorCard){
        name.setText(authorCard.getName());
        title.setText(authorCard.getTitle());
        url.setText(authorCard.getUrl());


        try {
            description.setContentType("text/html");
            description.setText("<html>"+authorCard.getDescription()+"</html>");
        }
        catch (Exception e ){
            description.setContentType("text/plain");
            description.setText(authorCard.getDescription());
        }


        String ss = Integer.toString(authorCard.getSize()) + "K/" + Integer.toString(authorCard.getCount());
        sizeCount.setText(ss);

    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        name = new JLabel();
        sizeCount = new JLabel();
        url = new JLabel();
        title = new JLabel();
        scrollPane1 = new JScrollPane();
        description = new JTextPane();

        //======== this ========
        setBorder(new EtchedBorder());
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 1.0E-4};

        //---- name ----
        name.setText("text");
        name.setFont(name.getFont().deriveFont(name.getFont().getSize() + 6f));
        add(name, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- sizeCount ----
        sizeCount.setText("text");
        add(sizeCount, new GridBagConstraints(1, 0, 1, 3, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- url ----
        url.setText("text");
        add(url, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- title ----
        title.setText("text");
        add(title, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //======== scrollPane1 ========
        {
            scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scrollPane1.setBorder(null);
            scrollPane1.setWheelScrollingEnabled(false);

            //---- description ----
            description.setContentType("text/html");
            scrollPane1.setViewportView(description);
        }
        add(scrollPane1, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel name;
    private JLabel sizeCount;
    private JLabel url;
    private JLabel title;
    private JScrollPane scrollPane1;
    private JTextPane description;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
