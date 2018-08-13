package com.vp.jira.project.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.vp.jira.project.data.Contribution;

import java.util.List;

@Transactional
public interface ContributionService {
    Contribution add(double value, long returnID, long objectiveID, long projectID);

    void update(int id, double value);

    void remove(Contribution contribution);

    List<Contribution> all();
}
