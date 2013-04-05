/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
package org.goobi.webapi.dao;

import org.goobi.beans.Process;
import org.goobi.beans.Step;

import de.sub.goobi.helper.Helper;
import org.apache.log4j.Logger;
import org.goobi.webapi.beans.GoobiProcess;
import org.goobi.webapi.beans.GoobiProcessStep;
import org.goobi.webapi.beans.IdentifierPPN;

import java.util.ArrayList;
import java.util.List;

public class GoobiProcessDAO {

    private static final Logger myLogger = Logger.getLogger(GoobiProcessDAO.class);

    public static GoobiProcess getProcessByPPN(IdentifierPPN PPN) {
        GoobiProcess result = null;
//        Session session;
//
//        session = Helper.getHibernateSession();
//
//        try {
//
//            Criteria criteria = session
//                    .createCriteria(Process.class)
//                    .createAlias("vorlagen", "v")
//                    .createAlias("vorlagen.eigenschaften", "ve")
//                    .createAlias("werkstuecke", "w")
//                    .createAlias("werkstuecke.eigenschaften", "we")
//                    .add(Restrictions.or(Restrictions.eq("we.titel", "PPN digital a-Satz"), Restrictions.eq("we.titel", "PPN digital f-Satz")))
//                    .add(Restrictions.eq("ve.titel", "Titel"))
//                    .add(Restrictions.eq("we.wert", PPN.toString()))
//                    .addOrder(Order.asc("we.wert"))
//                    .setProjection(Projections.projectionList()
//                            .add(Projections.property("we.wert"), "identifier")
//                            .add(Projections.property("ve.wert"), "title")
//                    )
//                    .setResultTransformer(Transformers.aliasToBean(GoobiProcess.class));
//
//            result = (GoobiProcess) criteria.uniqueResult();
//
//        } catch (HibernateException he) {
//            myLogger.error("Catched Hibernate exception: " + he.getMessage());
//        }

        return result;
    }

    public static List<GoobiProcess> getAllProcesses() {
        List<GoobiProcess> result;

        result = new ArrayList<GoobiProcess>();
//        Session session;
//        session = Helper.getHibernateSession();
//
//        try {
//
//            Criteria criteria = session
//                    .createCriteria(Process.class)
//                    .createAlias("vorlagen", "v")
//                    .createAlias("vorlagen.eigenschaften", "ve")
//                    .createAlias("werkstuecke", "w")
//                    .createAlias("werkstuecke.eigenschaften", "we")
//                    .add(Restrictions.or(Restrictions.eq("we.titel", "PPN digital a-Satz"), Restrictions.eq("we.titel", "PPN digital f-Satz")))
//                    .add(Restrictions.eq("ve.titel", "Titel"))
//                    .addOrder(Order.asc("we.wert"))
//                    .setProjection(Projections.projectionList()
//                            .add(Projections.property("we.wert"), "identifier")
//                            .add(Projections.property("ve.wert"), "title")
//                    )
//                    .setResultTransformer(Transformers.aliasToBean(GoobiProcess.class));
//
//            @SuppressWarnings(value = "unchecked")
//            List<GoobiProcess> list = (List<GoobiProcess>) criteria.list();
//
//            if ((list != null) && (list.size() > 0)) {
//                result.addAll(list);
//            }
//        } catch (HibernateException he) {
//            myLogger.error("Catched Hibernate exception: " + he.getMessage());
//        }

        return result;
    }

    public static List<GoobiProcessStep> getAllProcessSteps(IdentifierPPN PPN) {
        List<GoobiProcessStep> result;
        result = new ArrayList<GoobiProcessStep>();
//        Session session;
//
//        session = Helper.getHibernateSession();
//
//        try {
//
//            Criteria criteria = session
//                    .createCriteria(Step.class)
//                    .createAlias("prozess", "p")
//                    .createAlias("prozess.werkstuecke", "w")
//                    .createAlias("prozess.werkstuecke.eigenschaften", "we")
//                    .add(Restrictions.or(Restrictions.eq("we.titel", "PPN digital a-Satz"), Restrictions.eq("we.titel", "PPN digital f-Satz")))
//                    .add(Restrictions.eq("we.wert", PPN.toString()))
//                    .addOrder(Order.asc("reihenfolge"))
//                    .setProjection(Projections.projectionList()
//                            .add(Projections.property("reihenfolge"), "sequence")
//                            .add(Projections.property("bearbeitungsstatus"), "state")
//                            .add(Projections.property("titel"), "title")
//                    )
//                    .setResultTransformer(Transformers.aliasToBean(GoobiProcessStep.class));
//
//            @SuppressWarnings(value = "unchecked")
//            List<GoobiProcessStep> list = (List<GoobiProcessStep>) criteria.list();
//
//            if ((list != null) && (!list.isEmpty())) {
//                result.addAll(list);
//            }
//        } catch (HibernateException he) {
//            myLogger.error("Catched Hibernate exception: " + he.getMessage());
//        }

        return result;
    }

}
