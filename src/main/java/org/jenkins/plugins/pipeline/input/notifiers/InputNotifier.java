package org.jenkins.plugins.pipeline.input.notifiers;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.jenkins.plugins.pipeline.input.models.InputNotificationEvent;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * @author jasper
 */
public abstract class InputNotifier extends AbstractDescribableImpl<InputNotifier> implements ExtensionPoint, Comparable<InputNotifier>, Serializable {

    private final static Logger LOGGER = Logger.getLogger(InputNotifier.class.getName());

    protected boolean disabled;

    public boolean isDisabled() {
        return disabled;
    }

    @DataBoundSetter
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public int compareTo(InputNotifier o) {
        return this.getDescriptor().compareTo(o.getDescriptor());
    }

    /**
     * Send notification after input started, approved, aborted
     * @param event input event
     */
    public abstract void notifyInputNotification(InputNotificationEvent event);

    public abstract static class DescriptorImpl extends Descriptor<InputNotifier> implements Comparable<DescriptorImpl> {

        /**
         * @return the ordinal of this reporter to execute publishers in predictable order. The smallest ordinal is executed first.
         */
        public int ordinal() {
            return 100;
        }

        @Override
        public int compareTo(DescriptorImpl o) {
            int compare = Integer.compare(this.ordinal(), o.ordinal());

            if (compare == 0) {
                compare = this.getId().compareTo(o.getId());
            }

            return compare;
        }
    }
}
