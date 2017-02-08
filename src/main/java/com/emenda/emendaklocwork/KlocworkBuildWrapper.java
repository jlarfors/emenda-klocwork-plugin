package com.emenda.emendaklocwork;

import org.apache.commons.lang3.StringUtils;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.DecoratedLauncher;
import hudson.Proc;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.Run.RunnerAbortedException;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.Builder;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.CopyOnWriteList;

import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
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
    public Launcher decorateLauncher(AbstractBuild build, Launcher launcher,
                             BuildListener listener) throws IOException, InterruptedException,
                             RunnerAbortedException {
        KlocworkLogger logger = new KlocworkLogger("BuildWrapper", listener.getLogger());
        logger.logMessage("Setting up PATH for Klocwork jobs...");
        KlocworkInstallConfig install = getDescriptor().getInstallConfig(installConfig);

        final Node node =  Computer.currentComputer().getNode();
        if (node == null) {
            throw new AbortException("Cannot add variables to deleted node");
        }

        return new DecoratedLauncher(launcher) {
            @Override
            public Proc launch(ProcStarter starter) throws IOException {
                EnvVars vars;
                // taken from CustomToolsPlugin
                try { // Dirty hack, which allows to avoid NPEs in Launcher::envs()
                    vars = toEnvVars(starter.envs());
                } catch (NullPointerException npe) {
                    vars = new EnvVars();
                } catch (InterruptedException x) {
                    throw new IOException(x);
                }

                if (install != null) {
                    logger.logMessage("Adding Klocwork to PATH. Using install \""
                    + install.getName() + "\"");
                    String paths = vars.get("PATH");
                    String separator = (launcher.isUnix()) ? ":" : ";";
                    paths += separator + install.getPaths();
                    vars.remove("PATH");
                    vars.put("PATH+", paths);
                }

                return getInner().launch(starter.envs(vars));
            }

            private EnvVars toEnvVars(String[] envs) throws IOException, InterruptedException {
                Computer computer = node.toComputer();
                EnvVars vars = computer != null ? computer.getEnvironment() : new EnvVars();
                for (String line : envs) {
                    vars.addLine(line);
                }
                return vars;
            }
        };
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher,
            BuildListener listener) throws IOException, InterruptedException {

            KlocworkLogger logger = new KlocworkLogger("BuildWrapper", listener.getLogger());
            logger.logMessage("Setting up environment variables for Klocwork jobs...");
            KlocworkServerConfig server = getDescriptor().getServerConfig(serverConfig);
            return new Environment() {
                @Override
                public void buildEnvVars(Map<String, String> env) {

                    if (server != null) {
                        logger.logMessage("Adding the Klocwork Server URL " + server.getUrl());
                        env.put(KlocworkConstants.KLOCWORK_URL, server.getUrl());
                        // if specific license details, else use the global ones
                        if (server.isSpecificLicense()) {
                            logger.logMessage("Using specific License for given server " +
                                server.getLicensePort() + "@" + server.getLicenseHost());
                            env.put(KlocworkConstants.KLOCWORK_LICENSE_HOST,
                                        server.getLicenseHost());
                            env.put(KlocworkConstants.KLOCWORK_LICENSE_PORT,
                                        server.getLicensePort());
                        } else {
                            logger.logMessage("Using Global License Settings " +
                                getDescriptor().getGlobalLicensePort() + "@" +
                                getDescriptor().getGlobalLicenseHost());
                            env.put(KlocworkConstants.KLOCWORK_LICENSE_HOST,
                                            getDescriptor().getGlobalLicenseHost());
                            env.put(KlocworkConstants.KLOCWORK_LICENSE_PORT,
                                            getDescriptor().getGlobalLicensePort());
                        }
                    } else {
                        logger.logMessage("Warning: No Klocwork server selected. " +
                            "Klocwork cannot perform server builds or synchronisations " +
                            "without a server.");
                        logger.logMessage("Using Global License Settings " +
                            getDescriptor().getGlobalLicensePort() + "@" +
                            getDescriptor().getGlobalLicenseHost());
                        env.put(KlocworkConstants.KLOCWORK_LICENSE_HOST,
                                        getDescriptor().getGlobalLicenseHost());
                        env.put(KlocworkConstants.KLOCWORK_LICENSE_PORT,
                                        getDescriptor().getGlobalLicensePort());
                    }

                    env.put(KlocworkConstants.KLOCWORK_PROJECT, serverProject);
                    if (StringUtils.isEmpty(buildSpec)) {
                        env.put(KlocworkConstants.KLOCWORK_BUILD_SPEC,
                            KlocworkConstants.DEFAULT_BUILD_SPEC);
                    } else {
                        env.put(KlocworkConstants.KLOCWORK_BUILD_SPEC, buildSpec);
                    }


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

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

         private String globalLicenseHost;
         private String globalLicensePort;
        //  private KlocworkServerConfig[] serverConfigs = new KlocworkServerConfig[0];

         private CopyOnWriteList<KlocworkServerConfig> serverConfigs = new CopyOnWriteList<KlocworkServerConfig>();
         private CopyOnWriteList<KlocworkInstallConfig> installConfigs = new CopyOnWriteList<KlocworkInstallConfig>();
        //  private KlocworkInstallConfig[] installConfigs = new KlocworkInstallConfig[0];

        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }


        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Klocwork Build Settings";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // serverConfigs = req.bindParametersToList(KlocworkServerConfig.class,
            //         "klocworkServer.").toArray(new KlocworkServerConfig[0]);
            // installConfigs = req.bindParametersToList(KlocworkInstallConfig.class,
            //         "klocworkInstall.").toArray(new KlocworkInstallConfig[0]);

            serverConfigs.replaceBy(req.bindJSONToList(KlocworkServerConfig.class, formData.get("serverConfigs")));
            installConfigs.replaceBy(req.bindJSONToList(KlocworkInstallConfig.class, formData.get("installConfigs")));

            // JSONObject json = formData.getJSONObject("klocworkConfig");
            // globalLicenseHost = json.getString("globalLicenseHost");
            // globalLicensePort = json.getString("globalLicensePort");
            globalLicenseHost = formData.getString("globalLicenseHost");
            globalLicensePort = formData.getString("globalLicensePort");
            save();
            return super.configure(req,formData);
        }

        public String getGlobalLicenseHost() { return globalLicenseHost; }
        public String getGlobalLicensePort() { return globalLicensePort; }

        public KlocworkServerConfig[] getServerConfigs() {
            return serverConfigs.toArray(new KlocworkServerConfig[0]);
        }

        public KlocworkInstallConfig[] getInstallConfigs() {
            return installConfigs.toArray(new KlocworkInstallConfig[0]);
        }

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
