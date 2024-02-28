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
 * 
 */

package de.sub.goobi.metadaten;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.threeten.extra.chrono.BritishCutoverDate;
import org.threeten.extra.chrono.JulianDate;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * This class provides a data structure to save a date as a string and its calendar type. It uses a primitive design to make working with it as easy
 * as possible.
 * 
 * @author Janos Sebök
 */
@EqualsAndHashCode
public class MetaConvertibleDate {
    public enum DateType {
        JULIAN,
        BRITISH,
        GREGORIAN;
    }

    // TODO: maybe use a java.time class instead of a string for compatibility?
    @Getter
    private String date;
    private DateType type;

    public MetaConvertibleDate(String date, DateType type) {
        this.date = date;
        this.type = type;
    }

    public boolean isJulian() {
        return this.type == DateType.JULIAN;
    }

    public boolean isBritish() {
        return this.type == DateType.BRITISH;
    }

    public boolean isGregorian() {
        return this.type == DateType.GREGORIAN;
    }

    /**
     * Convert a date of a specific calendar into a given calendar. Currently more functions are available than are actually used in production.
     * 
     * @param toType A calendar type of the DateType ENUM
     * @return A new calendar date
     */
    public MetaConvertibleDate convert(DateType toType) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        MetaConvertibleDate returnDate = new MetaConvertibleDate("0000-01-01", DateType.GREGORIAN);
        if (type == toType) {
            returnDate = new MetaConvertibleDate(date, toType);
        }
        LocalDate normalizedDate = LocalDate.from(dateFormatter.parse(date));
        final int year = normalizedDate.getYear();
        final int month = normalizedDate.getMonthValue();
        final int day = normalizedDate.getDayOfMonth();
        if (type == DateType.BRITISH && toType == DateType.GREGORIAN) {
            BritishCutoverDate britishDate = BritishCutoverDate.of(year, month, day);
            LocalDate gregorianDate = LocalDate.from(britishDate);
            returnDate = new MetaConvertibleDate(gregorianDate.format(dateFormatter), DateType.GREGORIAN);
        }
        if (type == DateType.GREGORIAN && toType == DateType.BRITISH) {
            BritishCutoverDate britishDate = BritishCutoverDate.from(normalizedDate);
            returnDate = new MetaConvertibleDate(britishDate.format(dateFormatter), DateType.BRITISH);
        }
        if (type == DateType.JULIAN && toType == DateType.GREGORIAN) {
            JulianDate julianDate = JulianDate.of(year, month, day);
            LocalDate gregorianDate = LocalDate.from(julianDate);
            returnDate = new MetaConvertibleDate(gregorianDate.format(dateFormatter), DateType.GREGORIAN);
        }
        if (type == DateType.GREGORIAN && toType == DateType.JULIAN) {
            JulianDate julianDate = JulianDate.from(normalizedDate);
            returnDate = new MetaConvertibleDate(julianDate.format(dateFormatter), DateType.JULIAN);
        }
        return returnDate;
    }

    /**
     * Checks whether date conforms to the YYYY-MM-DD format. Useful when no validation has been set.
     * 
     * @return true or false
     */
    public boolean isValid() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        try {
            dateFormatter.parse(date);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
}
