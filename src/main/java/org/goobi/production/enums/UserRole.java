package org.goobi.production.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.sub.goobi.config.ConfigurationHelper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://www.intranda.com
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
    Task_Menu,
    Task_Mets_Pagination,
    Task_Mets_Structure,
    Task_Mets_Metadata,
    Task_Mets_Files,

    Workflow_General_Batches,
    Workflow_General_Details,
    Workflow_General_Details_Edit,
    Workflow_General_Menu,
    Workflow_General_Plugins,
    Workflow_General_Search,
    Workflow_General_Show_All_Projects,

    Workflow_ProcessTemplates,
    Workflow_ProcessTemplates_Import_Multi,
    Workflow_ProcessTemplates_Import_Single,
    Workflow_ProcessTemplates_Clone,
    Workflow_ProcessTemplates_Create,

    Workflow_Processes,
    Workflow_Processes_Allow_Download,
    Workflow_Processes_Allow_Export,
    Workflow_Processes_Allow_GoobiScript,
    Workflow_Processes_Allow_Linking,
    Workflow_Processes_Show_Finished,
    Workflow_Processes_Show_Deactivated_Projects,
    Workflow_Processes_Allow_Template_Change,
    Workflow_Processes_Show_Journal_File_Deletion,

    Statistics_CurrentUsers,
    Statistics_CurrentUsers_Details,
    Statistics_General,
    Statistics_Menu,
    Statistics_Plugins,

    Admin_Dockets,
    Admin_Harvester,
    Admin_Ldap,
    Admin_Menu,
    Admin_Plugins,
    Admin_Projects,
    Admin_Rulesets,
    Admin_Administrative_Tasks,
    Admin_Users,
    Admin_Users_Allow_Switch,
    Admin_Users_Change_Passwords,
    Admin_Usergroups,
    Admin_Export_Processdata,
    Admin_All_Mail_Notifications,
    Admin_Queue,
    Admin_Vocabulary,
    Admin_Vocabulary_Management,
    Admin_Jobtypes,
    Admin_Quartz,
    Admin_ApiKey,
    Admin_Templates,
    Admin_config_file_editor;

    public static List<String> getAllRoles() {
        List<String> roles = new ArrayList<>();
        for (UserRole ur : UserRole.values()) {
            roles.add(ur.name());
        }
        roles.addAll(ConfigurationHelper.getInstance().getAdditionalUserRights());

        Collections.sort(roles);
        return roles;
    }
}
