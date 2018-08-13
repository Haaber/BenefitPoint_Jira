package com.vp.jira.project.data;

import net.java.ao.Entity;

public interface Contribution extends Entity {

    double getValue();

    void setValue(double value);

    long getReturnID();

    void setReturnID(long returnID);

    long getObjectiveID();

    void setObjectiveID(long objectiveID);

    long getProjectID();

    void setProjectID(long projectID);
}
