package commandline;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(name = "GEO-Uploader", header = "%n@|green Uploads RAW Data to GEO directly from openBis|@")
public class CommandLineParameters {

  @Option(names = {"-u", "--user"}, required = true, description = "The user name.")
  public String userName;
  @Option(names = {"-p", "--project"}, required = true, description = "The project name.")
  public String project;
  @Option(names = {"-o", "--out"}, required = true, description = "The output directory.")
  public String output;
  @Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
  private boolean helpRequested = false;


}
