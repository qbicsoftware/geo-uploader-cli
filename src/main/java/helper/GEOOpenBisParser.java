package helper;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import main.Main;
import model.geo.RawDataGEO;
import model.geo.SampleGEO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GEOOpenBisParser {

    public static Logger log = LogManager.getLogger(GEOOpenBisParser.class);
    public String spaceName, sessionToken;
    public IApplicationServerApi app;
    public IDataStoreServerApi dss;

    public GEOOpenBisParser(String spaceName, String sessionToken, IApplicationServerApi app, IDataStoreServerApi dss) {
        this.spaceName = spaceName;
        this.sessionToken = sessionToken;
        this.app = app;
        this.dss = dss;

        checkSpaceAvailability();
    }

    public void checkSpaceAvailability() {
        // invoke other API methods using the session token, for instance search for spaces
        SearchResult<Space> spaces = app.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());

        // Check if space if space is available for user
        Space space = null;
        for (Space sp : spaces.getObjects()) {
            if (sp.getCode().equals(spaceName)) {
                space = sp;
            }
        }

        if (space == null) {
            log.error("Space " + spaceName + " does not exist for user " + Main.ch.getUserID());
            System.out.println("Space " + spaceName + " does not exist for user " + Main.ch.getUserID());
            app.logout(sessionToken);
            System.exit(0);
        } else {
            log.info("Found space " + spaceName + " for user " + Main.ch.getUserID());
            System.out.println("Found space " + spaceName + " for user " + Main.ch.getUserID());
        }
    }

    public HashMap<String, List> parseSingle() {
        // Set up fetch options
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withProject();
        fetchOptions.withSpace();
        fetchOptions.withProperties();
        DataSetFetchOptions dataSetFetchOptions = new DataSetFetchOptions();
        dataSetFetchOptions.withExperiment();
        fetchOptions.withDataSetsUsing(dataSetFetchOptions);


        // Set up search criterias
        SampleSearchCriteria sampleCriteria = new SampleSearchCriteria();
        sampleCriteria.withSpace().withCode().thatEquals(spaceName);

        SampleSearchCriteria rawDataCriteria = new SampleSearchCriteria();
        rawDataCriteria.withSpace().withCode().thatEquals(spaceName);
        rawDataCriteria.withType().withCode().thatEquals("Q_NGS_SINGLE_SAMPLE_RUN");

        SampleSearchCriteria sampleSourcesCriteria = new SampleSearchCriteria();
        sampleSourcesCriteria.withSpace().withCode().thatEquals(spaceName);
        sampleSourcesCriteria.withType().withCode().thatEquals("Q_BIOLOGICAL_ENTITY");

        SampleSearchCriteria extractedSamplesCriteria = new SampleSearchCriteria();
        extractedSamplesCriteria.withSpace().withCode().thatEquals(spaceName);
        extractedSamplesCriteria.withType().withCode().thatEquals("Q_BIOLOGICAL_SAMPLE");

        SampleSearchCriteria measuredSampleCriteria = new SampleSearchCriteria();
        measuredSampleCriteria.withSpace().withCode().thatEquals(spaceName);
        measuredSampleCriteria.withType().withCode().thatEquals("Q_TEST_SAMPLE");

        SearchResult<Sample> samples = app.searchSamples(sessionToken, sampleCriteria, fetchOptions);
        SearchResult<Sample> rawData = app.searchSamples(sessionToken, rawDataCriteria, fetchOptions);
        SearchResult<Sample> sampleSources = app.searchSamples(sessionToken, sampleSourcesCriteria, fetchOptions);
        SearchResult<Sample> measuredSamples = app.searchSamples(sessionToken, measuredSampleCriteria, fetchOptions);
        SearchResult<Sample> extractedSamples = app.searchSamples(sessionToken, extractedSamplesCriteria, fetchOptions);

        List<SampleGEO> sampleGEOList = new ArrayList<>();
        List<RawDataGEO> rawDataGEOList = new ArrayList<>();
        for (int i = 0; i < rawData.getObjects().size(); i++) {
            Sample rawDataSample = rawData.getObjects().get(i);
            SampleGEO geo = new SampleGEO();
            RawDataGEO rawGeo = new RawDataGEO();
            geo.setSampleName("Sample " + (i + 1));
            geo.setCode("Code: " + rawDataSample.getCode());
            DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
            criteria.withDataSet().withSample().withCode().thatEquals(rawDataSample.getCode());
            SearchResult<DataSetFile> files = dss.searchFiles(sessionToken, criteria, new DataSetFileFetchOptions());
            for (DataSetFile file : files.getObjects()) {
                if (file.getPermId().toString().contains(".fastq")) {
                    String[] path = file.getPermId().toString().split("/");
                    geo.setRawFile(path[path.length - 1]);
                    rawGeo.setFileName(geo.getRawFile());

                    //TODO hard coded
                    rawGeo.setFileType("fastq");
                    rawGeo.setSingleOrPairedEnd("single");
                }
            }
            sampleGEOList.add(geo);
            rawDataGEOList.add(rawGeo);
        }

        for (Sample measuredSample : measuredSamples.getObjects()) {
            for (SampleGEO geo : sampleGEOList) {
                if (geo.getCode().contains(measuredSample.getCode()) && measuredSample.getProperty("Q_SAMPLE_TYPE").equals("RNA")) {
                    geo.setTitle(measuredSample.getProperty("Q_SECONDARY_NAME"));
                    geo.setMolecule(measuredSample.getProperty("Q_SAMPLE_TYPE"));
                    geo.setCharacteristics(OpenBisPropertyParser.parseProperty(measuredSample.getProperty("Q_PROPERTIES"), "qcategorical"));
                    while (geo.getCharacteristics().keySet().size() < 3) {
                        geo.getCharacteristics().put("", "");
                    }
                }
            }
        }

        for (Sample extractedSample : extractedSamples.getObjects()) {
            for (SampleGEO geo : sampleGEOList) {
                if (geo.getTitle().equals(extractedSample.getProperty("Q_SECONDARY_NAME"))) {
                    geo.setSourceName(extractedSample.getProperty("Q_TISSUE_DETAILED"));
                }
            }
        }

        for (Sample sampleSource : sampleSources.getObjects()) {
            for (SampleGEO geo : sampleGEOList) {
                if (geo.getTitle().equals(sampleSource.getProperty("Q_SECONDARY_NAME"))) {

                    geo.setOrganism(sampleSource.getProperty("Q_NCBI_ORGANISM"));
                }
            }
        }

        HashMap<String, List> parsed = new HashMap<>();
        parsed.put("sample", sampleGEOList);
        parsed.put("raw", rawDataGEOList);

        return parsed;
    }

    //public HashMap<String, List> parsePaired() {
    //
    //}


}
