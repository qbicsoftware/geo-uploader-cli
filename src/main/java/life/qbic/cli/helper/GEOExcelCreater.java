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

    public GEOExcelCreater(List<SampleGEO> samples, List<RawDataGEO> raws, String outPath,
                           String fileName) throws IOException {
        XSSFWorkbook wb;

//MAKE SURE THAT TEMPLATE IS PRESENT IN SAME FOLDER AS THIS CLASS!!
        try (InputStream in = this.getClass().getResourceAsStream("/geo_template.xlsx")) {

            wb = new XSSFWorkbook(in);
        }
        //XSSFWorkbook wb = new XSSFWorkbook("geo_template.xlsx");

        //Get first sheet from the workbook

        Boolean pairedEnd = checkSingleOrPaired(raws);
        XSSFSheet sheet = wb.getSheetAt(0);
        if (pairedEnd) {
            System.out.println("Writing " + samples.size() / 2 + " Samples to Excel File ...");

        } else {


            System.out.println("Writing " + samples.size() + " Samples to Excel File ...");
        }
        //add paired end information and raw information for paired end experiments
        //method returns true if project contains paired ened data
        if (pairedEnd) {
            try {
                if (pairedEnd)
                    System.out.println("Found paired end data");
                addPairedEndFilesRow(sheet, raws, pairedEnd);
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

        String header = "Sample name\ttitle\tsource name\torganism\t" + charLabels
                + "molecule\tdescription\tprocessed data file\traw file";
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
        sheet.shiftRows(20, sheet.getLastRowNum(), samples.size() - 3);
        for (int i = 0; i < samples.size(); i++) {

            sheet.createRow(i + 20);
            try {
                for (int j = 0; j < sheet.getRow(19).getLastCellNum(); j++) {

                    sheet.getRow(i + 20).createCell(j);
                    sheet.getRow(i + 20).getCell(j).setCellValue(samples.get(i).getSampleRow()[j]);
                }
            } catch (ArrayIndexOutOfBoundsException e) {

            }

        }
    }


    private void addPairedEndFilesRow(Sheet sheet, List<RawDataGEO> raw, Boolean pairedEnd) {

        List rawMapList = computeRawMapIdentList(raw);
        Map<Object, ArrayList<Object>> rawMap = (Map<Object, ArrayList<Object>>) rawMapList.get(0);
        ArrayList<String> identList = (ArrayList<String>) rawMapList.get(1);


        sheet.shiftRows(60, sheet.getLastRowNum(), raw.size() - 2);
        int sampleNum;
        if (pairedEnd)
            sampleNum = identList.size() / 2;
        else
            sampleNum = identList.size();
        for (int i = 0; i < sampleNum; i++) {
            sheet.createRow(i + 60);
            try {


                sheet.getRow(i + 59).createCell(0);
                sheet.getRow(i + 59).getCell(0).setCellValue((String) rawMap.get(identList.get(i)).get(0));
                sheet.getRow(i + 59).createCell(1);
                sheet.getRow(i + 59).getCell(1).setCellValue((String) rawMap.get(identList.get(i)).get(1));


            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Index error. One sample may have no or only one raw file");

            } catch (IndexOutOfBoundsException e) {
                System.out.println("Index error. One sample may have no or only one raw file");
            }

        }


    }

    //Method returns true if project contains paired end data
    private void addRawFilesRows(Sheet sheet, List<RawDataGEO> raw) {

        List rawMapList = computeRawMapIdentList(raw);
        Map<Object, ArrayList<Object>> rawMap = (Map<Object, ArrayList<Object>>) rawMapList.get(0);
        ArrayList<String> identList = (ArrayList<String>) rawMapList.get(1);


//remove duplicates if any


        for (int i = 0; i < raw.size(); i++) {
            if (rawMap.get(identList.get(i)).size() > 1) {
                raw.get(i).setSingleOrPairedEnd("Paired End");
            } else {
                raw.get(i).setSingleOrPairedEnd("Single End");
            }

        }

        Set setItems = new LinkedHashSet(raw);
        raw.clear();
        raw.addAll(setItems);
        sheet.shiftRows(53, sheet.getLastRowNum(), raw.size() - 2);
        for (int i = 0; i < raw.size(); i++) {
            sheet.createRow(i + 53);
            for (int j = 0; j < 6; j++) {
                sheet.getRow(i + 53).createCell(j);
                try {
                    sheet.getRow(i + 53).getCell(j).setCellValue(raw.get(i).getRawFilesRow()[j]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }

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
        Comparator<RawDataGEO> compareByFileName = (RawDataGEO r1, RawDataGEO r2) -> r1.getFileName().compareTo(r2.getFileName());
        Collections.sort(raw, compareByFileName);

        Map<Object, ArrayList<Object>> rawMap = new HashMap<>();
        ArrayList<String> identList = new ArrayList<>();


        for (int i = 0; i < raw.size(); i++) {
            String name = raw.get(i).getFileName();
            String[] idents = name.split("_");
            try {
                String ident = idents[0] + "_" + idents[1] + "_" + idents[2] + "_" + idents[3];
                identList.add(ident);
                ArrayList<Object> rawList = new ArrayList<>();


                for (int j = 0; j < raw.size(); j++) {
                    String name2 = raw.get(j).getFileName();

                    if (name2.contains(ident) && name2 != name) {
                        rawList.add(name2);
                        rawMap.put(ident, rawList);
                    }
                }


            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }


        }

        List returnList = new ArrayList();
        returnList.add(rawMap);
        returnList.add(identList);

        return returnList;
    }


}
