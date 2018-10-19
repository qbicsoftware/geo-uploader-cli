package life.qbic.cli.main;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import life.qbic.QbicDataLoader;
import life.qbic.cli.QBiCTool;
import life.qbic.cli.helper.GEOExcelCreater;
import life.qbic.cli.helper.GEOOpenBisParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

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


  //read in password from file
    // The name of the file to open.
    String fileName = "temp.txt";

    // This will reference one line at a time
    String line = null;

    try {
      // FileReader reads text files in the default encoding.
      FileReader fileReader =
              new FileReader("pw.txt");

      // Always wrap FileReader in BufferedReader.
      BufferedReader bufferedReader =
              new BufferedReader(fileReader);

      while((line = bufferedReader.readLine()) != null) {
        password = line;
      }

      // Always close files.
      bufferedReader.close();
    }
    catch(FileNotFoundException ex) {
      System.out.println(
              "Unable to open file '" +
                      fileName + "'");
    }
    catch(IOException ex) {
      System.out.println(
              "Error reading file '"
                      + fileName + "'");
      // Or we could just do this:
      // ex.printStackTrace();
    }

    IDataStoreServerApi dss = null;
    IApplicationServerApi app = null;

    //Use postman to download sample files if -m parameter is given

    if (command.md5 != null) {
      System.out.println("Downloading sample files ");

      QbicDataLoader loader = new QbicDataLoader("https://qbis.qbic.uni-tuebingen.de/openbis/openbis","https://qbis.qbic.uni-tuebingen.de:444/datastore_server",
              "zxmvi59",password,1024,"");
        int returnCode = loader.login();
        LOG.info(String.format("OpenBis login returned with %s", returnCode));
        if (returnCode != 0) {
            LOG.error("Connection to openBIS failed.");
            System.exit(1);
        }
        LOG.info("Connection to openBIS was successful.");      loader.findAllDatasetsRecursive("QJWABENTITY-1");

        List<DataSet> foundDataSets = loader.findAllDatasetsRecursive("QJWABENTITY-1");

        LOG.info(String.format("Number of data sets found: %s", foundDataSets.size()));

        if (foundDataSets.size() > 0) {
            LOG.info("Initialize download ...");
            int datasetDownloadReturnCode = -1;
            try {
                datasetDownloadReturnCode = loader.downloadDataset(foundDataSets);
            } catch (NullPointerException e) {
                LOG.error("Datasets were found by the application server, but could not be found on the datastore server for "
                        + "QJWABENTITY-1" + "." + " Try to supply the correct datastore server using a config file!");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (datasetDownloadReturnCode != 0) {
                LOG.error("Error while downloading dataset: " + "QJWABENTITY-1");
            } else {
                LOG.info("Download successfully finished.");
            }

        } else {
            LOG.info("Nothing to download.");
        }

    }


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
/*
commented out while using ide to develop - delete comment when using cmd for starting program
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
*/
    // login to obtain a session token
    String sessionToken = "";
    try {
      sessionToken = app.login(command.userName, password);
      LOG.info("Logged in successfully to openBis");
    } catch (Exception e) {
      LOG.error("Could not log in to openBis. Please check your username and password");
    }

    // Parse space
    System.out.println(command.md5);
    GEOOpenBisParser geoParser = new GEOOpenBisParser(command.project, command.userName,
        sessionToken,
        app, dss);
    HashMap<String, List> geo = geoParser.parseSingle();



    // logout to release the resources related with the session
    app.logout(sessionToken);

    //Download samples with qpostman and calculate md5checksum


    // Create excel from template
    try {
      GEOExcelCreater xls = new GEOExcelCreater(geo.get("sample"), geo.get("raw"), command.output,
          command.project);
      System.out.println("Creating Excel file finished successfully.");
    } catch (IOException e) {
      LOG.error("Could not create excel file.");
      e.printStackTrace();
    } catch (InvalidFormatException e) {
      e.printStackTrace();
    }


  }

  // TODO: override the shutdown() method if you are implementing a daemon and want to take advantage of a shutdown hook for clean-up tasks
}