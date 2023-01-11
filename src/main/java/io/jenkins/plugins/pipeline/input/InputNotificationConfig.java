package io.jenkins.plugins.pipeline.input;

import hudson.Extension;
import io.jenkins.plugins.pipeline.input.notifiers.InputNotifier;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author jasper
 */
@Extension
public class InputNotificationConfig extends GlobalConfiguration {

    private final static Logger LOGGER = Logger.getLogger(InputNotificationConfig.class.getName());

    /**
     * input notification list
     */
    private List<InputNotifier> inputNotifiers;

    @DataBoundConstructor
    public InputNotificationConfig() {
        load();
    }

    public static InputNotificationConfig get() {
        return GlobalConfiguration.all().get(InputNotificationConfig.class);
    }

    public List<InputNotifier> getInputNotifiers() {
        return inputNotifiers == null ? Collections.emptyList() : inputNotifiers;
    }

    @DataBoundSetter
    public void setInputNotifiers(List<InputNotifier> inputNotifiers) {
        this.inputNotifiers = inputNotifiers;
    }

    @Override
    public boolean configure(StaplerRequest request, JSONObject formData) {
        request.bindJSON(this, formData);
        this.inputNotifiers = request.bindJSONToList(InputNotifier.class, formData.get("inputNotifiers"));
        save();
        return true;
    }

}
