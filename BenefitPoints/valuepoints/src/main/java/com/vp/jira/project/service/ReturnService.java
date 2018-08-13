package com.vp.jira.project.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.vp.jira.project.data.Return;

import java.util.List;

@Transactional
public interface ReturnService {
    Return add(String description, double contribution, long projectID);

    void update(int id, String description, double contribution);

    void remove(Return return1);

    List<Return> all();
}
