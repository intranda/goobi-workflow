package org.goobi.beans;

import lombok.Data;

@Data
public class SimpleAltoWord {
    private String id;
    private String value;
    private String lineId;

    private int x, y, height, width;
}
