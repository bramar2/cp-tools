package me.bramar.cphelper.lang;

import me.bramar.cphelper.CompetitiveProgrammingHelperProgram;
import me.bramar.cphelper.functional.ISConsumer;
import me.bramar.cphelper.functional.OSConsumer;

import java.io.*;
import java.nio.file.Files;

import static me.bramar.cphelper.CompetitiveProgrammingHelperProgram.ANSI_RED;
import static me.bramar.cphelper.CompetitiveProgrammingHelperProgram.ANSI_RESET;

public class Java extends Lang {
    private static final String IMPORTS =
            """
            import java.lang.*;
            import java.io.*;
            import java.math.*;
            import java.lang.reflect.*;
            import java.time.*;
            import java.util.*;
            import java.util.concurrent.*;
            import java.util.concurrent.atomic.*;
            import java.util.regex.*;
            import java.util.function.*;
            """;

    public Java(CompetitiveProgrammingHelperProgram main) {
        super(main);
    }

    private byte[] modify(byte[] bytes, File sourceFile) {
        String className = sourceFile.getName();
        className = className.substring(0, className.lastIndexOf('.'));
        StringBuilder b = new StringBuilder();
        return (IMPORTS + new String(bytes)
                .replaceFirst("public class Main", "public class " + className)
                ).getBytes();
    }
    @Override
    public void build(String arg) throws IOException, InterruptedException {
        byte[] bytes = null;
        File sourceFile = sourceFile(arg), buildFile = buildFile(arg);
        try {
            if(buildFile.exists()) {
                Files.delete(buildFile.toPath());
            }
            try(FileInputStream in = new FileInputStream(sourceFile)) {
                bytes = in.readAllBytes();
            }
            byte[] mainClassBytes = modify(bytes, sourceFile);
            try(FileOutputStream out = new FileOutputStream(sourceFile)) {
                out.write(mainClassBytes);
            }
            ProcessBuilder builder = new ProcessBuilder(
                    "javac.exe", sourceFile.getAbsolutePath()
            );
            Process process = builder.start();
            process.waitFor();
            try(FileOutputStream out = new FileOutputStream(sourceFile)) {
                out.write(bytes);
                bytes = null; // not used anymore
            }
            if(process.exitValue() != 0) {
                try(InputStream err = process.getErrorStream()) {
                    throw new IOException(ANSI_RED + "Non-zero exit code " + process.exitValue() + ": " + ANSI_RESET + new String(err.readAllBytes()));
                }
            }
        }finally {
            if(bytes != null) {
                try(FileOutputStream out = new FileOutputStream(sourceFile)) {
                    out.write(bytes);
                }
            }
        }
    }

    @Override
    public void exec(String arg, OSConsumer input, ISConsumer output)
            throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("java",
                className(arg));
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

    private String className(String arg) {
        String n = sourceFile(arg).getName(); // So it matches case
        return n.substring(0, n.lastIndexOf('.'));
    }

    @Override
    public File sourceFile(String arg) {
        return new File(arg + extension());
    }

    @Override
    public File buildFile(String arg) {
        return new File(arg + ".class");
    }

    @Override
    public String extension() {
        return ".java";
    }
}
