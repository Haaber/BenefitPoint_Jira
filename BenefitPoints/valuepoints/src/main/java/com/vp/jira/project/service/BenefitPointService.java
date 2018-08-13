package com.vp.jira.project.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.vp.jira.project.data.BenefitPoint;

import java.util.List;

@Transactional
public interface BenefitPointService {
    BenefitPoint add(int value, long epicID, long objectiveID, long projectID);

    void update(int id, int value);

    void remove(BenefitPoint benefitPoint);

    List<BenefitPoint> all();
}
