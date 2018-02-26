package io;

import model.geo.RawDataGEO;
import model.geo.SampleGEO;

import java.util.List;

public class OutputWriter {

    public OutputWriter() {

    }

    public static void writeNumberOfSamples(List<SampleGEO> sampleGEOList) {
        System.out.println("#Samples: " + sampleGEOList.size());
    }

    public static void writeSamplesToConsole(List<SampleGEO> sampleGEOList) {
        String charLabels = "";
        for (String key : sampleGEOList.get(0).getCharacteristics().keySet()) {
            charLabels = "characteristics: " + key + "\t" + charLabels;
        }

        String header = "Sample name\ttitle\tsource name\torganism\t" + charLabels + "molecule\tdescription\tprocessed data file\traw file";
        System.out.println(header);

        for (SampleGEO geo : sampleGEOList) {
            String charValues = "";
            for (String key : geo.getCharacteristics().keySet()) {
                charValues = geo.getCharacteristics().get(key) + "\t" + charValues;
            }

            String line = geo.getSampleName() + "\t" + geo.getTitle() + "\t" + geo.getSourceName() + "\t" + geo.getOrganism() + "\t" + charValues + geo.getMolecule() + "\t" + geo.getDescription() + "\t" + geo.getProcessedDataFile() + "\t" + geo.getRawFile();
            line = line.replace("null", "").replace(" ", "");
            System.out.println(line);
        }
    }

    public static void writeRawDataToConsole(List<RawDataGEO> rawDataGEOList) {
        String header = "file name\tfile type\tfile checksum\tinstrument model\tread length\tsingle or paired-end";
        System.out.println(header);

        for (RawDataGEO rawGeo : rawDataGEOList) {
            String line = rawGeo.getFileName() + "\t" + rawGeo.getFileType() + "\t" + rawGeo.getFileChecksum() + "\t" + rawGeo.getInstrumentModel() + "\t" + rawGeo.getReadLength() + "\t" + rawGeo.getSingleOrPairedEnd();
            line = line.replace("null", "").replace(" ", "");
            System.out.println(line);
        }
    }

}
