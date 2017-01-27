
package com.emenda.emendaklocwork;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;


public class KlocworkInstallConfig {

    private final String name;
    private final String paths;


    @DataBoundConstructor
    public KlocworkInstallConfig(String name, String paths) {
        this.name = name;
        this.paths = paths;
    }

    public String getName() { return name; }
    public String getPaths() { return paths; }
}
