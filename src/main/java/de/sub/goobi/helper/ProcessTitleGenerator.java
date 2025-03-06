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
import java.util.regex.Pattern;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.enums.ManipulationType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProcessTitleGenerator {
    @Getter
    private int bodyTokenLengthLimit = 10;
    @Getter
    private int headTokenLengthLimit = 0; // 0 if head token should not be shortened
    @Getter
    private boolean isAfterLastAddable = true;
    @Getter
    private boolean isBeforeFirstAddable = true;
    @Getter
    private List<Token> bodyTokens = new ArrayList<>();

    // true if the full id with its spaces and special chars replaced by _ should be used
    @Getter
    private boolean useFullIdNoSpecialChars = false;

    @Getter
    private Token headToken = null;
    @Getter
    private Token tailToken = null;

    // used to maintain the original full id in case the shorter one is not suitable
    @Getter
    private String original = null;

    // string that is used to combine all tokens into a title
    @Getter
    private String separator = "_";
    // alternative title generated using original full id in case the shorter one is not suitable
    private String alternativeTitle = null;
    // Pattern to check if the input id is uuid
    private static final Pattern UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    @Getter
    @Setter
    private String specialCharacterReplacement = "_";

    /**
     * use default settings or use setters to initialize individually
     */
    public ProcessTitleGenerator() {

    }

    /**
     * 
     * @param useFullIdNoSpecialChars
     * @param limit maximum length of the title name excluding its head
     */
    public ProcessTitleGenerator(boolean useFullIdNoSpecialChars, int limit) {
        this.useFullIdNoSpecialChars = useFullIdNoSpecialChars;
        if (limit > 0) {
            bodyTokenLengthLimit = limit;
        }
    }

    /**
     * 
     * @param useFullIdNoSpecialChars
     * @param separator string that should be used to connect tokens
     */
    public ProcessTitleGenerator(boolean useFullIdNoSpecialChars, String separator) {
        this.useFullIdNoSpecialChars = useFullIdNoSpecialChars;
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
            bodyTokenLengthLimit = limit;
        }
        if (separator != null) {
            this.separator = separator;
        }
    }

    /**
     * 
     * @param useFullIdNoSpecialChars
     * @param limit maximum length of the title name excluding its head
     * @param separator string that should be used to connect tokens
     */
    public ProcessTitleGenerator(boolean useFullIdNoSpecialChars, int limit, String separator) {
        this.useFullIdNoSpecialChars = useFullIdNoSpecialChars;
        if (limit > 0) {
            bodyTokenLengthLimit = limit;
        }
        if (separator != null) {
            this.separator = separator;
        }
    }

    /**
     * 
     * @param limit maximum length of the title name excluding its head
     */
    public void setBodyTokenLengthLimit(int limit) {
        if (limit > 0) {
            bodyTokenLengthLimit = limit;
        }
    }

    /**
     * 
     * @param limit maximum length of the head token, 0 if the head token should not be shortened
     */
    public void setHeadTokenLengthLimit(int limit) {
        if (limit >= 0) {
            headTokenLengthLimit = limit;
        }
    }

    /**
     * 
     * @param useFullIdNoSpecialChars
     */
    public void setUseFullIdNoSpecialChars(boolean useFullIdNoSpecialChars) {
        this.useFullIdNoSpecialChars = useFullIdNoSpecialChars;
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

        if (type == ManipulationType.BEFORE_FIRST_SEPARATOR) {
            return getSimplifiedHead(result);
        }

        if (type == ManipulationType.AFTER_LAST_SEPARATOR) {
            return getSimplifiedTail(result);
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
        // use full id with special chars and spaces replaced
        if (useFullIdNoSpecialChars) {
            String valueWithoutSpecialChars = replaceSpecialAndSpaceChars(value);
            original = valueWithoutSpecialChars;
            return valueWithoutSpecialChars;
        }
        // use shorter id
        // save the original id just in case that the simplified one can not guarantee the uniqueness of the generated title
        original = value;

        // if value is uuid then return the tail after the last -, otherwise return a shorter version of itself if headTokenLengthLimit is positive
        return UUID_REGEX.matcher(value).matches() ? value.substring(value.lastIndexOf("-") + 1) : cutString(value, headTokenLengthLimit);
    }

    /**
     * simplify the value of the tail token
     * 
     * @param value value of the tail token that should be modified
     * @return simplified value of the tail token
     */
    private String getSimplifiedTail(String value) {
        return replaceSpecialAndSpaceChars(value);
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
        return value.replaceAll(ConfigurationHelper.getInstance().getProcessTitleReplacementRegex(), specialCharacterReplacement);
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
        for (String word2 : words) {
            String word = word2;
            word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
            sb.append(word);
        }

        return sb.toString();
    }

    /**
     * cut string short if it is too long, should only be used for body tokens
     * 
     * @param value string whose length should be limited
     * @return the string head that is not longer than lengthLimit
     */
    private String cutString(String value) {
        return cutString(value, bodyTokenLengthLimit);
    }

    /**
     * cut string short if it is too long
     * 
     * @param value string whose length should be limited
     * @param limit limit of the string length, 0 if the value should not be shortened at all
     * @return the string head that is not longer than limit
     */
    private String cutString(String value, int limit) {
        if (limit == 0) {
            return value;
        }

        return value.substring(0, Math.min(value.length(), limit));
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
            log.debug("separator can not be null, the default method will be used instead");
            return generateTitle();
        }
        String titleBody = generateTitleBody(separator);

        String simplifiedHead = "";
        String originalHead = "";

        if (headToken != null) {
            simplifiedHead = headToken.getValue() + separator;
            originalHead = original + separator;
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
