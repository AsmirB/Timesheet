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

package org.catrobat.jira.timesheet.rest.json;

import org.catrobat.jira.timesheet.activeobjects.Team;
import org.catrobat.jira.timesheet.activeobjects.TeamToGroup;
import org.catrobat.jira.timesheet.services.ConfigService;
import org.catrobat.jira.timesheet.services.TeamService;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)

/* Info: @deprecated
 * for further projects / classes use the very powerful GSON library from Google. Have a look here:
 * https://github.com/google/gson/blob/master/UserGuide.md
 * It is easier to use and you haven't create a class for each object you would like to serialise.
 * BTW: it is already included in this project, so feel free to use it up to now.
*/
public class JsonTeam {

    @XmlElement
    private int teamID;
    @XmlElement
    private String teamName;
    @XmlElement
    private List<Integer> categoryIDs;
    @XmlElement
    private List<String> teamCategoryNames;
    @XmlElement
    private List<String> coordinatorGroups;
    @XmlElement
    private List<String> developerGroups;

    public JsonTeam() {

    }

    public JsonTeam(int teamID, String teamName, List<Integer> categoryIDs) {
        this.teamID = teamID;
        this.teamName = teamName;
        this.categoryIDs = categoryIDs;
    }

    public JsonTeam(String name) {
        this.teamName = name;
        coordinatorGroups = new ArrayList<>();
        developerGroups = new ArrayList<>();
    }

    public JsonTeam(Team toCopy, ConfigService configService, TeamService teamService) {
        this.teamID = toCopy.getID();
        this.teamName = toCopy.getTeamName();
        this.coordinatorGroups = teamService.getGroupsForRole(this.teamName, TeamToGroup.Role.COORDINATOR);
        this.developerGroups = teamService.getGroupsForRole(this.teamName, TeamToGroup.Role.DEVELOPER);
        this.teamCategoryNames = configService.getCategoryNamesForTeam(this.teamName);
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public List<String> getTeamCategoryNames() {
        return teamCategoryNames;
    }

    public void setTeamCategoryNames(List<String> teamCategoryNames) {
        this.teamCategoryNames = teamCategoryNames;
    }

    public List<String> getCoordinatorGroups() {
        return coordinatorGroups;
    }

    public void setCoordinatorGroups(List<String> coordinatorGroups) {
        this.coordinatorGroups = coordinatorGroups;
    }

    public List<String> getDeveloperGroups() {
        return developerGroups;
    }

    public void setDeveloperGroups(List<String> developerGroups) {
        this.developerGroups = developerGroups;
    }

    @Override
    public int hashCode() {
        return 7;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JsonTeam other = (JsonTeam) obj;
        if (this.teamID != other.teamID) {
            return false;
        }
        if ((this.teamName == null) ? (other.teamName != null) : !this.teamName.equals(other.teamName)) {
            return false;
        }
        return categoryIDs.equals(other.categoryIDs);
    }

    @Override
    public String toString() {
        return "JsonTeam{" +
                "teamID=" + teamID +
                ", teamName='" + teamName + '\'' +
                ", coordinatorGroups ='" + coordinatorGroups + '\'' +
                ", developerGroups ='" + developerGroups + '\'' +
                ", categoryIDs='" + categoryIDs + '\'' +
                '}';
    }
}
