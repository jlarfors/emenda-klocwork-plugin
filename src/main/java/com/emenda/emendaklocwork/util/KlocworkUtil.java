package com.emenda.emendaklocwork.util;

import com.emenda.emendaklocwork.KlocworkConstants;

import org.apache.commons.lang3.StringUtils;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.InterruptedException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class KlocworkUtil {

    public static String[] getLtokenValues(EnvVars envVars, Launcher launcher) throws IOException {
        try {
            String[] ltokenLine = launcher.getChannel().call(
                new KlocworkLtokenFetcher(
                getAndExpandEnvVar(envVars, KlocworkConstants.KLOCWORK_URL)));

            if (ltokenLine.length < 4) {
                throw new IOException("Error: ltoken string returned is too short: " +
                "\"" + Arrays.toString(ltokenLine) + "\"");
            } else if (StringUtils.isEmpty(ltokenLine[KlocworkConstants.LTOKEN_USER_INDEX])) {
                throw new IOException("Error: ltoken invalid. Reason: user is empty" +
                "\"" + Arrays.toString(ltokenLine) + "\"");
            }  else if (StringUtils.isEmpty(ltokenLine[KlocworkConstants.LTOKEN_HASH_INDEX])) {
                throw new IOException("Error: ltoken invalid. Reason: ltoken is empty" +
                "\"" + Arrays.toString(ltokenLine) + "\"");
            } else {
                return ltokenLine;
            }
        } catch (InterruptedException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }

    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static String getAndExpandEnvVar(EnvVars envVars, String var) {
        String value = envVars.get(var, "");
        if (StringUtils.isEmpty(value)) {
            return ""; // TODO - handle empty vs null
        }
        return envVars.expand(value);
    }

    public static String getKlocworkProjectUrl(EnvVars envVars) throws IOException {
        try {
            // handle URLs ending with "/", e.g. http://kwserver:8080/
            String urlStr = getAndExpandEnvVar(envVars, KlocworkConstants.KLOCWORK_URL);
            String separator = (urlStr.endsWith("/")) ? "" : "/";
            URL url = new URL(urlStr + separator +
                getAndExpandEnvVar(envVars, KlocworkConstants.KLOCWORK_PROJECT));
            return url.toString();
        } catch (MalformedURLException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }

    public static String getBuildSpecPath(EnvVars envVars, FilePath workspace)
                    throws IOException, InterruptedException {
        String envBuildSpec = getAndExpandEnvVar(envVars, KlocworkConstants.KLOCWORK_BUILD_SPEC);
        String tmpBuildSpec = (StringUtils.isEmpty(envBuildSpec)) ? KlocworkConstants.DEFAULT_BUILD_SPEC : envBuildSpec;
        // return (new FilePath(workspace, tmpBuildSpec)).getRemote();
        return tmpBuildSpec;
    }

    public static String getKwtablesDir(String tablesDir) {
        String tmpTablesDir = (StringUtils.isEmpty(tablesDir)) ? KlocworkConstants.DEFAULT_TABLES_DIR : tablesDir;
        // return (new FilePath(workspace, envVars.expand(tmpTablesDir))).getRemote();
        return tablesDir;
    }


}
