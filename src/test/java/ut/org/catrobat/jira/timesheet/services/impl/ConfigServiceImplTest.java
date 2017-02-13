/*
 * Copyright 2014 Stephan Fellhofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ut.org.catrobat.jira.timesheet.services.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import net.java.ao.EntityManager;
import net.java.ao.Query;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.catrobat.jira.timesheet.activeobjects.CategoryToTeam;
import org.catrobat.jira.timesheet.activeobjects.Config;
import org.catrobat.jira.timesheet.services.*;
import org.catrobat.jira.timesheet.activeobjects.Team;
import org.catrobat.jira.timesheet.services.impl.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ut.org.catrobat.jira.timesheet.activeobjects.MySampleDatabaseUpdater;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(MySampleDatabaseUpdater.class)
public class ConfigServiceImplTest {

    @SuppressWarnings("UnusedDeclaration")
    private EntityManager entityManager;
    private ActiveObjects ao;
    private CategoryService cs;
    private TimesheetEntryService entryService;
    private TeamService teamService;
    private TimesheetService timesheetService;
    private ConfigService configurationService;

    @Before
    public void setUp() throws Exception {
        assertNotNull(entityManager);
        ao = new TestActiveObjects(entityManager);
        cs = new CategoryServiceImpl(ao);
        timesheetService = new TimesheetServiceImpl(ao);
        entryService = new TimesheetEntryServiceImpl(ao, timesheetService);
        teamService = new TeamServiceImpl(ao, entryService);
        configurationService = new ConfigServiceImpl(ao, cs, teamService);
    }

    @Test
    public void testGetConfiguration() {
        assertEquals(0, ao.find(Config.class).length);
        assertNotNull(configurationService.getConfiguration());
        ao.flushAll();
        configurationService.getConfiguration();
        configurationService.getConfiguration();
        ao.flushAll();
        assertEquals(1, ao.find(Config.class).length);

        Config configuration = configurationService.getConfiguration();
        assertTrue(configuration.getID() != 0);
        assertEquals(0, configuration.getTimesheetAdminUsers().length);
        assertEquals(0, configuration.getTimesheetAdminGroups().length);
        assertEquals(0, configuration.getTeams().length);
    }

    @Test
    public void testAddedTeamHasSpecialCategories() {
        Team testteam = configurationService.addTeam("Test", null, null, null);
        CategoryToTeam[] categoryToTeamArray = ao.find(CategoryToTeam.class, Query.select().where("\"TEAM_ID\" = ?", testteam.getID()));

        List<CategoryToTeam> categoryToTeamList = Arrays.asList(categoryToTeamArray);

        for (String specialCategory : SpecialCategories.DefaultCategories) {
            Assert.assertFalse(categoryToTeamList.contains(specialCategory));
        }
    }

    @Test
    public void testEditReadOnlyUsers() {
        String editedReadOnlyUsers = "EditedReadOnlyUsers";
        Config config = configurationService.editReadOnlyUsers(editedReadOnlyUsers);
        assertEquals(editedReadOnlyUsers, config.getReadOnlyUsers());
    }

    @Test
    public void testEditMail() {
        String mailFromName = "mailFrom";
        String mailFrom = "mailFrom";
        String mailSubjectTime = "mailSubjectTime";
        String mailSubjectInactive = "mailSubjectInactive";
        String mailSubjectOffline = "mailSubjectOffline";
        String mailSubjectActive = "mailSubjectActive";
        String mailSubjectEntry = "mailSubjectEntry";
        String mailBodyTime = "mailBodyTime";
        String mailBodyInactive = "mailBodyInactive";
        String mailBodyOffline = "mailBodyOffline";
        String mailBodyActive = "mailBodyActive";
        String mailBodyEntry = "mailBodyEntry";

        Config config = configurationService.editMail(mailFromName, mailFrom, mailSubjectTime,
                mailSubjectInactive, mailSubjectOffline, mailSubjectActive, mailSubjectEntry,
                mailBodyTime, mailBodyInactive, mailBodyOffline, mailBodyActive, mailBodyEntry);

        assertEquals(mailFromName, config.getMailFromName());
        assertEquals(mailFrom, config.getMailFrom());
        assertEquals(mailSubjectTime, config.getMailSubjectTime());
        assertEquals(mailSubjectInactive, config.getMailSubjectInactiveState());
        assertEquals(mailSubjectOffline, config.getMailSubjectOfflineState());
        assertEquals(mailSubjectActive, config.getMailSubjectActiveState());
        assertEquals(mailSubjectEntry, config.getMailSubjectEntry());
        assertEquals(mailBodyTime, config.getMailBodyTime());
        assertEquals(mailBodyInactive, config.getMailBodyInactiveState());
        assertEquals(mailBodyOffline, config.getMailBodyOfflineState());
        assertEquals(mailBodyActive, config.getMailBodyActiveState());
        assertEquals(mailBodyEntry, config.getMailBodyEntry());
    }
}
