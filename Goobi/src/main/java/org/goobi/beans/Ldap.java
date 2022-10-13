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

import org.goobi.security.authentication.IAuthenticationProvider;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ldap implements Serializable, DatabaseObject, IAuthenticationProvider {
    private static final long serialVersionUID = 931296142933906486L;
    private Integer ldapgruppenID;
    private String titel;
    private String homeDirectory = "/home/{login}";
    private String gidNumber = "100";
    private String userDN = "CHANGE_ME_cn={login},ou=users,o=example,c=net";
    private String objectClasses = "top,inetOrgPerson,posixAccount,shadowAccount,sambaSamAccount";
    private String sambaSID = "CHANGE_ME-{uidnumber*2+1000}";
    private String sn = "{login}";
    private String uid = "{login}";
    private String description = "Goobi user";
    private String displayName = "{user full name}";
    private String gecos = "Goobi user";
    private String loginShell = "/bin/false";
    private String sambaAcctFlags = "[UX         ]";
    private String sambaLogonScript = "_{login}.bat";
    private String sambaPrimaryGroupSID = "CHANGE_ME";

    private String sambaPwdMustChange = "2147483647"; //NOSONAR, its no password, its the time until a pw change is required
    private String sambaPasswordHistory = "0000000000000000000000000000000000000000000000000000000000000000"; //NOSONAR no password
    private String sambaLogonHours = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
    private String sambaKickoffTime = "0";

    private String adminLogin = "CHANGE_ME_cn=administrator,ou=users,o=example,c=net";
    private String adminPassword = "CHANGE_ME_password";
    private String ldapUrl = "CHANGE_ME_http://localhost:389/";
    private String attributeToTest;
    private String valueOfAttribute;
    private String nextFreeUnixId = "CHANGE_ME_cn=NextFreeUnixId,o=example,c=net";
    private String pathToRootCertificate;
    private String pathToPdcCertificate;
    private String encryptionType = "SHA";
    private boolean useSsl;

    private String authenticationType;
    private boolean readonly;
    private boolean readDirectoryAnonymous;
    private boolean useLocalDirectoryConfiguration;
    private String ldapHomeDirectoryAttributeName = "homeDirectory";
    private boolean useTLS;

    @Override
    public void lazyLoad() {
        // nothing to load lazy here
    }

    @Override
    public Integer getId() {
        return this.ldapgruppenID;
    }

    @Override
    public void setId(Integer id) {
        this.ldapgruppenID = id;
    }

    @Override
    public AuthenticationType getAuthenticationTypeEnum() {
        return AuthenticationType.getByTitle(authenticationType);
    }

    @Override
    public String getTitle() {
        return titel;
    }

    @Override
    public void setTitle(String title) {
        this.titel = title;
    }
}
