package org.goobi.beans;

import java.util.List;

import lombok.Data;

@Data
public class SimpleAltoLine {
    private String id;
    private List<SimpleAltoWord> words;

    private int x, y, height, width;
}
