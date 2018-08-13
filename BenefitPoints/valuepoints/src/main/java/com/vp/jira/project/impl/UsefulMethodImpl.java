package com.vp.jira.project.impl;

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
import com.atlassian.sal.api.project.ProjectManager;

import java.util.List;

public class UsefulMethodImpl {

    @ComponentImport
    private final UserProjectHistoryManager userProjectHistoryManager;

    public UsefulMethodImpl (UserProjectHistoryManager userProjectHistoryManager) {
        this.userProjectHistoryManager = userProjectHistoryManager;
    }

    public List<Issue> getEpics()
    {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        Project p = getCurrentProject();

        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        // Our JQL clause is simple project="TUTORIAL"
        SearchService searchService = ComponentAccessor
                .getComponentOfType(SearchService.class);
        com.atlassian.query.Query query = jqlClauseBuilder.issueType().eq("10000").and().project(p.getId()).buildQuery(); //10000 is the Type for Epics! 10001 is the type for Stories!
        // A page filter is used to provide pagination. Let's use an unlimited filter to
        // to bypass pagination.
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        com.atlassian.jira.issue.search.SearchResults searchResults = null;
        try {
            // Perform search results
            searchResults = searchService.search(user, query, pagerFilter);
        } catch (SearchException e) {
            e.printStackTrace();
        }
        // return the results
        return searchResults.getIssues();
    }

    public Project getCurrentProject(){
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        return userProjectHistoryManager.getCurrentProject(Permissions.BROWSE, user);
    }
}
