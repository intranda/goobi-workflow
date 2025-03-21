package org.goobi.beans;

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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Template implements Serializable, IPropertyHolder {
    private static final long serialVersionUID = 1736135433162833277L;
    private Integer id;
    private String herkunft;
    private Process prozess;
    private Integer processId;
    private List<GoobiProperty> eigenschaften;

    private boolean panelAusgeklappt = true;

    public Template() {
        this.eigenschaften = new ArrayList<>();
    }

    /*
     * ##################################################### ##################################################### ## ## Helper ##
     * ##################################################### ####################################################
     */

    public int getEigenschaftenSize() {
        return getEigenschaften().size();
    }

    public List<GoobiProperty> getEigenschaftenList() {
        return getEigenschaften();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eigenschaften == null) ? 0 : eigenschaften.hashCode());
        result = prime * result + ((herkunft == null) ? 0 : herkunft.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (panelAusgeklappt ? 1231 : 1237);
        result = prime * result + ((processId == null) ? 0 : processId.hashCode());
        result = prime * result + ((prozess == null) ? 0 : prozess.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Template other = (Template) obj;
        if (herkunft == null) {
            if (other.herkunft != null) {
                return false;
            }
        } else if (!herkunft.equals(other.herkunft)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public List<GoobiProperty> getProperties() {
        return getEigenschaften();
    }

    @Override
    public void setProperties(List<GoobiProperty> properties) {
        setEigenschaften(properties);
    }
}
