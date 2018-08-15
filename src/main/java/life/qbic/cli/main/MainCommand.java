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

  @Option(names = {"-u", "--user"}, required = true, description = "The user name.")
  public String userName;
  @Option(names = {"-p", "--project"}, required = true, description = "The project name.")
  public String project;
  @Option(names = {"-o", "--out"}, required = true, description = "The output directory.")
  public String output;
  @Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
  private boolean helpRequested = false;
}