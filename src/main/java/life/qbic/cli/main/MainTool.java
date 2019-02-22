package life.qbic.cli.main;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import life.qbic.cli.connection.CredentialHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import life.qbic.QbicDataLoader;
import life.qbic.cli.model.geo.Config;
import life.qbic.cli.QBiCTool;
import life.qbic.cli.helper.GEOExcelCreater;
import life.qbic.cli.helper.GEOOpenBisParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * Implementation of GEO Upload command-line Tool. Its command-line arguments are contained in
 * instances of {@link MainCommand}.
 */
public class MainTool extends QBiCTool<MainCommand> {



    private ArrayList<String> parsingConfig;
    private GEOOpenBisParser geoParser;
    private Config config;

    public Boolean checkIfFileInFolder(String path, String identifier) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int j = 0; j < listOfFiles.length; j++) {
            if (listOfFiles[j].getName().contains(identifier)) {
                System.out.println("File found for identifier " + identifier + " checking next identifier.");
                return true;
            }


        }
        System.out.println("No file found for identifier " + identifier + " beginning download.");
        return false;
    }

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



        config = parseConfig(command.configPath);

        IDataStoreServerApi dss = null;
        IApplicationServerApi app = null;

        //Use postman to download sample files if -m parameter is given

        if (command.md5 != null) {
            System.out.println("Downloading sample files ");

            QbicDataLoader loader = new QbicDataLoader("https://qbis.qbic.uni-tuebingen.de/openbis/openbis", "https://qbis.qbic.uni-tuebingen.de:444/datastore_server",
                    command.userName, password, 4 * 1024, "");
            int returnCode = loader.login();
            LOG.info(String.format("OpenBis login returned with %s", returnCode));
            if (returnCode != 0) {
                LOG.error("Connection to openBIS failed.");
                System.exit(1);
            }
            LOG.info("Connection to openBIS was successful.");


            //now get all identifiers using a file containing the identifiers
            ArrayList<String> identifiers;
            identifiers = new ArrayList<>();
            String l;
            try {
                File file = new File("identifiers");
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                while ((l = bufferedReader.readLine()) != null) {
                    identifiers.add(l);
                }
                fileReader.close();
                System.out.println("Contents of file:");
                System.out.println(identifiers.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 1; i < identifiers.size(); i++) {
                List<DataSet> foundDataSets = loader.findAllDatasetsRecursive(identifiers.get(i));
                if (!checkIfFileInFolder("samples/", identifiers.get(i))) {


                    LOG.info(String.format("Number of data sets found: %s", foundDataSets.size()));

                    if (foundDataSets.size() > 0) {
                        LOG.info("Initialize download ...");
                        int datasetDownloadReturnCode = -1;
                        try {
                            datasetDownloadReturnCode = loader.downloadDataset(foundDataSets);
                        } catch (NullPointerException e) {
                            LOG.error("Datasets were found by the application server, but could not be found on the datastore server for "
                                    + identifiers.get(i) + "." + " Try to supply the correct datastore server using a config file!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (datasetDownloadReturnCode != 0) {
                            LOG.error("Error while downloading dataset: " + identifiers.get(i));
                        } else {
                            LOG.info("Download successfully finished.");

                        }

                    } else {
                        LOG.info("Nothing to download.");
                    }
                }
            }
        }



// Connect to openBis

        try {
            // Reference the DSS
            dss =
                    HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
                            config.getDss()
                                    + IDataStoreServerApi.SERVICE_URL, 10000);

            // get a reference to AS API
            app = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class,
                    config.getApp() + IApplicationServerApi.SERVICE_URL,
                    10000);
            LOG.info("openBis connection established");
        } catch (Exception e) {
            LOG.error("Could not connect to openBis");
            System.exit(1);
        }

//Get the password for openBis login. If not provided in the config.yaml the user can enter it in the command line
        if(config.getPassword() == null) {
        try {
            java.io.Console console = System.console();
            password = new String(console.readPassword("Password: "));
            if (password.isEmpty()) {
                System.out.println("You need to provide a password");
                LOG.error("No password provided");
                System.exit(1);
            }
        } catch (NullPointerException e) {

            CredentialHandler ch = new CredentialHandler("credentialProperties");
            password = ch.getPw();
        }}

        else{
            password = config.getPassword();
        }

        // login to obtain a session token
        String sessionToken = "";
        try {
            sessionToken = app.login(command.userName, password);
            LOG.info("Logged in successfully to openBis");
        } catch (Exception e) {
            LOG.error("Could not log in to openBis. Please check your username and password");
        }

        //If a openBis parsing config is given then the keywords in it will be used for parsing the openBis data

        geoParser = new GEOOpenBisParser(command.project,command.userName,sessionToken,app,dss);




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


    public Config parseConfig(String configpath) {
        Config config = new Config();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            config = mapper.readValue(new File(configpath), Config.class);




        } catch (Exception e) {

            // TODO Auto-generated catch block

           e.printStackTrace();

        }
    return config;}
    // TODO: override the shutdown() method if you are implementing a daemon and want to take advantage of a shutdown hook for clean-up tasks


}