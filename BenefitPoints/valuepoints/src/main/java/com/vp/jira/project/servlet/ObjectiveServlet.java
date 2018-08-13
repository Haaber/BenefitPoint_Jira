package com.vp.jira.project.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.vp.jira.project.data.BenefitPoint;
import com.vp.jira.project.data.Contribution;
import com.vp.jira.project.data.Objective;
import com.vp.jira.project.service.BenefitPointService;
import com.vp.jira.project.service.ContributionService;
import com.vp.jira.project.service.ObjectiveService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ObjectiveServlet extends HttpServlet
{
    private final ObjectiveService objectiveService;
    private final BenefitPointService benefitPointService;
    private final ContributionService contributionService;
    private UserProjectHistoryManager userProjectHistoryManager;

    public ObjectiveServlet(ObjectiveService objectiveService, BenefitPointService benefitPointService, ContributionService contributionService, UserProjectHistoryManager userProjectHistoryManager)
    {
        this.objectiveService = checkNotNull(objectiveService);
        this.benefitPointService = checkNotNull(benefitPointService);
        this.contributionService = checkNotNull(contributionService);
        this.userProjectHistoryManager = userProjectHistoryManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        String tempDel = req.getParameter("objective-delete-submit");
        String tempEdit = req.getParameter("edit-objective-submit");
        if (!(tempDel == null) && tempDel.equals("Delete")){
            doDelete(req, res);
        } else if (!(tempEdit == null) && tempEdit.equals("Edit")){
            doUpdate(req, res);
        } else {
            final String description = req.getParameter("objectiveDescription");
            objectiveService.add(description, getCurrentProject().getId());
        }
        res.sendRedirect(req.getHeader("Referer"));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        long objectiveID = Long.parseLong(req.getParameter("objectiveID"));
        for (Objective objective: objectiveService.all().stream().filter(o -> o.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList())) {
            if (objective.getID() == objectiveID){
                for (BenefitPoint benefitPoint: benefitPointService.all().stream().filter(b -> b.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList())) {
                    if (benefitPoint.getObjectiveID() == objectiveID){
                        benefitPointService.remove(benefitPoint);
                    }
                }

                for (Contribution contribution: contributionService.all().stream().filter(c -> c.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList())) {
                    if (contribution.getObjectiveID() == objectiveID){
                        contributionService.remove(contribution);
                    }
                }
                objectiveService.remove(objective);
            }
        }
    }
    protected void doUpdate(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        for (Objective objective: objectiveService.all().stream().filter(o -> o.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList())) {
            if (objective.getID() == Long.parseLong(req.getParameter("objectiveID"))){
                objectiveService.update(objective.getID(), req.getParameter("objectiveDescription"));

            }
        }
    }

    public Project getCurrentProject(){
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        return userProjectHistoryManager.getCurrentProject(Permissions.BROWSE, user);
    }
}
