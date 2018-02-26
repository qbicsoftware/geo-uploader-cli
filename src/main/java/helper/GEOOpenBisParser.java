package helper;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import main.Main;
import model.geo.RawDataGEO;
import model.geo.SampleGEO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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
        fetchOptions.withExperiment().withProperties();
        DataSetFetchOptions dataSetFetchOptions = new DataSetFetchOptions();
        dataSetFetchOptions.withExperiment();
        fetchOptions.withDataSetsUsing(dataSetFetchOptions);

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
            rawGeo.setInstrumentModel(rawDataSample.getExperiment().getProperty("Q_SEQUENCER_DEVICE").replace("_", " ").replace("IMGAG", "").trim());
            rawGeo.setSingleOrPairedEnd(rawDataSample.getExperiment().getProperty("Q_SEQUENCING_MODE").replace("_", "-"));
            DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
            criteria.withDataSet().withSample().withCode().thatEquals(rawDataSample.getCode());
            SearchResult<DataSetFile> files = dss.searchFiles(sessionToken, criteria, new DataSetFileFetchOptions());
            for (DataSetFile file : files.getObjects()) {
                if (file.getPermId().toString().contains(".fastq")) {
                    String[] path = file.getPermId().toString().split("/");
                    computeMd5(file, rawGeo);
                    geo.setRawFile(path[path.length - 1]);
                    rawGeo.setFileName(geo.getRawFile());
                    //TODO hard coded
                    rawGeo.setFileType("fastq");
                }
            }
            sampleGEOList.add(geo);
            rawDataGEOList.add(rawGeo);
        }

        for (Sample measuredSample : measuredSamples.getObjects()) {
            for (SampleGEO geo : sampleGEOList) {
                //TODO equals RNA is to hard coded?
                if (geo.getCode().contains(measuredSample.getCode()) && measuredSample.getProperty("Q_SAMPLE_TYPE").equals("RNA")) {
                    geo.setTitle(measuredSample.getProperty("Q_SECONDARY_NAME"));
                    geo.setMolecule(measuredSample.getProperty("Q_SAMPLE_TYPE"));
                    geo.setCharacteristics(parseProperty(measuredSample.getProperty("Q_PROPERTIES"), "qcategorical"));
                    while (geo.getCharacteristics().keySet().size() < 3) {
                        geo.getCharacteristics().put("", "");
                    }
                }
            }
        }

        for (Sample extractedSample : extractedSamples.getObjects()) {
            for (SampleGEO geo : sampleGEOList) {
                if (geo.getTitle().equals(extractedSample.getProperty("Q_SECONDARY_NAME"))) {
                    geo.setSourceName(extractedSample.getProperty("Q_PRIMARY_TISSUE")
                            + "_" + extractedSample.getProperty("Q_TISSUE_DETAILED"));
                    VocabularyTermSearchCriteria vocabularyTermSearchCriteria = new VocabularyTermSearchCriteria();
                    vocabularyTermSearchCriteria.withCode().thatEquals(extractedSample.getProperty("Q_TISSUE_DETAILED"));
                    SearchResult<VocabularyTerm> vocabularyTermSearchResult = app.searchVocabularyTerms(sessionToken, vocabularyTermSearchCriteria, new VocabularyTermFetchOptions());
                    for (VocabularyTerm vocabularyTerm : vocabularyTermSearchResult.getObjects()) {
                        if (vocabularyTerm.getCode().equals(extractedSample.getProperty("Q_TISSUE_DETAILED"))) {
                            geo.setSourceName(vocabularyTerm.getDescription());
                        }
                    }
                }
            }
        }

        for (Sample sampleSource : sampleSources.getObjects()) {
            for (SampleGEO geo : sampleGEOList) {
                if (geo.getTitle().equals(sampleSource.getProperty("Q_SECONDARY_NAME"))) {
                    geo.setOrganism(sampleSource.getProperty("Q_NCBI_ORGANISM"));
                    //TODO hard coded
                    VocabularyTermSearchCriteria vocabularyTermSearchCriteria = new VocabularyTermSearchCriteria();
                    vocabularyTermSearchCriteria.withCode().thatEquals(geo.getOrganism());
                    SearchResult<VocabularyTerm> vocabularyTermSearchResult = app.searchVocabularyTerms(sessionToken, vocabularyTermSearchCriteria, new VocabularyTermFetchOptions());
                    for (VocabularyTerm vocabularyTerm : vocabularyTermSearchResult.getObjects()) {
                        if(vocabularyTerm.getCode().equals(geo.getOrganism())){
                            geo.setOrganism(vocabularyTerm.getDescription());
                        }
                    }
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

    public static Map<String, String> parseProperty(String xml, String property) {
        HashMap<String, String> properties = new HashMap<>();
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(xml.split("\n")));
        for (String line : lines) {
            if (line.contains(property)) {
                String label = line.trim().split(" ")[1].replace("label=", "").replace("/>", "").replace("\"", "");
                String value = line.trim().split(" ")[2].replace("value=", "").replace("/>", "").replace("\"", "");
                properties.put(label, value);
            }
        }

        return properties;
    }

    public static byte[] getBytesOfMd5(InputStream is) throws IOException {
        byte[] buffer = new byte[1024];
        MessageDigest complete = null;
        try {
            complete = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        int numRead;
        do {
            numRead = is.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        is.close();
        return complete.digest();
    }

    public void computeMd5(DataSetFile file, RawDataGEO rawGeo) {
        IDataSetFileId fileId = new DataSetFilePermId(new DataSetPermId(file.getDataSetPermId().toString()));
        InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(fileId) , new DataSetFileDownloadOptions());
        try {
            rawGeo.setFileChecksum(DatatypeConverter.printHexBinary(getBytesOfMd5(stream)));
        } catch (IOException e) {
            log.error("Could not compute MD5 checksum");
        }
    }


}