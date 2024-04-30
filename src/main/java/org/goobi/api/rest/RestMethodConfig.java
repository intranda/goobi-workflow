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
package org.goobi.api.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.configuration.SubnodeConfiguration;

import lombok.Data;

@Data
public class RestMethodConfig {
    private String method;
    private boolean allAllowed;
    private Map<String, String> netmaskPasswordPairs;
    private Map<String, List<String>> customHeaders;

    public static RestMethodConfig fromSubnodeConfig(SubnodeConfiguration methodC) {
        RestMethodConfig conf = new RestMethodConfig();
        conf.setMethod(methodC.getString("@name"));
        Map<String, String> netmaskPasswordPairs = new TreeMap<>();
        List<?> allowedList = methodC.configurationsAt("allow");
        for (Object element : allowedList) {
            SubnodeConfiguration allowedC = (SubnodeConfiguration) element;
            String ip = allowedC.getString("@netmask");
            String pw = allowedC.getString("@token");
            if (ip == null && pw == null) {
                conf.setAllAllowed(true);
                continue;
            }
            if (ip == null) {
                ip = "0.0.0.0/0";
            }
            netmaskPasswordPairs.put(ip, pw);
        }
        conf.setNetmaskPasswordPairs(netmaskPasswordPairs);
        Map<String, List<String>> customHeaders = new TreeMap<>();
        List<?> headerList = methodC.configurationsAt("header");
        for (Object element : headerList) {
            SubnodeConfiguration headerC = (SubnodeConfiguration) element;
            String name = headerC.getString("@name");
            if (name == null) {
                continue;
            }
            List<String> values = Arrays.asList(headerC.getStringArray("value"));
            customHeaders.put(name, values);
        }
        conf.setCustomHeaders(customHeaders);
        return conf;
    }
}
