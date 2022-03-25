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
        for (int i = 0; i < allowedList.size(); i++) {
            SubnodeConfiguration allowedC = (SubnodeConfiguration) allowedList.get(i);
            String ip = allowedC.getString("@netmask");
            String pw = allowedC.getString("@token", "");
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
        for (int i = 0; i < headerList.size(); i++) {
            SubnodeConfiguration headerC = (SubnodeConfiguration) headerList.get(i);
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
