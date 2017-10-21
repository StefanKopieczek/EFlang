package eflang.hammer;

import eflang.core.MusicSource;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HammerLoader {

    /**
     * Loads the given directory as a HammerSuite by looking
     * inside for files names *.test and loading them as tests.
     * @param folder - Directory to load
     * @return the HammerSuite loaded from the given directory
     * @throws IOException upon failure to load the folder or any file within it.
     */
    public static HammerSuite loadSuite(File folder) throws IOException {
        // Check we were given a directory
        if (!folder.isDirectory()) {
            HammerLog.error("ERROR: Path to load must be a directory.");
            return null;
        }

        String suiteName = null;
        String path = folder.getPath();

        // Attempt to get the suite name from suite.info
        // Otherwise just use the name of the folder
        try {
            FileReader infoFile = new FileReader(path + "/suite.info");
            BufferedReader br = new BufferedReader(infoFile);
            suiteName = br.readLine();
            br.close();
        }
        catch (IOException e) {
            HammerLog.debug("suite.info not found");
        }
        finally {
            if (suiteName == null) {
                suiteName = folder.getName();
            }
        }

        HammerSuite suite = new HammerSuite(suiteName);

        HammerLog.info("Loading HAMMER test suite: " + suiteName);

        File[] files = folder.listFiles();

        for (File file : files) {
            // Only try to load .test files
            if (getFileExtension(file).equals("test")) {
                HammerLog.info("Loading test: " + file.getName());
                HammerTest test = loadTest(file);
                suite.addTest(test);
            }
        }
        HammerLog.info("");
        return suite;
    }

    public static HammerSuite loadSuite(String path) throws IOException {
        return loadSuite(new File(path));
    }

    public static List<HammerTest> loadTestsFromDirectory(File testDir) {
        if (!testDir.isDirectory()) {
            throw new RuntimeException("Not a directory");
        }
        File[] testFiles = testDir.listFiles((File dir, String name) -> name.endsWith(".test"));

        if (testFiles == null) {
            throw new RuntimeException("Not a directory, or IO Exception");
        }

        return Arrays.stream(testFiles)
                .map(HammerLoader::mustLoadTest)
                .collect(Collectors.toList());
    }

    public static HammerTest mustLoadTest(File file) {
        try {
            return loadTest(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads a HammerTest from a file.
     * @param file file to load the test from
     * @return the loaded HammerTest
     * @throws IOException upon failure to read the file
     */
    public static HammerTest loadTest(File file) throws IOException {
        if (!file.isFile()) {
            HammerLog.error("ERROR: Not a file.");
            return null;
        }

        HammerTest test;

        String name = null;
        TestType type = TestType.EF;
        String code;
        String sourceFile = null;
        ArrayList<String> IOs = new ArrayList<>();

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        Pattern title = Pattern.compile("\\[(.*)\\]");
        String sectionName = "";
        StringBuilder builder = new StringBuilder();

        boolean cantFindSource = false;

        for (String line = br.readLine();
                line != null;
                line = br.readLine()) {

            Matcher matcher = title.matcher(line);
            if (matcher.find()) {
                // Found section title
                sectionName = matcher.group(1);
                continue;
            }

            switch (sectionName) {
            case "Source":
                sourceFile = line;
                sectionName = "";
                break;

            case "Code":
                builder.append(line);
                builder.append("\n");
                break;

            case "Test":
                IOs.add(line);
                break;

            case "Name":
                name = line;
                HammerLog.debug("Test name: " + name);
                sectionName = "";
                break;

            case "Type":
                switch (line) {
                case "Earfuck":
                    type = TestType.EF;
                    break;
                case "EAR":
                    type = TestType.EAR;
                    break;
                case "LOBE":
                    type = TestType.LOBE;
                    break;
                }
                sectionName = "";
            }
        }

        br.close();

        if (sourceFile != null) {
            HammerLog.debug("Found source file: " + sourceFile);
            sourceFile = file.getParent() + File.separator + sourceFile;
            HammerLog.debug("Loading source file: " + sourceFile);
            try {
                fr = new FileReader(sourceFile);
                br = new BufferedReader(fr);
                builder = new StringBuilder();
                for (String line = br.readLine();
                        line != null;
                        line = br.readLine()) {
                    builder.append(line);
                    builder.append("\n");
                }
                code = builder.toString();
                br.close();
            }
            catch (FileNotFoundException e) {
                cantFindSource = true;
                code = "";
            }
        } else {
            code = builder.toString();
        }

        HammerLog.log("Test code: " + code, HammerLog.LogLevel.DEV);

        Supplier<MusicSource> codeSupplier;
        switch (type) {
            case EAR:
                codeSupplier = new EarCodeSupplier(code);
                break;
            case LOBE:
                codeSupplier = new LobeCodeSupplier(code);
                break;
            case EF:
                codeSupplier = new EFCodeSupplier(code);
                break;
            default:
                throw new RuntimeException("Unknown test type: " + type);
        }

        test = new HammerTest(name, codeSupplier);

        if (cantFindSource) {
            test.setupFailed = true;
            test.failureMessage = "Could not locate source file: " + sourceFile;
        }

        for (String IO : IOs) {
            String[] split = IO.split("\\s+");
            if (split.length >= 2) {
                if (split[0].equals("//")) {
                    // Comment
                    continue;
                }
                int value = Integer.parseInt(split[1]);
                switch (split[0]) {
                case ">":
                    test.addTask(new HammerTest.InputTask(value));
                    break;
                case "<":
                    test.addTask(new HammerTest.OutputTask(value));
                    break;
                }
            }
            else if (split.length >= 1) {
                switch (split[0]) {
                case "=":
                    test.addTask(new HammerTest.RestartTask());
                    break;
                }
            }
        }

        return test;
    }

    public static HammerTest loadTest(String filename) throws IOException {
        return loadTest(new File(filename));
    }

    private static String getFileExtension(File file) {
        String extension = "";
        String name = file.getName();
        String[] split = name.split("\\.");

        if (split.length > 1) {
            extension = split[split.length - 1];
        }

        return extension;
    }

    private enum TestType {
        EF,
        EAR,
        LOBE
    }
}
