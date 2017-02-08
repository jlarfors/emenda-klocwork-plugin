
package com.emenda.emendaklocwork;

import com.emenda.emendaklocwork.util.KlocworkUtil;

import org.apache.commons.lang3.StringUtils;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.lang.InterruptedException;

public class KlocworkDesktopUtil extends AbstractDescribableImpl<KlocworkDesktopUtil> {

    private final String projectDir;
    private final String reportFile;
    private final String additionalOpts;

    @DataBoundConstructor
    public KlocworkDesktopUtil(String projectDir, String reportFile, String additionalOpts) {

        this.projectDir = projectDir;
        this.reportFile = reportFile;
        // this.reportFile = StringUtils.isEmpty(reportFile) ? "kwcheck_report.xml" : reportFile;
        this.additionalOpts = additionalOpts;
    }

    public ArgumentListBuilder getVersionCmd()
                                        throws IOException, InterruptedException {
        ArgumentListBuilder versionCmd = new ArgumentListBuilder("kwcheck");
        versionCmd.add("--version");
        return versionCmd;
    }

    public ArgumentListBuilder getKwcheckCreateCmd(EnvVars envVars, FilePath workspace)
                                        throws IOException, InterruptedException {

        validateParentProjectDir(getKwlpDir(workspace, envVars).getParent());

        ArgumentListBuilder kwcheckCreateCmd = new ArgumentListBuilder("kwcheck", "create");
        kwcheckCreateCmd.add("--url", KlocworkUtil.getKlocworkProjectUrl(envVars));
        kwcheckCreateCmd.add("--project-dir", getKwlpDir(workspace, envVars).getRemote());
        kwcheckCreateCmd.add("--settings-dir", getKwpsDir(workspace, envVars).getRemote());
        kwcheckCreateCmd.add("--build-spec", KlocworkUtil.getBuildSpecPath(envVars, workspace));
        return kwcheckCreateCmd;
    }

    public ArgumentListBuilder getKwcheckRunCmd(EnvVars envVars, FilePath workspace)
                                        throws IOException, InterruptedException {
        ArgumentListBuilder kwcheckRunCmd =
            new ArgumentListBuilder("kwcheck", "run");
        kwcheckRunCmd.add("--project-dir", getKwlpDir(workspace, envVars).getRemote());
        kwcheckRunCmd.add("--license-host", KlocworkUtil.getAndExpandEnvVar(
            envVars, KlocworkConstants.KLOCWORK_LICENSE_HOST));
        kwcheckRunCmd.add("--license-port", KlocworkUtil.getAndExpandEnvVar(
            envVars, KlocworkConstants.KLOCWORK_LICENSE_PORT));
        kwcheckRunCmd.add("-F", "xml", "--report", reportFile);
        kwcheckRunCmd.add("--build-spec", KlocworkUtil.getBuildSpecPath(envVars, workspace));
        if (!StringUtils.isEmpty(additionalOpts)) {
            kwcheckRunCmd.addTokenized(envVars.expand(additionalOpts));
        }

        // TODO: check if reportFile is used and parse if so
        String diffFiles = ""; // get diff file list ()
        if (!StringUtils.isEmpty(diffFiles)) {
            // add contents of diffFiles
        }

        return kwcheckRunCmd;
    }

    /*
    function to check if a local project already exists.
    If the creation of a project went wrong before, there may be some left over .kwlp or .kwps directories
    so we need to make sure to clean these up.
    If both .kwlp and .kwps exist then we reuse them
     */
    public boolean hasExistingProject(FilePath workspace, EnvVars envVars)
        throws IOException, InterruptedException {
        FilePath kwlp = getKwlpDir(workspace, envVars);
        FilePath kwps = getKwpsDir(workspace, envVars);

        if (kwlp.exists()) {
            if (kwps.exists()) {
                // both directories exist
                return true;
            } else {
                // clean up directories because something has gone wrong
                cleanupExistingProject(kwlp, kwps);
                return false;
            }
        } else if (kwps.exists()) {
            // clean up directories because something has gone wrong
            cleanupExistingProject(kwlp, kwps);
            return false;
        } else {
            // no existing project
            return false;
        }
    }

    private void validateParentProjectDir(FilePath dir) throws IOException, InterruptedException {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private FilePath getKwlpDir(FilePath workspace, EnvVars envVars) {
        return new FilePath(
            workspace.child(envVars.expand(projectDir)), ".kwlp");
    }

    private FilePath getKwpsDir(FilePath workspace, EnvVars envVars) {
        return new FilePath(
            workspace.child(envVars.expand(projectDir)), ".kwps");
    }

    private void cleanupExistingProject(FilePath kwlp, FilePath kwps)
        throws IOException, InterruptedException {
        if (kwlp.exists()) {
            kwlp.deleteRecursive();
        }
        if (kwps.exists()) {
            kwps.deleteRecursive();
        }
    }


    public String getProjectDir() { return projectDir; }
    public String getReportFile() { return reportFile; }
    public String getAdditionalOpts() { return additionalOpts; }



    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkDesktopUtil> {
        public String getDisplayName() { return null; }
    }


}
