package com.emenda.emendaklocwork;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.Builder;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildWrapperDescriptor;

import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link HelloWorldBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class KlocworkBuildWrapper extends BuildWrapper {

    private final String serverConfig;
    private final String installConfig;
    private final String serverProject;
    private final String buildSpec;

    @DataBoundConstructor
    public KlocworkBuildWrapper(String serverConfig, String installConfig,
                    String serverProject, String buildSpec) {
        this.serverConfig = serverConfig;
        this.installConfig = installConfig;
        this.serverProject = serverProject;
        this.buildSpec = buildSpec;
    }



    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher,
                             BuildListener listener) throws IOException, InterruptedException {

        KlocworkLogger logger = new KlocworkLogger(listener.getLogger());
        logger.logMessage("Setting up environment for Klocwork jobs...");

        KlocworkServerConfig server = getDescriptor().getServerConfig(serverConfig);
        KlocworkInstallConfig install = getDescriptor().getInstallConfig(installConfig);

        return new Environment() {
            @Override
            public void buildEnvVars(Map<String, String> env) {

                if (server != null) {
                    logger.logMessage("Adding the Klocwork Server URL");
                    env.put(KlocworkConstants.KLOCWORK_URL, server.getUrl());
                    // if specific license details, else use the global ones
                    if (server.isSpecificLicense()) {
                        logger.logMessage("Using specific License");
                        env.put(KlocworkConstants.KLOCWORK_LICENSE_HOST,
                                    server.getLicenseHost());
                        env.put(KlocworkConstants.KLOCWORK_LICENSE_PORT,
                                    server.getLicensePort());
                    } else {
                        logger.logMessage("Using Global License Settings");
                        env.put(KlocworkConstants.KLOCWORK_LICENSE_HOST,
                                        getDescriptor().getGlobalLicenseHost());
                        env.put(KlocworkConstants.KLOCWORK_LICENSE_PORT,
                                        getDescriptor().getGlobalLicensePort());
                    }
                }

                if (install != null) {
                    logger.logMessage("Adding Klocwork to the PATH");
                    String paths = env.get("PATH");
                    logger.logMessage("PATH = " + paths);
                    String separator = ":"; // TODO: handle Windows
                    paths += separator + install.getPaths();
                    logger.logMessage("new PATH = " + paths);
                    env.put("PATH", paths);
                }

                env.put(KlocworkConstants.KLOCWORK_PROJECT, serverProject);
                env.put(KlocworkConstants.KLOCWORK_BUILD_SPEC, buildSpec);

            }
        };
    }



    /**
     * We'll use this from the {@code config.jelly}.
     */
    public String getServerConfig() { return serverConfig; }
    public String getInstallConfig() { return installConfig; }
    public String getServerProject() { return serverProject; }
    public String getBuildSpec() { return buildSpec; }


    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/KlocworkBuildWrapper/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use {@code transient}.
         */

         private String globalLicenseHost;
         private String globalLicensePort;
         private KlocworkServerConfig[] serverConfigs = new KlocworkServerConfig[0];
         private KlocworkInstallConfig[] installConfigs = new KlocworkInstallConfig[0];

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(AbstractProject<?, ?> item) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }


        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Emenda Klocwork Build Settings";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            serverConfigs = req.bindParametersToList(KlocworkServerConfig.class,
                    "klocworkServer.").toArray(new KlocworkServerConfig[0]);
            installConfigs = req.bindParametersToList(KlocworkInstallConfig.class,
                    "klocworkInstall.").toArray(new KlocworkInstallConfig[0]);

            JSONObject json = formData.getJSONObject("klocworkConfig");
            globalLicenseHost = json.getString("globalLicenseHost");
            globalLicensePort = json.getString("globalLicensePort");
            save();
            return super.configure(req,formData);
        }

        public String getGlobalLicenseHost() { return globalLicenseHost; }
        public String getGlobalLicensePort() { return globalLicensePort; }
        public KlocworkServerConfig[] getServerConfigs() { return serverConfigs; }
        public KlocworkInstallConfig[] getInstallConfigs() { return installConfigs; }

        public KlocworkServerConfig getServerConfig(String name) {
            for (KlocworkServerConfig config : serverConfigs) {
                if (config.getName().equals(name))
                    return config;
            }
            return null;
        }

        public KlocworkInstallConfig getInstallConfig(String name) {
            for (KlocworkInstallConfig config : installConfigs) {
                if (config.getName().equals(name))
                    return config;
            }
            return null;
        }

    }
}
