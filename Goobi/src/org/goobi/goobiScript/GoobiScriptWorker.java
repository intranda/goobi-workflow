package org.goobi.goobiScript;

import java.util.Optional;

import org.goobi.production.enums.GoobiScriptResultType;

import lombok.Setter;

public class GoobiScriptWorker implements Runnable {
    private GoobiScriptManager gsm;
    @Setter
    private volatile boolean shouldStop;

    public GoobiScriptWorker(GoobiScriptManager gsm) {
        super();
        this.gsm = gsm;
    }

    @Override
    public void run() {
        while (!shouldStop && !Thread.interrupted()) {
            Optional<GoobiScriptResult> next = gsm.getNextScript();
            next.ifPresent(gsr -> {
                Optional<IGoobiScript> goobiScript = gsm.getGoobiScriptForAction(gsr.getParameters().get("action"));
                if (goobiScript.isPresent()) {
                    IGoobiScript gs = goobiScript.get();
                    gs.execute(gsr);
                } else {
                    gsr.setResultMessage(String.format("Can't find GoobiScript for action %s", gsr.getParameters().get("action")));
                    gsr.setResultType(GoobiScriptResultType.ERROR);
                }
                gsm.pushUpdateToUsers(false);
            });
            if (!next.isPresent()) {
                //we stop this thread - the GoobiScriptManager will start a new one.
                break;
            }
        }
        gsm.pushUpdateToUsers(true);

    }

}
