package me.bramar.cphelper.stresstesting;

import java.io.*;

public class StressTester {
    private static class TestResult {
        byte[] validatorOutput;
        boolean correct;
    }
    private final ProcessBuilder exec, generator, validator;
    private final File testOutputDir;
    private final boolean multianswer, seeded;
    private Thread thread;
    protected StressTester(ProcessBuilder exec, ProcessBuilder generator, ProcessBuilder validator,
                           File testOutputDir, boolean seeded, boolean multianswer) {
        this.exec = exec;
        this.generator = generator;
        this.validator = validator;
        this.testOutputDir = testOutputDir;
        this.multianswer = multianswer;
        this.seeded = seeded;
    }

    private byte[] generateTest(int num) throws IOException, InterruptedException {
        Process p = generator.start();
        if(seeded) {
            try(OutputStream out = p.getOutputStream()) {
                out.write(Integer.toString(num).getBytes());
                out.write('\n');
                out.flush();
            }
        }
        byte[] bytes;
        try(InputStream in = p.getInputStream()) {
            bytes = in.readAllBytes();
        }
//        p.waitFor();
        return bytes;
    }
    private byte[] execute(byte[] input) throws IOException, InterruptedException {
        Process p = exec.start();
        try(OutputStream out = p.getOutputStream()) {
            out.write(input);
            out.write('\n');
            out.flush();
        }
        byte[] bytes;
        try(InputStream in = p.getInputStream()) {
            bytes = in.readAllBytes();
        }
//        p.waitFor();
        return bytes;
    }
    private TestResult validate(byte[] input, byte[] output) throws IOException, InterruptedException {
        Process p = validator.start();
        try(OutputStream out = p.getOutputStream()) {
            out.write(input);
            out.write('\n');
            if(multianswer) {
                out.write(output);
                out.write('\n');
            }
            out.flush();
        }
        TestResult result = new TestResult();
        try(InputStream in = p.getInputStream()) {
            result.validatorOutput = in.readAllBytes();
            if(multianswer) {
                result.correct = new String(result.validatorOutput).contains("OK");
            }else {
                String execOut = new String(output).replaceAll("(\\s){2,}", "$1").trim();
                String validatorOut = new String(result.validatorOutput).replaceAll("(\\s){2,}", "$1").trim();
                result.correct = execOut.equals(validatorOut);
            }
        }
//        p.waitFor();
        return result;
    }

    private void loop() {
        int y = 1;
        try {
            while(!Thread.currentThread().isInterrupted() && y <= 1e9) {
                byte[] test = generateTest(y);
                byte[] codeOutput = execute(test);
                TestResult res = validate(test, codeOutput);
                try(FileOutputStream out = new FileOutputStream(
                        new File(testOutputDir, "ST_%05d.test".formatted(y)))) {
                    out.write("Test #%s:\n".formatted(y).getBytes());
                    out.write(test);
                    out.write("\n\nExecution Output:\n".getBytes());
                    out.write(codeOutput);
                    out.write("\n\nValidator Output: [".getBytes());
                    out.write(res.correct ? "OK".getBytes() : "BAD".getBytes());
                    out.write("]\n".getBytes());
                    out.write(res.validatorOutput);
                }
                if(!res.correct) {
                    System.out.printf("\rWrong Answer! check %05d.test%n", y);
                }

                update(y++);
            }
        }catch(IOException|InterruptedException e) {
            throw new RuntimeException("Error at test " + y, e);
        }
    }

    public void start() {
        // run on new thread
        thread = new Thread(this::loop);
        thread.start();
    }
    public void update(int count) {
        System.out.printf("\rStress testing... %d tests", count);
    }
    public void stop() {
        if(thread == null) return;
        thread.interrupt();
        thread = null;
    }

}
