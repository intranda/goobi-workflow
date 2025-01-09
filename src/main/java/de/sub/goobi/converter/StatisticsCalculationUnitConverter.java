package de.sub.goobi.converter;

import org.goobi.production.flow.statistics.enums.CalculationUnit;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

/**
 * StatisticCalculationUnitConverter for statistics CalculationUnits as select-items in jsf-guis
 * 
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 **************************************************************************************/

@FacesConverter("StatisticsCalculationUnitConverter")
public class StatisticsCalculationUnitConverter implements Converter<CalculationUnit> {

    /**
     * convert String to CalculationUnit
     **************************************************************************************/
    @Override
    public CalculationUnit getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
        if (value == null) {
            return CalculationUnit.volumes;
        } else {
            return CalculationUnit.getById(value);
        }
    }

    /**
     * convert ResultOutput to String
     **************************************************************************************/
    @Override
    public String getAsString(FacesContext context, UIComponent component, CalculationUnit value) throws ConverterException {
        if (!(value instanceof CalculationUnit)) {
            return CalculationUnit.volumes.getId();
        } else {
            return value.getId();
        }
    }

}
