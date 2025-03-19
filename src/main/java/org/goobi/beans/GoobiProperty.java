package org.goobi.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor

public abstract class GoobiProperty implements Serializable {

    private static final long serialVersionUID = -947157110530797855L;

    // values stored in the database:

    // auto increment
    protected Integer id;
    // process id, template id, project id, ...
    protected Integer objectId;

    protected String propertyName;

    protected String propertyValue;

    protected boolean required;

    protected Date creationDate;

    protected String container;

    protected Integer dataType;

    @NonNull
    private PropertyOwnerType propertyType;

    @AllArgsConstructor
    public enum PropertyOwnerType {

        PROCESS("process"),
        TEMPLATE("template"),
        MASTERPIECE("masterpiece"),
        USER("user"),
        ERROR("error");

        @Getter
        private String title;

        public static PropertyOwnerType getByTitle(String title) {
            for (PropertyOwnerType type : values()) {
                if (type.getTitle().equals(title)) {
                    return type;
                }
            }
            return PropertyOwnerType.PROCESS;
        }
    }

    // properties and methods to display data in UI:

    public List<String> valueList;

    public String getContainer() {
        if (container == null) {
            return "0";
        }
        return container;
    }

    public void setContainer(String order) {
        if (order == null) {
            order = "0";
        }
        container = order;
    }

    public String getNormalizedTitle() {
        return propertyName.replace(" ", "_").trim();
    }

    public String getNormalizedValue() {
        return propertyValue.replace(" ", "_").trim();
    }

    public String getNormalizedDate() {
        return Helper.getDateAsFormattedString(creationDate);
    }

    public List<String> getValueList() {
        if (valueList == null) {
            valueList = new ArrayList<>();
        }
        return valueList;
    }

    /**
     * set datentyp to specific value from {@link PropertyType}
     * 
     * @param inType as {@link PropertyType}
     */
    public void setType(PropertyType inType) {
        dataType = inType.getId();
    }

    /**
     * get datentyp as {@link PropertyType}
     * 
     * @return current datentyp
     */
    public PropertyType getType() {
        if (dataType == null) {
            dataType = PropertyType.STRING.getId();
        }
        return PropertyType.getById(dataType);
    }

    // legacy methods, kept for plugin compatibility

    @Deprecated
    public void setTitel(String name) {
        setPropertyName(name);
    }

    @Deprecated
    public String getTitel() {
        return getPropertyName();
    }

    @Deprecated
    public void setWert(String value) {
        setPropertyValue(value);
    }

    @Deprecated
    public String getWert() {
        return getPropertyValue();
    }

}
