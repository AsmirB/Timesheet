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

package org.catrobat.jira.timesheet.servlet;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.service.ServiceException;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.websudo.WebSudoManager;
import org.catrobat.jira.timesheet.activeobjects.*;
import org.catrobat.jira.timesheet.helper.CsvConfigImporter;
import org.catrobat.jira.timesheet.services.CategoryService;
import org.catrobat.jira.timesheet.services.PermissionService;
import org.catrobat.jira.timesheet.services.TeamService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ImportConfigCsvServlet extends HelperServlet {

    private final ConfigService configService;
    private final CategoryService categoryService;
    private final TeamService teamService;
    private final ActiveObjects activeObjects;

    public ImportConfigCsvServlet(LoginUriProvider loginUriProvider, WebSudoManager webSudoManager,
                                  ConfigService configService, CategoryService categoryService,
                                  TeamService teamService, ActiveObjects activeObjects,
                                  PermissionService permissionService) {
        super(loginUriProvider, webSudoManager, permissionService);
        this.configService = configService;
        this.categoryService = categoryService;
        this.teamService = teamService;
        this.activeObjects = activeObjects;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        super.doGet(request, response);

        // Dangerous servlet - should be forbidden in production use
        if (configService.getConfiguration().getTeams().length != 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        PrintWriter writer = response.getWriter();
        writer.print("<html>" +
                "<body>" +
                "<h1>Dangerzone!</h1>" +
                "Just upload files when you know what you're doing - this upload will manipulate the database!<br />" +
                "<form action=\"config\" method=\"post\"><br />" +
                "<textarea name=\"csv\" rows=\"20\" cols=\"175\" wrap=\"off\">" +
                "# lines beginning with '#' are comments and will be ignored;;;;;;;;;;;;;;;;;;;;;;;\n" +
                "# Name;Version;Type of Device;Operating System;Producer;Article Number;Price;IMEI;Serial Number;Inventory Number;Received Date;Received From;Useful Life Of Asset;Sorted Out Comment;Sorted Out Date;Lending Begin;Lending End;Lending Purpose;Lending Comment;Lending Issuer;Lent By;Device Comment;Device Comment Author;Device Comment Date\n" +
                "Nexus 6;32 GB;Smartphone;Android Lollipop;Motorola;1337;600;123123;123123;123123;10/14/2014;Google Inc.;3 Years;Sorted Out Comment;10/14/2014;10/14/2014;10/14/2014;testing;just lending;issuer;lent by;Device Comment;comment author;10/14/2014\n" +
                "Nexus 6;32 GB;Smartphone;Android Lollipop;Motorola;1337;600;234234;234234;234234;10/14/2014;Google Inc.;3 Years;;;;;;;;;;;\n" +
                "Nexus 6;32 GB;Smartphone;Android Lollipop;Motorola;1337;600;345345;345345;345345;10/14/2014;Google Inc.;3 Years;;;10/14/2014;;testing 3;just lending 3;issuer 3;lent by 3;Device Comment 3;comment author 3;10/14/2014\n" +
                "</textarea><br />\n" +
                "<input type=\"checkbox\" name=\"drop\" value=\"drop\">Drop existing table entries<br /><br />\n" +
                "<input type=\"submit\" />" +
                "</form>" +
                "</body>" +
                "</html>");
        writer.flush();
        writer.close();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);

        // Dangerous servlet - should be forbidden in production use
        if (configService.getConfiguration().getTeams().length != 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String csvString = request.getParameter("csv");

        if (request.getParameter("drop") != null && request.getParameter("drop").equals("drop")) {
            dropEntries();
        }

        CsvConfigImporter csvImporter = new CsvConfigImporter(configService, categoryService, teamService);
        String errorString = null;
        try {
            errorString = csvImporter.importCsv(csvString);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        response.getWriter().print("Successfully executed following string:<br />" +
                "<textarea rows=\"20\" cols=\"200\" wrap=\"off\" disabled>" + csvString + "</textarea>" +
                "<br /><br />" +
                "Following errors occurred:<br />" + errorString);
    }

    private void dropEntries() {
        activeObjects.deleteWithSQL(ApprovedGroup.class, "1=?", "1");
        activeObjects.deleteWithSQL(ApprovedUser.class, "1=?", "1");
        activeObjects.deleteWithSQL(Category.class, "1=?", "1");
        activeObjects.deleteWithSQL(CategoryToTeam.class, "1=?", "1");
        activeObjects.deleteWithSQL(Config.class, "1=?", "1");
        activeObjects.deleteWithSQL(Group.class, "1=?", "1");
        activeObjects.deleteWithSQL(Team.class, "1=?", "1");
        activeObjects.deleteWithSQL(TeamToGroup.class, "1=?", "1");
    }
}