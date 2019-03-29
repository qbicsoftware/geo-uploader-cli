package life.qbic.cli.model.geo;

public class RawDataGEO {

    private String fileName,
            fileType,
            fileChecksum,
            instrumentModel,
            readLength,
            singleOrPairedEnd;

    public RawDataGEO() {

    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }

    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public String getInstrumentModel() {
        return instrumentModel;
    }

    public void setInstrumentModel(String instrumentModel) {
        this.instrumentModel = instrumentModel;
    }

    public String getReadLength() {
        return readLength;
    }

    public void setReadLength(String readLength) {
        this.readLength = readLength;
    }

    public String getSingleOrPairedEnd() {
        return singleOrPairedEnd;
    }

    public void setSingleOrPairedEnd(String singleOrPairedEnd) {
        this.singleOrPairedEnd = singleOrPairedEnd;
    }

    public String checkNull(String s) {
        if (s == null)
            return ("Not Specified");
        else
            return (s);
    }

    public String[] getRawFilesRow() {
        String line = checkNull(fileName) + "\t" + checkNull(fileType) + "\t" + checkNull(fileChecksum) + "\t" + checkNull(instrumentModel) + "\t"
                + checkNull(readLength) + "\t" + checkNull(singleOrPairedEnd);
        line = line.replace("null", "");
        return line.split("\t");
    }
}
