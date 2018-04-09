package main;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import commandline.CommandLineParameters;
import helper.GEOExcelCreater;
import helper.GEOOpenBisParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import picocli.CommandLine;

public class Main {

  public static void main(String[] args) throws IOException {
    CommandLineParameters params = new CommandLineParameters();
    new CommandLine(params).parse(args);

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
    String sessionToken = app.login(params.userName, password);

    System.out.println("Connection to openbis established succesfully.");

    System.out.format("Provide project identifier:  ");

    if (params.project.isEmpty() || params.project == null) {
      System.out.println("You need to provide a project.");
      System.exit(1);
    }
    String[] split = params.project.split("/");
    params.project = split[split.length - 1];
    System.out.println(params.project);
    // Parse space
    GEOOpenBisParser geoParser = new GEOOpenBisParser(params.project, params.userName, sessionToken,
        app, dss);
    HashMap<String, List> geo = geoParser.parseSingle();

    // logout to release the resources related with the session
    app.logout(sessionToken);

    // Create excel from template
    GEOExcelCreater xls = new GEOExcelCreater(geo.get("sample"), geo.get("raw"), params.output,
        params.project);

  }
}
