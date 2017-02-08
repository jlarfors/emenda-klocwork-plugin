
package com.emenda.emendaklocwork;

import com.emenda.emendaklocwork.util.KlocworkUtil;

import org.kohsuke.stapler.DataBoundConstructor;

import org.apache.commons.lang3.StringUtils;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.lang.InterruptedException;

public class KlocworkServerUtil extends AbstractDescribableImpl<KlocworkServerUtil> {

    private final String tablesDir;
    private final boolean incrementalAnalysis;
    private final boolean ignoreReturnCodes;
    private final String additionalOpts;

    @DataBoundConstructor
    public KlocworkServerUtil(String buildName, String tablesDir,
            boolean incrementalAnalysis, boolean ignoreReturnCodes,
            String additionalOpts) {

        this.tablesDir = tablesDir;
        this.incrementalAnalysis = incrementalAnalysis;
        this.ignoreReturnCodes = ignoreReturnCodes;
        this.additionalOpts = additionalOpts;
    }

    public ArgumentListBuilder getKwdeployCmd(EnvVars envVars, FilePath workspace) {
        ArgumentListBuilder kwdeployCmd =
            new ArgumentListBuilder("kwdeploy");
        kwdeployCmd.add("sync");
        kwdeployCmd.add("--url", KlocworkUtil.getAndExpandEnvVar(envVars,
            KlocworkConstants.KLOCWORK_URL));
        return kwdeployCmd;
    }

    public ArgumentListBuilder getVersionCmd()
                                        throws IOException, InterruptedException {
        ArgumentListBuilder versionCmd = new ArgumentListBuilder("kwbuildproject");
        versionCmd.add("--version");
        return versionCmd;
    }

    public ArgumentListBuilder getKwbuildprojectCmd(EnvVars envVars,
        FilePath workspace) throws IOException, InterruptedException {

        ArgumentListBuilder kwbuildprojectCmd =
            new ArgumentListBuilder("kwbuildproject");
        kwbuildprojectCmd.add("--tables-directory", KlocworkUtil.getKwtablesDir(tablesDir));
        kwbuildprojectCmd.add("--license-host");
        kwbuildprojectCmd.add(KlocworkUtil.getAndExpandEnvVar(envVars,
            KlocworkConstants.KLOCWORK_LICENSE_HOST));
        kwbuildprojectCmd.add("--license-port");
        kwbuildprojectCmd.add(KlocworkUtil.getAndExpandEnvVar(envVars,
            KlocworkConstants.KLOCWORK_LICENSE_PORT));
        kwbuildprojectCmd.add("--url");
        kwbuildprojectCmd.add(KlocworkUtil.getKlocworkProjectUrl(envVars));

        if (incrementalAnalysis) {
            kwbuildprojectCmd.add("--incremental");
        } else {
            kwbuildprojectCmd.add("--force");
        }
        if (!StringUtils.isEmpty(additionalOpts)) {
            kwbuildprojectCmd.addTokenized(envVars.expand(additionalOpts));
        }
        // Note: this has to be final step, because the build spec always comes
        // last!
        kwbuildprojectCmd.add(KlocworkUtil.getBuildSpecPath(envVars, workspace));
        return kwbuildprojectCmd;
    }

    public ArgumentListBuilder getKwadminImportCmd(EnvVars envVars, FilePath workspace) {
        // FilePath kwTablesDir = new FilePath(workspace, envVars.expand(tablesDir));
        ArgumentListBuilder kwadminCmd =
            new ArgumentListBuilder("kwadmin");
        kwadminCmd.add("--url", KlocworkUtil.getAndExpandEnvVar(envVars,
            KlocworkConstants.KLOCWORK_URL));
        kwadminCmd.add("import-config");
        // TODO: add more!
        return kwadminCmd;
    }
    
    public String getTablesDir() { return tablesDir; }
    public boolean getIncrementalAnalysis() { return incrementalAnalysis; }
    public boolean getIgnorereturnCodes() { return ignoreReturnCodes; }
    public String getAdditionalOpts() { return additionalOpts; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkServerUtil> {
        public String getDisplayName() { return null; }
    }

}
