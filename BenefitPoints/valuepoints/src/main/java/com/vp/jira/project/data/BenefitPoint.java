package com.vp.jira.project.data;

import net.java.ao.Entity;

public interface BenefitPoint extends Entity {

    int getValue();

    void setValue(int value);

    long getEpicID();

    void setEpicID(long epicID);

    long getObjectiveID();

    void setObjectiveID(long objectiveID);

    long getProjectID();

    void setProjectID(long projectID);
}
