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

    public static final String SUB_OPAC = "SUB";
    public static final String VKI_OPAC = "VKI";
    public static final String VKI_AALG_OPAC = "VKI_AALG";
    public static final String VKI_AAC_OPAC = "VKI_AAC";
    public static final String VKI_AAC_OLCOPAC = "VKI_AAC_OLC";
    public static final String GBV_OPAC = "GBV";
    public static final String JAL_OPAC = "JAL";
    public static final String OLC_MATH_OPAC = "OLC_MATH";
    public static final String HEBIS_OPAC = "HEBIS";
    public static final String SWB_OPAC = "SWB";
    public static final String DNB_OPAC = "DNB";
    public static final String ZDB_OPAC = "ZDB";
    public static final String GEO_GUIDE_OPAC = "GEO_GUIDE";
    public static final String TIB_OPAC = "TIB";
    public static final String SBB_OPAC = "SBB";

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

    public Catalogue(String opac) throws IOException {
        super();

        this.catalogue = opac;
        if (opac.equals(GBV_OPAC)) {
            setGbv();
        } else if (opac.equals(VKI_OPAC)) {
            setVki();
        } else if (opac.equals(VKI_AALG_OPAC)) {
            setVkiAalg();
        } else if (opac.equals(VKI_AAC_OPAC)) {
            setVkiAac();
        } else if (opac.equals(VKI_AAC_OLCOPAC)) {
            setVkiAacolc();
        } else if (opac.equals(OLC_MATH_OPAC)) {
            setOlcMath();
        } else if (opac.equals(SWB_OPAC)) {
            setSwb();
        } else if (opac.equals(HEBIS_OPAC)) {
            setHebis();
        } else if (opac.equals(ZDB_OPAC)) {
            setZdb();
        } else if (opac.equals(DNB_OPAC)) {
            setDnb();
        } else if (opac.equals(GEO_GUIDE_OPAC)) {
            setGeoGuide();
        } else if (opac.equals(JAL_OPAC)) {
            setJalEmden();
        } else if (opac.equals(TIB_OPAC)) {
            setTib();
        } else if (opac.equals(SBB_OPAC)) {
            setSbb();
        } else { // as default use SUB
            setSub();
        }
    }

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

    private void setGbv() {
        this.dataBase = "2.1";
        this.serverAddress = "gso.gbv.de";
        this.port = 80;
        this.description = "GVK";
    }

    private void setVki() {
        this.dataBase = "1.85";
        this.serverAddress = "gso.gbv.de";
        this.port = 80;
        this.description = "VKI";
    }

    private void setVkiAalg() {
        this.dataBase = "8.2";
        this.serverAddress = "gso.gbv.de";
        this.port = 80;
        this.description = "VKI-AALG";
    }

    private void setVkiAac() {
        this.dataBase = "2.97";
        this.serverAddress = "gso.gbv.de";
        this.port = 80;
        this.description = "VKI-AAC";
    }

    private void setVkiAacolc() {
        this.dataBase = "2.297";
        this.serverAddress = "gso.gbv.de";
        this.port = 80;
        this.description = "VKI-AAC+OLC";
    }

    private void setOlcMath() {
        this.dataBase = "2.77";
        this.serverAddress = "gso.gbv.de";
        this.port = 80;
        this.description = "OLC-MATH";
    }

    private void setJalEmden() {
        this.dataBase = "1";
        this.serverAddress = "emdbs2.fho-emden.de";
        this.port = 8080;
        this.description = "JAL";
    }

    private void setZdb() {
        this.dataBase = "1.1";
        this.serverAddress = "dispatch.opac.d-nb.de";
        this.port = 80;
        this.description = "ZDB";
    }

    private void setDnb() {
        this.dataBase = "4.1";
        this.serverAddress = "dispatch.opac.d-nb.de";
        this.port = 80;
        this.description = "DNB";
    }

    private void setHebis() {
        this.dataBase = "2.1";
        this.serverAddress = "cbsopac.rz.uni-frankfurt.de";
        this.port = 80;
        this.description = "HeBIS";
    }

    private void setSub() {
        this.serverAddress = "opac.sub.uni-goettingen.de";
        this.port = 80;
        this.dataBase = "1";
        this.description = "SUB Goettingen";
    }

    private void setSwb() {
        this.dataBase = "2.1";
        this.serverAddress = "pollux.bsz-bw.de";
        this.port = 80;
        this.description = "SWB";
    }

    private void setGeoGuide() {
        this.dataBase = "8.4";
        this.serverAddress = "gso4.gbv.de";
        this.port = 80;
        this.description = "GEO_GUIDE";
    }

    private void setTib() {
        this.dataBase = "1";
        this.serverAddress = "opc4.tib.uni-hannover.de";
        this.port = 8080;
        this.description = "TIB";
    }

    private void setSbb() {
        this.dataBase = "1";
        this.serverAddress = "stabikat.de";
        this.port = 80;
        this.description = "SBB";
    }
}
