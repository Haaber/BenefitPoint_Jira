package com.vp.jira.project.data;

import net.java.ao.Entity;

public interface Return extends Entity {
    String getDescription();

    void setDescription(String description);

    double getContribution();

    void setContribution(double contribution);

    long getProjectID();

    void setProjectID(long projectID);
}
