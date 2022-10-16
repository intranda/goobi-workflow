package de.sub.goobi.metadaten;

public class MetaConvertibleDate {
    public enum DateType {
        JULIAN,
        BRITISH,
        GEORGIAN;
    }

    // Maybe use a java.time class instead of a string for compatibility?
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

    public boolean isGeorgian() {
        return this.type == DateType.GEORGIAN ? true : false;
    }

    // impl. concrete flex. date
    // TODO: types of conversion
    public MetaConvertibleDate convert(DateType toType) {
        MetaConvertibleDate new_date = new MetaConvertibleDate(this.date, toType);
        return new_date;
    }
}
