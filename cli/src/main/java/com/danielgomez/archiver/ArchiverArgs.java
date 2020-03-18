package com.danielgomez.archiver;

import com.beust.jcommander.Parameter;

public class ArchiverArgs {

    @Parameter(names = { "-h", "--help" }, help = true)
    private boolean help;

    @Parameter( names = { "-a", "--archiver" }, description = "The archiver to use")
    private String archiver = "zip";

    public boolean isHelp() {
        return help;
    }

    public String getArchiver() {
        return archiver;
    }
}
