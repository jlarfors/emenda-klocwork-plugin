package com.emenda.emendaklocwork;

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

import java.util.Arrays;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link KlocworkDesktopBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class KlocworkDesktopBuilder extends Builder {

    private final KlocworkDesktopUtil desktopUtil;

    @DataBoundConstructor
    public KlocworkDesktopBuilder(KlocworkDesktopUtil desktopUtil) {
        this.desktopUtil = desktopUtil;
    }

    public KlocworkDesktopUtil getDesktopUtil() { return desktopUtil; }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener)
        throws IOException {
        KlocworkLogger logger = new KlocworkLogger("DesktopBuilder", listener.getLogger());
        EnvVars envVars = new EnvVars();
        try {
            envVars = build.getEnvironment(launcher.getListener());
            logger.logMessage(Arrays.toString(launcher.launch().envs()));
            KlocworkCmdExecuter cmdExec = new KlocworkCmdExecuter();

            int rc_version = cmdExec.executeCommand(launcher, listener,
                    build.getWorkspace(), envVars,
                    desktopUtil.getVersionCmd());
            if (rc_version != 0) {
                throw new AbortException(desktopUtil.getVersionCmd().toString() +
                    "command failed with return code " +
                    Integer.toString(rc_version));
            }

            if (!desktopUtil.hasExistingProject(build.getWorkspace(), envVars)) {
                int rc_kwcheckCreate = cmdExec.executeCommand(launcher, listener,
                        build.getWorkspace(), envVars,
                        desktopUtil.getKwcheckCreateCmd(envVars, build.getWorkspace()));

                if (rc_kwcheckCreate != 0) {
                    logger.logMessage("kwcheck create return code " + Integer.toString(rc_kwcheckCreate));
                    return false;
                }
            } else {
                // TODO: should we update existing project with settings, e.g project
            }

            int rc_kwcheckRun = cmdExec.executeCommand(launcher, listener,
                    build.getWorkspace(), envVars,
                    desktopUtil.getKwcheckRunCmd(envVars, build.getWorkspace()));

            if (rc_kwcheckRun != 0) {
                logger.logMessage("kwcheck run return code " + Integer.toString(rc_kwcheckRun));
                return false;
            }

        }  catch (IOException | InterruptedException ex) {
            throw new AbortException(KlocworkUtil.exceptionToString(ex));
        }


        return true;
    }

    // private String getDiffFileList(ProcStarter ps) throws IOException, InterruptedException {
    //     ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //     List<String> fileList = new ArrayList<String>();
    //
    //     if (!ekwDiffCmd.equals("")) {
    //         ps.stdout(baos).
    //             cmds(new ArgumentListBuilder().addTokenized(ekwDiffCmd)).
    //             join();
    //     }
    //
    //     StringBuilder fileListStr = new StringBuilder();
    //     BufferedReader br = new BufferedReader(new StringReader(baos.toString()));
    //     String line=null;
    //     while ((line=br.readLine()) != null) {
    //         fileListStr.append(line);
    //         fileListStr.append(" ");
    //     }
    //
    //     return fileListStr.toString();
    //     // return baos.toString();
    // }


    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link KlocworkDesktopBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/KlocworkDesktopBuilder/*.jelly}
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
            return "Emenda Klocwork Desktop Analysis";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            save();
            return super.configure(req,formData);
        }
    }
}
