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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.production.plugin.interfaces;

import org.goobi.production.enums.PluginType;

public abstract class AbstractStatisticsPlugin implements IStatisticPlugin {

    protected String filter;

    @Override
    public PluginType getType() {
        return PluginType.Statistics;
    }

    public String getDescription() {
        return "Statistics Plugin";
    }

    @Override
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
