package de.sub.goobi.metadaten;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.threeten.extra.chrono.BritishCutoverDate;
import org.threeten.extra.chrono.JulianDate;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class MetaConvertibleDate {
    public enum DateType {
        JULIAN,
        BRITISH,
        GREGORIAN;
    }

    // Maybe use a java.time class instead of a string for compatibility?
    @Getter
    private String date;
    private DateType type;

    public MetaConvertibleDate(String date, DateType type) {
        this.date = date;
        this.type = type;
    }

    public boolean isJulian() {
        return this.type == DateType.JULIAN ? true : false;
    }

    public boolean isBritish() {
        return this.type == DateType.BRITISH ? true : false;
    }

    public boolean isGregorian() {
        return this.type == DateType.GREGORIAN ? true : false;
    }

    // TODO: real docstring
    // When called, returns a new MetaConvertibleDate of the desired type.
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
            LocalDate gregorianDate = LocalDate.parse(britishDate.format(dateFormatter));
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

    // // B2G, G2B, J2G, G2J
    ///
    // LocalDate ld = LocalDate.parse(date);
    // JulianDate jd = JulianDate.from(ld);
    // BritishCutoverDate bd = BritishCutoverDate.from(ld);
    //
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
