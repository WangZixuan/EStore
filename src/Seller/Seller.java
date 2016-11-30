package Seller;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.HashMap;

/**
 * Created by Zixuan Wang on 2016-11-28.
 */
public class Seller extends Agent
{
    private HashMap catalog;

    //GUI of Seller agent.
    private SellerGui gui;

    public Seller()
    {
        catalog = new HashMap();
        gui=new SellerGui(this);
        gui.setVisible(true);
        addBehaviour(new SellerBehaviour());
    }

    protected void setup()
    {
        System.out.println("Buyer " + getAID().getName() + " online.");
    }

    protected void takedown()
    {
        System.out.println("Buyer " + getAID().getName() + " terminated.");
    }

    private class SellerBehaviour extends Behaviour
    {
        @Override
        public void action()
        {
            ACLMessage msg=gui.oneSeller.receive();

            //Deal with received message.
            if (null!=msg)
            {

            }
        }

        @Override
        public boolean done()
        {
            return false;
        }
    }
}
