package com.vp.jira.project.servlet;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.vp.jira.project.data.BenefitPoint;
import com.vp.jira.project.data.Objective;
import com.vp.jira.project.impl.UsefulMethodImpl;
import com.vp.jira.project.service.BenefitPointService;
import com.vp.jira.project.service.ObjectiveService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public final class BenefitPointServlet extends HttpServlet
{
    private final BenefitPointService benefitPointService;
    private final ObjectiveService objectiveService;

    @ComponentImport
    private final UserProjectHistoryManager userProjectHistoryManager;


    public BenefitPointServlet(BenefitPointService benefitPointService, ObjectiveService objectiveService, UserProjectHistoryManager userProjectHistoryManager)
    {
        this.benefitPointService = checkNotNull(benefitPointService);
        this.objectiveService = checkNotNull(objectiveService);
        this.userProjectHistoryManager = userProjectHistoryManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        // Add what to do when it gets it
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        List<Objective> objectives = objectiveService.all().stream().filter(o -> o.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        List<BenefitPoint> benefitPoints = benefitPointService.all().stream().filter(b -> b.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        List<Issue> issues = getIssues();

        if (benefitPoints.isEmpty()) {
            for (Objective objective: objectives) {
                for (Issue issue : issues) {
                    String temp = req.getParameter(issue.getId() + "-" + objective.getID());
                    if (temp == null || temp.equals("")) {

                    } else {
                        addBenefitPoint(Integer.parseInt(temp), issue.getId(), objective.getID());
                    }
                }
            }
        } else {

            for (Objective objective : objectives) {
                for (Issue issue : issues) {
                    boolean added = false;
                    for (BenefitPoint benefitPoint : benefitPoints) {
                        if (issue.getId() == benefitPoint.getEpicID() && objective.getID() == benefitPoint.getObjectiveID()) {
                            String temp = req.getParameter(issue.getId() + "-" + objective.getID());
                            if (temp == null || temp.equals("")) {

                            } else {
                                benefitPointService.update(benefitPoint.getID(), Integer.parseInt(temp));
                                added = true;
                            }
                        } else {
                            // Does nothing
                        }
                    }
                    if (added) {

                    } else {
                        String temp = req.getParameter(issue.getId() + "-" + objective.getID());
                        if (temp == null || temp.equals("")) {

                        } else {
                            addBenefitPoint(Integer.parseInt(temp), issue.getId(), objective.getID());
                        }
                    }
                }
            }
        }
        res.sendRedirect(req.getHeader("Referer"));

    }
    // Method to add a benefit point to the database through the benefitPointService.
    public void addBenefitPoint(int value, long epicID, long objectiveID)
    {
        benefitPointService.add(value, epicID, objectiveID, getCurrentProject().getId());
    }

    private List<Issue> getIssues()
    {
        UsefulMethodImpl usefulMethod = new UsefulMethodImpl(userProjectHistoryManager);
        return usefulMethod.getEpics();
    }

    public Project getCurrentProject(){
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        return userProjectHistoryManager.getCurrentProject(Permissions.BROWSE, user);
    }

}
