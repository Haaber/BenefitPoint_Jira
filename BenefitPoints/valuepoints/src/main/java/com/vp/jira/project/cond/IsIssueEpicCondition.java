package com.vp.jira.project.cond;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractIssueWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Map;

public class IsIssueEpicCondition extends AbstractIssueWebCondition {
    private Map<String, String> params;

    @Override
    public void init(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, Issue issue, JiraHelper jiraHelper) {
        boolean out = false;

        try {
            // You can change "epic" to "story" to check condition on a story instead.
            if (issue.getIssueType().toString().contains("epic")){
                return true;
            }

        } catch (Exception ex) {
            return out;
        }
        return out;
    }
}
