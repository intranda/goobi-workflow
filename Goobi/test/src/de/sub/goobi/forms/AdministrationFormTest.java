package de.sub.goobi.forms;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
 */
import static org.junit.Assert.*;

import org.junit.Test;

public class AdministrationFormTest {

    @Test
    public void testConstructor() {
       AdministrationForm form = new AdministrationForm();
       assertNotNull(form);
    }
    
    @Test
    public void testLogin() {
        AdministrationForm form = new AdministrationForm();
        form.setPasswort("wrong password");
        form.Weiter();
        
        form.setPasswort("test");
        assertEquals("test", form.getPasswort());
        form.Weiter();
        assertEquals("098f6bcd4621d373cade4e832627b4f6", form.getPasswort());
        
    }

}
