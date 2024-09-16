package me.bramar.cphelper.stresstesting;

import java.io.File;
import java.nio.file.Files;
import java.util.Scanner;

public class StressTesterTest {
    public static void main(String[] args) throws Exception {
        File exe = new File("code.exe");
        File gen = new File("gen.exe");
        File val = new File("validate.exe");
        File o = new File("stresstest");
        Files.createDirectories(o.toPath());
        StartedStressTester stressTester = new StartedStressTester(
                new ProcessBuilder(exe.getAbsolutePath()),
                new ProcessBuilder(gen.getAbsolutePath()),
                new ProcessBuilder(val.getAbsolutePath()),
                o,
                true, false
        );
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press [ENTER] to stop.");
        stressTester.start(20);
        scanner.nextLine();
        stressTester.stop();
    }
}
