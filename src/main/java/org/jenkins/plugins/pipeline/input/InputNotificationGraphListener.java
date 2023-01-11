package org.jenkins.plugins.pipeline.input;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Run;
import jenkins.model.CauseOfInterruption;
import jenkins.model.Jenkins;
import lombok.SneakyThrows;
import org.jenkins.plugins.pipeline.input.models.InputNotificationEvent;
import org.jenkins.plugins.pipeline.input.notifiers.InputNotifier;
import org.jenkinsci.plugins.workflow.actions.ArgumentsAction;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;
import org.jenkinsci.plugins.workflow.support.steps.input.InputSubmittedAction;
import org.jenkinsci.plugins.workflow.support.steps.input.Rejection;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author jasper
 */
@Extension
public class InputNotificationGraphListener implements GraphListener {

    private static final Logger LOGGER = Logger.getLogger(InputNotificationGraphListener.class.getName());
    private static final String INPUT_FUNCTION_NAME = "input";

    /**
     * Check if exists `input` step, if so, send notification after input started, approved, aborted.
     * @param fn flow node
     */
    @Override
    @SneakyThrows
    public void onNewHead(FlowNode fn) {
        if (isCurrentNodeInputStep(fn)) {
            Map<String, Object> arguments = ArgumentsAction.getArguments(fn);
            Run<?, ?> run = runFor(fn.getExecution());
            notifyWhenInputStarted(arguments, run);
        } else if (isParentNodeInputStep(fn)) {
            Run<?, ?> run = runFor(fn.getExecution());
            FlowNode inputStepFn = getInputStepNodeFromParent(fn);
            Map<String, Object> arguments = ArgumentsAction.getArguments(inputStepFn);
            ErrorAction errorAction = inputStepFn.getError();
            if (errorAction == null) {
                InputSubmittedAction inputSubmittedAction = inputStepFn.getAction(InputSubmittedAction.class);
                notifyWhenInputApproved(arguments, inputSubmittedAction, run);
            } else {
                if (errorAction.getError() instanceof FlowInterruptedException) {
                    FlowInterruptedException fie = (FlowInterruptedException) errorAction.getError();
                    for (CauseOfInterruption cause : fie.getCauses()) {
                        if (cause instanceof Rejection) {
                            notifyWhenInputAborted(arguments, (Rejection) cause, run);
                        }
                    }
                }
            }
        }

    }

    private boolean isCurrentNodeInputStep(FlowNode fn) {
        return fn instanceof StepAtomNode && INPUT_FUNCTION_NAME.equals(fn.getDisplayFunctionName());
    }

    private boolean isParentNodeInputStep(FlowNode fn) {
        for (FlowNode n : fn.getParents()) {
            if (INPUT_FUNCTION_NAME.equals(n.getDisplayFunctionName())) {
                return true;
            }
        }
        return false;
    }

    private FlowNode getInputStepNodeFromParent(FlowNode fn) {
        for (FlowNode n : fn.getParents()) {
            if (INPUT_FUNCTION_NAME.equals(n.getDisplayFunctionName())) {
                return n;
            }
        }
        return null;
    }

    /**
     * Gets the jenkins run object of the specified executing workflow.
     * @param exec execution of a workflow
     * @return jenkins run object of a job
     */
    private Run<?, ?> runFor(FlowExecution exec) {
        Queue.Executable executable;
        try {
            executable = exec.getOwner().getExecutable();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, null, e);
            return null;
        }
        if (executable instanceof Run) {
            return (Run<?, ?>) executable;
        } else {
            return null;
        }
    }

    private void notifyWhenInputStarted(Map<String, Object> arguments, Run<?, ?> run) {
        if (null == run) {
            LOGGER.log(Level.WARNING, "Could not find Run - notification will not be send");
            return;
        }

        Job job = run.getParent();
        InputNotificationEvent event = InputNotificationEvent.builder()
            .jenkinsUrl(Jenkins.get().getRootUrl())
            .jobUrl(run.getUrl())
            .jobFullName(job.getFullName())
            .buildNumber(run.getNumber())
            .inputId((String) arguments.get("id"))
            .submitter((String) arguments.get("submitter"))
            .result("PENDING")
            .build();

        for (InputNotifier notifier : InputNotificationConfig.get().getInputNotifiers()) {
            if (!notifier.isDisabled()) {
                notifier.notifyInputNotification(event);
            }
        }
    }

    private void notifyWhenInputApproved(Map<String, Object> arguments, InputSubmittedAction inputSubmittedAction, Run<?, ?> run) {
        if (null == run) {
            LOGGER.log(Level.WARNING, "Could not find Run - notification will not be send");
            return;
        }
        Job job = run.getParent();
        InputNotificationEvent event = InputNotificationEvent.builder()
            .jenkinsUrl(Jenkins.get().getRootUrl())
            .jobUrl(run.getUrl())
            .jobFullName(job.getFullName())
            .buildNumber(run.getNumber())
            .inputId((String) arguments.get("id"))
            .approver((String) inputSubmittedAction.getParameters().get(arguments.get("submitterParameter")))
            .result("APPROVED")
            .build();

        for (InputNotifier notifier : InputNotificationConfig.get().getInputNotifiers()) {
            if (!notifier.isDisabled()) {
                notifier.notifyInputNotification(event);
            }
        }
    }

    private void notifyWhenInputAborted(Map<String, Object> arguments, Rejection rejection, Run<?, ?> run) {
        if (null == run) {
            LOGGER.log(Level.WARNING, "Could not find Run - notification will not be send");
            return;
        }
        Job job = run.getParent();
        String rejector = rejection.getUser() != null ? rejection.getUser().getFullName() : null;
        InputNotificationEvent event = InputNotificationEvent.builder()
            .jenkinsUrl(Jenkins.get().getRootUrl())
            .jobUrl(run.getUrl())
            .jobFullName(job.getFullName())
            .buildNumber(run.getNumber())
            .inputId((String) arguments.get("id"))
            .approver(rejector)
            .result("ABORTED")
            .build();

        for (InputNotifier notifier : InputNotificationConfig.get().getInputNotifiers()) {
            if (!notifier.isDisabled()) {
                notifier.notifyInputNotification(event);
            }
        }
    }

}
