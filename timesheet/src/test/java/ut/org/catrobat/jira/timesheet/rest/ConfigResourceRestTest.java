package ut.org.catrobat.jira.timesheet.rest;

import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.mail.queue.MailQueue;
import net.java.ao.EntityManager;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.catrobat.jira.timesheet.activeobjects.*;
import org.catrobat.jira.timesheet.activeobjects.impl.ConfigServiceImpl;
import org.catrobat.jira.timesheet.rest.ConfigResourceRest;
import org.catrobat.jira.timesheet.rest.TimesheetRest;
import org.catrobat.jira.timesheet.rest.json.JsonCategory;
import org.catrobat.jira.timesheet.rest.json.JsonConfig;
import org.catrobat.jira.timesheet.rest.json.JsonTeam;
import org.catrobat.jira.timesheet.services.*;
import org.catrobat.jira.timesheet.services.impl.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import ut.org.catrobat.jira.timesheet.activeobjects.MySampleDatabaseUpdater;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(ActiveObjectsJUnitRunner.class)
@Data(MySampleDatabaseUpdater.class)
@PrepareForTest({ComponentAccessor.class, ConfigResourceRest.class, TimesheetService.class,
        TimesheetEntryService.class})
public class ConfigResourceRestTest {

    private UserManager userManagerJiraMock;
    private GroupManager groupManagerJiraMock;

    private CategoryService categoryService;
    private ConfigService configService;
    private TimesheetService timesheetService;
    private TimesheetEntryService timesheetEntryService;
    private TeamService teamService;
    private PermissionService permissionService;

    private TimesheetService timesheetServiceMock;
    private TeamService teamServiceMock;
    private CategoryService categoryServiceMock;
    private TimesheetEntryService timesheetEntryServiceMock;
    private PermissionService permissionServiceMock;
    private ConfigService configServiceMock;

    private UserUtil userUtilMock;
    private Timesheet timesheetMock;
    private Category categoryMock;
    private Team teamMock;
    private TimesheetEntry timesheetEntryMock;

    private ConfigResourceRest configResourceRest;
    private ConfigResourceRest configResourceRestMock;
    private TimesheetRest spyTimesheetRest;

    private javax.ws.rs.core.Response response;
    private HttpServletRequest request;

    private MailQueue mailQueueMock;

    private SimpleDateFormat sdf;

    private TestActiveObjects ao;
    private EntityManager entityManager;
    private UserManager userManager;
    private ApplicationUser userMock;
    private JiraAuthenticationContext jiraAuthMock;
    private GroupManager groupManagerMock;

    @Before
    public void setUp() throws Exception {
        assertNotNull(entityManager);
        ao = new TestActiveObjects(entityManager);

        userManagerJiraMock = mock(UserManager.class, RETURNS_DEEP_STUBS);
        groupManagerJiraMock = mock(GroupManager.class, RETURNS_DEEP_STUBS);
        userUtilMock = mock(UserUtil.class, RETURNS_DEEP_STUBS);
        configServiceMock = mock(ConfigService.class, RETURNS_DEEP_STUBS);
        categoryServiceMock = mock(CategoryService.class, RETURNS_DEEP_STUBS);
        permissionServiceMock = mock(PermissionService.class, RETURNS_DEEP_STUBS);
        timesheetEntryServiceMock = mock(TimesheetEntryService.class, RETURNS_DEEP_STUBS);
        timesheetServiceMock = mock(TimesheetService.class, RETURNS_DEEP_STUBS);
        teamServiceMock = mock(TeamService.class, RETURNS_DEEP_STUBS);
        request = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
        mailQueueMock = mock(MailQueue.class, RETURNS_DEEP_STUBS);
        timesheetMock = mock(Timesheet.class, RETURNS_DEEP_STUBS);
        categoryMock = mock(Category.class, RETURNS_DEEP_STUBS);
        teamMock = mock(Team.class, RETURNS_DEEP_STUBS);
        timesheetEntryMock = mock(TimesheetEntry.class, RETURNS_DEEP_STUBS);
        userMock = mock(ApplicationUser.class, RETURNS_DEEP_STUBS);
        jiraAuthMock = mock(JiraAuthenticationContext.class, RETURNS_DEEP_STUBS);
        groupManagerMock = mock(GroupManager.class, RETURNS_DEEP_STUBS);

        categoryService = new CategoryServiceImpl(ao);
        configService = new ConfigServiceImpl(ao, categoryService);
        teamService = new TeamServiceImpl(ao, configService);
        permissionService = new PermissionServiceImpl(teamService, configService);
        timesheetEntryService = new TimesheetEntryServiceImpl(ao);
        timesheetService = new TimesheetServiceImpl(ao);
        configResourceRest = new ConfigResourceRest(configService, teamService, categoryService, permissionService);

        configResourceRestMock = new ConfigResourceRest(configServiceMock, teamServiceMock, categoryServiceMock, permissionServiceMock);

        PowerMockito.mockStatic(ComponentAccessor.class);
        PowerMockito.when(ComponentAccessor.getUserManager()).thenReturn(userManagerJiraMock);
        PowerMockito.when(ComponentAccessor.getUserUtil()).thenReturn(userUtilMock);
        PowerMockito.when(ComponentAccessor.getJiraAuthenticationContext()).thenReturn(jiraAuthMock);
        PowerMockito.when(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()).thenReturn(userMock);
        PowerMockito.when(ComponentAccessor.getGroupManager()).thenReturn(groupManagerMock);

        //additional mocks
        when(permissionServiceMock.checkIfUserExists(request)).thenReturn(userMock);
    }

