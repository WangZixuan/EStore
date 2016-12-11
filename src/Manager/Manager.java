package Manager;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Created by Zixuan Wang on 2016-12-10.
 * Manager class, show info of every move in this e-store.
 */
public class Manager extends Agent
{
    private ManagerGui gui;

    public Manager()
    {
        addBehaviour(new ManagerListener());
    }

    @Override
    protected void setup()
    {
        System.out.println("Manager " + getAID().getName() + " online.");

        gui = new ManagerGui();
        gui.setGui(this);
        gui.setVisible(true);

        //Register in the YellowPage.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("managers");
        sd.setName(getLocalName() + "-manager");
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
        System.out.println("Manager " + getAID().getName() + " terminated.");

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

    private class ManagerListener extends CyclicBehaviour
    {
        private MessageTemplate mt;

        ManagerListener()
        {
            mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
        }

        @Override
        public void action()
        {
            ACLMessage msg = receive(mt);
            if (null != msg)
            {
                String detail = msg.getContent();
                gui.appendStoreInfo(detail);
            }
            else
                block();
        }
    }
}