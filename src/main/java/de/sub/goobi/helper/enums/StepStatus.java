package de.sub.goobi.helper.enums;

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
import de.sub.goobi.helper.Helper;
import lombok.Getter;

/**
 * Enum for status of steps, each one with integer value for database, with title and images for gui.
 * 
 * @author Steffen Hankiewicz
 * @version 17.05.2009
 */
public enum StepStatus {

    /**
     * Locked = step not startable.
     */
    LOCKED(0, "statusGesperrt", "red_10.gif", "red_15a.gif", "steplocked", "status-locked"),
    /**
     * open = someone can beginn with this step.
     */
    OPEN(1, "statusOffen", "orange_10.gif", "orange_15a.gif", "stepopen", "status-open"),
    /**
     * inwork = someone is currently working on that step.
     */
    INWORK(2, "statusInBearbeitung", "yellow_10.gif", "yellow_15a.gif", "stepinwork", "status-inwork"),
    /**
     * done = step is executed.
     */
    DONE(3, "statusAbgeschlossen", "green_10.gif", "green_15a.gif", "stepdone", "status-done"),
    /**
     * error = step is stopped as an error occured.
     */
    ERROR(4, "statusError", "red_10.gif", "red_15a.gif", "steperror", "status-error"),
    /**
     * deactivated = step is deactivated and gets activated maybe.
     */
    DEACTIVATED(5, "statusDeactivated", "red_10.gif", "red_15a.gif", "stepdeactivated", "status-deactivated"),
    /**
     * inflight = step is waiting in fast queue, slow queue or external queue for automatic execution.
     */
    INFLIGHT(6, "statusInFlight", "yellow_10.gif", "yellow_15a.gif", "stepinflight", "status-inflight");

    private int value;
    private String title;
    private String imageSmall;
    private String imageBig;
    @Getter
    private String searchString;
    @Getter
    private String color;

    /**
     * private constructor, initializes integer value, title, small and big image
     */
    StepStatus(int inValue, String inTitle, String smallImage, String bigImage, String searchString, String color) {
        this.value = inValue;
        this.title = inTitle;
        this.imageSmall = smallImage;
        this.imageBig = bigImage;
        this.searchString = searchString;
        this.color = color;
    }

    /**
     * return integer value for database savings.
     * 
     * @return value as integer
     */
    public Integer getValue() {
        return this.value;
    }

    /**
     * get title from status type.
     * 
     * @return title as translated string for current locale from standard-jsf-messages
     */
    public String getTitle() {
        return Helper.getTranslation(this.title);
    }

    /**
     * get untranslated title from status type.
     * 
     * @return title as translated string for current locale from standard-jsf-messages
     */
    public String getUntranslatedTitle() {
        return this.title;
    }

    /**
     * get file name for small image.
     * 
     * @return file name for small image
     */
    public String getSmallImagePath() {
        return "images/status/" + this.imageSmall;
    }

    /**
     * get file name for big image.
     * 
     * @return file name for big image
     */
    public String getBigImagePath() {
        return "images/status/" + this.imageBig;
    }

    /**
     * retrieve StepStatus by integer value, neccessary for database handlings, where only integer is saved but not type safe.
     * 
     * @param inValue as integer value
     * @return {@link StepStatus} for given integer
     */
    public static StepStatus getStatusFromValue(Integer inValue) {
        for (StepStatus ss : values()) {
            if (ss.getValue() == inValue.intValue()) {
                return ss;
            }
        }
        return LOCKED;
    }
}