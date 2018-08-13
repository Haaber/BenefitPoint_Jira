package com.vp.jira.project.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.vp.jira.project.data.Contribution;
import com.vp.jira.project.data.Objective;
import com.vp.jira.project.data.Return;
import com.vp.jira.project.service.ContributionService;
import com.vp.jira.project.service.ObjectiveService;
import com.vp.jira.project.service.ReturnService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ContributionServlet extends HttpServlet
{
    private final ContributionService contributionService;
    private final ObjectiveService objectiveService;
    private final ReturnService returnService;
    private UserProjectHistoryManager userProjectHistoryManager;

    public ContributionServlet(ContributionService contributionService, ObjectiveService objectiveService, ReturnService returnService, UserProjectHistoryManager userProjectHistoryManager)
    {
        this.contributionService = checkNotNull(contributionService);
        this.objectiveService = checkNotNull(objectiveService);
        this.returnService = checkNotNull(returnService);
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
        // Gets a list of all contributions.
        List<Contribution> contributions = contributionService.all().stream().filter(c -> c.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        List<Objective> objectives = objectiveService.all().stream().filter(o -> o.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());
        List<Return> returns = returnService.all().stream().filter(r -> r.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList());

        if (contributions.isEmpty()){
            for (Objective objective: objectives) {
                for (Return ret: returns) {
                    String temp = req.getParameter(ret.getID() + "-" + objective.getID());
                    if (temp == null || temp.equals("")){

                    } else {
                        addContribution(Double.parseDouble(temp), ret.getID(), objective.getID());
                    }
                }
            }
        } else {
            boolean added = false;
            for (Objective objective: objectives) {
                for (Return ret: returns) {
                    for (Contribution contribution: contributions) {
                        if (ret.getID() == contribution.getReturnID() && objective.getID() == contribution.getObjectiveID()){
                            String temp = req.getParameter(ret.getID() + "-" + objective.getID());
                            if (temp == null || temp.equals("")){

                            } else {
                                contributionService.update(contribution.getID(), Double.parseDouble(temp));
                                added = true;
                            }
                        } else {

                        }
                    }
                    if (added) {
                        added = false;
                    } else {
                        String temp = req.getParameter(ret.getID() + "-" + objective.getID());
                        if (temp == null || temp.equals("")){

                        } else {
                            addContribution(Double.parseDouble(temp), ret.getID(), objective.getID());
                        }
                    }
                }
            }
        }
        res.sendRedirect(req.getHeader("Referer"));



        //boolean added = false;
        // Checks if list of contributions is not empty. If empty, it will just add the new contribution and redirect to current page.
        /*if(!contributions.isEmpty()){
            for (Contribution cont: contributions) {
                // For each contribution, if there's a match, it will update it.
                if(cont.getReturnID() == Long.parseLong(req.getParameter("returnIDInput")) && cont.getObjectiveID() == Long.parseLong(req.getParameter("objectiveIDInput"))){
                    contributionService.update(cont.getID(), Double.parseDouble(req.getParameter("contributionPerInput")));
                    added = true;
                }
            }
            if (added){
                res.sendRedirect(req.getHeader("Referer"));
            } else {
                addContribution(req, res);
                res.sendRedirect(req.getHeader("Referer"));
            }
        } else {
            addContribution(req, res);
            res.sendRedirect(req.getHeader("Referer"));
        }*/
    }
    // Method to add a contribution to the database through the contributionService.
    public void addContribution(double value, long returnID, long objectiveID){
        contributionService.add(value, returnID, objectiveID, getCurrentProject().getId());
    }

    public Project getCurrentProject(){
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        return userProjectHistoryManager.getCurrentProject(Permissions.BROWSE, user);
    }
}
