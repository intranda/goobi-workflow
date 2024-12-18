/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.goobiScript;

import java.util.Optional;

import de.sub.goobi.helper.Helper;
import lombok.extern.log4j.Log4j2;
import org.goobi.production.enums.GoobiScriptResultType;

import lombok.Setter;

@Log4j2
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
                try {
                    if (gsr.getCustomGoobiScriptImpl() != null) {
                        gsr.getCustomGoobiScriptImpl().execute(gsr);
                    } else {
                        Optional<IGoobiScript> goobiScript = gsm.getGoobiScriptForAction(gsr.getParameters().get("action"));
                        if (goobiScript.isPresent()) {
                            IGoobiScript gs = goobiScript.get();
                            gs.execute(gsr);
                        } else {
                            gsr.setResultMessage(String.format("Can't find GoobiScript for action %s", gsr.getParameters().get("action")));
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                        }
                    }
                    gsm.pushUpdateToUsers(false);
                } catch (Exception e) {
                    log.error("Exception during GoobiScript execution", e);
                    gsr.setResultMessage(String.format("Exception occurred during GoobiScript execution: %s", e.getMessage()));
                    gsr.setResultType(GoobiScriptResultType.ERROR);
                }
            });
            if (!next.isPresent()) {
                //we stop this thread - the GoobiScriptManager will start a new one.
                break;
            }
        }
        gsm.pushUpdateToUsers(true);

    }

}