    @Test
    public void testGetTeamsOk() throws Exception {
        String userName = "test";
        String userKey = "USER_KEY_1";

        Category[] categories = {categoryMock};

        Team team1 = Mockito.mock(Team.class);
        when(team1.getID()).thenReturn(1);
        when(team1.getTeamName()).thenReturn("Catroid");
        when(team1.getCategories()).thenReturn(categories);

        Team team2 = Mockito.mock(Team.class);
        when(team2.getID()).thenReturn(2);
        when(team2.getTeamName()).thenReturn("IRC");
        when(team2.getCategories()).thenReturn(new Category[0]);

        Set<Team> teams = new HashSet<Team>();
        teams.add(team1);
        teams.add(team2);

        ApplicationUser user1 = mock(ApplicationUser.class);
        ApplicationUser user2 = mock(ApplicationUser.class);

        //user1 should be the testUser
        when(user1.getName()).thenReturn(userName);

        when(ComponentAccessor.getUserManager().getUserByName(anyString()).getKey()).thenReturn(userKey);

        when(teamServiceMock.getTeamsOfUser(anyString())).thenReturn(teams);

        response = configResourceRest.getTeams(request);
        List<JsonTeam> responseTeamList = (List<JsonTeam>) response.getEntity();
        assertNotNull(responseTeamList);
    }

    @Test
    public void testGetCategoriesOk() throws Exception {
        response = configResourceRest.getCategories(request);
        List<JsonCategory> responseTeamList = (List<JsonCategory>) response.getEntity();
        assertNotNull(responseTeamList);
    }

    @Test
    public void testRemoveCategoryOk() throws Exception {
        String userName = "test";
        String userKey = "USER_KEY_1";

        Category[] categories = {categoryMock};

        Team team1 = Mockito.mock(Team.class);
        when(team1.getID()).thenReturn(1);
        when(team1.getTeamName()).thenReturn("Catroid");
        when(team1.getCategories()).thenReturn(categories);

        Team team2 = Mockito.mock(Team.class);
        when(team2.getID()).thenReturn(2);
        when(team2.getTeamName()).thenReturn("IRC");
        when(team2.getCategories()).thenReturn(new Category[0]);

        Set<Team> teams = new HashSet<>();
        teams.add(team1);
        teams.add(team2);

        ApplicationUser user1 = mock(ApplicationUser.class);
        ApplicationUser user2 = mock(ApplicationUser.class);

        //user1 should be the testUser
        when(user1.getName()).thenReturn(userName);

        when(ComponentAccessor.getUserManager().getUserByName(anyString()).getKey()).thenReturn(userKey);


        when(permissionServiceMock.checkPermission(request)).thenReturn(response);
        when(categoryServiceMock.removeCategory(anyString())).thenReturn(true);

        response = configResourceRestMock.removeCategory(anyString(), request);
        assertNull(response.getEntity());
    }

    @Test
    public void testGetTeamListOk() throws Exception {
        String userName = "test";
        String userKey = "USER_KEY_1";

        Category[] categories = {categoryMock};

        Team team1 = Mockito.mock(Team.class);
        when(team1.getID()).thenReturn(1);
        when(team1.getTeamName()).thenReturn("Catroid");
        when(team1.getCategories()).thenReturn(categories);

        Team team2 = Mockito.mock(Team.class);
        when(team2.getID()).thenReturn(2);
        when(team2.getTeamName()).thenReturn("IRC");
        when(team2.getCategories()).thenReturn(new Category[0]);

        Team[] teams = {team1, team2};

        when(ComponentAccessor.getUserManager().getUserByName(anyString()).getKey()).thenReturn(userKey);


        when(permissionServiceMock.checkPermission(request)).thenReturn(response);
        when(configServiceMock.getConfiguration().getTeams()).thenReturn(teams);

        response = configResourceRestMock.getTeamList(request);
        assertNotNull(response.getEntity());
    }

