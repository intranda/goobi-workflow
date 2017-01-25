package org.goobi.production.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * 
 */
public enum UserRole {
	
	Task_List, 
	Task_Mets_Pagination, 
	Task_Mets_Structure, 
	Task_Mets_Metadata, 
	Task_Mets_Files, 
	
	Workflow_Batches,
	Workflow_Details, 
	Workflow_Import_Multi, 
	Workflow_Import_Single,
	Workflow_Processes,
	Workflow_ProcessTemplates,
	Workflow_Search,
	
	Admin_Dockets, 
	Admin_Ldap, 
	Admin_Projects,
	Admin_Rulesets,
	Admin_Users, 
	Admin_Usergroups;
	
	public static List<String> getAllRoles() {
		List<String> roles = new ArrayList<String>();
		for (UserRole ur : UserRole.values()) {
			roles.add(ur.name());
		}
		Collections.sort(roles);
		return roles;
	}
}
