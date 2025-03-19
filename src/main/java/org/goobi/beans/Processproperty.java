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
import java.util.Date;

import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.Setter;

public class Processproperty extends GoobiProperty implements Serializable, Comparable<Processproperty> {
    private static final long serialVersionUID = -2356566712752716107L;

    @Setter
    private Process prozess;
    @Setter
    private int processId;

    public Processproperty() {
        super(PropertyOwnerType.PROCESS);
        this.creationDate = new Date();
    }

    public Process getProzess() {
        if (prozess == null) {
            prozess = ProcessManager.getProcessById(processId);
        }
        return this.prozess;
    }

    @Override
    public int compareTo(Processproperty o) {
        return propertyName.toLowerCase().compareTo(o.getPropertyName().toLowerCase());
    }

    public Integer getProcessId() {
        return processId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getContainer() == null) ? 0 : getContainer().hashCode());
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + processId;
        result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
        result = prime * result + ((propertyValue == null) ? 0 : propertyValue.hashCode());
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
        Processproperty other = (Processproperty) obj;
        if (id != null && other.id != null && id.equals(other.id)) {
            return true;
        }
        if (getContainer() == null) {
            if (other.getContainer() != null) {
                return false;
            }
        } else if (!getContainer().equals(other.getContainer())) {
            return false;
        }
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }

        if (processId != other.processId) {
            return false;
        }
        if (propertyName == null) {
            if (other.propertyName != null) {
                return false;
            }
        } else if (!propertyName.equals(other.propertyName)) {
            return false;
        }
        if (propertyValue == null) {
            if (other.propertyValue != null) {
                return false;
            }
        } else if (!propertyValue.equals(other.propertyValue)) {
            return false;
        }
        return true;
    }

}
