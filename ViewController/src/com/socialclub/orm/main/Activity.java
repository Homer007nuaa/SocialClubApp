package com.socialclub.orm.main;

import com.socialclub.orm.annotation.ForeignColumn;
import com.socialclub.orm.annotation.Key;

import java.util.Date;
@Key(KeyColumns = {"activityId"})
public class Activity {
    public Activity() {
        super();
    }


    public Activity(String activityId, String activityName, String location, Date startDate, PersonInfo[] attendee,
                    PersonInfo leader) {
        super();
        this.activityId = activityId;
        this.activityName = activityName;
        this.location = location;
        this.startDate = startDate;
        this.attendee = attendee;
        this.leader = leader;
    }

    public String activityId;
    public String activityName ;
    public String location ;
    public Date startDate ;
    @ForeignColumn(foreignKey = {"activityId"})
    public PersonInfo[] attendee;
    @ForeignColumn(foreignKey = {"activityId"})
    public PersonInfo leader;


    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setAttendee(PersonInfo[] attendee) {
        this.attendee = attendee;
    }

    public PersonInfo[] getAttendee() {
        return attendee;
    }

    public void setLeader(PersonInfo leader) {
        this.leader = leader;
    }

    public PersonInfo getLeader() {
        return leader;
    }
}
