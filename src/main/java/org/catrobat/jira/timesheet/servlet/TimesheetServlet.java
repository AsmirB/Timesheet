/*
 * Copyright 2016 Adrian Schnedlitz
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

package org.catrobat.jira.timesheet.servlet;

import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.service.ServiceException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;
import org.catrobat.jira.timesheet.activeobjects.Timesheet;
import org.catrobat.jira.timesheet.services.PermissionService;
import org.catrobat.jira.timesheet.services.TimesheetService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class TimesheetServlet extends HttpServlet {

    private final LoginUriProvider loginUriProvider;
    private final TemplateRenderer templateRenderer;
    private final TimesheetService sheetService;
    private final PermissionService permissionService;


    public TimesheetServlet(final LoginUriProvider loginUriProvider, final TemplateRenderer templateRenderer,
            final TimesheetService sheetService, final PermissionService permissionService) {
        this.loginUriProvider = loginUriProvider;
        this.templateRenderer = templateRenderer;
        this.sheetService = sheetService;
        this.permissionService = permissionService;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Response unauthorized = permissionService.checkUserPermission();
        if (unauthorized != null) {
            response.sendError(HttpServletResponse.SC_CONFLICT, unauthorized.getEntity().toString());
        }
        super.service(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ApplicationUser user = permissionService.checkIfUserExists();
            String userKey = user.getKey();
            Map<String, Object> paramMap = Maps.newHashMap();
            Timesheet timesheet;

            //info: testuser added
            if (permissionService.checkIfUserIsGroupMember("Administrators") ||
                    permissionService.checkIfUserIsGroupMember("administrators")) {
                paramMap.put("isadmin", true);
                timesheet = sheetService.getTimesheetByUser(userKey, false);
            } else {
                paramMap.put("isadmin", false);
                if (sheetService.userHasTimesheet(userKey, false)) {
                    timesheet = sheetService.getTimesheetByUser(userKey, false);
                } else {
                    timesheet = null;
                }
            }

            //check if user is Team-Coordinator
            //TODO: is administrator the correct group, or should it be timesheet
            if (permissionService.checkIfUserIsGroupMember("Administrators") ||
                    permissionService.checkIfUserIsGroupMember("administrators") ||
                    permissionService.isUserTeamCoordinator(user)) {
                paramMap.put("iscoordinator", true);
            } else {
                paramMap.put("iscoordinator", false);
            }

            if (timesheet == null) {
                timesheet = sheetService.add(userKey, 0, 0, 150, 0, 0, "Bachelor Thesis",
                        "", 5, true, false, false, true);
            }

            paramMap.put("timesheetid", timesheet.getID());
            paramMap.put("ismasterthesistimesheet", false);
            response.setContentType("text/html;charset=utf-8");
            templateRenderer.render("timesheet.vm", paramMap, response.getWriter());
        } catch (ServiceException e) {
            redirectToLogin(request, response);
        } catch (PermissionException e) {
            redirectToLogin(request, response);
        }
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

}
