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
import life.qbic.cli.main.MainCommand;
import life.qbic.cli.model.geo.Config;
import life.qbic.cli.model.geo.RawDataGEO;
import life.qbic.cli.model.geo.SampleGEO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class GEOOpenBisParser {

    private String rawFileName;
    private String spaceCode, projectCode, sessionToken, username;
    private IApplicationServerApi app;
    private IDataStoreServerApi dss;
    private Project project = null;
    private Config config;
    private String organism;
    private String sourceName;
    private String sourceNameDetailed;
    private String title;
    private String molecule;
    private String characteristics;
    private String property;
    private String experiment;
    private String outPath;
    private MainCommand command;
    private String md5 = "";


    public GEOOpenBisParser(String projectCode, String username, String sessionToken,
                            IApplicationServerApi app, IDataStoreServerApi dss, Config config, MainCommand command)

    {


        this.projectCode = projectCode;
        this.username = username;
        this.sessionToken = sessionToken;
        this.app = app;
        this.dss = dss;
        this.config = config;
        this.command = command;
        outPath = command.getOutput();


        checkSpaceAvailability();


        organism = "Q_NCBI_ORGANISM";
        this.organism = config.getOrganism();
        sourceName = "Q_PRIMARY_TISSUE";
        this.sourceName = config.getSource_name();
        sourceNameDetailed = "Q_TISSUE_DETAILED";
        this.sourceNameDetailed = config.getSource_name_detailed();
        this.title = config.getTitle();
        molecule = "Q_SAMPLE_TYPE";
        this.molecule = config.getMolecule();
        characteristics = "Q_PROPERTIES";
        this.characteristics = config.getCharacteristics();
        property = "qcategorical";
        this.property = config.getProperty();
        this.experiment = config.getExperiment();


    }

    private static Map<String, String> parseProperty(String xml, String property) {
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


    private String checkNull(String s) {
        if (s == null)
            return ("Not specified");
        else
            return (s);
    }

    //public HashMap<String, List> parsePaired() {
    //
    //}

    private void checkSpaceAvailability() {
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



    public String computeMd5(String rawName) {
        String md5 = "";


        try (FileInputStream fis = new FileInputStream(new File(outPath + '/' + rawName))) {
            md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(String.valueOf(fis));
        } catch (FileNotFoundException e) {
            if (rawName.contains("fastq"))
                System.out.println("File " + rawName + " does not exist download it with -f option or put it into " + outPath +
                        " manually to compute its checksum!");

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

            //This is the original line from Julian to set the instrument model. I replaced it
            // to be N/A all the time. THE Q_SEQUENCER_DEVICE field has to be filled for the original line to work correctly
            //rawGeo.setInstrumentModel(
            // rawDataSample.getExperiment().getProperty("Q_SEQUENCER_DEVICE").replace("_", " ")
            //     .replace("IMGAG", "").trim());


            DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
            criteria.withDataSet().withSample().withCode().thatEquals(rawDataSample.getCode());
            SearchResult<DataSetFile> files = dss
                    .searchFiles(sessionToken, criteria, new DataSetFileFetchOptions());
            for (DataSetFile file : files.getObjects())
                if (file.getPermId().toString().contains(".fastq")
                        && !file.getPermId().toString().contains(".sha256sum")
                        && !file.getPermId().toString().contains("origlabfilename")) {
                    SampleGEO geo = new SampleGEO();
                    geo.setSampleName("Sample " + (i + 1));
                    geo.setCode("Code: " + rawDataSample.getCode());

                    String[] path = file.getPermId().toString().split("/");
                    RawDataGEO rawGeo = new RawDataGEO();

                    rawGeo.setInstrumentModel("N/A");
                    rawFileName = path[path.length - 1];

                    rawGeo.setFileName(rawFileName);


                    if (rawFileName.contains("dropbox"))
                        continue;
                    //Create md5 checksum

                    if (command.getIdentifierPath() != null) {
                        md5 = computeMd5(rawFileName);
                        rawGeo.setFileChecksum(md5);
                    }


                    //Check if sample has sequecing_mode if it has not then
                    // determine sequencing mode checking for R1 in raw data name
                    if (rawFileName.contains("_R1") || rawFileName.contains("_R2")) {
                        rawGeo.setSingleOrPairedEnd("Paired End");


                    } else {
                        rawGeo.setSingleOrPairedEnd("Single End");
                    }


                    //TODO hard coded
                    if (rawFileName.contains(".fastq"))
                        rawGeo.setFileType("fastq");
                    geo.setRawFile(rawFileName);

                    sampleGEOList.add(geo);
                    rawDataGEOList.add(rawGeo);
                }
        }

        for (Sample measuredSample : measuredSamples.getObjects()) {
            //TODO equals RNA is to hard coded?
            for (SampleGEO geo : sampleGEOList)
                if (geo.getCode().contains(measuredSample.getCode()) && measuredSample
                        .getProperty(this.molecule).equals("RNA")) {
                    geo.setTitle(measuredSample.getProperty(this.title.trim()));
                    geo.setMolecule(measuredSample.getProperty(this.molecule));
                    if (measuredSample.getProperties().containsKey(this.characteristics)) {
                        geo.setCharacteristics(
                                parseProperty(measuredSample.getProperty(this.characteristics), this.property));
                        while (geo.getCharacteristics().keySet().size() < 3) {
                            geo.getCharacteristics().put("", "");
                        }
                    }
                }
        }

        for (Sample extractedSample : extractedSamples.getObjects()) {


            for (SampleGEO geo : sampleGEOList) {

                extractedSample.getProperties();
                if (extractedSample.getProperty(this.sourceName) != null)
                    geo.setSourceName(extractedSample.getProperty(this.sourceName));
                if (geo.getTitle() != null)
                    if (geo.getTitle().contains(checkNull(extractedSample.getProperty(this.title)))) {
                        geo.setSourceName(extractedSample.getProperty(this.sourceName)
                                + "_" + extractedSample.getProperty(this.sourceNameDetailed));
                        VocabularyTermSearchCriteria vocabularyTermSearchCriteria = new VocabularyTermSearchCriteria();
                        vocabularyTermSearchCriteria.withCode()
                                .thatEquals(extractedSample.getProperty(this.sourceNameDetailed));
                        SearchResult<VocabularyTerm> vocabularyTermSearchResult = app
                                .searchVocabularyTerms(sessionToken, vocabularyTermSearchCriteria,
                                        new VocabularyTermFetchOptions());
                        for (VocabularyTerm vocabularyTerm : vocabularyTermSearchResult.getObjects()) {
                            if (vocabularyTerm.getCode().equals(extractedSample.getProperty(this.sourceNameDetailed))) {
                                geo.setSourceName(vocabularyTerm.getDescription());
                            }
                        }
                    }
            }
        }


        for (Sample sampleSource : sampleSources.getObjects()) {
            for (SampleGEO geo : sampleGEOList) {
                if (checkNull(geo.getTitle()).equals(sampleSource.getProperty(this.title))) {
                    geo.setOrganism(sampleSource.getProperty(this.organism));
                    //TODO hard coded

                    VocabularyTermSearchCriteria vocabularyTermSearchCriteria = new VocabularyTermSearchCriteria();
                    vocabularyTermSearchCriteria.withCode().thatEquals(geo.getOrganism());
                    SearchResult<VocabularyTerm> vocabularyTermSearchResult = app
                            .searchVocabularyTerms(sessionToken, vocabularyTermSearchCriteria,
                                    new VocabularyTermFetchOptions());
                    for (VocabularyTerm vocabularyTerm : vocabularyTermSearchResult.getObjects()) {
                        if (vocabularyTerm.getCode().equals(geo.getOrganism())) {
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
