package com.vp.jira.project.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.vp.jira.project.data.Return;
import com.vp.jira.project.service.ReturnService;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class ReturnServiceImpl implements ReturnService {
    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public ReturnServiceImpl(ActiveObjects ao)
    {
        this.ao = checkNotNull(ao);
    }

    @Override
    public Return add(String description, double contribution, long projectID)
    {
        final Return return1 = ao.create(Return.class);
        return1.setDescription(description);
        return1.setContribution(contribution);
        return1.setProjectID(projectID);
        return1.save();
        return return1;
    }

    @Override
    public void update(int id, String description, double contribution){
        for (Return ret: ao.find(Return.class, "ID like ?", id)) {
            ret.setDescription(description);
            ret.setContribution(contribution);
            ret.save();
        }
    }

    @Override
    public List<Return> all()
    {
        return newArrayList(ao.find(Return.class));
    }

    @Override
    public void remove(Return return1){
        ao.delete(return1);
    }
}
