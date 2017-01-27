package com.emenda.emendaklocwork;

import com.emenda.emendaklocwork.util.KlocworkCmdExecuter;
import com.emenda.emendaklocwork.util.KlocworkUtil;

import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Proc;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;

import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.InterruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link KlocworkServerBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class KlocworkServerBuilder extends Builder {

    private final KlocworkServerUtil serverUtil;

    @DataBoundConstructor
    public KlocworkServerBuilder(KlocworkServerUtil serverUtil) {
        this.serverUtil = serverUtil;

    }

    /**
     * We'll use this from the {@code config.jelly}.
     */
    public KlocworkServerUtil getServerUtil() {
        return serverUtil;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) {
        KlocworkLogger logger = new KlocworkLogger(listener.getLogger());
        EnvVars envVars = null;
        try {
            envVars = build.getEnvironment(listener);

            // serverUtil.populateArguments(envVars, build.getWorkspace());

            KlocworkCmdExecuter cmdExec = new KlocworkCmdExecuter();
            int rc_kwdeploy = cmdExec.executeCommand(launcher, listener,
                    build.getWorkspace(), envVars,
                    serverUtil.getKwdeployCmd(envVars, build.getWorkspace()));

            int rc_kwbuild = cmdExec.executeCommand(launcher, listener,
                    build.getWorkspace(), envVars,
                    serverUtil.getKwbuildprojectCmd(envVars, build.getWorkspace()));

            int rc_kwadmin = cmdExec.executeCommand(launcher, listener,
                    build.getWorkspace(), envVars,
                    serverUtil.getKwadminCmd(envVars, build.getWorkspace()));


            if (rc_kwdeploy != 0) {
                logger.logMessage("rc_kwdeploy create return code " + Integer.toString(rc_kwdeploy));
                return false;
            }
            if (rc_kwbuild != 0) {
                logger.logMessage("rc_kwbuild create return code " + Integer.toString(rc_kwbuild));
                return false;
            }
            if (rc_kwadmin != 0) {
                logger.logMessage("rc_kwadmin create return code " + Integer.toString(rc_kwadmin));
                return false;
            }

        } catch (IOException ioe) {
            logger.logMessage(KlocworkUtil.exceptionToString(ioe));
            return false;
        } catch (InterruptedException ie) {
            logger.logMessage(KlocworkUtil.exceptionToString(ie));
            return false;
        }

        return true;

    }
    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link KlocworkServerBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/KlocworkServerBuilder/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use {@code transient}.
         */


        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         *      <p>
         *      Note that returning {@link FormValidation#error(String)} does not
         *      prevent the form from being saved. It just means that a message
         *      will be displayed to the user.
         */
        /*
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        */

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }


        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Emenda Klocwork Server Analysis";
        }



        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            save();
            return super.configure(req,formData);
        }
    }
}
