package Seller;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Zixuan Wang on 2016-11-28.
 * Seller agent.
 */
public class Seller extends Agent
{
    private AID manager;

    private Map<String, ArrayList<Integer>> catalog;

    //GUI of Seller agent.
    private SellerGui gui;

    public Seller()
    {
        catalog = new TreeMap<>();

        addBehaviour(new SellerCalculation());

        addBehaviour(new SellerDecision());

    }

    @Override
    protected void setup()
    {
        System.out.println("Seller " + getAID().getName() + " online.");

        //Get manager and register to it.
        addBehaviour(
                new OneShotBehaviour()
                {
                    @Override
                    public void action()
                    {
                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();
                        sd.setType("managers");
                        template.addServices(sd);
                        try
                        {
                            DFAgentDescription[] result = DFService.search(myAgent, template);
                            for (DFAgentDescription aResult : result)
                                manager = aResult.getName();

                            ACLMessage cfp = new ACLMessage(ACLMessage.SUBSCRIBE);

                            cfp.addReceiver(manager);
                            cfp.setContent("Seller " + getAID().getName() + " online.");
                            cfp.setConversationId("book-trade");
                            cfp.setReplyWith("cfp" + System.currentTimeMillis()); //A unique value
                            send(cfp);
                        }
                        catch (FIPAException fe)
                        {
                            fe.printStackTrace();
                        }
                    }
                }
        );

        gui = new SellerGui();
        gui.setGui(this);
        gui.setVisible(true);

        //Register in the YellowPage.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("sellers");
        sd.setName(getLocalName() + "-selling-books");
        dfd.addServices(sd);
        try
        {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }
    }

    @Override
    protected void takeDown()
    {
        System.out.println("Seller " + getAID().getName() + " terminated.");

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

    void addBook(String title, int count, int price)
    {
        ArrayList<Integer> infoList = new ArrayList<>(2);

        //If this book existed.
        if (catalog.containsKey(title))
            count += catalog.get(title).get(0);

        infoList.add(count);
        infoList.add(price);
        catalog.put(title, infoList);
        setListText();
    }

    private void setListText()
    {
        String result = "Title\t\tCount\t\tPrice\n";

        for (Map.Entry<String, ArrayList<Integer>> entry : catalog.entrySet())
        {
            String line = "\n";
            line += entry.getKey();
            line += "\t\t";
            ArrayList<Integer> lineInfo = entry.getValue();
            line += String.valueOf(lineInfo.get(0));
            line += "\t\t";
            line += String.valueOf(lineInfo.get(1));

            result += line;
        }


        gui.setBooksList(result);
    }

    private class SellerCalculation extends CyclicBehaviour
    {
        private MessageTemplate mt;

        SellerCalculation()
        {
            mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        }

        @Override
        public void action()
        {
            ACLMessage msg = receive(mt);
            if (null != msg)
            {
                ACLMessage reply = msg.createReply();
                //Deal with received message, a buyer asks for info about a book.
                String title = msg.getContent();
                String myReply = title + "#";
                //Search in catalog.
                if (catalog.containsKey(title))
                {
                    myReply += String.valueOf(catalog.get(title).get(0)) + "#";
                    myReply += String.valueOf(catalog.get(title).get(1));
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(myReply);
                }
                else
                {
                    myReply += "0#0";
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent(myReply);
                }
                send(reply);

            }
            else
                block();
        }
    }

    private class SellerDecision extends CyclicBehaviour
    {
        private MessageTemplate mt;

        SellerDecision()
        {
            mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }

        @Override
        public void action()
        {
            ACLMessage msg = receive(mt);

            //Deal with received message.
            if (null != msg)
            {
                //Buyer sends a message to me that he will buy from me.
                //Remove the books from my catalog.
                String[] orderInfo = msg.getContent().split("#");
                ArrayList info = catalog.get(orderInfo[0]);

                //Count of the remaining books.
                int remaining = Integer.parseInt(info.get(0).toString()) - Integer.parseInt(orderInfo[1]);

                if (remaining < 0)
                    System.out.println("Critical error!");
                else
                {
                    info.set(0, remaining);
                    catalog.replace(orderInfo[0], info);

                    gui.setLog("Just sell " + orderInfo[1] + " " + orderInfo[0]);
                    setListText();
                }

            }
            else
                block();
        }

    }
}