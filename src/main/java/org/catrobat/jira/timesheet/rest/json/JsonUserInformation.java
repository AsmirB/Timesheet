package org.catrobat.jira.timesheet.rest.json;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JsonUserInformation {
    @XmlElement
    private String userName;
    @XmlElement
    private String state;
    @XmlElement
    private int hoursPerMonth;
    @XmlElement
    private int hoursPerHalfYear;
    @XmlElement
    @JsonDeserialize(using = DateAndTimeDeserialize.class)
    private Date latestEntryDate;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getHoursPerMonth() {
        return hoursPerMonth;
    }

    public void setHoursPerMonth(int hoursPerMonth) {
        this.hoursPerMonth = hoursPerMonth;
    }

    public int getHoursPerHalfYear() {
        return hoursPerHalfYear;
    }

    public void setHoursPerHalfYear(int hoursPerHalfYear) {
        this.hoursPerHalfYear = hoursPerHalfYear;
    }

    public Date getLatestEntryDate() {
        return latestEntryDate;
    }

    public void setLatestEntryDate(Date latestEntryDate) {
        this.latestEntryDate = latestEntryDate;
    }
}
