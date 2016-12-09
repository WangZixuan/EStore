package Seller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Zixuan Wang on 2016-11-30.
 */
class SellerGui extends JFrame
{
    Seller oneSeller;
    JTextArea booksList;
    JTextArea logArea;

    SellerGui()
    {
        //Make sure that you close the window, you terminate the agent.
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                oneSeller.doDelete();
            }
        });
    }

    public void setGui(Seller seller)
    {
        oneSeller = seller;

        setSize(500, 800);
        setTitle(seller.getAID().getName());

        //AddBook Panel.
        JPanel addPanel = new JPanel();
        addPanel.setLayout(new GridLayout(0, 2));

        JLabel addLabel = new JLabel("Add a new book:");
        addPanel.add(addLabel);
        JLabel emptyLabel1 = new JLabel();
        addPanel.add(emptyLabel1);

        JLabel titleLabel = new JLabel("Title:");
        JTextField titleText = new JTextField();
        addPanel.add(titleLabel);
        addPanel.add(titleText);

        JLabel countLabel = new JLabel("Count:");
        JTextField countText = new JTextField();
        addPanel.add(countLabel);
        addPanel.add(countText);

        JLabel priceLabel = new JLabel("Price:");
        JTextField priceText = new JTextField();
        addPanel.add(priceLabel);
        addPanel.add(priceText);

        JLabel emptyLabel2 = new JLabel();
        //Event listener for this button.
        JButton addButton = new JButton("add");
        addPanel.add(emptyLabel2);
        addPanel.add(addButton);

        add(addPanel, BorderLayout.NORTH);

        //ListBooks Panel.
        JPanel listPanel = new JPanel();
        booksList = new JTextArea();
        booksList.setEditable(false);
        listPanel.add(booksList);

        add(listPanel, BorderLayout.CENTER);

        //Log Panel.
        JPanel logPanel=new JPanel();
        logArea=new JTextArea();
        logPanel.add(logArea);

        add(logPanel, BorderLayout.SOUTH);

        //Event listener for addButton.
        addButton.addActionListener(e ->
        {
            String title = titleText.getText();
            int count = Integer.parseInt(countText.getText());
            int price = Integer.parseInt(priceText.getText());
            oneSeller.addBook(title, count, price);
        });

    }

    void setBooksList(String list)
    {
        booksList.setText(list);
    }

    void setLog(String log)
    {
        logArea.setText(log);
    }
}
