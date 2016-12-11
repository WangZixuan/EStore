package Buyer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

enum Move{SEND, ACK, PAY, TELL, END}

/**
 * Created by Zixuan Wang on 2016-11-28.
 * Buyer agent.
 */
public class Buyer extends Agent
{
    private AID manager;

    //The list of known seller agents.
    private Vector<AID> allSellers;

    private Map<String, Integer> catalog;

    private int money;

    private BuyerGui gui;

    private String orderInfo;

    public Buyer()
    {
        catalog = new TreeMap<>();
        money = 1000;
        allSellers = new Vector<>();
        orderInfo = "";
    }

    @Override
    protected void setup()
    {
        System.out.println("Buyer " + getAID().getName() + " online.");

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
                            cfp.setContent("Buyer " + getAID().getName() + " online.");
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


        //Get sellers.
        addBehaviour(
                new OneShotBehaviour()
                {
                    @Override
                    public void action()
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
                                allSellers.add(aResult.getName());
                        }
                        catch (FIPAException fe)
                        {
                            fe.printStackTrace();
                        }
                    }
                }
        );


        gui = new BuyerGui();
        gui.setGui(this);
        gui.setVisible(true);

        //Update the list of seller agents every minute.
        addBehaviour(
                new TickerBehaviour(this, 60000)
                {
                    @Override
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
                                allSellers.add(aResult.getName());
                        }
                        catch (FIPAException fe)
                        {
                            fe.printStackTrace();
                        }
                    }
                });

    }

    @Override
    protected void takeDown()
    {
        System.out.println("Buyer " + getAID().getName() + " terminated.");

        if (null != gui)
            gui.dispose();
    }

    void buyBook(String title, int count)
    {
        addBehaviour(new BuyerDecision(title, count));
        setListText();
    }

    private void setListText()
    {
        String result = "Now I have:\nTitle\t\tCount";

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

    private class BuyerDecision extends Behaviour
    {
        private String title;
        private int count;
        private int replyCount = 0;

        Vector<AID> allSellersVec;
        Vector<Integer> allPriceVec;
        Vector<Integer> allCountsVec;

        Move move;
        private MessageTemplate mt;
        String log;

        BuyerDecision(String t, int c)
        {
            title = t;
            count = c;
            move = Move.SEND;

            allSellersVec = new Vector<>();
            allPriceVec = new Vector<>();
            allCountsVec = new Vector<>();

            log = "";
        }

        @Override
        public void action()
        {

            switch (move)
            {
                case SEND:
                    //Send the title of the book ths buyer wants.
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < allSellers.size(); ++i)
                        cfp.addReceiver(allSellers.elementAt(i));
                    cfp.setContent(title);
                    cfp.setConversationId("book-trade");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); //A unique value
                    send(cfp);
                    mt = MessageTemplate.and(
                            MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    move = Move.ACK;
                    break;

                case ACK:
                    //Here this buyer receives all info and decide which to buy.
                    ACLMessage reply = receive(mt);

                    if (null != reply)
                    {
                        if (reply.getPerformative() == ACLMessage.PROPOSE)
                        {
                            //Format:title#count+price
                            String[] info = reply.getContent().split("#");
                            allSellersVec.add(reply.getSender());
                            allCountsVec.add(Integer.parseInt(info[1]));
                            allPriceVec.add(Integer.parseInt(info[2]));
                        }
                        ++replyCount;

                        if (replyCount >= allSellers.size())
                        {
                            int decision = decideWhatToBuy();
                            if (1 == decision)
                                log += "Not enough money.\n";
                            else if (2 == decision)
                                log += "You are buying too many!\n";
                            move = Move.PAY;
                        }
                    }
                    else
                        block();
                    break;

                case PAY:
                    //Send to all the sellers in oderInfo.
                    String[] sellersInfoForThis = orderInfo.split(";");

                    for (String oneSellerInfo : sellersInfoForThis)
                    {
                        String[] details = oneSellerInfo.split("#");
                        AID chosenSeller = allSellersVec.elementAt(Integer.parseInt(details[0]));
                        int chosenCount = Integer.parseInt(details[1]);

                        //Send to chosen seller.
                        ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        order.addReceiver(chosenSeller);
                        order.setContent(title + "#" + chosenCount);
                        order.setConversationId("book-trade");
                        send(order);

                        //Update catalog.
                        if (catalog.containsKey(title))
                        {
                            int existed = catalog.get(title);
                            existed += chosenCount;
                            catalog.replace(title, existed);
                        }
                        else
                            catalog.put(title, chosenCount);
                        log += "Buy " + chosenCount + " " + title + " from " + chosenSeller.getName() + ".\n";
                    }
                    gui.setLog(log + "You have " + money + " yuan left.");
                    setListText();
                    move = Move.TELL;
                    break;
                //We omit the sellers' confirmations step.
                case TELL:
                    //Tell manager what I buy.
                    addBehaviour(new TellManager(log));
                    move = Move.END;
                    break;
                case END:
                    break;

            }
        }

        @Override
        public boolean done()
        {
            return Move.END == move;
        }

        /**
         * @return 0: done, 1: not enough money, 2: not enough books
         */
        int decideWhatToBuy()
        {
            int orderedCount = 0;
            while (orderedCount < count)
            {
                int minIndex = allPriceVec.indexOf(Collections.min(allPriceVec));

                if (Integer.MAX_VALUE == allPriceVec.elementAt(minIndex))
                    return 2;

                if (allCountsVec.elementAt(minIndex) + orderedCount <= count)
                {
                    money -= allCountsVec.elementAt(minIndex) * allPriceVec.elementAt(minIndex);
                    orderedCount += allCountsVec.elementAt(minIndex);
                    orderInfo += String.valueOf(minIndex) + "#" + String.valueOf(allCountsVec.elementAt(minIndex)) + ";";
                    allPriceVec.setElementAt(Integer.MAX_VALUE, minIndex);//Set its price to max.
                }
                else//What the buyer need is fewer than this seller has.
                {
                    money -= (count - orderedCount) * allPriceVec.elementAt(minIndex);
                    orderInfo += String.valueOf(minIndex) + "#" + String.valueOf(count - orderedCount) + ";";
                    orderedCount = count;
                }

                if (money < 0)
                    return 1;
            }

            return 0;
        }

        private class TellManager extends OneShotBehaviour
        {
            String log;

            TellManager(String inputLog)
            {
                log = inputLog;
            }

            @Override
            public void action()
            {
                ACLMessage cfp = new ACLMessage(ACLMessage.SUBSCRIBE);

                cfp.addReceiver(manager);
                cfp.setContent(log);
                cfp.setConversationId("book-trade");
                cfp.setReplyWith("cfp" + System.currentTimeMillis()); //A unique value
                send(cfp);

            }

        }
    }

}