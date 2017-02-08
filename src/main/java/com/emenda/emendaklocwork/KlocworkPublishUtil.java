
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

public class KlocworkPublishUtil extends AbstractDescribableImpl<KlocworkPublishUtil> {

    private final String buildName;
    private final String tablesDir;
    private final String additionalOpts;

    @DataBoundConstructor
    public KlocworkPublishUtil(String buildName, String tablesDir,
            String additionalOpts) {

        this.buildName = buildName;
        this.tablesDir = tablesDir;
        this.additionalOpts = additionalOpts;
    }

    public ArgumentListBuilder getVersionCmd()
                                        throws IOException, InterruptedException {
        ArgumentListBuilder versionCmd = new ArgumentListBuilder("kwadmin");
        versionCmd.add("--version");
        return versionCmd;
    }

    public ArgumentListBuilder getKwadminLoadCmd(EnvVars envVars, FilePath workspace) {
        ArgumentListBuilder kwadminCmd =
            new ArgumentListBuilder("kwadmin");
        kwadminCmd.add("--url", KlocworkUtil.getAndExpandEnvVar(envVars,
            KlocworkConstants.KLOCWORK_URL));
        kwadminCmd.add("load");

        // add options such as --name of build
        if (!StringUtils.isEmpty(buildName)) {
            kwadminCmd.add("--name", envVars.expand(buildName));
        }

        kwadminCmd.add(KlocworkUtil.getAndExpandEnvVar(envVars,
            KlocworkConstants.KLOCWORK_PROJECT));
        kwadminCmd.add(KlocworkUtil.getKwtablesDir(tablesDir));
        return kwadminCmd;
    }



    public String getBuildName() { return buildName; }
    public String getTablesDir() { return tablesDir; }
    public String getAdditionalOpts() { return additionalOpts; }

    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkPublishUtil> {
        public String getDisplayName() { return null; }
    }

}
