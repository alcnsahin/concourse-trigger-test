package com.devament.concourse.workflow;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

import com.devament.concourse.client.ConcourseClient;

public class ConcoursePostFunction extends AbstractJiraFunctionProvider {

	public static final Logger log = LoggerFactory.getLogger(ConcoursePostFunction.class);

	public ConcoursePostFunction() {

	}

	@Override
	public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		MutableIssue issue = getIssue(transientVars);

		CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();

		CustomField customField = customFieldManager.getCustomFieldObject(10940L);
		String pipelineName = (String) issue.getCustomFieldValue(customField);
		try {
			ConcourseClient.execute(pipelineName);
		} catch (Exception e) {
			throw new WorkflowException(e.getMessage());
		}

	}

}
