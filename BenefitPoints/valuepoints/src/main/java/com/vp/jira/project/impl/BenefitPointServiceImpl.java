package com.vp.jira.project.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.vp.jira.project.data.BenefitPoint;
import com.vp.jira.project.service.BenefitPointService;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class BenefitPointServiceImpl implements BenefitPointService{
    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public BenefitPointServiceImpl(ActiveObjects ao)
    {
        this.ao = checkNotNull(ao);
    }

    @Override
    public BenefitPoint add(int value, long epicID, long objectiveID, long projectID)
    {
        final BenefitPoint benefitPoint = ao.create(BenefitPoint.class);
        benefitPoint.setValue(value);
        benefitPoint.setEpicID(epicID);
        benefitPoint.setObjectiveID(objectiveID);
        benefitPoint.setProjectID(projectID);
        benefitPoint.save();
        return benefitPoint;
    }

    @Override
    public List<BenefitPoint> all()
    {
        return newArrayList(ao.find(BenefitPoint.class));
    }

    @Override
    public void update(int id, int value){
        for (BenefitPoint bp: ao.find(BenefitPoint.class, "ID like ?", id)) {
            bp.setValue(value);
            bp.save();
        }
    }

    @Override
    public void remove(BenefitPoint benefitPoint){
        ao.delete(benefitPoint);
    }
}
