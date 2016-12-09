package Buyer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Zixuan Wang on 2016-12-09.
 */
public class BuyerGui extends JFrame
{
    Buyer oneBuyer;
    JTextArea booksList;

    BuyerGui()
    {
        //Make sure that you close the window, you terminate the agent.
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                oneBuyer.doDelete();
            }
        });
    }

    public void setGui(Buyer buyer)
    {
        oneBuyer=buyer;

        setSize(500,500);
        setTitle(buyer.getAID().getName());

        //Buybook Panel.
        JPanel buyPanel=new JPanel();
        buyPanel.setLayout(new GridLayout(0, 2));

        JLabel buyLabel=new JLabel("I want to buy:");
        buyPanel.add(buyLabel);
        JLabel emptyLabel1 = new JLabel();
        buyPanel.add(emptyLabel1);

        JLabel titleLabel = new JLabel("Title:");
        JTextField titleText = new JTextField();
        buyPanel.add(titleLabel);
        buyPanel.add(titleText);

        JLabel countLabel = new JLabel("Count:");
        JTextField countText = new JTextField();
        buyPanel.add(countLabel);
        buyPanel.add(countText);

        JLabel emptyLabel2 = new JLabel();
        //Event listener for this button.
        JButton buyButton = new JButton("buy");
        buyPanel.add(emptyLabel2);
        buyPanel.add(buyButton);

        add(buyPanel, BorderLayout.NORTH);

        //ListBooks Panel.
        JPanel listPanel = new JPanel();
        booksList = new JTextArea();
        booksList.setEditable(false);
        listPanel.add(booksList);

        add(listPanel, BorderLayout.CENTER);


        //Event listener for addButton.
        buyButton.addActionListener(e ->
        {
            String title = titleText.getText();
            int count = Integer.parseInt(countText.getText());
            oneBuyer.buyBook(title, count);
        });

    }

    void setBooksList(String list)
    {
        booksList.setText(list);
    }
}
