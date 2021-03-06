package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.elevenpaths.latch.LatchApp;
import com.elevenpaths.latch.LatchResponse;
import com.elevenpaths.latch.modelo.LatchModel;
import com.elevenpaths.latch.util.Utilities;
import com.google.gson.JsonObject;

public class Unpair extends JiraWebActionSupport {

	private final LatchModel modelo;
	private HttpServletRequest request;
	private String error;
	private I18nResolver i18nResolver;
	private JiraAuthenticationContext jiraAuthenticationContext;

	private final String UNPAIR_ERROR_CONF = "com.elevenpaths.latch.latch-plugin-jira.unpairErrorConf";
	private final String CSRF_ERROR = "com.elevenpaths.latch.latch-plugin-jira.xsrfError";
	private final String LATCH_INDEX = "/secure/LatchIndex.jspa";


	/**
	 * Constructor
	 * @param pluginSettingsFactory object to save data
	 * @param i18nResolver translate
	 */
	public Unpair(PluginSettingsFactory pluginSettingsFactory, I18nResolver i18nResolver) {
		this.modelo = new LatchModel(pluginSettingsFactory);
		this.request = ServletActionContext.getRequest();
		this.i18nResolver = i18nResolver;
		this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
	}

	@Override
	protected void doValidation() {
		this.error = "";

		String username = Utilities.getUsername(jiraAuthenticationContext);
		if (!username.equals("")) {
			if (!Utilities.isPaired(username, modelo)) {
				Utilities.redirectTo(LATCH_INDEX);
			}
		} else {
			Utilities.redirectToLogin();
		}
	}

	@Override
	protected String doExecute() throws Exception {
		if (request.getMethod().equals("POST")) {
			XsrfTokenGenerator xsrfTokenGenerator = ComponentAccessor.getComponentOfType(XsrfTokenGenerator.class);
			String token = request.getParameter(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY) != null ? request.getParameter(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY) : "";

			if (xsrfTokenGenerator.validateToken(request, token)) {
				String username = Utilities.getUsername(jiraAuthenticationContext);
				String accountId = modelo.getAccountId(username);

				if (accountId != null) {
					String appId = modelo.getAppId();
					String secret = modelo.getSecret();
					if (appId != null && secret != null) {
						LatchApp latch = new LatchApp(appId, secret);
						LatchResponse unpairResponse = null;
						try {
							unpairResponse = latch.unpair(accountId);
						} catch (NullPointerException ignored) {
						}
						JsonObject jObject;
						if (unpairResponse != null) {
							jObject = unpairResponse.getData();
							if (jObject != null) {
								modelo.deleteAccountId(username);
							} else {
								com.elevenpaths.latch.Error error = unpairResponse.getError();
								if (error != null) {
									if (error.getCode() == 102) {
										setError(getError() + i18nResolver.getText(UNPAIR_ERROR_CONF));
									}
								}
							}
						}
						modelo.deleteAccountId(username);
					} else {
						setError(getError() + i18nResolver.getText(UNPAIR_ERROR_CONF));
					}
				}
				Utilities.redirectTo(LATCH_INDEX);
            }else {
				setError(getError() + i18nResolver.getText(CSRF_ERROR));
			}
		}
		return SUCCESS;
	}

	public String getError() {
		return this.error.length() == 0 ? "" : this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
