package life.qbic.cli.main;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import life.qbic.cli.QBiCTool;
import life.qbic.cli.helper.GEOExcelCreater;
import life.qbic.cli.helper.GEOOpenBisParser;
import life.qbic.cli.model.geo.Config;
import life.qbic.core.PostmanFilterOptions;
import life.qbic.core.authentication.PostmanConfig;
import life.qbic.core.authentication.PostmanSessionManager;
import life.qbic.dataLoading.PostmanDataDownloaderV3;
import life.qbic.dataLoading.PostmanDataFilterer;
import life.qbic.dataLoading.PostmanDataFinder;
import life.qbic.exceptions.PostmanOpenBISLoginFailedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implementation of GEO Upload command-line Tool. Its command-line arguments are contained in
 * instances of {@link MainCommand}.
 * This class starts the main functionality of the CLI, instantiates the most important objects and calls most methods
 */
public class MainTool extends QBiCTool<MainCommand> {


    private static final Logger LOG = LogManager.getLogger(MainTool.class);
    private GEOOpenBisParser geoParser;
    private Config config;

    /**
     * Constructor.
     *
     * @param command an object that represents the parsed command-line arguments.
     */
    public MainTool(final MainCommand command) {
        super(command);
    }

    private Boolean checkIfFileInFolder(String path, String identifier) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null)
        for (File listOfFile : listOfFiles) {
            if (listOfFile.getName().contains(identifier)) {
                System.out.println("File found for identifier " + identifier + " checking next identifier.");
                return true;
            }


        }
        System.out.println("No file found for identifier " + identifier + " beginning download.");
        return false;
    }

    @Override
    public void execute() {

        // get the parsed command-line arguments
        final MainCommand command = super.getCommand();
        LOG.info("Parse commands");

        String password;


        config = parseConfig(command.getConfigPath());

        IDataStoreServerApi dss;
        IApplicationServerApi app;


//Get the password for openBis login. If not provided in the config.yaml the user can enter it in the command line
        if (config.getPassword() == null) {
            try {
                java.io.Console console = System.console();
                password = new String(console.readPassword("Password: "));
                if (password.isEmpty()) {
                    System.out.println("You need to provide a password");
                    LOG.error("No password provided");
                    System.exit(1);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();


            }
        }
        PostmanSessionManager manager;
        try {
            manager = loginToOpenBis();
        } catch (PostmanOpenBISLoginFailedException e) {
            System.out.println("Could not connect to openBis. Check your config file");
            return;
        }
        app = manager.getApplicationServer();
        dss = manager.getDataStoreServer();
        String sessionToken = manager.getSessionToken();


        //Use postman to download sample files if -m parameter is given
        if (command.getIdentifierPath() != null) {

            PostmanDataFilterer filterer = new PostmanDataFilterer();

            PostmanDataFinder finder = new PostmanDataFinder(manager.getApplicationServer(), manager.getDataStoreServer()
                    , filterer, manager.getSessionToken());
            PostmanDataDownloaderV3 loader = new PostmanDataDownloaderV3(dss, finder, sessionToken);

            PostmanFilterOptions postmanOptions = new PostmanFilterOptions();
            postmanOptions.setFileType(".fastq.gz");
            List<String> regexPatterns = new ArrayList<>();
            regexPatterns.add(".fastq.gz");
            postmanOptions.setRegexPatterns(regexPatterns);


            //now get all identifiers using a file containing the identifiers
            ArrayList<String> identifiers;
            identifiers = new ArrayList<>();
            String l;
            try {

                BufferedReader bufferedReader = new BufferedReader(new FileReader(command.getIdentifierPath()));

                while ((l = bufferedReader.readLine()) != null) {
                    identifiers.add(l);
                }
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Check if files are already downloaded and remove identifier from list if yes
            identifiers.removeIf(a -> checkIfFileInFolder(command.getOutput(), a));


            if (identifiers.size() != 0) {
                //Download all data for given identifiers
                LOG.info("Initialize download ...");
                int datasetDownloadReturnCode = -1;
                try {

                    loader.downloadRequestedFilesOfDatasets(identifiers, postmanOptions, command.getOutput());


                } catch (IOException e) {
                    e.printStackTrace();
                }

                LOG.info("Download successfully finished.");


            } else {
                System.out.println("Raw files already present in output folder");
            }

        }
        try {
            //could not stop postman downloading this file so check for it and delete it in case
            Files.deleteIfExists(Paths.get(command.getOutput() + "/source_dropbox.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }





        //If a openBis parsing config is given then the keywords in it will be used for parsing the openBis data

        geoParser = new GEOOpenBisParser(command.getProject(), config.getUsername(), sessionToken, app, dss, config, command);


        HashMap<String, List> geo = geoParser.parseSingle();


        // logout to release the resources related with the session
        app.logout(sessionToken);
        //Download samples with qpostman and calculate md5checksum

        // Create excel from template
        try {
            final boolean mkdirs = new File(command.getOutput()).mkdirs();

            List sampleList = geo.get("sample");
            List rawList = geo.get("raw");
            new GEOExcelCreater(sampleList, rawList, command.getOutput(),
                    command.getProject());
            System.out.println("Creating Excel file finished successfully.");
        } catch (IOException e) {
            LOG.error("Could not create excel file.");
            e.printStackTrace();
        }


    }


    private Config parseConfig(String configpath) {
        Config config = new Config();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            config = mapper.readValue(new File(configpath), Config.class);


        } catch (Exception e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

        }
        return config;
    }

    private PostmanSessionManager loginToOpenBis() throws PostmanOpenBISLoginFailedException {

        PostmanSessionManager manager = PostmanSessionManager.getPostmanSessionManager();
        PostmanConfig postman_conf = new PostmanConfig(config.getUsername(), config.getPassword(), config.getApp(), config.getDss());

        manager.loginToOpenBIS(postman_conf);


        return (manager);


    }

    // TODO: override the shutdown() method if you are implementing a daemon and want to take advantage of a shutdown hook for clean-up tasks


}