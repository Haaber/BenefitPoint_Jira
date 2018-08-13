package com.vp.jira.project.web;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.vp.jira.project.classes.BalancedBenefitPoint;
import com.vp.jira.project.data.BenefitPoint;
import com.vp.jira.project.data.Contribution;
import com.vp.jira.project.data.Objective;
import com.vp.jira.project.data.Return;
import com.vp.jira.project.service.BenefitPointService;
import com.vp.jira.project.service.ContributionService;
import com.vp.jira.project.service.ObjectiveService;
import com.vp.jira.project.service.ReturnService;
import com.vp.jira.project.impl.UsefulMethodImpl;


import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.vp.jira.project.calc.BenefitCalculation.*;

@Scanned
public final class ValuePanel extends AbstractJiraContextProvider
{
    private ObjectiveService objectiveService;
    private BenefitPointService benefitPointService;
    private ReturnService returnService;
    private ContributionService contributionService;


    @ComponentImport
    private final UserProjectHistoryManager userProjectHistoryManager;

    List<Issue> issueList;
    List<Objective> objectiveList;
    List<BenefitPoint> benefitPointList;
    List<Return> returnList;
    List<Contribution> contributionList;


    @Inject
    public ValuePanel(ObjectiveService objectiveService, BenefitPointService benefitPointService, ReturnService returnService, ContributionService contributionService, UserProjectHistoryManager userProjectHistoryManager)
    {
        this.objectiveService = checkNotNull(objectiveService);
        this.benefitPointService = checkNotNull(benefitPointService);
        this.returnService = checkNotNull(returnService);
        this.contributionService = checkNotNull(contributionService);
        this.userProjectHistoryManager = userProjectHistoryManager;

    }

    @Override
    public Map getContextMap(ApplicationUser user, JiraHelper jiraHelper)
    {
        Map contextMap = new HashMap();

        contextMap.put("project", getCurrentProject());
        contextMap.put("objectives", getObjectiveList());
        contextMap.put("issues", getIssues());
        contextMap.put("returns", getReturnList());
        contextMap.put("contributions", getContributionList());
        contextMap.put("conObjTotal", getReducedDecimalsDoubleList(getcontObjTotal()));
        contextMap.put("conRetTotal", getContRetTotal());
        contextMap.put("conTotal", getReducedDecimalsDouble(contributionTotal(getcontObjTotal()), 2));
        contextMap.put("benefitpoints", getBenefitPointList());
        contextMap.put("epicObjTotal", getEpicObjTotal());
        contextMap.put("objEpicsTotal", getObjEpicTotal());
        contextMap.put("bpTotal", getBPTotal());
        contextMap.put("projectWeights", getReducedDecimalsDoubleList(getProjectWeight()));
        contextMap.put("projectWeightTotal", getReducedDecimalsDouble(getProjectWeight().stream().mapToDouble(Double::doubleValue).sum(), 2));
        contextMap.put("balancedBenefitPoints", getBalancedBenefitPointsDecimals());
        contextMap.put("balancedEpicTotals", getReducedDecimalsDoubleList(getEpicBalancedTotalList()));
        contextMap.put("balancedObjectiveTotals", getReducedDecimalsDoubleList(getBalancedObjectiveTotals()));
        contextMap.put("balancedBenefitPointsTotal", getReducedDecimalsDouble(getBalancedBenefitPointsTotal(), 2));

        return contextMap;
    }

    private List<Issue> getIssues()
    {
        UsefulMethodImpl usefulMethod = new UsefulMethodImpl(userProjectHistoryManager);
        return usefulMethod.getEpics();
    }

