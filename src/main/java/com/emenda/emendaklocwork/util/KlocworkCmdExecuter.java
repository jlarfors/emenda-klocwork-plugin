package com.emenda.emendaklocwork.util;

import com.emenda.emendaklocwork.KlocworkConstants;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.util.ArgumentListBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.InterruptedException;
import java.util.ArrayList;
import java.util.List;

public class KlocworkCmdExecuter {

    private List<String> stdout;
    private List<String> stderr;

    public KlocworkCmdExecuter() {
        stdout = new ArrayList<String>();
        stderr = new ArrayList<String>();
    }

    public int executeCommand(Launcher launcher, BuildListener listener,
                        FilePath buildDir, EnvVars envVars, ArgumentListBuilder cmds)
                        throws IOException, InterruptedException {
        if (launcher.isUnix()) {
            cmds = new ArgumentListBuilder("/bin/sh", "-c", cmds.toString());
        } else {
            cmds = cmds.toWindowsCommand();
            // cmds = new ArgumentListBuilder("cmd", "/c", cmds.toString());
        }

        return launcher.launch().
            stdout(listener).stderr(listener.getLogger()).
            pwd(buildDir).envs(envVars).cmds(cmds)
            .join();
    }

    public int executeCommandGetResults(Launcher launcher, BuildListener listener,
                        FilePath buildDir, EnvVars envVars, ArgumentListBuilder cmds)
                        throws IOException, InterruptedException {
        int retCode = 0;

        stdout.clear();
        stderr.clear();

        ByteArrayOutputStream baosStdout = new ByteArrayOutputStream();
        ByteArrayOutputStream baosStderr = new ByteArrayOutputStream();

        retCode = launcher.launch().
            stdout(baosStdout).stderr(baosStderr).
            pwd(buildDir).envs(envVars).cmds(cmds)
            .join();

        writeToList(stdout, baosStdout);
        writeToList(stderr, baosStderr);

        return retCode;
    }

    private void writeToList(List<String> list, ByteArrayOutputStream baos)
                                                            throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(baos.toString()));
        String line = null;
        while ((line=br.readLine()) != null) {
            list.add(line);
        }
    }

    public List<String> getStdout() { return stdout; }
    public List<String> getStderr() { return stderr; }

}
