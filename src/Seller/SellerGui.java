package Seller;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Zixuan Wang on 2016-11-30.
 */
class SellerGui extends JFrame
{
    Seller oneSeller;

    SellerGui(Seller seller)
    {
        oneSeller=seller;
        setGui();
    }

    private void setGui()
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
}
