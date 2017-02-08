package com.emenda.emendaklocwork;

import com.emenda.emendaklocwork.util.KlocworkLtokenFetcher;
import com.emenda.emendaklocwork.util.KlocworkCmdExecuter;
import com.emenda.emendaklocwork.util.KlocworkUtil;

import hudson.AbortException;
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
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;
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
import java.io.Serializable;
import java.io.StringReader;
import java.lang.InterruptedException;
import java.lang.NumberFormatException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;



import java.util.Properties; // DELETE

public class KlocworkXSyncBuilder extends Builder implements Serializable {

    private final KlocworkXSyncUtil xsyncUtil;

    @DataBoundConstructor
    public KlocworkXSyncBuilder(KlocworkXSyncUtil xsyncUtil) {
        this.xsyncUtil = xsyncUtil;
    }

    public KlocworkXSyncUtil getXsyncUtil() { return xsyncUtil; }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener)
        throws IOException {
        KlocworkLogger logger = new KlocworkLogger("XSyncBuilder", listener.getLogger());
        EnvVars envVars = null;

        try {
            envVars = build.getEnvironment(listener);

            KlocworkCmdExecuter cmdExec = new KlocworkCmdExecuter();
            int rc_version = cmdExec.executeCommand(launcher, listener,
                    build.getWorkspace(), envVars,
                    xsyncUtil.getVersionCmd());
            if (rc_version != 0) {
                throw new AbortException(xsyncUtil.getVersionCmd().toString() +
                    "command failed with return code " +
                    Integer.toString(rc_version));
            }
            int rc = cmdExec.executeCommand(launcher, listener,
                     build.getWorkspace(), envVars, xsyncUtil.getxsyncCmd(envVars, launcher));

        } catch (IOException | InterruptedException ex) {
            throw new AbortException(KlocworkUtil.exceptionToString(ex));
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
     * Descriptor for {@link KlocworkXSyncBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/KlocworkXSyncBuilder/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }


        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }


        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Emenda Klocwork Project Synchronisation";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            save();
            return super.configure(req,formData);
        }
    }
}
