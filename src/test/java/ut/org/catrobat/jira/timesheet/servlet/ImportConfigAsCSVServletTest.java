package ut.org.catrobat.jira.timesheet.servlet;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.websudo.WebSudoManager;
import net.java.ao.EntityManager;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.catrobat.jira.timesheet.activeobjects.TimesheetAdmin;
import org.catrobat.jira.timesheet.activeobjects.Config;
import org.catrobat.jira.timesheet.services.ConfigService;
import org.catrobat.jira.timesheet.services.impl.ConfigServiceImpl;
import org.catrobat.jira.timesheet.rest.TimesheetRest;
import org.catrobat.jira.timesheet.services.*;
import org.catrobat.jira.timesheet.servlet.ImportConfigCsvServlet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import ut.org.catrobat.jira.timesheet.activeobjects.MySampleDatabaseUpdater;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(ActiveObjectsJUnitRunner.class)
@Data(MySampleDatabaseUpdater.class)
@PrepareForTest({ComponentAccessor.class, TimesheetRest.class, TimesheetService.class,
        TimesheetEntryService.class})
public class ImportConfigAsCSVServletTest {

    String test_key = "test_key";
    private ImportConfigCsvServlet importConfigCsvServlet;
    private EntityManager entityManager;
    private ActiveObjects ao;
    private LoginUriProvider loginUriProvider;
    private PermissionService permissionService;
    private WebSudoManager webSudoManager;
    private ConfigService configService;
    private Config config;
    private CategoryService categoryService;
    private TeamService teamService;
    private PrintWriter printWriter;
    private HttpServletResponse response;
    private HttpServletRequest request;
    private ApplicationUser user;
    private ServletOutputStream outputStream;
    private CategoryService cs;
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Before
    public void setUp() throws Exception {
        new MockComponentWorker().init();

        PowerMockito.mockStatic(ComponentAccessor.class);

        assertNotNull(entityManager);
        ao = new TestActiveObjects(entityManager);
        configService = new ConfigServiceImpl(ao, cs);

        loginUriProvider = mock(LoginUriProvider.class);
        webSudoManager = mock(WebSudoManager.class);
        permissionService = mock(PermissionService.class);
        user = mock(ApplicationUser.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        outputStream = mock(ServletOutputStream.class);
        config = mock(Config.class);
        categoryService = mock(CategoryService.class);
        teamService = mock(TeamService.class);
        printWriter = mock(PrintWriter.class);
        configService = mock(ConfigService.class);
        jiraAuthenticationContext = mock(JiraAuthenticationContext.class);

        importConfigCsvServlet = new ImportConfigCsvServlet(loginUriProvider, webSudoManager,
                configService, categoryService, teamService, ao, permissionService);

        when(user.getUsername()).thenReturn("test");
        when(user.getKey()).thenReturn(test_key);

        when(permissionService.checkIfUserExists()).thenReturn(user);

        when(permissionService.checkIfUserIsGroupMember(PermissionService.JIRA_ADMINISTRATORS)).thenReturn(false);
        when(permissionService.checkIfUserIsGroupMember("Timesheet")).thenReturn(true);

        when(response.getOutputStream()).thenReturn(outputStream);
        when(response.getWriter()).thenReturn(printWriter);

        when(configService.getConfiguration()).thenReturn(config);
        when(config.getTimesheetAdminUsers()).thenReturn(new TimesheetAdmin[0]);

        PowerMockito.when(ComponentAccessor.getJiraAuthenticationContext()).thenReturn(jiraAuthenticationContext);
        PowerMockito.when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(user);
    }

    @Test
    public void testDoGet() throws Exception {
        importConfigCsvServlet.doGet(request, response);
    }

    @Test
    public void testDoPosNoDrop() throws Exception {
        String csvString = ">This\n" +
                "is\n" +
                "a\n" +
                "Test";

        Mockito.when(request.getParameter("csv")).thenReturn(csvString);
        Mockito.when(request.getParameter("drop")).thenReturn(null);

        importConfigCsvServlet.doPost(request, response);
    }
}
