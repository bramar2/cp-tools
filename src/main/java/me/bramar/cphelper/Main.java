package me.bramar.cphelper;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
//    public static final String ANSI_RESET = "";
//    public static final String ANSI_BLACK = "";
//    public static final String ANSI_RED = "";
//    public static final String ANSI_GREEN = "";
//    public static final String ANSI_YELLOW = "";
//    public static final String ANSI_BLUE = "";
//    public static final String ANSI_PURPLE = "";
//    public static final String ANSI_CYAN = "";
//    public static final String ANSI_WHITE = "";



    private static final String PROGRAM_NAME = "cf";
    private static final ContestSite SITE = new Codeforces();
    private static final String PROBLEM_FILE = "." + PROGRAM_NAME + "-problems";
    private static final String TEMPLATE_CPP = "template.cpp";
    private static final String SAMPLES_IN_EXT = ".in";
    private static final String SAMPLES_OUT_EXT = ".out";
    private static final String TEST_OUT_EXT = ".tout";
    private static final String HELP =
            """
            %s-Helper
            ==== Help ====
            %s d/download <contest-id>
            %s g/gen
            %s p/problem <max-letter>
            %s s/sample <problem>
            %s t/test [-s] <problem>
            %s v/diff <problem> <sample>
            %s template <file>
            ==== Help ====
            """.replace("%s", PROGRAM_NAME);
    private Main() {}
    private File jarDirectory() throws URISyntaxException {
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getParentFile();
    }
    private List<String> getContestIndices(String contestId) throws IOException {
        if(new File(PROBLEM_FILE).exists()) {
            try(FileInputStream in = new FileInputStream(PROBLEM_FILE)) {
                return new ArrayList<>(Arrays.asList(new String(in.readAllBytes()).split(" ")));
            }
        }
        List<String> indices = SITE.contestIndices(contestId);
        try(FileOutputStream out = new FileOutputStream(PROBLEM_FILE)) {
            out.write(String.join(" ", indices).getBytes());
        }
        return indices;
    }
    private String incr(String s) {
        StringBuilder b = new StringBuilder(s);
        for(int i = s.length(); i --> 0;) {
            if(b.charAt(i) == 'Z') {
                b.setCharAt(i, 'A');
            }else {
                b.setCharAt(i, (char) (b.charAt(i) + 1));
                return b.toString();
            }
        }
        // all Z
        return b.append('A').toString();
    }
    private List<String> indicesFromMaxRange(String s) {
        List<String> res = new ArrayList<>();
        String curr = "A";
        for(int i = 0; !curr.equalsIgnoreCase(s); i++) {
            if(i >= 1e6) throw new StackOverflowError("stack overflow error 1e9 max_range = " + s);
            res.add(curr);
            curr = incr(curr);
        }
        return res;
    }
    public void setProblemIndices(String s) throws IOException {
        List<String> indices = indicesFromMaxRange(s);
        try(FileOutputStream out = new FileOutputStream(PROBLEM_FILE)) {
            out.write(String.join(" ", indices).getBytes());
        }
        System.out.println(ANSI_YELLOW + "Set problem indices to: " + ANSI_CYAN + String.join(" ", indices) + ANSI_RESET);
    }

    private void download(String contestId) throws IOException {
        List<String> indices = getContestIndices(contestId);
        List<String> success = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        System.out.println(ANSI_YELLOW + "Downloading samples..." + ANSI_RESET);
        for(String idx : indices) {
            AtomicInteger x = new AtomicInteger(1);
            AtomicBoolean WOOOO = new AtomicBoolean(false);
            SITE.downloadSamples(contestId, idx, (in, out) -> {
                File inFile = new File(idx + "-" + x.get() + SAMPLES_IN_EXT);
                File outFile = new File(idx + "-" + x.get() + SAMPLES_OUT_EXT);
                try(FileOutputStream o = new FileOutputStream(inFile)) {
                    o.write(in.getBytes());
                }
                try(FileOutputStream o = new FileOutputStream(outFile)) {
                    o.write(out.getBytes());
                }
                x.getAndIncrement();
                if(!WOOOO.get()) {
                    success.add(idx);
                    WOOOO.set(true);
                }
            }, (err) -> {
                failed.add(idx);
                System.err.println(err);
            });
        }

        System.out.println("\n"+ANSI_YELLOW+"Finished!");
        System.out.println(ANSI_GREEN + "Success: " + String.join(" ", success));
        if(!failed.isEmpty())
            System.out.println(ANSI_RED + "Failed: " + String.join(" ", failed));
        System.out.println(ANSI_RESET);
    }

    public void gen() throws IOException, URISyntaxException {
        File templateFile = new File(jarDirectory(), TEMPLATE_CPP);
        if(!templateFile.exists()) {
            throw new IllegalArgumentException(ANSI_RED + "No template.cpp found in JAR directory. " + templateFile.getAbsolutePath() + ANSI_RESET);
        }
        byte[] bytes;
        try(FileInputStream in = new FileInputStream(templateFile)) {
            bytes = in.readAllBytes();
        }
        List<String> indices = getContestIndices(null);
        for(String idx : indices) {
            try(FileOutputStream out = new FileOutputStream(idx + ".cpp")) {
                out.write(bytes);
            }
        }
        System.out.println(ANSI_YELLOW + "Generated .cpp files for " + ANSI_CYAN + String.join(" ", indices) + ANSI_RESET);
    }
    public void setTemplate(String f) throws IOException, URISyntaxException {
        File file = new File(f);
        if(!file.exists()) throw new FileNotFoundException();
        File cppTemplate = new File(jarDirectory(), TEMPLATE_CPP);
        try(FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(cppTemplate)) {
            byte[] buffer = new byte[2048];
            int len;
            while((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }

        System.out.println(ANSI_YELLOW + "Successfully set .cpp template" + ANSI_RESET);
    }
    public void test(String arg, boolean build) throws IOException, InterruptedException {
        File cppFile = new File(arg + ".cpp");
        if(build) {
            System.out.println(ANSI_YELLOW + "Building..." + ANSI_RESET);
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "g++.exe",
                    "-O2",
                    cppFile.getAbsolutePath()
            );
            Process process = processBuilder.start();
            process.waitFor();
        }
        File exeFile = new File(arg + ".exe");
        if(!exeFile.exists()) {
            System.out.println(ANSI_RED + "exe file not found! (build failed?) " + ANSI_RESET + exeFile.getAbsolutePath());
            return;
        }
        File dir = exeFile.getAbsoluteFile().getParentFile();
        File[] files = dir.listFiles();
        if(files == null) {
            System.out.println(ANSI_RED + "dir.listfiles() is null" + ANSI_RESET);
            return;
        }
        List<File> samples = new ArrayList<>();
        for(File sampleFile : files) {
            if(sampleFile.getName().endsWith(SAMPLES_IN_EXT) &&
            sampleFile.getName().split("-")[0].equalsIgnoreCase(arg)) {
                samples.add(sampleFile);
            }
        }
        samples.sort(Comparator.comparing(f -> {
            return Long.parseLong(f.getName().split("-")[1].split("\\.")[0]);
        }));
        int passed = 0;
        for(File sample : samples) {
            long sampleNum = Long.parseLong(sample.getName().split("-")[1].split("\\.")[0]);
            System.out.print("\r" + ANSI_YELLOW + "Test " + sampleNum + " ..." + ANSI_RESET);
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", exeFile.getAbsolutePath());
            Process process = processBuilder.start();
            try(OutputStream out = process.getOutputStream();
                FileInputStream in = new FileInputStream(sample)) {
                out.write(in.readAllBytes());
                out.write('\n');
                out.flush();
                process.waitFor();
            }
            File testOut = new File(
                    sample.getName().substring(0, sample.getName().lastIndexOf('.'))
                            + TEST_OUT_EXT
            );
            File sampleOut = new File(
                    sample.getName().substring(0, sample.getName().lastIndexOf('.'))
                            + SAMPLES_OUT_EXT
            );
            boolean p = true;
            try(InputStream in = process.getInputStream();
                FileInputStream correct = new FileInputStream(sampleOut);
                FileOutputStream out = new FileOutputStream(testOut)) {
                String correctBytes = new String(correct.readAllBytes()).replace("\r",""),
                        outBytes = new String(in.readAllBytes()).replace("\r","");

                out.write(outBytes.getBytes());
                if(outBytes.length() < correctBytes.length()) {
                    p = false;
                    break;
                }
                for(int i = correctBytes.length(); i --> 0; ) {
                    if(outBytes.charAt(i) != correctBytes.charAt(i)) {
                        p = false;
                        break;
                    }
                }
            }
            // Check
            System.out.print("\r" + ANSI_YELLOW + "Test " + sampleNum + "... " +
                    (p ? ANSI_GREEN + "passed" : ANSI_RED + "failed") + ANSI_RESET + "\n");
            if(p) passed++;
        }
        System.out.println();
        if(passed == samples.size()) System.out.print(ANSI_GREEN);
        else System.out.print(ANSI_RED);
        System.out.println("===============");
        if(passed == samples.size()) {
            System.out.println("All tests passed.");
        }else {
            System.out.println(passed + "/" + samples.size() + " tests passed.");
        }
        System.out.println("===============" + ANSI_RESET);
    }
    public void showdiff(String problemS, String sampleS) throws IOException, InterruptedException {
        File sampleOut = new File(problemS + "-" + sampleS + SAMPLES_OUT_EXT);
        File testOut = new File(problemS + "-" + sampleS + TEST_OUT_EXT);

        System.out.println("Test | Expected");
        ProcessBuilder processBuilder = new ProcessBuilder("diff",
                "-y",
                testOut.getAbsolutePath(),
                sampleOut.getAbsolutePath());
        processBuilder.inheritIO().start().waitFor();
    }
    public void manualSample(String problem) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Sample count: ");
        int sampleCount = Integer.parseInt(scanner.nextLine());
        for(int i = 0; i < sampleCount; i++) {
            File in = new File(problem + "-" + (i+1) + SAMPLES_IN_EXT);
            File out = new File(problem + "-" + (i+1) + SAMPLES_OUT_EXT);
            try(FileOutputStream inStream = new FileOutputStream(in)) {
                System.out.println(ANSI_CYAN + "==== TYPE INPUT [Sample %s] ==== :d to continue".formatted(i+1) + ANSI_RESET);
                boolean first = true;
                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if(line.equalsIgnoreCase(":d")) break;
                    if(first) {
                        first = false;
                    }else {
                        inStream.write('\n');
                    }
                    inStream.write(line.getBytes());
                }
                System.out.println(ANSI_CYAN + "==== END INPUT ====" + ANSI_RESET);
            }
            try(FileOutputStream outStream = new FileOutputStream(out)) {
                System.out.println(ANSI_CYAN + "==== TYPE OUTPUT [Sample %s] ==== :d to continue".formatted(i+1) + ANSI_RESET);
                boolean first = true;
                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if(line.equalsIgnoreCase(":d")) break;
                    if(first) {
                        first = false;
                    }else {
                        outStream.write('\n');
                    }
                    outStream.write(line.getBytes());
                }
                System.out.println(ANSI_CYAN + "==== END OUTPUT ====" + ANSI_RESET);
            }
        }
        System.out.println(ANSI_YELLOW + "Finished with " + sampleCount + " samples." + ANSI_RESET);
    }
    public void run(String[] args) throws Exception {
        if(args.length == 0) {
            System.out.println(HELP);
            return;
        }
        switch(args[0].toLowerCase()) {
            case "p":
            case "problem":
                if(args.length < 2) throw new IllegalArgumentException("Usage: %s p/problem <max-letter>".formatted(PROGRAM_NAME));
                setProblemIndices(args[1]);
                break;
            case "d":
            case "download":
                if(args.length < 2) throw new IllegalArgumentException("Usage: %s d/download <contest-id>".formatted(PROGRAM_NAME));
                download(args[1]);
                break;
            case "s":
            case "sample":
                if(args.length < 2) throw new IllegalArgumentException("Usage: %s s/sample <problem>".formatted(PROGRAM_NAME));
                manualSample(args[1]);
                break;
            case "g":
            case "gen":
                gen();
                break;
            case "template":
                if(args.length < 2) throw new IllegalArgumentException("Usage: %s template <file>".formatted(PROGRAM_NAME));
                setTemplate(args[1]);
                break;
            case "t":
            case "test":
                if(args.length < 2) throw new IllegalArgumentException("Usage: %s t/test [-s] <problem>".formatted(PROGRAM_NAME));
                if(args[1].equalsIgnoreCase("-s")) {
                    if(args.length < 3) throw new IllegalArgumentException("Usage: %s t/test [-s] <problem>".formatted(PROGRAM_NAME));
                    test(args[2], false);
                }else test(args[1], true);
                break;
            case "v":
            case "diff":
                if(args.length < 3) throw new IllegalArgumentException("Usage: %s v/diff <problem> <sample>".formatted(PROGRAM_NAME));
                showdiff(args[1], args[2]);
                break;
        }
    }


    public static void main(String[] args) {
        try {
            new Main().run(args);
        }catch(IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }catch(IOException e) {
            System.err.println("IO Error: ");
            e.printStackTrace(System.err);
        }catch(Exception e) {
            System.err.println(e.getClass());
            e.printStackTrace(System.err);
        }
    }
}
