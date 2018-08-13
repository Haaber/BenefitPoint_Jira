package com.vp.jira.project.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.vp.jira.project.data.BenefitPoint;
import com.vp.jira.project.data.Contribution;
import com.vp.jira.project.data.Return;
import com.vp.jira.project.service.BenefitPointService;
import com.vp.jira.project.service.ContributionService;
import com.vp.jira.project.service.ReturnService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ReturnServlet extends HttpServlet
{
    private final ReturnService returnService;
    private final BenefitPointService benefitPointService;
    private final ContributionService contributionService;
    private UserProjectHistoryManager userProjectHistoryManager;

    public ReturnServlet(ReturnService returnService, BenefitPointService benefitPointService, ContributionService contributionService, UserProjectHistoryManager userProjectHistoryManager)
    {
        this.returnService = checkNotNull(returnService);
        this.benefitPointService = checkNotNull(benefitPointService);
        this.contributionService = checkNotNull(contributionService);
        this.userProjectHistoryManager = userProjectHistoryManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        //Nothing to see here
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        String tempDel = req.getParameter("return-delete-submit");
        String tempEdit = req.getParameter("edit-return-submit");
        if (!(tempDel == null) && tempDel.equals("Delete")){ // This one brings an error. Better method to check.. It takes the value of the input field based on the name=""
            doDelete(req, res);
        } else if (!(tempEdit == null) && tempEdit.equals("Edit")){
            doUpdate(req, res);
        } else {
            // Adds a new return to the database and redirects to the current page.
            final String description = req.getParameter("returnDescription");
            final double value = Double.parseDouble(req.getParameter("returnContribution"));
            returnService.add(description, value, getCurrentProject().getId());
        }
        res.sendRedirect(req.getHeader("Referer"));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        long returnID = Long.parseLong(req.getParameter("returnID"));
        for (Return return1: returnService.all().stream().filter(r -> r.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList())) {
            if (return1.getID() == returnID){
                for (Contribution contribution: contributionService.all().stream().filter(c -> c.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList())) {
                    if (contribution.getReturnID() == returnID){
                        contributionService.remove(contribution);
                    }
                }
                returnService.remove(return1);
            }
        }
    }

    protected void doUpdate(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        for (Return ret: returnService.all().stream().filter(r -> r.getProjectID() == getCurrentProject().getId()).collect(Collectors.toList())) {
            if (ret.getID() == Long.parseLong(req.getParameter("returnID"))){
                returnService.update(ret.getID(), req.getParameter("returnDescription"), Double.parseDouble(req.getParameter("returnContribution")));
                break;
            }
        }
    }

    public Project getCurrentProject(){
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        return userProjectHistoryManager.getCurrentProject(Permissions.BROWSE, user);
    }
}
