package com.vp.jira.project.data;

import net.java.ao.Entity;

public interface Objective extends Entity {

    String getDescription();

    void setDescription(String description);

    long getProjectID();

    void setProjectID(long projectID);
}