    public List<Objective> getObjectiveList()
    {
        objectiveList = objectiveService.all().stream().filter(o -> o.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        return objectiveList;
    }

    public List<BenefitPoint> getBenefitPointList()
    {
        benefitPointList = benefitPointService.all().stream().filter(b -> b.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        return benefitPointList;
    }

    public List<Return> getReturnList()
    {
        returnList = returnService.all().stream().filter(r -> r.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        return returnList;
    }

    public List<Contribution> getContributionList()
    {
        contributionList = contributionService.all().stream().filter(c -> c.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        return contributionList;
    }

    public List<Double> getcontObjTotal()
    {
        List<Double> totalResList = new ArrayList<>();
        for (Objective objective: objectiveList)
        {
            totalResList.add(contribution(getContributionList(), getReturnList(), objective.getID()));
        }
        return totalResList;
    }

    public List<Double> getProjectWeight(){
        List<Double> temp = new ArrayList<>();
        for (Double double1: getcontObjTotal()) {
            temp.add(projectWeight(double1, contributionTotal(getcontObjTotal())));
        }
        return temp;
    }

    public List<Integer> getEpicObjTotal(){
        List<Integer> temp = new ArrayList<>();
        for (Issue issue: getIssues()) {
            temp.add(totalObjEpic(getBenefitPointList(), getObjectiveList(), issue.getId()));
        }
        return temp;
    }

    public List<Integer> getObjEpicTotal(){
        List<Integer> temp = new ArrayList<>();
        for (Objective objective: getObjectiveList()) {
            temp.add(totalEpicObj(getBenefitPointList(), getIssues(), objective.getID()));
        }
        return temp;
    }

    public int getBPTotal(){
        int total = 0;

        if (getEpicObjTotal().stream().mapToInt(Integer::intValue).sum() == getObjEpicTotal().stream().mapToInt(Integer::intValue).sum()){
            total = getEpicObjTotal().stream().mapToInt(Integer::intValue).sum();
        }
        return total;
    }

    public List<Double> getContRetTotal(){
        List<Double> temp = new ArrayList<>();

        for (Return return1: getReturnList()) {
            temp.add(totalReturnCont(getContributionList(), getObjectiveList(), return1.getID()));
        }
        return temp;
    }

    public List<Double> getReducedDecimalsDoubleList(List<Double> inputList){
        List<Double> reducedList = new ArrayList<>();
        int places = 2;

        for (Double d: inputList) {

            reducedList.add(getReducedDecimalsDouble(d, places));
        }

        return  reducedList;
    }

    public Double getReducedDecimalsDouble(Double value, int places){

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public List<BalancedBenefitPoint> getBalancedBenefitPoints(){
        List<BalancedBenefitPoint> temp = new ArrayList<>();
        boolean added = false;

        for (Objective objective: getObjectiveList()) {
            for (Issue issue: getIssues()) {
                for (BenefitPoint benefitpoint: getBenefitPointList()) {
                    if (benefitpoint.getObjectiveID() == objective.getID() && benefitpoint.getEpicID() == issue.getId()){
                        temp.add(new BalancedBenefitPoint(issue.getId(), objective.getID(), getBalancedBenefitPoint(benefitpoint.getValue(), getObjEpicTotal().get(getObjectiveList().indexOf(objective)), getcontObjTotal().get(getObjectiveList().indexOf(objective)))));
                        added = true;
                    }
                }
                if (added){
                    added = false;
                } else {
                    temp.add(new BalancedBenefitPoint(issue.getId(), objective.getID(), 0.0));
                }
            }
        }
        return temp;
    }

    public List<BalancedBenefitPoint> getBalancedBenefitPointsDecimals(){
        List<BalancedBenefitPoint> temp = new ArrayList<>();

        for (BalancedBenefitPoint balancedBenefitPoint: getBalancedBenefitPoints()) {
            temp.add(new BalancedBenefitPoint(balancedBenefitPoint.getEpicID(), balancedBenefitPoint.getObjectiveID(), getReducedDecimalsDouble(balancedBenefitPoint.getBalancedBP(), 2)));
        }
        return temp;
    }

    // Returns a balanced benefit point when called
    public double getBalancedBenefitPoint(int bp, int totalobjbp, double cont){
        double projectWeight = projectWeight(cont, contributionTotal(getcontObjTotal()));

        return balanceBenefitPoint(projectWeight, getBPTotal(), bp, totalobjbp);
    }

    public List<Double> getEpicBalancedTotalList() {
        List<Double> temp = new ArrayList<>();

        for (Issue issue: getIssues()) {
            temp.add(getEpicBalancedTotal(issue.getId()));
        }
        return temp;
    }

    public double getEpicBalancedTotal(long epicID){
        double total = 0;

        for (BalancedBenefitPoint balancedBenefitPoint: getBalancedBenefitPoints()) {
            if (balancedBenefitPoint.getEpicID() == epicID){
                total += balancedBenefitPoint.getBalancedBP();
            }
        }
        return total;
    }

    public List<Double> getBalancedObjectiveTotals(){
        List<Double> temp = new ArrayList<>();

        for (Objective objective: getObjectiveList()) {
            double total = 0;
            for (BalancedBenefitPoint balancedBenefitPoint: getBalancedBenefitPoints()) {
                if (balancedBenefitPoint.getObjectiveID() == objective.getID()){
                    total += balancedBenefitPoint.getBalancedBP();
                }
            }
            temp.add(total);
        }
        return temp;
    }

    public double getBalancedBenefitPointsTotal(){
        double total = 0;
        //double test1 = getEpicBalancedTotalList().stream().mapToDouble(Double::doubleValue).sum();
        //double test2 = getBalancedObjectiveTotals().stream().mapToDouble(Double::doubleValue).sum();
        //List<BalancedBenefitPoint> bbplist = getBalancedBenefitPoints();
        total = getEpicBalancedTotalList().stream().mapToDouble(Double::doubleValue).sum();
        // Todo: This check was triggered when it is meant to never be triggered. The two value were off by a fraction.
        /*if (getEpicBalancedTotalList().stream().mapToDouble(Double::doubleValue).sum() == getBalancedObjectiveTotals().stream().mapToDouble(Double::doubleValue).sum()){
            total = getEpicBalancedTotalList().stream().mapToDouble(Double::doubleValue).sum();
        }*/
        return total;
    }

    public Project getCurrentProject(){
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        return userProjectHistoryManager.getCurrentProject(Permissions.BROWSE, user);
    }
}
