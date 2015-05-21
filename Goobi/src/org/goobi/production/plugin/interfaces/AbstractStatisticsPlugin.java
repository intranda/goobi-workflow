package org.goobi.production.plugin.interfaces;

import org.goobi.production.enums.PluginType;

public abstract class AbstractStatisticsPlugin implements IStatisticPlugin {


    

    protected String filter;
    

    
    @Override
    public PluginType getType() {
        return PluginType.Statistics;
    }

    @Override
    public String getDescription() {
        return "Statistics Plugin";
    }




    
    public void setFilter(String filter) {
        this.filter = filter;
    }
    
    public String getFilter() {
        return filter;
    }
    
//  protected TimeUnit sourceTimeUnit;
//  protected TimeUnit targetTimeUnit;
//  protected TimeUnit timeUnit;
//  protected CalculationUnit calculationUnit;
//  protected CalculationUnit targetCalculationUnit;
  
//  protected int sourceNumberOfTimeUnits = 0;
//  protected boolean showAverage = false;
//  @Override
//  public void setCalculationUnit(CalculationUnit calculationUnit) {
//      this.calculationUnit = calculationUnit;
//  }
//
//  @Override
//  public CalculationUnit getCalculationUnit() {
//      return calculationUnit;
//  }   
//  /**
//   * @return the sourceNumberOfTimeUnitsAsString
//   *************************************************************************************/
//  public String getSourceNumberOfTimeUnitsAsString() {
//      if (sourceNumberOfTimeUnits == 0) {
//          return "";
//      } else {
//          return String.valueOf(sourceNumberOfTimeUnits);
//      }
//  }
//
//  /**
//   * @param inUnits the sourceNumberOfTimeUnits to set given as String to show an empty text field in gui
//   *************************************************************************************/
//  public void setSourceNumberOfTimeUnitsAsString(String inUnits) {
//      if (StringUtils.isNotBlank(inUnits) && StringUtils.isNumericSpace(inUnits)) {
//          sourceNumberOfTimeUnits = Integer.parseInt(inUnits);
//      } else {
//          sourceNumberOfTimeUnits = 0;
//      }
//  }
  
//  /**
//   * @return the sourceTimeUnit
//   *************************************************************************************/
//  public TimeUnit getSourceTimeUnit() {
//      return sourceTimeUnit;
//  }
//
//  /**
//   * @param sourceTimeUnit the sourceTimeUnit to set
//   *************************************************************************************/
//  public void setSourceTimeUnit(TimeUnit sourceTimeUnit) {
//      this.sourceTimeUnit = sourceTimeUnit;
//  }
//
//  /**
//   * @return the targetTimeUnit
//   *************************************************************************************/
//  public TimeUnit getTargetTimeUnit() {
//      return targetTimeUnit;
//  }
//
//  /**
//   * @param targetTimeUnit the targetTimeUnit to set
//   *************************************************************************************/
//  public void setTargetTimeUnit(TimeUnit targetTimeUnit) {
//      this.targetTimeUnit = targetTimeUnit;
//  }


//  /**
//   * @return the targetCalculationUnit
//   *************************************************************************************/
//  public CalculationUnit getTargetCalculationUnit() {
//      return targetCalculationUnit;
//  }
//  
//  
//  /**
//   * @param targetCalculationUnit the targetCalculationUnit to set
//   *************************************************************************************/
//  public void setTargetCalculationUnit(CalculationUnit targetCalculationUnit) {
//      this.targetCalculationUnit = targetCalculationUnit;
//  }
//  
//  /**
//   * @return the showAverage
//   *************************************************************************************/
//  public boolean isShowAverage() {
//      return showAverage;
//  }
//
//  /**
//   * @param showAverage the showAverage to set
//   *************************************************************************************/
//  public void setShowAverage(boolean showAverage) {
//      this.showAverage = showAverage;
//  }
}
