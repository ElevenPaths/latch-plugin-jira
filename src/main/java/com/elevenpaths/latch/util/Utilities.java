package com.elevenpaths.latch.util;

import java.io.IOException;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.elevenpaths.latch.modelo.LatchModel;

public class Utilities {

    /**
     * check if the user is paired with latch
     *
     * @param username user logged currently
     * @param model    it saves data
     * @return if is paired or not
     */
    public static boolean isPaired(String username, LatchModel model) {
        System.out.println(model.getAccountId(username));
        return model.getAccountId(username) != null;
    }

    /**
     * Redirect to another page
     *
     * @param nextUrl where the user goes
     */
    public static void redirectTo(String nextUrl) {
        JiraWebActionSupport redirect = new JiraWebActionSupport();
        String contextPath = redirect.getHttpRequest().getContextPath();
        if (contextPath != null) {
            nextUrl = contextPath + nextUrl;
        }
        try {
            redirect.getHttpResponse().sendRedirect(nextUrl);
        } catch (IOException | IllegalStateException ignored) {
        }
    }

    public static void redirectToLogin() {
        JiraWebActionSupport redirect = new JiraWebActionSupport();
        String contextPath = redirect.getHttpRequest().getContextPath();
        try {
            redirect.getHttpResponse().sendRedirect(contextPath);
        } catch (IOException | IllegalStateException ignored) {
        }
    }

    /**
     * check if the current user is the admin
     *
     * @param jiraAuthenticationContext get current user
     * @param userManager               check if user is admin
     * @return if the user is admin or not
     */

    public static boolean isAdmin(JiraAuthenticationContext jiraAuthenticationContext, UserManager userManager) {
        ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
        if (user != null) {
            String key = user.getKey();
            UserKey userKey = new UserKey(key);
            return userManager.isSystemAdmin(userKey);
        } else {
            return false;
        }
    }

    /**
     * check if exists a logged user
     *
     * @param jiraAuthenticationContext get current user
     * @return the name of the user logged, if not exists return a empty string
     */
    public static String getUsername(JiraAuthenticationContext jiraAuthenticationContext) {
        ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
        return (user != null) ? user.getUsername() : "";
    }

}
