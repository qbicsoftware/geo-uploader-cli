package life.qbic.cli.main;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import life.qbic.cli.QBiCTool;
import life.qbic.cli.connection.CredentialHandler;
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
    LOG.info("Parse commands");

    String password = "";

    IDataStoreServerApi dss = null;
    IApplicationServerApi app = null;

    // Connect to openBis

    try {
      // Reference the DSS
       dss =
          HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
              "https://qbis.qbic.uni-tuebingen.de:444/datastore_server"
                  + IDataStoreServerApi.SERVICE_URL, 10000);

      // get a reference to AS API
       app = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class,
          "https://qbis.qbic.uni-tuebingen.de/openbis/openbis" + IApplicationServerApi.SERVICE_URL,
          10000);
       LOG.info("openBis connection established");
    } catch (Exception e) {
      LOG.error("Could not connect to openBis");
      System.exit(1);
    }

    try {
      java.io.Console console = System.console();
      password = new String(console.readPassword("Password: "));
      if (password.isEmpty()) {
        System.out.println("You need to provide a password");
        LOG.error("No password provided");
        System.exit(1);
      }
    }catch (NullPointerException e) {
      CredentialHandler ch = new CredentialHandler("/Users/spaethju/qbic-ext.properties");
      password = ch.getPw();
    }

    // login to obtain a session token
    String sessionToken = "";
    try {
      sessionToken = app.login(command.userName, password);
      LOG.info("Logged in successfully to openBis");
    } catch (Exception e) {
      LOG.error("Could not log in to openBis. Please check your username and password");
    }

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