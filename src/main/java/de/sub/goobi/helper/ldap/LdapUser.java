package de.sub.goobi.helper.ldap;

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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.binary.Base64;
import org.goobi.beans.Ldap;
import org.goobi.beans.User;

import de.sub.goobi.helper.encryption.MD4;
import lombok.extern.log4j.Log4j2;

/**
 * This class is used by the DirObj example. It is a DirContext class that can be stored by service providers like the LDAP system providers.
 */
@Log4j2
public class LdapUser implements DirContext {
    private String type;
    private Attributes myAttrs;

    /**
     * Constructor of LdapUser.
     */
    public LdapUser() {
        this.myAttrs = new BasicAttributes(true);
    }

    /**
     * configure LdapUser with Userdetails.
     * 
     * @param inUser
     * @param inPassword
     * @param inUidNumber
     * @throws NamingException
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     * @throws IOException
     */
    public void configure(User inUser, String inPassword, String inUidNumber)
            throws NamingException, NoSuchAlgorithmException, IOException, InterruptedException {
        Ldap lp = inUser.getLdapGruppe();
        if (!lp.isReadonly()) {

            this.type = inUser.getLogin();
            if (lp.getObjectClasses() == null) {
                throw new NamingException("no objectclass defined");
            }

            /* ObjectClasses */
            Attribute oc = new BasicAttribute("objectclass");
            StringTokenizer tokenizer = new StringTokenizer(lp.getObjectClasses(), ",");
            while (tokenizer.hasMoreTokens()) {
                oc.add(tokenizer.nextToken());
            }
            this.myAttrs.put(oc);

            this.myAttrs.put("uid", replaceVariables(lp.getUid(), inUser, inUidNumber));
            this.myAttrs.put("cn", replaceVariables(lp.getUid(), inUser, inUidNumber));
            this.myAttrs.put("displayName", replaceVariables(lp.getDisplayName(), inUser, inUidNumber));
            this.myAttrs.put("description", replaceVariables(lp.getDescription(), inUser, inUidNumber));
            this.myAttrs.put("gecos", replaceVariables(lp.getGecos(), inUser, inUidNumber));
            this.myAttrs.put("loginShell", replaceVariables(lp.getLoginShell(), inUser, inUidNumber));
            this.myAttrs.put("sn", replaceVariables(lp.getSn(), inUser, inUidNumber));
            this.myAttrs.put(lp.getLdapHomeDirectoryAttributeName(), replaceVariables(lp.getHomeDirectory(), inUser, inUidNumber));

            this.myAttrs.put("sambaAcctFlags", replaceVariables(lp.getSambaAcctFlags(), inUser, inUidNumber));
            this.myAttrs.put("sambaLogonScript", replaceVariables(lp.getSambaLogonScript(), inUser, inUidNumber));
            this.myAttrs.put("sambaPrimaryGroupSID", replaceVariables(lp.getSambaPrimaryGroupSID(), inUser, inUidNumber));
            this.myAttrs.put("sambaSID", replaceVariables(lp.getSambaSID(), inUser, inUidNumber));

            this.myAttrs.put("sambaPwdMustChange", replaceVariables(lp.getSambaPwdMustChange(), inUser, inUidNumber));
            this.myAttrs.put("sambaPasswordHistory", replaceVariables(lp.getSambaPasswordHistory(), inUser, inUidNumber));
            this.myAttrs.put("sambaLogonHours", replaceVariables(lp.getSambaLogonHours(), inUser, inUidNumber));
            this.myAttrs.put("sambaKickoffTime", replaceVariables(lp.getSambaKickoffTime(), inUser, inUidNumber));
            this.myAttrs.put("sambaPwdLastSet", String.valueOf(System.currentTimeMillis() / 1000L));

            this.myAttrs.put("uidNumber", inUidNumber);
            this.myAttrs.put("gidNumber", replaceVariables(lp.getGidNumber(), inUser, inUidNumber));

            /*
             * -------------------------------- Samba passwords --------------------------------
             */
            /* LanMgr */
            try {
                this.myAttrs.put("sambaLMPassword", toHexString(lmHash(inPassword)));
            } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchPaddingException e) {
                log.error(e);
            }
            /* NTLM */
            byte[] hmm = MD4.mdfour(inPassword.getBytes(StandardCharsets.UTF_16LE));
            this.myAttrs.put("sambaNTPassword", toHexString(hmm));

            /*
             * -------------------------------- Encryption of password und Base64-Enconding --------------------------------
             */

            MessageDigest md = MessageDigest.getInstance(lp.getEncryptionType());
            md.update(inPassword.getBytes());
            String digestBase64 = new String(Base64.encodeBase64(md.digest()));
            this.myAttrs.put("userPassword", "{" + lp.getEncryptionType() + "}" + digestBase64);
        }
    }

    /**
     * Replace Variables with current user details
     * 
     * @param inString
     * @param inUser
     * @return String with replaced variables
     */
    private String replaceVariables(String inString, User inUser, String inUidNumber) {
        if (inString == null) {
            return "";
        }
        String rueckgabe = inString.replace("{login}", inUser.getLogin());
        rueckgabe = rueckgabe.replace("{user full name}", inUser.getVorname() + " " + inUser.getNachname());
        rueckgabe = rueckgabe.replace("{uidnumber*2+1000}", String.valueOf(Integer.parseInt(inUidNumber) * 2 + 1000));
        rueckgabe = rueckgabe.replace("{uidnumber*2+1001}", String.valueOf(Integer.parseInt(inUidNumber) * 2 + 1001));
        if (log.isDebugEnabled()) {
            log.debug("Replace instring: " + inString + " - " + inUser + " - " + inUidNumber);
            log.debug("Replace outstring: " + rueckgabe);
        }
        return rueckgabe;
    }

    /**
     * Creates the LM Hash of the user's password.
     * 
     * @param password The password.
     * 
     * @return The LM Hash of the given password, used in the calculation of the LM Response.
     */
    public static byte[] lmHash(String password)
            throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        byte[] oemPassword = password.toUpperCase().getBytes(StandardCharsets.US_ASCII);
        int length = Math.min(oemPassword.length, 14);
        byte[] keyBytes = new byte[14];
        System.arraycopy(oemPassword, 0, keyBytes, 0, length);
        Key lowKey = createDESKey(keyBytes, 0);
        Key highKey = createDESKey(keyBytes, 7);
        byte[] magicConstant = "KGS!@#$%".getBytes(StandardCharsets.US_ASCII);
        Cipher des = Cipher.getInstance("DES/ECB/NoPadding"); //NOSONAR
        des.init(Cipher.ENCRYPT_MODE, lowKey);
        byte[] lowHash = des.doFinal(magicConstant);
        des.init(Cipher.ENCRYPT_MODE, highKey);
        byte[] highHash = des.doFinal(magicConstant);
        byte[] lmHash = new byte[16];
        System.arraycopy(lowHash, 0, lmHash, 0, 8);
        System.arraycopy(highHash, 0, lmHash, 8, 8);
        return lmHash;
    }

    /**
     * Creates a DES encryption key from the given key material.
     * 
     * @param bytes A byte array containing the DES key material.
     * @param offset The offset in the given byte array at which the 7-byte key material starts.
     * 
     * @return A DES encryption key created from the key material starting at the specified offset in the given byte array.
     */
    private static Key createDESKey(byte[] bytes, int offset) {
        byte[] keyBytes = new byte[7];
        System.arraycopy(bytes, offset, keyBytes, 0, 7);
        byte[] material = new byte[8];
        material[0] = keyBytes[0];
        material[1] = (byte) (keyBytes[0] << 7 | (keyBytes[1] & 0xff) >>> 1);
        material[2] = (byte) (keyBytes[1] << 6 | (keyBytes[2] & 0xff) >>> 2);
        material[3] = (byte) (keyBytes[2] << 5 | (keyBytes[3] & 0xff) >>> 3);
        material[4] = (byte) (keyBytes[3] << 4 | (keyBytes[4] & 0xff) >>> 4);
        material[5] = (byte) (keyBytes[4] << 3 | (keyBytes[5] & 0xff) >>> 5);
        material[6] = (byte) (keyBytes[5] << 2 | (keyBytes[6] & 0xff) >>> 6);
        material[7] = (byte) (keyBytes[6] << 1);
        oddParity(material);
        return new SecretKeySpec(material, "DES");
    }

    /**
     * Applies odd parity to the given byte array.
     * 
     * @param bytes The data whose parity bits are to be adjusted for odd parity.
     */
    private static void oddParity(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            boolean needsParity = (((b >>> 7) ^ (b >>> 6) ^ (b >>> 5) ^ (b >>> 4) ^ (b >>> 3) ^ (b >>> 2) ^ (b >>> 1)) & 0x01) == 0;
            if (needsParity) {
                bytes[i] |= (byte) 0x01;
            } else {
                bytes[i] &= (byte) 0xfe;
            }
        }
    }

    public static String toHexString(byte[] bytes) {
        StringBuilder buffer = new StringBuilder();
        for (byte element : bytes) {
            buffer.append(Integer.toHexString(0x0100 + (element & 0x00FF)).substring(1));
        }
        return buffer.toString().toUpperCase();
    }

    @Override
    public Attributes getAttributes(String name) throws NamingException {
        if (!"".equals(name)) {
            throw new NameNotFoundException();
        }
        return (Attributes) this.myAttrs.clone();
    }

    @Override
    public Attributes getAttributes(Name name) throws NamingException {
        return getAttributes(name.toString());
    }

    @Override
    public Attributes getAttributes(String name, String[] ids) throws NamingException {
        if (!"".equals(name)) {
            throw new NameNotFoundException();
        }

        Attributes answer = new BasicAttributes(true);
        Attribute target;
        for (String id : ids) {
            target = this.myAttrs.get(id);
            if (target != null) {
                answer.put(target);
            }
        }
        return answer;
    }

    @Override
    public Attributes getAttributes(Name name, String[] ids) throws NamingException {
        return getAttributes(name.toString(), ids);
    }

    @Override
    public String toString() {
        return this.type;
    }

    // not used for this example

    @Override
    public Object lookup(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Object lookup(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void unbind(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void unbind(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void close() throws NamingException {
        throw new OperationNotSupportedException();
    }

    // -- DirContext
    @Override
    public void modifyAttributes(Name name, int modOp, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void modifyAttributes(String name, int modOp, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void bind(Name name, Object obj, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void bind(String name, Object obj, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rebind(Name name, Object obj, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rebind(String name, Object obj, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext getSchema(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext getSchema(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext getSchemaClassDefinition(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext getSchemaClassDefinition(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes, String[] attributesToReturn) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs, SearchControls cons) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs, SearchControls cons) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        throw new OperationNotSupportedException();
    }
}
