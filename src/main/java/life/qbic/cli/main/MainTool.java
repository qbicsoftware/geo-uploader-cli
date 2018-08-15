package life.qbic.cli.main;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import life.qbic.cli.QBiCTool;
import life.qbic.cli.helper.GEOExcelCreater;
import life.qbic.cli.helper.GEOOpenBisParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of GEO Upload command-line Tool. Its command-line arguments are contained in
 * instances of {@link MainCommand}.
 */
public class MainTool extends QBiCTool<MainCommand> {

  private static final Logger LOG = LogManager.getLogger(MainTool.class);

  /**
   * Constructor.
   *
   * @param command an object that represents the parsed command-line arguments.
   */
  public MainTool(final MainCommand command) {
    super(command);
  }

  @Override
  public void execute() {
    // get the parsed command-line arguments
    final MainCommand command = super.getCommand();

    java.io.Console console = System.console();
    String password = new String(console.readPassword("Password: "));
    if (password.isEmpty()) {
      System.out.println("You need to provide a password.");
      System.exit(1);
    }

    // Reference the DSS
    IDataStoreServerApi dss =
        HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
            "https://qbis.qbic.uni-tuebingen.de:444/datastore_server"
                + IDataStoreServerApi.SERVICE_URL, 10000);

    // get a reference to AS API
    IApplicationServerApi app = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class,
        "https://qbis.qbic.uni-tuebingen.de/openbis/openbis" + IApplicationServerApi.SERVICE_URL,
        10000);

    // login to obtain a session token
    String sessionToken = app.login(command.userName, password);

    System.out.println("Connection to openbis established succesfully.");

    System.out.format("Provide project identifier:  ");

    if (command.project.isEmpty() || command.project == null) {
      System.out.println("You need to provide a project.");
      System.exit(1);
    }
    String[] split = command.project.split("/");
    command.project = split[split.length - 1];
    System.out.println(command.project);
    // Parse space
    GEOOpenBisParser geoParser = new GEOOpenBisParser(command.project, command.userName,
        sessionToken,
        app, dss);
    HashMap<String, List> geo = geoParser.parseSingle();

    // logout to release the resources related with the session
    app.logout(sessionToken);

    // Create excel from template
    try {
      GEOExcelCreater xls = new GEOExcelCreater(geo.get("sample"), geo.get("raw"), command.output,
          command.project);
      System.out.println("Creating Excel file finished successfully.");
    } catch (IOException e) {
      LOG.error("Could not create excel file.");
      e.printStackTrace();
    }


  }

  // TODO: override the shutdown() method if you are implementing a daemon and want to take advantage of a shutdown hook for clean-up tasks
}