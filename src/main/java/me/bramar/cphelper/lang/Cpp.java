package me.bramar.cphelper.lang;

import me.bramar.cphelper.CompetitiveProgrammingHelperProgram;
import me.bramar.cphelper.functional.ISConsumer;
import me.bramar.cphelper.functional.OSConsumer;
import me.bramar.cphelper.site.Atcoder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.bramar.cphelper.CompetitiveProgrammingHelperProgram.*;

public class Cpp extends Lang {
    public Cpp(CompetitiveProgrammingHelperProgram main) {
        super(main);
    }
    @Override
    public void build(String arg) throws IOException, InterruptedException {
        File x = buildFile(arg);
        if(x.exists()) {
            Files.delete(x.toPath());
        }
        List<String> args = new ArrayList<>(
                Arrays.asList("g++.exe", "-O2")
        );
        main.getSite().customLibrary(args, this);
        args.add("-o");
        args.add(buildFile(arg).getAbsolutePath());
        args.add(sourceFile(arg).getAbsolutePath());

        ProcessBuilder builder = new ProcessBuilder(args);
        Process process = builder.start();
        process.waitFor();
        if(process.exitValue() != 0) {
            try(InputStream err = process.getErrorStream()) {
                throw new IOException(ANSI_RED + "Non-zero exit code " + process.exitValue() + ": " + ANSI_RESET + new String(err.readAllBytes()));
            }
        }
    }

    @Override
    public void exec(String arg, OSConsumer input, ISConsumer output)
            throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c",
                buildFile(arg).getAbsolutePath());
        Process process = processBuilder.start();
        try(OutputStream out = process.getOutputStream()) {
            input.accept(out);
            out.flush();
            process.waitFor();
        }
        try(InputStream in = process.getInputStream()) {
            output.accept(in);
        }
    }

    @Override
    public File sourceFile(String arg) {
        return new File(arg + extension());
    }

    @Override
    public File buildFile(String arg) {
        return new File(arg + ".exe");
    }

    @Override
    public String extension() {
        return ".cpp";
    }
}
