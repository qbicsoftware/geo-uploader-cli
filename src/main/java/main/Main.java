package main;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import connection.CredentialHandler;
import helper.GEOExcelCreater;
import helper.GEOOpenBisParser;
import io.OutputWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static Logger log = LogManager.getLogger(Main.class);

    // Enter space name to parse
    public static String spaceName = "MFT_SYNERGY_GLIOBLASTOMA_TREATMENT";

    // Enter path to credentials file: datasource.url, datasource.user, datasource.password
    //TODO Argument parser or portlet
    public static CredentialHandler ch = new CredentialHandler("/Users/spaethju/liferay/qbic-ext.properties");

    public static void main(String[] args) throws IOException {
        // Reference the DSS
        IDataStoreServerApi dss =
                HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
                        "https://qbis.qbic.uni-tuebingen.de:444/datastore_server" + IDataStoreServerApi.SERVICE_URL, 10000);

        // get a reference to AS API
        IApplicationServerApi app = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, "https://qbis.qbic.uni-tuebingen.de/openbis/openbis" + IApplicationServerApi.SERVICE_URL, 10000);

        // login to obtain a session token
        String sessionToken = app.login(ch.getUserID(), ch.getPw());

        // Parse space
        GEOOpenBisParser geoParser = new GEOOpenBisParser(spaceName, sessionToken, app, dss);
        HashMap<String, List> geo = geoParser.parseSingle();

        // logout to release the resources related with the session
        app.logout(sessionToken);

        // write output to console
        OutputWriter.writeNumberOfSamples(geo.get("sample"));
        OutputWriter.writeSamplesToConsole(geo.get("sample"));
        OutputWriter.writeRawDataToConsole(geo.get("raw"));

        // Create excel from template
        GEOExcelCreater xls = new GEOExcelCreater(geo.get("sample"), geo.get("raw"));

    }
}
