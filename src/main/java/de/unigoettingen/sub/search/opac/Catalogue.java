package de.unigoettingen.sub.search.opac;

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

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

public class Catalogue {

    @Getter
    private String catalogue;
    @Getter
    private String description;
    @Getter
    @Setter
    private String cbs = "";

    @Getter
    private String dataBase;
    @Getter
    private String serverAddress;
    @Getter
    private int port;

    @Getter
    private String charset = "iso-8859-1";

    @Getter
    @Setter
    private String protocol = "http://";

    @Getter
    @Setter
    private boolean verbose = false;

    public Catalogue(String description, String serverAddress, int port, String cbs, String database) throws IOException {
        this.description = description;
        this.serverAddress = serverAddress;
        this.port = port;
        this.dataBase = database;
        this.cbs = cbs;
    }

    public Catalogue(String description, String serverAddress, int port, String charset, String cbs, String database) throws IOException {
        this(description, serverAddress, port, cbs, database);
        this.charset = charset;
    }

}
