package model.geo;

import java.util.Map;

public class SampleGEO {

  private String code;
  private String sampleName, title, sourceName, organism, molecule, description, processedDataFile, rawFile;
  private Map<String, String> characteristics;

  public SampleGEO() {

  }

  public String getSampleName() {
    return sampleName;
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

  public String[] getSampleRow() {
    String charLabels = "";
    for (String key : characteristics.keySet()) {
      charLabels = "characteristics: " + key + "\t" + charLabels;
    }

    String header = "Sample name\ttitle\tsource name\torganism\t" + charLabels
        + "molecule\tdescription\tprocessed data file\traw file";

    String charValues = "";
    for (String key : characteristics.keySet()) {
      charValues = characteristics.get(key) + "\t" + charValues;
    }

    String line =
        sampleName + "\t" + title.replace(" ", "") + "\t" + sourceName + "\t" + organism + "\t"
            + charValues + molecule + "\t" + description + "\t" + processedDataFile + "\t"
            + rawFile;
    line = line.replace("null", "");
    return line.split("\t");
  }

}
