package me.bramar.cphelper;

import me.bramar.cphelper.lang.Cpp;
import me.bramar.cphelper.lang.Java;
import me.bramar.cphelper.lang.Lang;
import me.bramar.cphelper.site.Atcoder;
import me.bramar.cphelper.site.Codeforces;
import me.bramar.cphelper.site.ContestSite;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CompetitiveProgrammingHelperProgram {
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

    public static final String CONFIG_LANG = "lang";
    public static final String CONFIG_SITE = "site";
    public static final String CONFIG_ATCODER_LIB_PATH = "atcoder-lib-path";
    private final Map<String, Lang> LANG_MAP = Map.of(
            "cpp", new Cpp(this),
            "java", new Java(this)
    );
    private final Map<String, ContestSite> SITE_MAP = Map.of(
            "codeforces", new Codeforces(this),
            "atcoder", new Atcoder(this)
    );

    private final Map<String, String> DEFAULT_CONFIG = Map.of(
            CONFIG_LANG, "cpp",
            CONFIG_SITE, "codeforces",
            CONFIG_ATCODER_LIB_PATH, ""
    );

    private final Map<String, Set<String>> ALLOWED_CONFIG = Map.of(
            CONFIG_LANG, LANG_MAP.keySet(),
            CONFIG_SITE, SITE_MAP.keySet()
    );

    private static final String CONFIG_FILE = ".cp-config";
    private static final String SAMPLES_IN_EXT = ".in";
    private static final String SAMPLES_OUT_EXT = ".out";
    private static final String TEST_OUT_EXT = ".tout";

    private final String[] args;
    private Lang lang = LANG_MAP.get(DEFAULT_CONFIG.get(CONFIG_LANG));
    private ContestSite site = SITE_MAP.get(DEFAULT_CONFIG.get(CONFIG_SITE));
    private Map<String, String> config = new HashMap<>();
    private CompetitiveProgrammingHelperProgram(String[] args) throws IOException, URISyntaxException {
        File config = new File(jarDirectory(), CONFIG_FILE);
        if(config.exists()) {
           Properties props = new Properties();
           try(FileInputStream in = new FileInputStream(config)) {
               props.load(in);
           }
           loadProperties(props);
        }
        List<String> newArgs = new ArrayList<>();
        for(String arg : args) {
            String[] split = arg.split("=");
            if(split.length == 2) {
                if(split[0].matches("^-{1,2}l(ang(uage)?)?$")) {
                    if(!LANG_MAP.containsKey(split[1])) {
                        System.err.println(ANSI_RED + "Unknown lang argument: " + split[1] + ANSI_RESET);
                        System.err.println(ANSI_RED + "Available: " + LANG_MAP.keySet() + ANSI_RESET);
                        throw new IllegalArgumentException();
                    }
                    lang = LANG_MAP.get(split[1]);
                    continue;
                }else if(split[0].matches("^-{1,2}(c(ontest|p)-?)?s(ite)?$")) {
                    if(!SITE_MAP.containsKey(split[1])) {
                        System.err.println(ANSI_RED + "Unknown site argument: " + split[1] + ANSI_RESET);
                        System.err.println(ANSI_RED + "Available: " + SITE_MAP.keySet() + ANSI_RESET);
                        throw new IllegalArgumentException();
                    }
                    site = SITE_MAP.get(split[1]);
                    continue;
                }
            }
            newArgs.add(arg);
        }
        this.args = newArgs.toArray(new String[0]);
    }
    private void loadProperties(Properties props) {
        if(props.containsKey(CONFIG_LANG)) {
            String ll = String.valueOf(props.get(CONFIG_LANG));
            if(!LANG_MAP.containsKey(ll)) {
                System.err.println(ANSI_RED + "Unknown lang property in .config (of jardirectory): " + ll + ANSI_RESET);
                System.err.println(ANSI_RED + "Available: " + LANG_MAP.keySet() + ANSI_RESET);
                throw new IllegalArgumentException();
            }
            lang = LANG_MAP.get(ll);
        }
        if(props.containsKey(CONFIG_SITE)) {
            String ss = String.valueOf(props.get(CONFIG_SITE));
            if(!SITE_MAP.containsKey(ss)) {
                System.err.println(ANSI_RED + "Unknown site property in .config (of jardirectory): " + ss + ANSI_RESET);
                System.err.println(ANSI_RED + "Available: " + SITE_MAP.keySet() + ANSI_RESET);
                throw new IllegalArgumentException();
            }
            site = SITE_MAP.get(ss);
        }
        props.forEach((k, v) -> {
            if(DEFAULT_CONFIG.containsKey(String.valueOf(k))) {
                CompetitiveProgrammingHelperProgram.this.config
                        .put(String.valueOf(k), String.valueOf(v));
            }
        });
    }

    private String help() {
        return """
            {C}%c-Helper
            {Y}==== Help ===={W}
            %s d/download <contest-id>
            %s g/gen
            %s p/problem <letters/indices...>
            %s s/sample <problem> [start-idx]
            %s t/test [-s] <problem>
            %s v/diff <problem> <sample>
            %s template <file>
            %s c/config
            {Y}==== Help ====
            {R}"""
                .replace("%s", site.shortName())
                .replace("%c", site.longName().substring(0,1).toUpperCase()+site.longName().substring(1))
                .replace("{C}", ANSI_CYAN)
                .replace("{Y}", ANSI_YELLOW)
                .replace("{W}", ANSI_WHITE)
                .replace("{R}", ANSI_RESET);
    }
    private File problemFile() {
        return new File("." + site.shortName() + "-problems");
    }
    private File jarDirectory() throws URISyntaxException {
        return new File(CompetitiveProgrammingHelperProgram.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getParentFile();
    }
    private List<String> getContestIndices(String contestId) throws IOException {
        if(problemFile().exists()) {
            try(FileInputStream in = new FileInputStream(problemFile())) {
                return new ArrayList<>(Arrays.asList(new String(in.readAllBytes()).split(" ")));
            }
        }
        List<String> indices = site.contestIndices(contestId);
        try(FileOutputStream out = new FileOutputStream(problemFile())) {
            out.write(String.join(" ", indices).getBytes());
        }
        return indices;
    }
//    private String incr(String s) {
//        StringBuilder b = new StringBuilder(s);
//        for(int i = s.length(); i --> 0;) {
//            if(b.charAt(i) == 'Z') {
//                b.setCharAt(i, 'A');
//            }else {
//                b.setCharAt(i, (char) (b.charAt(i) + 1));
//                return b.toString();
//            }
//        }
//        // all Z
//        return b.append('A').toString();
//    }
//    private List<String> indicesFromMaxRange(String s) {
//        List<String> res = new ArrayList<>();
//        String curr = "A";
//        for(int i = 0; !curr.equalsIgnoreCase(s); i++) {
//            if(i >= 1e6) throw new StackOverflowError("stack overflow error 1e9 max_range = " + s);
//            res.add(curr);
//            curr = incr(curr);
//        }
//        res.add(curr);
//        return res;
//    }
    public void setProblemIndices(String[] s) throws IOException {
        try(FileOutputStream out = new FileOutputStream(problemFile())) {
            out.write(String.join(" ", s).getBytes());
        }
        System.out.println(ANSI_YELLOW + "Set problem indices to: " + ANSI_CYAN + String.join(" ", s) + ANSI_RESET);
    }

    public void download(String contestId) throws IOException {
        List<String> indices = getContestIndices(contestId);
        List<String> success = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        System.out.println(ANSI_YELLOW + "Downloading samples..." + ANSI_RESET);
        for(String idx : indices) {
            AtomicInteger x = new AtomicInteger(1);
            AtomicBoolean WOOOO = new AtomicBoolean(false);
            site.downloadSamples(contestId, idx, (in, out) -> {
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
        File templateFile = new File(jarDirectory(), "template" + lang.extension());
        if(!templateFile.exists()) {
            throw new IllegalArgumentException(ANSI_RED + "No template" + lang.extension() + "  found in JAR directory. " + templateFile.getAbsolutePath() + ANSI_RESET);
        }
        byte[] bytes;
        try(FileInputStream in = new FileInputStream(templateFile)) {
            bytes = in.readAllBytes();
        }
        List<String> indices = getContestIndices(null);
        for(String idx : indices) {
            try(FileOutputStream out = new FileOutputStream(idx + lang.extension())) {
                out.write(bytes);
            }
        }

        System.out.println(ANSI_YELLOW + "Generated " + lang.extension() + " files for " + ANSI_CYAN + String.join(" ", indices) + ANSI_RESET);
    }
    public void setTemplate(String f) throws IOException, URISyntaxException {
        File file = new File(f);
        if(!file.exists()) throw new FileNotFoundException();
        String filename = file.getName();
        if(filename.split("\\.").length == 1 || filename.split("\\.")[1].isEmpty()) {
            System.out.println(ANSI_RED + "Failed to detect file programming language by extension!" + ANSI_RESET);
            return;
        }
        String extension = file.getName().substring(file.getName().lastIndexOf('.')+1);
        if(!LANG_MAP.containsKey(extension)) {
            System.out.println(ANSI_RED + "Unsupported language with ext " + extension + "!" + ANSI_RESET);
            return;
        }
        String ext = LANG_MAP.get(extension).extension();
        File templateFile = new File(jarDirectory(), "template" + ext);
        try(FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(templateFile)) {
            byte[] buffer = new byte[2048];
            int len;
            while((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }

        System.out.println(ANSI_YELLOW + "Successfully set " + ext + " template" + ANSI_RESET);
    }
    public void test(String arg, boolean build) throws IOException, InterruptedException {
        if(build) {
            System.out.println(ANSI_YELLOW + "Building..." + ANSI_RESET);
            lang.build(arg);
        }
        File exeFile = lang.buildFile(arg);
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
            File testOut = new File(
                    sample.getName().substring(0, sample.getName().lastIndexOf('.'))
                            + TEST_OUT_EXT
            );
            File sampleOut = new File(
                    sample.getName().substring(0, sample.getName().lastIndexOf('.'))
                            + SAMPLES_OUT_EXT
            );
            boolean[] passes = {true};
            lang.exec(arg, (o) -> {
                try(FileInputStream in = new FileInputStream(sample)) {
                    o.write(in.readAllBytes());
                    o.write('\n');
                }
            }, (in) -> {
                try(FileInputStream correct = new FileInputStream(sampleOut);
                FileOutputStream out = new FileOutputStream(testOut)) {
                    String correctBytes = new String(correct.readAllBytes()).replace("\r",""),
                            outBytes = new String(in.readAllBytes()).replace("\r","");

                    out.write(outBytes.getBytes()); out.flush();
                    if(outBytes.length() < correctBytes.length()) {
                        passes[0] = false;
                        return;
                    }
                    for(int i = correctBytes.length(); i --> 0; ) {
                        if(outBytes.charAt(i) != correctBytes.charAt(i)) {
                            passes[0] = false;
                            return;
                        }
                    }
                }
            });

            // Check
            System.out.print("\r" + ANSI_YELLOW + "Test " + sampleNum + "... " +
                    (passes[0] ? ANSI_GREEN + "passed" : ANSI_RED + "failed") + ANSI_RESET + "\n");
            if(passes[0]) passed++;
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
    public void manualSample(String problem, int start) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Sample count: ");
        int sampleCount = Integer.parseInt(scanner.nextLine());
        for(int i = 0; i < sampleCount; i++) {
            File in = new File(problem + "-" + (start+i) + SAMPLES_IN_EXT);
            File out = new File(problem + "-" + (start+i) + SAMPLES_OUT_EXT);
            try(FileOutputStream inStream = new FileOutputStream(in)) {
                System.out.println(ANSI_CYAN + "==== TYPE INPUT [Sample %s] ==== :d to continue".formatted(start+i) + ANSI_RESET);
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
                System.out.println(ANSI_CYAN + "==== TYPE OUTPUT [Sample %s] ==== :d to continue".formatted(start+i) + ANSI_RESET);
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
        scanner.close();
    }
    public void openconfig() throws IOException, URISyntaxException {
        Properties props = new Properties();
        File configFile = new File(jarDirectory(), CONFIG_FILE);
        if(!configFile.exists()) {
            try(FileOutputStream out = new FileOutputStream(configFile)) {
                for(Map.Entry<String,String> def : DEFAULT_CONFIG.entrySet()) {
                    out.write(def.getKey().getBytes());
                    out.write('=');
                    out.write(def.getValue().getBytes());
                    out.write('\n');
                }
                out.flush();
            }
        }
        Scanner scanner = new Scanner(System.in);
        try(RandomAccessFile raf = new RandomAccessFile(configFile, "rw")) {
            FileChannel channel = raf.getChannel();
            raf.seek(0);
            byte[] full = new byte[(int) raf.length()];
            raf.read(full);
            try(ByteArrayInputStream i = new ByteArrayInputStream(full)) {
                props.load(i);
            }
            for(Map.Entry<String,String> entry : DEFAULT_CONFIG.entrySet()) {
                if(!props.containsKey(entry.getKey())) {
                    props.setProperty(entry.getKey(), entry.getValue());
                }
            }
            String message = null;
            while(true) {
                System.out.println(ANSI_YELLOW + "======= CP-Helper Config =======" + ANSI_RESET);
                for(String key : DEFAULT_CONFIG.keySet()) {
                    System.out.println(ANSI_CYAN + key + ANSI_YELLOW + " = " + ANSI_CYAN + props.get(key) + ANSI_RESET);
                }
                System.out.println(ANSI_YELLOW + "================================" + ANSI_RESET);
                System.out.println(ANSI_BLUE + "(key)=(value) -> change config" + ANSI_RESET);
                System.out.println(ANSI_BLUE + ":q -> exit" + ANSI_RESET);
                if(message != null) {
                    System.out.println(message);
                }
                System.out.println(ANSI_YELLOW + "================================" + ANSI_RESET);
                message = null;
                String line = scanner.nextLine();
                if(line.equalsIgnoreCase(":q")) break;
                if(line.isBlank()) continue;
                String[] split = line.split("=");
                if(split.length != 2) {
                    message = ANSI_RED + "Invalid format!" + ANSI_RESET;
                    continue;
                }
                String key = split[0].trim(),
                        val = split[1].trim();
                if(!DEFAULT_CONFIG.containsKey(key)) {
                    message = ANSI_RED + "Unknown property: '" + key + "'" + ANSI_RESET;
                    continue;
                }
                if(ALLOWED_CONFIG.containsKey(key) && !ALLOWED_CONFIG.get(key).contains(val)) {
                    message = ANSI_RED + "Invalid value: '" + val + "'\nAvailable: " + ALLOWED_CONFIG.get(key) + ANSI_RESET;
                    continue;
                }
                props.setProperty(key, val);
                message = ANSI_GREEN + "Set property '" + key + "' -> '" + val + "'" + ANSI_RESET;
                channel.truncate(0);
                raf.seek(0);
                try(ByteArrayOutputStream o = new ByteArrayOutputStream()) {
                    props.store(o, null);
                    raf.write(o.toByteArray());
                }
            }
        }
        scanner.close();
        loadProperties(props); // the program actually just exists after this so no load is needed
    }
    public void run() throws Exception {
        if(args.length == 0) {
            System.out.println(help());
            return;
        }
        switch(args[0].toLowerCase()) {
            case "p":
            case "problem":
                if(args.length < 2) throw new IllegalArgumentException("Usage: %s p/problem <letters/indices...>".formatted(site.shortName()));
                setProblemIndices(Arrays.copyOfRange(args, 1, args.length));
                break;
            case "d":
            case "download":
                if(args.length < 2) throw new IllegalArgumentException("Usage: %s d/download <contest-id>".formatted(site.shortName()));
                download(args[1]);
                break;
            case "s":
            case "sample":
                if(args.length < 2) throw new IllegalArgumentException("Usage: %s s/sample <problem> [start-idx]".formatted(site.shortName()));
                int start = 1;
                if(args.length >= 3)
                    start = Integer.parseInt(args[2]);
                manualSample(args[1], start);
                break;
            case "g":
            case "gen":
                gen();
                break;
            case "template":
                if(args.length < 2) throw new IllegalArgumentException("Usage: %s template <file>".formatted(site.shortName()));
                setTemplate(args[1]);
                break;
            case "t":
            case "test":
                if(args.length < 2) throw new IllegalArgumentException("Usage: %s t/test [-s] <problem>".formatted(site.shortName()));
                if(args[1].equalsIgnoreCase("-s")) {
                    if(args.length < 3) throw new IllegalArgumentException("Usage: %s t/test [-s] <problem>".formatted(site.shortName()));
                    test(args[2], false);
                }else {
                    //noinspection SimplifiableConditionalExpression
                    test(args[1], (args.length >= 3 && args[2].equalsIgnoreCase("-s")) ? false : true);
                }
                break;
            case "v":
            case "diff":
                if(args.length < 3) throw new IllegalArgumentException("Usage: %s v/diff <problem> <sample>".formatted(site.shortName()));
                showdiff(args[1], args[2]);
                break;
            case "c":
            case "config":
                openconfig();
                break;
        }
    }

    public Lang getLang() {
        return lang;
    }

    public ContestSite getSite() {
        return site;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public static void main(String[] args) {
        try {
            CompetitiveProgrammingHelperProgram main = new CompetitiveProgrammingHelperProgram(args);
            main.run();
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
