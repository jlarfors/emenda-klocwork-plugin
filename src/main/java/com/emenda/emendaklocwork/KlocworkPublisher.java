package com.emenda.emendaklocwork;

import com.emenda.emendaklocwork.services.KlocworkApiService;
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
import hudson.security.ACL;
import hudson.tasks.Publisher;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;

import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
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
 * Sample {@link Publisher}.
 *
 * <p>
 * When the user configures the project and enables this Publisher,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link KlocworkPublisher} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class KlocworkPublisher extends Publisher {

    private final String diffCmd;

    @DataBoundConstructor
    public KlocworkPublisher(String diffCmd) {
        this.diffCmd = diffCmd;

    }

    /**
     * We'll use this from the {@code config.jelly}.
     */
    public String getDiffCmd() {
        return diffCmd;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) {
        KlocworkLogger logger = new KlocworkLogger(listener.getLogger());
        EnvVars envVars = null;
        try {
            envVars = build.getEnvironment(listener);

            build.addAction(new KlocworkBuildAction(build));

            String host = "";
            String port = "";
            String user = "";
            String ltoken = "";
            logger.logMessage("Credentials ID = " + user);
            logger.logMessage("LTOKEN         = " + ltoken);



            // KlocworkApiService kwService = new KlocworkApiService("xubuntu-emenda", 8082, user,
            //     ltoken);
            //
            // String request = "project=" + "git" +
        	// 		 	 "&action=" + "search";
            // if (kwService.sendRequest(request)) {
            //     logger.logMessage("Klocwork API request successful!");
            //
            // } else {
            //     logger.logMessage("Klocwork API request unsuccessful! Exiting.");
            //     logger.logMessage(kwService.getErrorMsg());
            //     // return false to mark job as failed due to failed API connection
            //     return false;
            // }
            //
            // // get list of results
            // JSONArray jsonResults = kwService.getJsonResponses();
            // for(int i = 0; i < jsonResults.size(); i++) {
            //       JSONObject jObj = jsonResults.getJSONObject(i);
            //       logger.logMessage(jObj.toString());
            // }


        } catch (IOException ioe) {
            logger.logMessage(KlocworkUtil.exceptionToString(ioe));
            return false;
        } catch (InterruptedException ie) {
            logger.logMessage(KlocworkUtil.exceptionToString(ie));
            return false;
        }
        // catch (KlocworkException ee) {
        //     logger.logMessage(KlocworkUtil.exceptionToString(ee));
        //     return false;
        // }


        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }


    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link KlocworkPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/KlocworkPublisher/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // Indicates that this Publisher can be used with all kinds of project types
            return true;
        }


        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Emenda Klocwork Report";
        }



        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            save();
            return super.configure(req,formData);
        }
    }
}
