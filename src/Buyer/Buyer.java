package Buyer;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;

/**
 * Created by Zixuan Wang on 2016-11-28.
 */
public class Buyer extends Agent
{
    //The title of the book to buy.
    private String targetBookTitle;

    //The count of the book to buy.
    private int targetBookCount;

    protected void setup()
    {
        System.out.println("Buyer " + getAID().getName() + " online.");
        //Get the title and count of the book to buy as a start-up argument.
        Object[] args = getArguments();
        if (args != null && args.length > 0)
        {
            targetBookTitle = (String) args[0];
            targetBookCount = (int) args[1];
            System.out.println("Trying to buy " + targetBookCount + " " + targetBookTitle);

            //Register the book-selling service in the yellow pages.
//            DFAgentDescription dfd = new DFAgentDescription();
//            dfd.setName(getAID());
//            ServiceDescription sd = new ServiceDescription();
//            sd.setType("Book-selling");
//            sd.setName(getLocalName() + "-Book-selling");
//            dfd.addServices(sd);
//            try
//            {
//                DFService.register(this, dfd);
//            } catch (FIPAException fe)
//            {
//                fe.printStackTrace();
//            }
        }
        else
        {
            //Terminate the agent immediately.
            System.out.println("No book title specified");
            doDelete();
        }

        //addBehaviour();

    }

    protected void takedown()
    {
        System.out.println("Buyer " + getAID().getName() + " terminated.");

        //De-register from the yellow pages.
        try
        {
            DFService.deregister(this);
        } catch (FIPAException fe)
        {
            fe.printStackTrace();
        }
    }

}