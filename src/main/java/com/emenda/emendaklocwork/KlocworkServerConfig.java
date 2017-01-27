
package com.emenda.emendaklocwork;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;


public class KlocworkServerConfig {

    private final String name;
    private final String url;
    private final boolean specificLicense;
    private final String licenseHost;
    private final String licensePort;


    @DataBoundConstructor
    public KlocworkServerConfig(String name, String url,
                           boolean specificLicense,
                           String licenseHost, String licensePort) {

        this.name = name;
        this.url = url;
        this.specificLicense = specificLicense;
        this.licenseHost = licenseHost;
        this.licensePort = licensePort;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isSpecificLicense() {
        return specificLicense;
    }

    public String getLicenseHost() {
        return licenseHost;
    }

    public String getLicensePort() {
        return licensePort;
    }

}
