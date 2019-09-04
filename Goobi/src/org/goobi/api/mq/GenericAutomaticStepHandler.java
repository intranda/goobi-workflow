package org.goobi.api.mq;

import org.goobi.beans.Step;
import org.goobi.production.enums.PluginReturnValue;

import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.persistence.managers.StepManager;

public class GenericAutomaticStepHandler implements TicketHandler<PluginReturnValue> {

    public static String HANDLERNAME = "generic_automatic_step";

    @Override
    public String getTicketHandlerName() {
        return HANDLERNAME;
    }

    @Override
    public PluginReturnValue call(TaskTicket ticket) {
        Step step = StepManager.getStepById(ticket.getStepId());
        try {
            ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(step);
            myThread.run();
        } catch (Exception e) {
            return PluginReturnValue.ERROR;
        }
        return PluginReturnValue.FINISH;
    }

}
