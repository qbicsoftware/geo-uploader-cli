package life.qbic.cli.helper;

import life.qbic.cli.model.geo.RawDataGEO;
import life.qbic.cli.model.geo.SampleGEO;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

public class GEOExcelCreater {

    private Boolean pairedEnd;
    private List rawMapList;
    private int numSamples;

    public GEOExcelCreater(List<SampleGEO> samples, List<RawDataGEO> raws, String outPath,
                           String fileName) throws IOException {

        rawMapList = computeRawMapIdentList(raws);
        Map<Object, ArrayList<Object>> rawNameMap = (Map<Object, ArrayList<Object>>) rawMapList.get(0);


        //determine if samples are paired end or not and set accordingly
        for (SampleGEO sample : samples) {

            if (rawNameMap.get(getIdentFromRawName(sample.getRawFile())).size() > 1) {
                sample.setPaired(true);
            }
        }

        XSSFWorkbook wb;

//MAKE SURE THAT TEMPLATE IS PRESENT IN SAME FOLDER AS THIS CLASS!!
        try (InputStream in = this.getClass().getResourceAsStream("/geo_template.xlsx")) {

            wb = new XSSFWorkbook(in);
        }
        //XSSFWorkbook wb = new XSSFWorkbook("geo_template.xlsx");

        //Get first sheet from the workbook

        //determine if there are any paired end reads at all
        pairedEnd = checkSingleOrPaired(raws);

        XSSFSheet sheet = wb.getSheetAt(0);
        if (pairedEnd) {
            System.out.println("Writing " + numSamples + " Samples to Excel File ...");

        } else {


            System.out.println("Writing " + numSamples + " Samples to Excel File ...");
        }
        //add paired end information and raw information for paired end experiments
        //method returns true if project contains paired ened data
        if (pairedEnd) {
            try {
                if (pairedEnd)
                    System.out.println("Found paired end data");
                addPairedEndFilesRow(sheet, raws);
                addRawFilesRows(sheet, raws);


            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("No samples found. Please check your input and try again.");
            }
        } else {
            // if the project is not paired end then only add the raw file infos
            addRawFilesRows(sheet, raws);
        }
        try {
            adaptSampleHeader(sheet, samples);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("No samples found. Please check your input and try again.");

        }


        try {
            addSampleRows(sheet, samples);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No samples found. Please check your input and try again.");
            e.printStackTrace();
        }


        try {
            FileOutputStream out = new FileOutputStream(new File(outPath + fileName + ".xlsx"), false);
            wb.write(out);
            out.close();
            System.out.println("Your file was written successfully! Good bye :-)");


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void adaptSampleHeader(Sheet sheet, List<SampleGEO> samples)
            throws IndexOutOfBoundsException {
        Row sampleHeader = sheet.getRow(19);

        //For each row, iterate through each columns
        Iterator<Cell> cellIterator = sampleHeader.cellIterator();
        String charLabels = "";
        if (samples.get(0).getCharacteristics() != null)
            for (String key : samples.get(0).getCharacteristics().keySet()) {
                charLabels = MessageFormat.format("characteristics: {0}\t{1}", key, charLabels);
            }
        String header;
        if (!pairedEnd) {
            header = "Sample name\ttitle\tsource name\torganism\t" + charLabels
                    + "molecule\tdescription\tprocessed data file\traw file";
        } else {
            header = "Sample name\ttitle\tsource name\torganism\t" + charLabels
                    + "molecule\tdescription\tprocessed data file\traw file\traw file";
        }

        String[] headerArray = header.split("\t");
        ArrayUtils.reverse(headerArray);
        List<String> headerElements = new ArrayList<>(Arrays.asList(headerArray));
        Stack<String> stack = new Stack<>(); //2 Create new stack
        stack.addAll(headerElements); //3 Add all items from the List to the stack.
        while (cellIterator.hasNext()) {

            Cell cell = cellIterator.next();
            cell.setCellValue("");
            if (!stack.empty()) {
                cell.setCellValue(stack.pop());
            }
        }
    }

    private void addSampleRows(Sheet sheet, List<SampleGEO> samples) {
        List<String> sampleIdentList = new ArrayList<>();
        Map<String, List<String>> sampleIdentMap = new HashMap<>();
        Map<String, SampleGEO> sampleMap = new HashMap<>();

        int numCharacteristicsMax = 0;
        List<Integer> characteristicsNumList = new ArrayList<>();


        for (SampleGEO sample : samples) {

            if (sample.getCharacteristics() != null)
                characteristicsNumList.add(sample.getCharacteristics().size());


            if (!sampleMap.containsKey(sample.getRawFile())) {


                sampleMap.put(sample.getSampleName(), sample);
            }
            try {
                numCharacteristicsMax = Collections.max(characteristicsNumList);
            } catch (NoSuchElementException e) {

            }

            if (!sampleIdentMap.containsKey(sample.getRawFile())) {
                sampleIdentList.add(sample.getRawFile());
                if (sample.getPaired()) {
                    if (sampleIdentList.size() == 2) {
                        sampleIdentMap.put(sample.getSampleName(), new ArrayList<>(sampleIdentList));
                        sampleIdentList.clear();
                    }
                } else {
                    if (sampleIdentList.size() == 1) {
                        sampleIdentMap.put(sample.getSampleName(), new ArrayList<>(sampleIdentList));
                        sampleIdentList.clear();
                    }

                }


            }
        }

        List<SampleGEO> sampleList = new ArrayList(sampleMap.values());

        //  for (SampleGEO sample : sampleList) {
        //     sample.setSampleName(sample.getSampleName().split(" ")[0]+"0"+sample.getSampleName().split(" ")[1]);
        // }


        Comparator<SampleGEO> compareByFileName = Comparator.comparingInt(SampleGEO::getSampleNameLength).thenComparing(SampleGEO::getSampleName);
        Collections.sort(sampleList, compareByFileName);


        sheet.shiftRows(20, sheet.getLastRowNum(), sampleList.size() - 3);
        for (int i = 0; i < sampleIdentMap.size(); i++) {

            sheet.createRow(i + 20);
            int addToColIndex = 0;
            int numCharacteristics;
            int lastColIndex;
            int lastColIndexSum;

            try {
                lastColIndex = sampleList.get(i).getSampleRow().length;

                if (sampleList.get(i).getCharacteristics() != null) {
                    numCharacteristics = sampleList.get(i).getCharacteristics().size();
                } else
                    numCharacteristics = 0;


                if (numCharacteristics < numCharacteristicsMax) {
                    addToColIndex = numCharacteristicsMax - numCharacteristics;

                }

                lastColIndexSum = lastColIndex + addToColIndex;

                Map<String, String> bufferCharacteristics = new HashMap<>();
                for (int l = 0; l < addToColIndex; l++) {
                    bufferCharacteristics.put("" + l, "");
                }
                if (sampleList.get(i).getCharacteristics() == null)
                    sampleList.get(i).setCharacteristics(bufferCharacteristics);

                for (int j = 0; j < lastColIndexSum; j++) {
                    sheet.getRow(i + 20).createCell(j);
                    sheet.getRow(i + 20).getCell(j).setCellValue(sampleMap.get(sampleList.get(i).getSampleName()).getSampleRow()[j]);
                    if (j == lastColIndexSum - 1) {
                        sheet.getRow(i + 20).createCell(j);
                        sheet.getRow(i + 20).getCell(j).setCellValue(sampleIdentMap.get(sampleList.get(i).getSampleName()).get(0));
                        if (sampleList.get(i).getPaired()) {
                            sheet.getRow(i + 20).createCell(j + 1);
                            sheet.getRow(i + 20).getCell(j + 1).setCellValue(sampleIdentMap.get(sampleList.get(i).getSampleName()).get(1));
                        }


                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {

            }

        }
    }


    private void addPairedEndFilesRow(Sheet sheet, List<RawDataGEO> raw) {

        //  Comparator<ArrayList<String>> compareByFileName = Comparator.comparing(String);
        // Collections.sort(raw, compareByFileName);

        List<String> R1 = new ArrayList<>();
        List<String> R2 = new ArrayList<>();
        List rawMapList = computeRawMapIdentList(raw);
        Map<Object, ArrayList<Object>> rawMap = (Map<Object, ArrayList<Object>>) rawMapList.get(0);
        ArrayList<String> identList = (ArrayList<String>) rawMapList.get(1);
        //delete duplicates from ident list
        Set<String> set = new HashSet<>(identList);
        identList.clear();
        identList.addAll(set);

        sheet.shiftRows(60, sheet.getLastRowNum(), rawMap.size() - 2);
        numSamples = rawMap.size();
        int j = 0;
        for (int i = 0; i < numSamples; i++) {

            //only write paired end samples
            if (rawMap.get(identList.get(i)).size() > 1) {
                sheet.createRow(j + 59);
                try {

                    R1.add((String) rawMap.get(identList.get(i)).get(0));
                    R2.add((String) rawMap.get(identList.get(i)).get(1));


                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Index error. One sample may have no or only one raw file");

                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Index error. One sample may have no or only one raw file");
                }
                j++;
            }


        }

        Collections.sort(R1);
        Collections.sort(R2);

        for (int i = 0; i < R1.size(); i++) {
            sheet.getRow(i + 59).createCell(0);
            sheet.getRow(i + 59).getCell(0).setCellValue(R1.get(i));

            sheet.getRow(i + 59).createCell(1);
            sheet.getRow(i + 59).getCell(1).setCellValue(R2.get(i));


            // sheet.getRow(j + 59).createCell(0);
            //sheet.getRow(j + 59).getCell(0).setCellValue((String) rawMap.get(identList.get(i)).get(0));

            //sheet.getRow(j + 59).createCell(1);
            //sheet.getRow(j + 59).getCell(1).setCellValue((String) rawMap.get(identList.get(i)).get(1));
        }


    }

    //Method returns true if project contains paired end data

    private void addRawFilesRows(Sheet sheet, List<RawDataGEO> raw) {

        Map<Object, ArrayList<Object>> rawNameMap = (Map<Object, ArrayList<Object>>) rawMapList.get(0);
        ArrayList<String> identList = (ArrayList<String>) rawMapList.get(1);
        Map<Object, RawDataGEO> rawMap = (Map<Object, RawDataGEO>) rawMapList.get(2);


//remove duplicates if any


        for (int i = 0; i < raw.size(); i++) {
            if (rawNameMap.get(identList.get(i)).size() > 1) {
                raw.get(i).setSingleOrPairedEnd("Paired End");
            } else {
                raw.get(i).setSingleOrPairedEnd("Single End");
            }

        }

        Set setItems = new LinkedHashSet(raw);
        raw.clear();
        raw.addAll(setItems);

        List<String> identListWithoutDuplicates = new ArrayList<>(identList);

        Set setItems2 = new LinkedHashSet(identListWithoutDuplicates);
        identListWithoutDuplicates.clear();
        identListWithoutDuplicates.addAll(setItems2);

        int numRaws = 0;

        for (int i = 0; i < rawNameMap.size(); i++) {
            numRaws += rawNameMap.get(identListWithoutDuplicates.get(i)).size();

        }


        sheet.shiftRows(53, sheet.getLastRowNum(), numRaws + 2);
        addRawFilesRowsSinglePaired(sheet, raw);

    }

    //creates and adds information to raw file rows
    private void addRawFilesRowsSinglePaired(Sheet sheet, List<RawDataGEO> raws) {
        //List<RawDataGEO> rawList = new ArrayList<RawDataGEO>(rawMap.values());
        int i = 0;
        List<String> added = new ArrayList<>();
        for (RawDataGEO raw : raws) {
            sheet.createRow(i + 53);
            for (int j = 0; j < 6; j++) {
                sheet.getRow(i + 53).createCell(j);
                try {

                    sheet.getRow(i + 53).getCell(j).setCellValue(raw.getRawFilesRow()[j]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            if (!added.contains(raw.getFileName()))
                i++;
            added.add(raw.getFileName());
        }


    }


    private Boolean checkSingleOrPaired(List<RawDataGEO> raws) {
        Boolean R1 = false;
        Boolean R2 = false;
        for (int i = 0; i < raws.size(); i++) {
            if (raws.get(i).getFileName().contains("_R1"))
                R1 = true;
            if (raws.get(i).getFileName().contains("_R2"))
                R2 = true;
            if (R1 && R2)

                break;

        }
        return R1 && R2;

    }

    //computes map with both corresponding reads for paired end samples or just one for single end samples
    //returns list with this map and identifier list used by function addPairedEndFilesRow()
    private List computeRawMapIdentList(List<RawDataGEO> raw) {


        //Sort list lexicographically
        Comparator<RawDataGEO> compareByFileName = Comparator.comparing(RawDataGEO::getFileName);
        Collections.sort(raw, compareByFileName);

        Map<Object, ArrayList<Object>> identRawNameMap = new HashMap<>();
        Map<Object, RawDataGEO> identRawMap = new HashMap<>();
        ArrayList<String> identList = new ArrayList<>();


        for (int i = 0; i < raw.size(); i++) {
            String name = raw.get(i).getFileName();
            String ident = getIdentFromRawName(name);
            try {
                identList.add(ident);
                identRawMap.put(ident, raw.get(i));
                ArrayList<Object> rawList = new ArrayList<>();


                for (int j = 0; j < raw.size(); j++) {
                    String name2 = raw.get(j).getFileName();

                    if (name2.contains(ident) && !rawList.contains(name2) && !rawList.contains(name)) {
                        rawList.add(name2);
                        identRawNameMap.put(ident, rawList);
                    }
                }


            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }


        }

        List returnList = new ArrayList();
        returnList.add(identRawNameMap);
        returnList.add(identList);
        returnList.add(identRawMap);

        return returnList;
    }

    public String getIdentFromRawName(String rawName) {
        String[] idents;
        String ident;
        try {
            idents = rawName.split("_");
            ident = idents[0] + "_" + idents[1] + "_" + idents[2] + "_" + idents[3];
        } catch (
                ArrayIndexOutOfBoundsException e) {
            ident = "";
            e.printStackTrace();
        }
        return ident;
    }


}

