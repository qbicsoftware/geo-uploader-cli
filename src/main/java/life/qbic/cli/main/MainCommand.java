package life.qbic.cli.main;

import life.qbic.cli.AbstractCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Abstraction of command-line arguments that will be passed to {@link MainTool} at construction
 * time.
 */
@Command(
        name = "GEO-Uploader",
        header = "%n@|green Uploads RAW Data to GEO directly from openBis|@")
public class MainCommand extends AbstractCommand {
    // TODO: add your command-line options as members of this class using picocli's annotations, for instance:
    //
    // @Option(names={"-u", "--url"}, description="openBIS server URL.", required=true)
    // String url;
    //
    // using package access level for these members will allow you access them within your main and test classes
    //
    // IMPORTANT: Typically you won't require a fancy constructor, but if you do, you must know that
    //            ToolExecutor requires that all command classes contain a public constructor that takes no arguments.
    //
    //            If you need a custom constructor, make sure to provide a no-arguments public constructor as well.
    //            See: https://docs.oracle.com/javase/tutorial/java/javaOO/constructors.html

    @Option(names = {"-p", "--project"}, required = true, description = "The project name.")
    private String project;
    @Option(names = {"-o", "--out"}, required = true, description = "The directory where the output files are created. New folders" +
            "are created automatically.")
    private String output;
    @Option(names = {"-c", "--config"}, required = true, description = "Path to a file containing an openBis parsing config." +
            "See the documentation for more info: https://github.com/qbicsoftware/geo-uploader-cli")
    private String configPath;
    @Option(names = {"-md5", "--md5checksum"}, description = "Path to a file containing the sample identifiers for downloading the samples." +
            "Provide this when you want to download the raw data for md5 checksum calculation.")
    private String identifierPath;


    public MainCommand() {

    }

    public String getIdentifierPath() {
        return identifierPath;
    }

    String getConfigPath() {
        return configPath;
    }

    public String getOutput() {
        return output;
    }

    String getProject() {
        return project;
    }
}