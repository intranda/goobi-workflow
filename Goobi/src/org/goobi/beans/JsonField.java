package org.goobi.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonField {
    public enum ValueType {
        STRING,
        INTEGER,
        FLOAT,
        BOOL
    }

    private String name;
    private String value;
    private ValueType valueType = ValueType.STRING;

    public ValueType[] getValueTypes() {
        return ValueType.values();
    }
}
