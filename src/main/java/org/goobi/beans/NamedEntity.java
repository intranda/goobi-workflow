package org.goobi.beans;

import java.util.Objects;

import lombok.Data;

@Data
public class NamedEntity {
    private String id;
    private String label;
    private String type;
    private String uri;

    public boolean equals(Object o) {
        if (o != null && o.getClass().equals(this.getClass())) {
            return Objects.equals(this.id, ((NamedEntity) o).id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return this.id.hashCode();
        } else {
            return 0;
        }
    }

    public NamedEntity() {
    }

    public NamedEntity(String id, String label, String type, String uri) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.uri = uri;
    }
}