    @Test
    public void testGetConfigOk() throws Exception {
        Category[] categories = {categoryMock};

        Team team1 = Mockito.mock(Team.class);
        when(team1.getID()).thenReturn(1);
        when(team1.getTeamName()).thenReturn("Catroid");
        when(team1.getCategories()).thenReturn(categories);

        Team team2 = Mockito.mock(Team.class);
        when(team2.getID()).thenReturn(2);
        when(team2.getTeamName()).thenReturn("IRC");
        when(team2.getCategories()).thenReturn(new Category[0]);

        Team[] teams = {team1, team2};

        ApprovedUser approvedUser1 = Mockito.mock(ApprovedUser.class);
        when(approvedUser1.getUserKey()).thenReturn("USER_KEY1");
        when(approvedUser1.getUserName()).thenReturn("User1");

        ApprovedUser approvedUser2 = Mockito.mock(ApprovedUser.class);
        when(approvedUser2.getUserKey()).thenReturn("USER_KEY2");
        when(approvedUser2.getUserName()).thenReturn("User2");

        ApprovedUser[] approvedUsers = {approvedUser1, approvedUser2};

        ApprovedGroup approvedGroup = Mockito.mock(ApprovedGroup.class);
        when(approvedGroup.getGroupName()).thenReturn("ApprovedGroup");

        ApprovedGroup[] approvedGroups = {approvedGroup};

        when(permissionServiceMock.checkPermission(request)).thenReturn(response);
        when(configServiceMock.getConfiguration().getTeams()).thenReturn(teams);
        when(configServiceMock.getConfiguration().getApprovedGroups()).thenReturn(approvedGroups);
        when(configServiceMock.getConfiguration().getApprovedUsers()).thenReturn(approvedUsers);

        response = configResourceRestMock.getConfig(request);
        assertNotNull(response.getEntity());
    }


    @Test
    public void testSetConfigOk() throws Exception {
        Category[] categories = {categoryMock};

        Team team1 = Mockito.mock(Team.class);
        when(team1.getID()).thenReturn(1);
        when(team1.getTeamName()).thenReturn("Catroid");
        when(team1.getCategories()).thenReturn(categories);

        Team team2 = Mockito.mock(Team.class);
        when(team2.getID()).thenReturn(2);
        when(team2.getTeamName()).thenReturn("IRC");
        when(team2.getCategories()).thenReturn(new Category[0]);

        Team[] teams = {team1, team2};

        ApprovedUser approvedUser1 = Mockito.mock(ApprovedUser.class);
        when(approvedUser1.getUserKey()).thenReturn("USER_KEY1");
        when(approvedUser1.getUserName()).thenReturn("User1");

        ApprovedUser approvedUser2 = Mockito.mock(ApprovedUser.class);
        when(approvedUser2.getUserKey()).thenReturn("USER_KEY2");
        when(approvedUser2.getUserName()).thenReturn("User2");

        ApprovedUser[] approvedUsers = {approvedUser1, approvedUser2};

        ApprovedGroup approvedGroup = Mockito.mock(ApprovedGroup.class);
        when(approvedGroup.getGroupName()).thenReturn("ApprovedGroup");

        ApprovedGroup[] approvedGroups = {approvedGroup};

        ApplicationUser user1 = mock(ApplicationUser.class);
        ApplicationUser user2 = mock(ApplicationUser.class);

        Collection<ApplicationUser> usersInGroup = new ArrayList();
        usersInGroup.add(user1);
        usersInGroup.add(user2);

        when(permissionServiceMock.checkPermission(request)).thenReturn(response);
        when(configServiceMock.getConfiguration().getTeams()).thenReturn(teams);
        when(configServiceMock.getConfiguration().getApprovedGroups()).thenReturn(approvedGroups);
        when(configServiceMock.getConfiguration().getApprovedUsers()).thenReturn(approvedUsers);

        PowerMockito.when(ComponentAccessor.getGroupManager().getUsersInGroup(anyString())).thenReturn(usersInGroup);

        JsonConfig jsonConfig = new JsonConfig(configServiceMock);

        response = configResourceRestMock.setConfig(jsonConfig, request);
        assertNull(response.getEntity());
    }
}
