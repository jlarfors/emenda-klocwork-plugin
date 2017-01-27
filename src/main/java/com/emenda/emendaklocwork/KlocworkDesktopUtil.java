
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
    private final String diffFile;

    @DataBoundConstructor
    public KlocworkDesktopUtil(String projectDir, String diffFile) {

        this.projectDir = projectDir;
        this.diffFile = diffFile;
    }

    public ArgumentListBuilder getKwcheckCreateCmd(EnvVars envVars, FilePath workspace)
                                        throws IOException, InterruptedException {

        validateParentProjectDir(getKwlpDir(workspace).getParent());

        ArgumentListBuilder kwcheckCreateCmd = new ArgumentListBuilder("kwcheck", "create");
        kwcheckCreateCmd.add("--url", KlocworkUtil.getKlocworkProjectUrl(envVars));
        kwcheckCreateCmd.add("--project-dir", getKwlpDir(workspace).getRemote());
        kwcheckCreateCmd.add("--settings-dir", getKwpsDir(workspace).getRemote());
        kwcheckCreateCmd.add("--build-spec", KlocworkUtil.getBuildSpecPath(envVars, workspace));
        return kwcheckCreateCmd;
    }

    public ArgumentListBuilder getKwcheckRunCmd(EnvVars envVars, FilePath workspace)
                                        throws IOException, InterruptedException {
        ArgumentListBuilder kwcheckRunCmd =
            new ArgumentListBuilder("kwcheck", "run");
        kwcheckRunCmd.add("--project-dir", getKwlpDir(workspace).getRemote());
        kwcheckRunCmd.add("--license-host", KlocworkUtil.getAndExpandEnvVar(
            envVars, KlocworkConstants.KLOCWORK_LICENSE_HOST));
        kwcheckRunCmd.add("--license-port", KlocworkUtil.getAndExpandEnvVar(
            envVars, KlocworkConstants.KLOCWORK_LICENSE_PORT));
        kwcheckRunCmd.add("-F", "xml", "--report", "kwcheck_report.xml");
        kwcheckRunCmd.add("--build-spec", KlocworkUtil.getBuildSpecPath(envVars, workspace));

        // TODO: check if diffFile is used and parse if so
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
    public boolean hasExistingProject(FilePath workspace) throws IOException, InterruptedException {
        FilePath kwlp = getKwlpDir(workspace);
        FilePath kwps = getKwpsDir(workspace);

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
    public String getDiffFile() { return diffFile; }



    @Extension
    public static class DescriptorImpl extends Descriptor<KlocworkDesktopUtil> {
        public String getDisplayName() { return null; }
    }


}
