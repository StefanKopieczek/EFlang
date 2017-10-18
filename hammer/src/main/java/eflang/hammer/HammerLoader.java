package eflang.hammer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HammerLoader {

    /**
     * Loads the given directory as a HammerSuite by looking
     * inside for files names *.test and loading them as tests.
     * @param folder - Directory to load
     * @return the HammerSuite loaded from the given directory
     * @throws IOException
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

    /**
     * Loads a HammerTest from a file.
     * @param file
     * @return
     * @throws IOException
     */
    public static HammerTest loadTest(File file) throws IOException {
        if (!file.isFile()) {
            HammerLog.error("ERROR: Not a file.");
            return null;
        }

        HammerTest test;

        String name = null;
        TestType type = TestType.EF;
        String code = null;
        String sourceFile = null;
        ArrayList<String> IOs = new ArrayList<String>();

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
            sourceFile = file.getParent() + file.separator + sourceFile;
            HammerLog.debug("Loading source file: " + sourceFile);
            try {
                fr = new FileReader(sourceFile);
                br = new BufferedReader(fr);
                code = "";
                for (String line = br.readLine();
                        line != null;
                        line = br.readLine()) {
                    code += line + "\n";
                }
                br.close();
            }
            catch (FileNotFoundException e) {
                cantFindSource = true;
                code = "";
            }
        }
        else {
            code = builder.toString();
        }

        HammerLog.log("Test code: " + code, HammerLog.LogLevel.DEV);

        switch (type) {
        case EAR:
            test = new EarTest(name, code);
            break;
        case LOBE:
            test = new LobeTest(name, code);
            break;
        default:
            test = new HammerTest(name, code);
        }

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

    private static enum TestType {
        EF,
        EAR,
        LOBE
    }
}
