package com.vp.jira.project.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.vp.jira.project.data.Objective;

import java.util.List;

@Transactional
public interface ObjectiveService {
    Objective add(String description, long projectID);

    void update(int id, String description);

    void remove(Objective objective);

    List<Objective> all();
}
