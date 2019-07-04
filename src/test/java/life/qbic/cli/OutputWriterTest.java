package life.qbic.cli;

import life.qbic.cli.io.OutputWriter;
import life.qbic.cli.model.geo.RawDataGEO;
import life.qbic.cli.model.geo.SampleGEO;
import org.junit.Test;
import org.springframework.beans.factory.support.ManagedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OutputWriterTest {
    @Test
    public void testOutWriter() {


        List<SampleGEO> sampleList = new ArrayList<>();
        List<RawDataGEO> rawList = new ArrayList<>();
        SampleGEO testSample = new SampleGEO(false);
        RawDataGEO testRaw = new RawDataGEO();
        testRaw.setInstrumentModel("TEST INSTRUMENT MODEL");
        testRaw.setSingleOrPairedEnd("PAIRED END");
        testSample.setSourceName("TEST SOURCE NAME");
        testSample.setOrganism("TEST ORGANISM");
        Map<String, String> testMap = new ManagedMap<>();
        testSample.setCharacteristics(testMap);
        testSample.setCode("Test code");
        testSample.setMolecule("TEST MOLECULE");
        testSample.setRawFile("TEST RAW FILE");
        testSample.setTitle("TEST TITLE");
        testSample.setSampleName("TEST SAMPLE NAME");
        testSample.setDescription("TEST DESCRIPTION");
        testSample.setProcessedDataFile("TEST DATA FILE");
        sampleList.add(testSample);
        rawList.add(testRaw);


        OutputWriter.writeNumberOfSamples(sampleList);
        //OutputWriter.writeRawDataToConsole(rawList);
        //OutputWriter.writeSamplesToConsole(sampleList);


    }
}
