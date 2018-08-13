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
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.vp.jira.project.data.BenefitPoint;
import com.vp.jira.project.data.Contribution;
import com.vp.jira.project.data.Objective;
import com.vp.jira.project.data.Return;
import com.vp.jira.project.impl.UsefulMethodImpl;
import com.vp.jira.project.service.BenefitPointService;
import com.vp.jira.project.service.ContributionService;
import com.vp.jira.project.service.ObjectiveService;
import com.vp.jira.project.service.ReturnService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.vp.jira.project.calc.BenefitCalculation.*;
import static com.vp.jira.project.calc.BenefitCalculation.totalEpicObj;

public final class EpicBalancedPanel extends AbstractJiraContextProvider {

    private ObjectiveService objectiveService;
    private BenefitPointService benefitPointService;
    private ReturnService returnService;
    private ContributionService contributionService;
    List<Objective> objectiveList;
    List<BenefitPoint> benefitPointList;
    List<Return> returnList;
    List<Contribution> contributionList;

    @ComponentImport
    private final UserProjectHistoryManager userProjectHistoryManager;

    // Checks that the various services we use is not null.
    @Inject
    public EpicBalancedPanel(ObjectiveService objectiveService, BenefitPointService benefitPointService, ReturnService returnService, ContributionService contributionService, UserProjectHistoryManager userProjectHistoryManager)
    {
        this.objectiveService = checkNotNull(objectiveService);
        this.benefitPointService = checkNotNull(benefitPointService);
        this.returnService = checkNotNull(returnService);
        this.contributionService = checkNotNull(contributionService);
        this.userProjectHistoryManager = userProjectHistoryManager;
    }

    // Method required to pass information to our velocity files using a hash map.
    @Override
    public Map getContextMap(ApplicationUser user, JiraHelper jiraHelper)
    {
        Map contextMap = new HashMap();
        Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");


        contextMap.put("objectives", getObjectiveList());
        contextMap.put("balancedpoints", getReducedDecimalsDoubleList(getTotalBalancedBPPerEpic(currentIssue.getId())));
        contextMap.put("totalBenefitPoints", getReducedDecimalsDouble(getTotalBalancedBenefitPoints(getTotalBalancedBPPerEpic(currentIssue.getId())), 2));

        return contextMap;
    }

    // Retrieves a list of all objectives
    public List<Objective> getObjectiveList()
    {
        objectiveList = objectiveService.all().stream().filter(o -> o.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        return objectiveList;
    }

    // Retrieves a list of all benefit points
    public List<BenefitPoint> getBenefitPointList()
    {
        benefitPointList = benefitPointService.all().stream().filter(b -> b.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        return benefitPointList;
    }

    // Retrieves a list of all returns
    public List<Return> getReturnList()
    {
        returnList = returnService.all().stream().filter(r -> r.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        return returnList;
    }

    // Retrieves a list of all contributions
    public List<Contribution> getContributionList()
    {
        contributionList = contributionService.all().stream().filter(c -> c.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        return contributionList;
    }

    // Returns a balanced benefit point when called
    public double getBalancedBenefitPoint(int bp, int totalobjbp, double cont){
        double projectWeight = projectWeight(cont, contributionTotal(getcontObjTotal()));

        return balanceBenefitPoint(projectWeight, getBPTotal(), bp, totalobjbp);
    }

    // Returns a list of all balanced benefit points related to a specific epic
    public List<Double> getTotalBalancedBPPerEpic(long epicID)
    {
        List<Double> temp = new ArrayList<>();
        boolean added = false;
        for (Objective objective: getObjectiveList()) {
            for (BenefitPoint benefitPoint: getBenefitPointList()) {
                if (benefitPoint.getObjectiveID() == objective.getID() && benefitPoint.getEpicID() == epicID){
                    temp.add(getBalancedBenefitPoint(benefitPoint.getValue(), getObjEpicTotal().get(getObjectiveList().indexOf(objective)), getcontObjTotal().get(getObjectiveList().indexOf(objective))));
                    added = true;
                }
            }
            if (added){
                added = false;
            } else {
                temp.add(0.0);
            }
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

    public double getTotalBalancedBenefitPoints(List<Double> bbpList)
    {
        return bbpList.stream().mapToDouble(Double::doubleValue).sum();
    }

    private List<Issue> getIssues()
    {
        UsefulMethodImpl usefulMethod = new UsefulMethodImpl(userProjectHistoryManager);
        return usefulMethod.getEpics();
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

    public Project getCurrentProject(){
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        return userProjectHistoryManager.getCurrentProject(Permissions.BROWSE, user);
    }
}
