package main;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import commandline.Argparser;
import helper.GEOExcelCreater;
import helper.GEOOpenBisParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static Logger log = LogManager.getLogger(Main.class);
    public static String username, password, project;

    public static void main(String[] args) throws IOException {
        Argparser.parseCmdArguments(args);

        System.out.format("Provide username: ");

        username = Argparser.readStringFromInputStream();

        if (username.isEmpty()) {
            System.out.println("You need to provide a username.");
            System.exit(1);
        }

        System.out.format("Provide password for user \'%s\': ", username);

        password = Argparser.readPasswordFromInputStream();

        if (password.isEmpty()) {
            System.out.println("You need to provide a password.");
            System.exit(1);
        }

        // Reference the DSS
        IDataStoreServerApi dss =
                HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
                        "https://qbis.qbic.uni-tuebingen.de:444/datastore_server" + IDataStoreServerApi.SERVICE_URL, 10000);

        // get a reference to AS API
        IApplicationServerApi app = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, "https://qbis.qbic.uni-tuebingen.de/openbis/openbis" + IApplicationServerApi.SERVICE_URL, 10000);

        // login to obtain a session token
        String sessionToken = app.login(username, password);

        System.out.println("Connection to openbis established succesfully.");

        System.out.format("Provide project identifier:  ");

        project = Argparser.readStringFromInputStream();

        if (project.isEmpty() || project == null) {
            System.out.println("You need to provide a project.");
            System.exit(1);
        }
        String[] split = project.split("/");
        project = split[split.length-1];
        System.out.println(project);
        // Parse space
        GEOOpenBisParser geoParser = new GEOOpenBisParser(project, username, sessionToken, app, dss);
        HashMap<String, List> geo = geoParser.parseSingle();

        // logout to release the resources related with the session
        app.logout(sessionToken);

        // Create excel from template
        GEOExcelCreater xls = new GEOExcelCreater(geo.get("sample"), geo.get("raw"));

    }
}
