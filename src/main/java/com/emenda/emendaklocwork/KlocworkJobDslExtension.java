package com.emenda.emendaklocwork;

import com.emenda.emendaklocwork.KlocworkBuildWrapper;
import com.emenda.emendaklocwork.KlocworkDesktopBuilder;
import com.emenda.emendaklocwork.KlocworkDesktopUtil;
import com.emenda.emendaklocwork.KlocworkServerBuilder;
import com.emenda.emendaklocwork.KlocworkServerUtil;
import com.emenda.emendaklocwork.KlocworkXSyncBuilder;
import com.emenda.emendaklocwork.KlocworkXSyncUtil;

import hudson.Extension;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.wrapper.WrapperContext;
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;


@Extension(optional = true)
public class KlocworkJobDslExtension extends ContextExtensionPoint {

    @DslExtensionMethod(context = WrapperContext.class)
    public Object klocworkWrapper(String configName, String projectName, String buildSpecification) {
        return new KlocworkBuildWrapper(configName, "", projectName, buildSpecification);
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkDesktopBuilder(String projectDir, String reportFile, String additionalOpts) {
        return new KlocworkDesktopBuilder(new KlocworkDesktopUtil(projectDir,reportFile, additionalOpts));
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkServerBuilder(String buildName, String tablesDir, boolean incrementalAnalysis, boolean ignoreReturnCodes, String additionalOptions) {
        return new KlocworkServerBuilder(new KlocworkServerUtil(buildName, tablesDir, incrementalAnalysis, ignoreReturnCodes, additionalOptions));
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkXSyncBuilder(boolean dryRun, String lastSync, String projectRegexp,
                                        boolean statusAnalyze, boolean statusIgnore,
                                        boolean statusNotAProblem, boolean statusFix,
                                        boolean statusFixInNextRelease, boolean statusFixInLaterRelease,
                                        boolean statusDefer, boolean statusFilter,
                                        String additionalOpts) {
        return new KlocworkXSyncBuilder(new KlocworkXSyncUtil(dryRun, lastSync, projectRegexp, statusAnalyze, statusIgnore,
                                                    statusNotAProblem, statusFix, statusFixInNextRelease,
                                                    statusFixInLaterRelease, statusDefer, statusFilter,
                                                    additionalOpts));
    }

    @DslExtensionMethod(context = PublisherContext.class)
    public Object klocworkPublisher() {
        return null;
    }

}
