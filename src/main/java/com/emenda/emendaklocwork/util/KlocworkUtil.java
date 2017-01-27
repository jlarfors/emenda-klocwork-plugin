package com.emenda.emendaklocwork.util;

import com.emenda.emendaklocwork.KlocworkConstants;

import org.apache.commons.lang3.StringUtils;

import hudson.EnvVars;
import hudson.FilePath;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class KlocworkUtil {

    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static String getAndExpandEnvVar(EnvVars envVars, String var) {
        String value = envVars.get(var, "");
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return envVars.expand(value);
    }

    public static String getKlocworkProjectUrl(EnvVars envVars) throws IOException {
        try {
            URL url = new URL(getAndExpand(envVars, KlocworkConstants.KLOCWORK_URL) +
                "/" + getAndExpand(envVars, KlocworkConstants.KLOCWORK_PROJECT));
            return url.toString();
        } catch (MalformedURLException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }

    public static String getBuildSpecPath(EnvVars envVars, FilePath workspace)
                    throws IOException, InterruptedException {
        if (StringUtils.isEmpty(envVars.get(KlocworkConstants.KLOCWORK_BUILD_SPEC))) {
            return (new FilePath(workspace, KlocworkConstants.KLOCWORK_DEFAULT_BUILD_SPEC)).getRemote();
        } else {
            return (new FilePath(workspace, getAndExpand(envVars,
                KlocworkConstants.KLOCWORK_BUILD_SPEC))).getRemote();
        }
    }


}
