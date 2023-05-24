/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.helper;

import java.util.ArrayList;
import java.util.List;

import de.sub.goobi.helper.enums.ManipulationType;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProcessTitleGenerator {
    @Getter
    private int lengthLimit = 10;
    @Getter
    private boolean isAfterLastAddable = true;
    @Getter
    private boolean isBeforeFirstAddable = true;
    @Getter
    private List<Token> bodyTokens = new ArrayList<>();
    @Getter
    private boolean useSignature = false;
    @Getter
    private Token headToken = null;
    @Getter
    private Token tailToken = null;
    @Getter
    private String uuid = null;
    @Getter
    private String separator = "_";

    private String alternativeTitle = null;

    /**
     * use default settings or use setters to initialize individually
     */
    public ProcessTitleGenerator() {

    }

    /**
     * 
     * @param useSignature whether or not to use signature as part of the process title
     * @param limit maximum length of the title name excluding its head
     */
    public ProcessTitleGenerator(boolean useSignature, int limit) {
        this.useSignature = useSignature;
        if (limit > 0) {
            lengthLimit = limit;
        }
    }

    /**
     * 
     * @param useSignature whether or not to use signature as part of the process title
     * @param separator string that should be used to connect tokens
     */
    public ProcessTitleGenerator(boolean useSignature, String separator) {
        this.useSignature = useSignature;
        if (separator != null) {
            this.separator = separator;
        }
    }

    /**
     * 
     * @param limit maximum length of the title name excluding its head
     * @param separator string that should be used to connect tokens
     */
    public ProcessTitleGenerator(int limit, String separator) {
        if (limit > 0) {
            lengthLimit = limit;
        }
        if (separator != null) {
            this.separator = separator;
        }
    }

    /**
     * 
     * @param useSignature whether or not to use signature as part of the process title
     * @param limit maximum length of the title name excluding its head
     * @param separator string that should be used to connect tokens
     */
    public ProcessTitleGenerator(boolean useSignature, int limit, String separator) {
        this.useSignature = useSignature;
        if (limit > 0) {
            lengthLimit = limit;
        }
        if (separator != null) {
            this.separator = separator;
        }
    }

    /**
     * 
     * @param limit maximum length of the title name excluding its head
     */
    public void setLengthLimit(int limit) {
        if (limit > 0) {
            lengthLimit = limit;
        }
    }

    /**
     * 
     * @param useSignature whether or not to use signature as part of the process title
     */
    public void setUseSignature(boolean useSignature) {
        this.useSignature = useSignature;
    }

    /**
     * 
     * @param separator string that should be used to connect tokens
     */
    public void setSeparator(String separator) {
        if (separator != null) {
            this.separator = separator;
        }
    }

    /**
     * add a new token whose value is to be modified regarding its ManipulationType
     * 
     * @param value value of the new token
     * @param type ManipulationType
     * @return true if the new token is successfully added, false if the new token is not addable
     */
    public boolean addToken(String value, ManipulationType type) {
        // preparation
        // 1. there should not be more than one AFTER_LAST_SEPARATOR or BEFORE_FIRST_SEPARATOR
        // 2. for cases NORMAL, CAMEL_CASE, CAMEL_CASE_LENGTH_LIMITED, all umlauts should be replaced 
        // 3. for cases NORMAL, CAMEL_CASE, CAMEL_CASE_LENGTH_LIMITED, all special and space chars should be replaced with _ for the moment
        if (value == null) {
            log.debug("token value can not be null");
            return false;
        }

        // check addability
        if (!checkAddability(type)) {
            log.debug("another token of type " + type + " is not addable");
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
            bodyTokens.add(token);
        }

        return true;
    }

    /**
     * check if a new token of the input ManipulationType is still addable
     * 
     * @param type ManipulationType
     * @return true if a new token of the input ManipulationType is still addable, false otherwise
     */
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

    /**
     * modify the input value according to its ManipulationType
     * 
     * @param value value of the token that is to be modified
     * @param type ManipulationType
     * @return modified value as String
     */
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

    /**
     * simplify the value of the head token
     * 
     * @param value value of the head token that should be modified
     * @return simplified value of the head token
     */
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

    /**
     * replace all umlauts
     * 
     * @param value string whose umlauts should be replaced
     * @return string with all of its umlauts replaced
     */
    private String replaceUmlauts(String value) {
        return value.replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss")
                .replace("Ä", "Ae")
                .replace("Ö", "Oe")
                .replace("Ü", "Ue");
    }

    /**
     * replace special letters and space letters with _
     * 
     * @param value string whose special and space letters should be replaced
     * @return string with all of its special and space letters replaced
     */
    private String replaceSpecialAndSpaceChars(String value) {
        return value.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * get the camel string of the input string
     * 
     * @param value string that should be camel-formated
     * @return the camel-formated string
     */
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

    /**
     * cut string short if it is too long
     * 
     * @param value string whose length should be limited
     * @return the string that is not longer than lengthLimit
     */
    private String cutString(String value) {
        return value.substring(0, Math.min(value.length(), lengthLimit));
    }

    /**
     * generate the title using the default separator settings
     * 
     * @return generated title as a string
     */
    public String generateTitle() {
        return generateTitle(this.separator);
    }

    /**
     * generate the title using the input separator
     * 
     * @param separator string that should be used to connect tokens
     * @return generated title as a string
     */
    public String generateTitle(String separator) {
        if (separator == null) {
            log.debug("separator can not be null");
            separator = this.separator;
        }
        String titleBody = generateTitleBody(separator);

        String simplifiedHead = "";
        String originalHead = "";

        if (headToken != null) {
            simplifiedHead = headToken.getValue() + separator;
            originalHead = uuid + separator;
        }

        alternativeTitle = originalHead + titleBody;

        return simplifiedHead + titleBody;
    }

    /**
     * retrieve the alternative title generated in parallel
     * 
     * @return the alternative title
     */
    public String getAlternativeTitle() {
        if (alternativeTitle == null) {
            // title is not generated yet, report this
            log.debug("there is no alternative title available, since the title itself is not generated yet");
        }

        return alternativeTitle;
    }

    /**
     * generate the title's body, which consists of all tokens except the heading one
     * 
     * @param separator string that should be used to connect tokens
     * @return
     */
    private String generateTitleBody(String separator) {
        StringBuilder sb = new StringBuilder();
        // add values of all body tokens
        for (Token token : bodyTokens) {
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

    @Data
    protected class Token {
        private String value;
        private ManipulationType type;

        public Token(String value, ManipulationType type) {
            this.value = value;
            this.type = type;
        }
    }
}
