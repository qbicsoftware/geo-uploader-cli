package life.qbic.cli.model.geo;

import java.util.Map;

public class SampleGEO {

    private String code;
    private String sampleName, title, sourceName, organism, molecule, description, processedDataFile, rawFile;
    private Map<String, String> characteristics;
    private Boolean isPaired;

    public SampleGEO(Boolean isPaired) {

        this.isPaired = isPaired;
    }

    public String getSampleName() {
        return sampleName;
    }

    public int getSampleNameLength() {
        return sampleName.length();
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public String getMolecule() {
        return molecule;
    }

    public void setMolecule(String molecule) {
        this.molecule = molecule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProcessedDataFile() {
        return processedDataFile;
    }

    public void setProcessedDataFile(String processedDataFile) {
        this.processedDataFile = processedDataFile;
    }

    public String getRawFile() {
        return rawFile;
    }

    public void setRawFile(String rawFile) {
        this.rawFile = rawFile;
    }

    public Map<String, String> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(Map<String, String> characteristics) {
        this.characteristics = characteristics;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private String checkNull(String s) {
        if (s == null)
            return ("Not specified");
        else
            return (s);
    }

    public String[] getSampleRow() {
        String line;
        StringBuilder charLabels = new StringBuilder();
        if (characteristics != null)
            for (String key : characteristics.keySet()) {
                charLabels.insert(0, "characteristics: " + key + "\t");
            }

        //   String header = "Sample name\ttitle\tsource name\torganism\t" + charLabels
        //         + "molecule\tdescription\tprocessed data file\traw file";

        StringBuilder charValues = new StringBuilder();
        if (characteristics != null)
            for (String key : characteristics.keySet()) {
                charValues.insert(0, characteristics.get(key) + "\t");
            }

        line =
                checkNull(sampleName) + "\t" + checkNull(title) + "\t" + checkNull(sourceName) + "\t" + checkNull(organism) + "\t"
                        + charValues + checkNull(molecule) + "\t" + checkNull(description) + "\t" + checkNull(processedDataFile) + "\t"
                        + checkNull(rawFile);
        line = line.replace("null", "");

        return line.split("\t");


    }

    public Boolean getPaired() {
        return isPaired;
    }

    public void setPaired(Boolean paired) {
        isPaired = paired;
    }
}