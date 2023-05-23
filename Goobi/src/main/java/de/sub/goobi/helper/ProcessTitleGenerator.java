package de.sub.goobi.helper;

import java.util.ArrayList;
import java.util.List;

import de.sub.goobi.helper.enums.ManipulationType;

public class ProcessTitleGenerator {
    private int lengthLimit = 10;
    private boolean isAfterLastAddable = true;
    private boolean isBeforeFirstAddable = true;
    private List<Token> tokens = new ArrayList<>();

    private boolean useSignature = false;

    private Token headToken = null;
    private Token tailToken = null;

    private String uuid = null;

    private String titleWithUuid = null;

    private String separator = "_";

    public ProcessTitleGenerator() {

    }

    public ProcessTitleGenerator(boolean signature, int limit) {
        useSignature = signature;
        lengthLimit = limit;
    }

    public void setLengthLimit(int limit) {
        if (limit > 0) {
            lengthLimit = limit;
        }
    }

    public void setUseSignature(boolean b) {
        useSignature = b;
    }

    public void setSeparator(String separator) {
        if (separator != null) {
            this.separator = separator;
        }
    }

    public boolean addToken(String value, ManipulationType type) {
        // preparation
        // 1. there should not be more than one AFTER_LAST_SEPARATOR or BEFORE_FIRST_SEPARATOR
        // 2. for cases NORMAL, CAMEL_CASE, CAMEL_CASE_LENGTH_LIMITED, all umlauts should be replaced 
        // 3. for cases NORMAL, CAMEL_CASE, CAMEL_CASE_LENGTH_LIMITED, all special and space chars should be replaced with _ for the moment

        // check addability
        if (!checkAddability(type)) {
            return false;
        }

        // modify the input string value
        String modifiedValue = modifyValueGivenType(value, type);

        // create Token and add it to the list (or save temporarily as headToken or tailToken)
        Token token = new Token(modifiedValue, type);
        if (type == ManipulationType.AFTER_LAST_SEPARATOR) {
            isAfterLastAddable = false;
            tailToken = token;
        } else if (type == ManipulationType.BEFORE_FIRST_SEPARATOR) {
            isBeforeFirstAddable = false;
            headToken = token;
        } else {
            tokens.add(token);
        }

        return true;
    }

    private boolean checkAddability(ManipulationType type) {
        switch (type) {
            case AFTER_LAST_SEPARATOR:
                return isAfterLastAddable;
            case BEFORE_FIRST_SEPARATOR:
                return isBeforeFirstAddable;
            default:
                return true;
        }
    }

    private String modifyValueGivenType(String value, ManipulationType type) {
        String result = value;

        if (type == ManipulationType.AFTER_LAST_SEPARATOR) {
            return result;
        }

        if (type == ManipulationType.BEFORE_FIRST_SEPARATOR) {
            return getSimplifiedHead(result);
        }

        result = replaceUmlauts(result);
        result = replaceSpecialAndSpaceChars(result);

        if (type == ManipulationType.CAMEL_CASE || type == ManipulationType.CAMEL_CASE_LENGTH_LIMITED) {
            result = getCamelString(result);
        }

        if (type == ManipulationType.CAMEL_CASE_LENGTH_LIMITED) {
            result = cutString(result);
        }

        return result;
    }

    private String getSimplifiedHead(String value) {
        // use signature
        if (useSignature) {
            String valueWithoutSpecialChars = replaceSpecialAndSpaceChars(value);
            uuid = valueWithoutSpecialChars;
            return valueWithoutSpecialChars;
        }
        // use uuid
        // save uuid just in case that the simplified one can not guarantee the uniqueness of the generated title
        uuid = value;
        // simplified uuid = substring of uuid following the last -
        return value.substring(value.lastIndexOf("-") + 1);
    }

    private String replaceUmlauts(String value) {
        return value.replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss")
                .replace("Ä", "Ae")
                .replace("Ö", "Oe")
                .replace("Ü", "Ue");
    }

    private String replaceSpecialAndSpaceChars(String value) {
        return value.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private String getCamelString(String value) {
        String[] words = value.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; ++i) {
            String word = words[i];
            word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
            sb.append(word);
        }

        return sb.toString();
    }

    private String cutString(String value) {
        return value.substring(0, Math.min(value.length(), lengthLimit));
    }

    public String generateTitle() {
        return generateTitle(this.separator);
    }

    public String generateTitle(String separator) {
        String titleBody = generateTitleBody(separator);

        String simplifiedHead = "";
        String originalHead = "";

        if (headToken != null) {
            simplifiedHead = headToken.getValue() + separator;
            originalHead = uuid + separator;
        }

        titleWithUuid = originalHead + titleBody;

        return simplifiedHead + titleBody;
    }

    public String getTitleWithUuid() {
        if (titleWithUuid == null) {
            // title is not generated yet, report this
            return "";
        }

        return titleWithUuid;
    }

    private String generateTitleBody(String separator) {
        StringBuilder sb = new StringBuilder();
        // add values of all body tokens
        for (Token token : tokens) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(token.getValue());
        }

        // check tailToken
        if (tailToken != null) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(tailToken.getValue());
        }

        return sb.toString();
    }

    private class Token {
        private String value;
        private ManipulationType type;

        public Token(String value, ManipulationType type) {
            this.value = value;
            this.type = type;
        }

        public String getValue() {
            return value;
        }
    }
}
