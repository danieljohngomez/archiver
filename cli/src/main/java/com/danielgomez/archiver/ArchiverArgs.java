package com.danielgomez.archiver;

import com.beust.jcommander.Parameter;

public class ArchiverArgs {

    @Parameter(names = { "--help", "-h" }, help = true)
    private boolean help;

    public boolean isHelp() {
        return help;
    }
}
