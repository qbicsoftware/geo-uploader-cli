package life.qbic.cli.io;

import life.qbic.cli.model.geo.RawDataGEO;
import life.qbic.cli.model.geo.SampleGEO;

import java.util.List;

public class OutputWriter {

    public OutputWriter() {

    }

    public static void writeNumberOfSamples(List<SampleGEO> sampleGEOList) {
        System.out.println("#Samples: " + sampleGEOList.size());
    }

    public static void writeSamplesToConsole(List<SampleGEO> sampleGEOList) {
        System.out.println();
        String charLabels = "";
        for (String key : sampleGEOList.get(0).getCharacteristics().keySet()) {
            charLabels = "characteristics: " + key + "\t" + charLabels;
        }

        String header = "Sample name\ttitle\tsource name\torganism\t" + charLabels
                + "molecule\tdescription\tprocessed data file\traw file";
        System.out.println(header);
        int i = 1;
        for (SampleGEO sampleGEO : sampleGEOList) {
            StringBuilder rowBuilder = new StringBuilder();
            for (String cell : sampleGEO.getSampleRow()) {
                rowBuilder.append(cell).append("\t");
            }
            String row = rowBuilder.toString();
            row.trim();
            System.out.printf("%d %s%n", i, row);
            i = i + 1;
        }
    }

    public static void writeRawDataToConsole(List<RawDataGEO> rawDataGEOList) {
        System.out.println();
        String header = "file name\tfile type\tfile checksum\tinstrument model\tread length\tsingle or paired-end";
        System.out.println(header);
        int i = 1;
        for (RawDataGEO rawGeo : rawDataGEOList) {
            String row = "";
            for (String cell : rawGeo.getRawFilesRow()) {
                row = String.format("%s%s\t", row, cell);
            }
            row.trim();
            System.out.println(i + " " + row);
            i = i + 1;
        }
    }

}
