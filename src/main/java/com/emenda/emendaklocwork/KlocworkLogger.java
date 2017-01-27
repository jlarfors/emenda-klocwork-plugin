
package com.emenda.emendaklocwork;

import java.io.PrintStream;
import hudson.model.TaskListener;

public class KlocworkLogger {

    private final String MSG_PREFIX = "[Klocwork] - ";
    private PrintStream printStream = null;

    public KlocworkLogger(PrintStream printStream) {
        this.printStream = printStream;
    }

    public void logMessage(String message) {
        printStream.println(MSG_PREFIX + message);
    }


}
