package Seller;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Zixuan Wang on 2016-11-28.
 */
public class Seller extends Agent
{
    private Map<String, ArrayList<Integer>> catalog;

    //GUI of Seller agent.
    private SellerGui gui;

    public Seller()
    {
        catalog = new TreeMap<>();

        addBehaviour(new SellerBehaviour());
    }

    protected void setup()
    {
        System.out.println("Buyer " + getAID().getName() + " online.");

        gui = new SellerGui();
        gui.setGui(this);
        gui.setVisible(true);

        //Register in YellowPage.
    }

    protected void takedown()
    {
        System.out.println("Buyer " + getAID().getName() + " terminated.");

        if (null != gui)
            gui.dispose();

        //De-register from the YellowPage.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("sellers");
        sd.setName(getLocalName()+"-seller");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

    }

    void addBook(String title, int count, int price)
    {
        ArrayList<Integer> infoList = new ArrayList<>(2);

        //If this book existed.
        if (catalog.containsKey(title))
            count+=catalog.get(title).get(0);

        infoList.add(count);
        infoList.add(price);
        catalog.put(title, infoList);
        setListText();
    }

    void setListText()
    {
        String result = "Title\t\tCount\t\tPrice\n";

        for (Map.Entry<String, ArrayList<Integer>> entry : catalog.entrySet())
        {
            String line = "\n";
            line += entry.getKey();
            line+="\t\t";
            ArrayList<Integer> lineInfo = entry.getValue();
            line+=String.valueOf(lineInfo.get(0));
            line+="\t\t";
            line+=String.valueOf(lineInfo.get(1));

            result+=line;
        }


        gui.setBooksList(result);
    }

    private class SellerBehaviour extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            ACLMessage msg = gui.oneSeller.receive();

            //Deal with received message.
            if (null != msg)
            {
                ACLMessage reply = msg.createReply();

            }
            else
                block();
        }


    }
}
