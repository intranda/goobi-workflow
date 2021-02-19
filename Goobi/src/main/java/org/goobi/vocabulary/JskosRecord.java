package org.goobi.vocabulary;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Data;

@Data
@XmlRootElement
@JsonPropertyOrder({ "uri", "type", "@context", "inScheme", "publisher", "prefLabel", "fields" })
@JsonInclude(Include.NON_NULL)
public class JskosRecord {

    private String uri;

    private List<String> type;

    @JsonProperty("@context")
    private String context;

    private List<Schema> inScheme;

    private Publisher publisher;

    private Fields prefLabel;

    // fieldname, <lang> <value>
    Map<String, Fields> fields;

    @Data
    @XmlRootElement
    @JsonInclude(Include.NON_NULL)
    public class Schema {
        private String uri;
        private Fields prefLabel;
        private List<String> type;
    }

    @Data
    @XmlRootElement
    @JsonInclude(Include.NON_NULL)
    public class Publisher {
        private String uri;
        private Fields prefLabel;
        private List<String> type;
    }

    @Data
    @XmlRootElement
    @JsonInclude(Include.NON_NULL)
    public class Fields {
        @JsonIgnore
        private Map<String, String> values;
        @JsonIgnore
        private String value;

        @JsonValue
        public Object getJsonValue() {
            if (StringUtils.isNotBlank(value)) {
                return value;
            } else {
                return values;
            }
        }

    }
}
