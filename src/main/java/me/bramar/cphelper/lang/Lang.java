package me.bramar.cphelper.lang;

import me.bramar.cphelper.CompetitiveProgrammingHelperProgram;
import me.bramar.cphelper.functional.ISConsumer;
import me.bramar.cphelper.functional.OSConsumer;

import java.io.File;
import java.io.IOException;

public abstract class Lang {
    protected CompetitiveProgrammingHelperProgram main;
    Lang(CompetitiveProgrammingHelperProgram main) {
        this.main = main;
    }
    public abstract void build(String arg) throws IOException, InterruptedException;
    public abstract void exec(String arg, OSConsumer input, ISConsumer output)
            throws IOException, InterruptedException;
    public abstract File sourceFile(String arg);
    public abstract File buildFile(String arg);
    public abstract String extension(); // including dot
}
