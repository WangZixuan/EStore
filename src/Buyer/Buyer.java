package Buyer;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Created by Zixuan Wang on 2016-11-28.
 */
public class Buyer extends Agent
{
    private Map<String, Integer> catalog;

    private int money;

    //The list of known seller agents.
    private Vector allSellers;

    private BuyerGui gui;

    Buyer()
    {
        catalog = new TreeMap<>();
        money = 1000;
        allSellers = new Vector();
    }

    protected void setup()
    {
        System.out.println("Buyer " + getAID().getName() + " online.");

        gui = new BuyerGui();
        gui.setGui(this);
        gui.setVisible(true);

        //Update the list of seller agents every ten seconds.
        addBehaviour(new TickerBehaviour(this, 10000)
        {
            protected void onTick()
            {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("sellers");
                template.addServices(sd);
                try
                {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    allSellers.clear();
                    for (DFAgentDescription aResult : result)
                        allSellers.addElement(aResult.getName());
                }
                catch (FIPAException fe)
                {
                    fe.printStackTrace();
                }
            }
        });

    }

    protected void takedown()
    {
        System.out.println("Buyer " + getAID().getName() + " terminated.");

        if (null != gui)
            gui.dispose();

        //De-register from the YellowPage.

        try
        {
            DFService.deregister(this);
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }
    }

    void buyBook(String title, int count)
    {
        addBehaviour(new BookNegotiator(title, count));
        setListText();
    }

    private void setListText()
    {
        String result = "Now I have:\n\nTitle\t\tCount\n";

        for (Map.Entry<String, Integer> entry : catalog.entrySet())
        {
            String line = "\n";
            line += entry.getKey();
            line += "\t\t";
            line += String.valueOf(entry.getValue());

            result += line;
        }

        gui.setBooksList(result);
    }

    private class BookNegotiator extends Behaviour
    {
        BookNegotiator(String title, int count)
        {

        }

        @Override
        public void action()
        {

        }

        @Override
        public boolean done()
        {
            return true;
        }
    }


}