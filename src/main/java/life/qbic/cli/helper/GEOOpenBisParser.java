package life.qbic.cli.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import life.qbic.cli.main.MainCommand;
import life.qbic.cli.main.MainTool;
import life.qbic.cli.model.geo.RawDataGEO;
import life.qbic.cli.model.geo.SampleGEO;

import java.security.DigestInputStream;


public class GEOOpenBisParser {

    public String rawFileName;
    public String spaceCode, projectCode, sessionToken, username;
    public IApplicationServerApi app;
    public IDataStoreServerApi dss;
    public Project project = null;

    public GEOOpenBisParser(String projectCode, String username, String sessionToken,
                            IApplicationServerApi app, IDataStoreServerApi dss) {
        this.projectCode = projectCode;
        this.username = username;
        this.sessionToken = sessionToken;
        this.app = app;
        this.dss = dss;

        checkSpaceAvailability();
    }


    public String checkNull(String s) {
        if (s == null)
            return ("Not specified");
        else
            return (s);
    }
    public static Map<String, String> parseProperty(String xml, String property) {
        HashMap<String, String> properties = new HashMap<>();
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(xml.split("\n")));
        for (String line : lines) {
            if (line.contains(property)) {
                String label = line.trim().split(" ")[1].replace("label=", "").replace("/>", "")
                        .replace("\"", "");
                String value = line.trim().split(" ")[2].replace("value=", "").replace("/>", "")
                        .replace("\"", "");
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

    //public HashMap<String, List> parsePaired() {
    //
    //}

    public void checkSpaceAvailability() {
        // invoke other API methods using the session token, for instance search for spaces
        ProjectSearchCriteria projectSearchCriteria = new ProjectSearchCriteria();
        projectSearchCriteria.withCode().thatEquals(projectCode);
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withSpace();
        SearchResult<Project> projects = app
                .searchProjects(sessionToken, projectSearchCriteria, projectFetchOptions);

        // Check if space if space is available for user

        project = projects.getObjects().get(0);

        if (project == null) {
            System.out.println("Project " + projectCode + " does not exist for user " + username);
            app.logout(sessionToken);
            System.exit(0);
        } else {
            System.out.println("Found project " + projectCode + " for user " + username);
            spaceCode = project.getSpace().getCode();
        }
    }

    public void createSampleFile(String projectIdentifier) {


    }

    public String computeMd5(String rawName) {


        String md5 = "";
        try (FileInputStream fis = new FileInputStream(new File("samples/" + rawName))) {
            md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(String.valueOf(fis));
        } catch (FileNotFoundException e) {
            if (rawName.contains("fastq"))
                System.out.println("Could not create checksum for file " + rawName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return md5;
    }

    public HashMap<String, List> parseSingle() {
        // Set up fetch options
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withProject();
        fetchOptions.withSpace();
        fetchOptions.withProperties();
        fetchOptions.withExperiment().withProperties();
        fetchOptions.withExperiment().withProject();
        DataSetFetchOptions dataSetFetchOptions = new DataSetFetchOptions();
        dataSetFetchOptions.withExperiment();
        fetchOptions.withDataSetsUsing(dataSetFetchOptions);

        SampleSearchCriteria rawDataCriteria = new SampleSearchCriteria();
        rawDataCriteria.withSpace().withCode().thatEquals(spaceCode);
        rawDataCriteria.withExperiment().withProject().withCode().thatEquals(projectCode);
        rawDataCriteria.withType().withCode().thatEquals("Q_NGS_SINGLE_SAMPLE_RUN");

        SampleSearchCriteria sampleSourcesCriteria = new SampleSearchCriteria();
        sampleSourcesCriteria.withSpace().withCode().thatEquals(spaceCode);
        sampleSourcesCriteria.withExperiment().withProject().withCode().thatEquals(projectCode);
        sampleSourcesCriteria.withType().withCode().thatEquals("Q_BIOLOGICAL_ENTITY");

        SampleSearchCriteria extractedSamplesCriteria = new SampleSearchCriteria();
        extractedSamplesCriteria.withSpace().withCode().thatEquals(spaceCode);
        extractedSamplesCriteria.withExperiment().withProject().withCode().thatEquals(projectCode);
        extractedSamplesCriteria.withType().withCode().thatEquals("Q_BIOLOGICAL_SAMPLE");

        SampleSearchCriteria measuredSampleCriteria = new SampleSearchCriteria();
        measuredSampleCriteria.withSpace().withCode().thatEquals(spaceCode);
        measuredSampleCriteria.withExperiment().withProject().withCode().thatEquals(projectCode);
        measuredSampleCriteria.withType().withCode().thatEquals("Q_TEST_SAMPLE");

        SearchResult<Sample> rawData = app.searchSamples(sessionToken, rawDataCriteria, fetchOptions);
        SearchResult<Sample> sampleSources = app
                .searchSamples(sessionToken, sampleSourcesCriteria, fetchOptions);
        SearchResult<Sample> measuredSamples = app
                .searchSamples(sessionToken, measuredSampleCriteria, fetchOptions);
        SearchResult<Sample> extractedSamples = app
                .searchSamples(sessionToken, extractedSamplesCriteria, fetchOptions);

        List<SampleGEO> sampleGEOList = new ArrayList<>();
        List<RawDataGEO> rawDataGEOList = new ArrayList<>();
        for (int i = 0; i < rawData.getObjects().size(); i++) {
            Sample rawDataSample = rawData.getObjects().get(i);
            SampleGEO geo = new SampleGEO();
            RawDataGEO rawGeo = new RawDataGEO();
            geo.setSampleName("Sample " + (i + 1));
            geo.setCode("Code: " + rawDataSample.getCode());
            rawGeo.setInstrumentModel(
                    rawDataSample.getExperiment().getProperty("Q_SEQUENCER_DEVICE").replace("_", " ")
                            .replace("IMGAG", "").trim());

            DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
            criteria.withDataSet().withSample().withCode().thatEquals(rawDataSample.getCode());
            SearchResult<DataSetFile> files = dss
                    .searchFiles(sessionToken, criteria, new DataSetFileFetchOptions());
            for (DataSetFile file : files.getObjects())
                if (file.getPermId().toString().contains(".fastq")
                        && !file.getPermId().toString().contains(".sha256sum")
                        && !file.getPermId().toString().contains("origlabfilename")) {
                    String[] path = file.getPermId().toString().split("/");

                    geo.setRawFile(path[path.length - 1]);
                    rawGeo.setFileName(geo.getRawFile());


                    //computeMd5(file, rawGeo);


                    rawFileName = rawGeo.getFileName();
                    //String md5 = computeMd5(rawFileName);
                    //rawGeo.setFileChecksum(md5);


                    //Check if sample has sequecing_mode if it has not then
                    // determine sequencing mode checking for R1 in raw data name
                    if (rawDataSample.getExperiment().getProperty("Q_SEQUENCING_MODE") == null) {
                        if (rawFileName != null)
                            if (rawFileName.contains("R1") || rawFileName.contains("R2"))  {
                                rawGeo.setSingleOrPairedEnd("Paired End");


                            } else {
                                if (rawFileName != null)
                                    rawGeo.setSingleOrPairedEnd("Single End");
                            }
                    }
                    if (rawDataSample.getExperiment().getProperty("Q_SEQUENCING_MODE") != null) {
                        rawGeo.setSingleOrPairedEnd(
                                rawDataSample.getExperiment().getProperty("Q_SEQUENCING_MODE").replace("_", "-"));
                    }
                    //TODO hard coded
                    if (rawFileName.contains(".fastq"))
                        rawGeo.setFileType("fastq");
                }
            sampleGEOList.add(geo);
            rawDataGEOList.add(rawGeo);
        }

        for (Sample measuredSample : measuredSamples.getObjects()) {
            for (SampleGEO geo : sampleGEOList) {
                //TODO equals RNA is to hard coded?
                if (geo.getCode().contains(measuredSample.getCode()) && measuredSample
                        .getProperty("Q_SAMPLE_TYPE").equals("RNA")) {
                    geo.setTitle(measuredSample.getProperty("Q_SECONDARY_NAME"));
                    geo.setMolecule(measuredSample.getProperty("Q_SAMPLE_TYPE"));
                    if (measuredSample.getProperties().containsKey("Q_PROPERTIES")) {
                        geo.setCharacteristics(
                                parseProperty(measuredSample.getProperty("Q_PROPERTIES"), "qcategorical"));
                        while (geo.getCharacteristics().keySet().size() < 3) {
                            geo.getCharacteristics().put("", "");
                        }
                    }
                }
            }
        }

        for (Sample extractedSample : extractedSamples.getObjects()) {


            for (SampleGEO geo : sampleGEOList) {

                extractedSample.getProperties();
                if(extractedSample.getProperty("Q_PRIMARY_TISSUE") != null)
                    geo.setSourceName(extractedSample.getProperty("Q_PRIMARY_TISSUE"));
                if (geo.getTitle().contains(checkNull(extractedSample.getProperty("Q_SECONDARY_NAME")))) {
                        geo.setSourceName(extractedSample.getProperty("Q_PRIMARY_TISSUE")
                                + "_" + extractedSample.getProperty("Q_TISSUE_DETAILED"));
                        VocabularyTermSearchCriteria vocabularyTermSearchCriteria = new VocabularyTermSearchCriteria();
                        vocabularyTermSearchCriteria.withCode()
                                .thatEquals(extractedSample.getProperty("Q_TISSUE_DETAILED"));
                        SearchResult<VocabularyTerm> vocabularyTermSearchResult = app
                                .searchVocabularyTerms(sessionToken, vocabularyTermSearchCriteria,
                                        new VocabularyTermFetchOptions());
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
                System.out.println(sampleSource.getProperty("Q_SECONDARY_NAME"));
                    if (checkNull(geo.getTitle()).equals(sampleSource.getProperty("Q_SECONDARY_NAME"))) {
                        geo.setOrganism(sampleSource.getProperty("Q_NCBI_ORGANISM"));
                        //TODO hard coded

                        VocabularyTermSearchCriteria vocabularyTermSearchCriteria = new VocabularyTermSearchCriteria();
                        vocabularyTermSearchCriteria.withCode().thatEquals(geo.getOrganism());
                        SearchResult<VocabularyTerm> vocabularyTermSearchResult = app
                                .searchVocabularyTerms(sessionToken, vocabularyTermSearchCriteria,
                                        new VocabularyTermFetchOptions());
                        for (VocabularyTerm vocabularyTerm : vocabularyTermSearchResult.getObjects()) {
                            if (vocabularyTerm.getCode().equals(geo.getOrganism())) {
                                System.out.println(vocabularyTerm.getDescription());
                                geo.setOrganism(vocabularyTerm.getDescription());
                            }
                        }
                    }
            }
        }

        HashMap<String, List> parsed = new HashMap<>();
        parsed.put("sample", sampleGEOList);
        parsed.put("raw", rawDataGEOList);

        System.out.println("Finished parsing succesfully ...");

        return parsed;
    }

}
