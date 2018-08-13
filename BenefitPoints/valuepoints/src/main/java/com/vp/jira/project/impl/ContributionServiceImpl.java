package com.vp.jira.project.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.vp.jira.project.data.Contribution;
import com.vp.jira.project.service.ContributionService;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class ContributionServiceImpl implements ContributionService {
    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public ContributionServiceImpl(ActiveObjects ao)
    {
        this.ao = checkNotNull(ao);
    }

    @Override
    public Contribution add(double value, long returnID, long objectiveID, long projectID)
    {
        final Contribution contribution = ao.create(Contribution.class);
        contribution.setValue(value);
        contribution.setReturnID(returnID);
        contribution.setObjectiveID(objectiveID);
        contribution.setProjectID(projectID);
        contribution.save();
        return contribution;
    }

    @Override
    public List<Contribution> all()
    {
        return newArrayList(ao.find(Contribution.class));
    }

    @Override
    public void update(int id, double value){
        for (Contribution cont: ao.find(Contribution.class, "ID like ?", id)) {
            cont.setValue(value);
            cont.save();
        }
    }

    @Override
    public void remove(Contribution contribution){
        ao.delete(contribution);
    }
}
