package Manager;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Zixuan Wang on 2016-12-11.
 * Manager GUI.
 */
class ManagerGui extends JFrame
{
    private Manager oneManager;
    private JTextArea storeInfo;

    ManagerGui()
    {
        //Make sure that you close the window, you terminate the agent.
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                oneManager.doDelete();
            }
        });
    }

    void setGui(Manager manager)
    {
        oneManager = manager;
        setSize(500, 800);
        setTitle(manager.getAID().getName());

        storeInfo = new JTextArea();
        add(storeInfo);
    }

    void setStoreInfo(String info)
    {
        storeInfo.setText(info);
    }

    void appendStoreInfo(String info)
    {
        String temp = storeInfo.getText();
        temp += "\n" + info;
        storeInfo.setText(temp);
    }
}