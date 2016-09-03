package ut.org.catrobat.jira.timesheet.servlet;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import net.java.ao.EntityManager;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.catrobat.jira.timesheet.activeobjects.Config;
import org.catrobat.jira.timesheet.activeobjects.ConfigService;
import org.catrobat.jira.timesheet.activeobjects.impl.ConfigServiceImpl;
import org.catrobat.jira.timesheet.services.*;
import org.catrobat.jira.timesheet.servlet.ImportTimesheetCsvServlet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import ut.org.catrobat.jira.timesheet.activeobjects.MySampleDatabaseUpdater;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.junit.Assert.assertNotNull;

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(MySampleDatabaseUpdater.class)
public class ImportAllTimesheetsServletTest {

    private ImportTimesheetCsvServlet importTimesheetCsvServlet;

    private EntityManager entityManager;
    private ActiveObjects ao;
    private LoginUriProvider loginUriProvider;
    private TemplateRenderer templateRenderer;
    private PermissionService permissionService;
    private WebSudoManager webSudoManager;
    private ConfigService configService;
    private ComponentAccessor componentAccessor;
    private TimesheetService timesheetService;
    private Config config;
    private CategoryService categoryService;
    private TeamService teamService;
    private TimesheetEntryService timesheetEntryService;
    private PrintWriter printWriter;

    private HttpServletResponse response;
    private HttpServletRequest request;

    UserKey test_key = new UserKey("test_key");
    private UserProfile userProfile;
    private ServletOutputStream outputStream;
    private CategoryService cs;

    @Before
    public void setUp() throws Exception {
        new MockComponentWorker().init();

        assertNotNull(entityManager);
        ao = new TestActiveObjects(entityManager);
        configService = new ConfigServiceImpl(ao, cs);

        loginUriProvider = Mockito.mock(LoginUriProvider.class);
        templateRenderer = Mockito.mock(TemplateRenderer.class);
        webSudoManager = Mockito.mock(WebSudoManager.class);
        permissionService = Mockito.mock(PermissionService.class);
        componentAccessor = Mockito.mock(ComponentAccessor.class);
        timesheetService = Mockito.mock(TimesheetService.class);
        userProfile = Mockito.mock(UserProfile.class);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        outputStream = Mockito.mock(ServletOutputStream.class);
        config = Mockito.mock(Config.class);
        categoryService = Mockito.mock(CategoryService.class);
        teamService = Mockito.mock(TeamService.class);
        timesheetEntryService = Mockito.mock(TimesheetEntryService.class);
        printWriter = Mockito.mock(PrintWriter.class);

        importTimesheetCsvServlet = new ImportTimesheetCsvServlet(loginUriProvider, webSudoManager,
                configService, timesheetService, timesheetEntryService, ao, permissionService,
                categoryService, teamService);

        Mockito.when(userProfile.getUsername()).thenReturn("test");
        Mockito.when(userProfile.getUserKey()).thenReturn(test_key);

        Mockito.when(permissionService.checkIfUserExists(request)).thenReturn(userProfile);

        Mockito.when(permissionService.checkIfUserIsGroupMember(request, "jira-administrators")).thenReturn(false);
        Mockito.when(permissionService.checkIfUserIsGroupMember(request, "Timesheet")).thenReturn(true);

        Mockito.when(response.getOutputStream()).thenReturn(outputStream);
        Mockito.when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    public void testDoGet() throws Exception {
        importTimesheetCsvServlet.doGet(request, response);
    }

    @Test
    public void testDoPosNoDrop() throws Exception {
        String csvString = ">This\n" +
                "is\n" +
                "a\n" +
                "Test";

        Mockito.when(request.getParameter("csv")).thenReturn(csvString);
        Mockito.when(request.getParameter("drop")).thenReturn(null);

        importTimesheetCsvServlet.doPost(request, response);
    }
}
