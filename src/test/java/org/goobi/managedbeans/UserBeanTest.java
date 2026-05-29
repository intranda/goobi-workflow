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
 */
package org.goobi.managedbeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserBeanTest {

    @Test
    void testCreateRandomPasswordHasCorrectLength() {
        assertEquals(12, UserBean.createRandomPassword(12).length());
        assertEquals(8, UserBean.createRandomPassword(8).length());
    }

    @Test
    void testCreateRandomPasswordContainsAllCharacterClasses() {
        // Use length 200 so all character classes appear with overwhelming probability
        String password = UserBean.createRandomPassword(200);
        assertTrue(password.chars().anyMatch(Character::isUpperCase), "Password must contain uppercase letters");
        assertTrue(password.chars().anyMatch(Character::isLowerCase), "Password must contain lowercase letters");
        assertTrue(password.chars().anyMatch(Character::isDigit), "Password must contain digits");
        assertTrue(password.chars().anyMatch(c -> !Character.isLetterOrDigit(c) && c >= 33 && c <= 126),
                "Password must contain printable special characters");
    }

    @Test
    void testCreateRandomPasswordOnlyContainsPrintableAscii() {
        String password = UserBean.createRandomPassword(200);
        assertTrue(password.chars().allMatch(c -> c >= 33 && c <= 126),
                "Password must only contain printable non-whitespace ASCII characters");
    }
}
