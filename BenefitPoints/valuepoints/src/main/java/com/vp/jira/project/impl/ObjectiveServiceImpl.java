package com.vp.jira.project.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.vp.jira.project.data.Objective;
import com.vp.jira.project.service.ObjectiveService;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class ObjectiveServiceImpl implements ObjectiveService {
    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public ObjectiveServiceImpl(ActiveObjects ao)
    {
        this.ao = checkNotNull(ao);
    }

    @Override
    public Objective add(String description, long projectID)
    {
        final Objective objective = ao.create(Objective.class);
        objective.setDescription(description);
        objective.setProjectID(projectID);
        objective.save();
        return objective;
    }

    @Override
    public void update(int id, String description){
        for (Objective ret: ao.find(Objective.class, "ID like ?", id)) {
            ret.setDescription(description);
            ret.save();
        }
    }

    @Override
    public List<Objective> all(){
        return newArrayList(ao.find(Objective.class));
    }

    @Override
    public void remove(Objective objective){
        ao.delete(objective);
    }
}
