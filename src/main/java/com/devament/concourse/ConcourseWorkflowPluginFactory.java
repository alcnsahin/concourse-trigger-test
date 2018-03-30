package com.devament.concourse;

import java.util.Map;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

public class ConcourseWorkflowPluginFactory extends AbstractWorkflowPluginFactory implements
        WorkflowPluginFunctionFactory {


    protected void getVelocityParamsForInput(Map<String, Object> map) {
    }

    protected void getVelocityParamsForEdit(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {
        getVelocityParamsForInput(map);
        getVelocityParamsForView(map,abstractDescriptor);

    }

    protected void getVelocityParamsForView(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {
        if (!(abstractDescriptor instanceof FunctionDescriptor)) {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }

    }

    public Map<String, ?> getDescriptorParams(Map<String, Object> map) {

        return map;

    }

}
