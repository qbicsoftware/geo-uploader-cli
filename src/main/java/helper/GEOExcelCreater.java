package helper;

import model.geo.RawDataGEO;
import model.geo.SampleGEO;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.util.*;


public class GEOExcelCreater {

    private String TEMPLATE_PATH = "/Users/spaethju/Documents/SRA-Upload-Prep/src/main/resources/geo_template.xls";
    private String OUT_PATH = "/Users/spaethju/Documents/SRA-Upload-Prep/src/main/resources/qbic.xls";

    public GEOExcelCreater(List<SampleGEO> samples, List<RawDataGEO> raws) throws IOException {
        InputStream in = new FileInputStream(TEMPLATE_PATH);

        //Get first sheet from the workbook
        Workbook wb = new HSSFWorkbook(in);
        HSSFSheet sheet = (HSSFSheet) wb.getSheetAt(0);

        addRawFilesRows(sheet, raws);
        adaptSampleHeader(sheet, samples);
        addSampleRows(sheet, samples);

        try {
            FileOutputStream out = new FileOutputStream(new File(OUT_PATH), false);
            wb.write(out);
            out.close();
            System.out.println("Excel written successfully! Good bye :-)");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void adaptSampleHeader(Sheet sheet, List<SampleGEO> samples) {
        Row sampleHeader = sheet.getRow(19);

        //For each row, iterate through each columns
        Iterator<Cell> cellIterator = sampleHeader.cellIterator();
        String charLabels = "";

        for (String key : samples.get(0).getCharacteristics().keySet()) {
            charLabels = "characteristics: " + key + "\t" + charLabels;
        }

        String header = "Sample name\ttitle\tsource name\torganism\t" + charLabels + "molecule\tdescription\tprocessed data file\traw file";
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
            for (int j = 0; j < sheet.getRow(19).getLastCellNum(); j++) {
                sheet.getRow(i + 20).createCell(j);
                sheet.getRow(i + 20).getCell(j).setCellValue(samples.get(i).getSampleRow()[j]);
            }

        }
    }

    private void addRawFilesRows(Sheet sheet, List<RawDataGEO> raw) {
        sheet.shiftRows(53, sheet.getLastRowNum(), raw.size() - 2);
        for (int i = 0; i < raw.size(); i++) {
            sheet.createRow(i + 53);
            for (int j = 0; j < sheet.getRow(52).getLastCellNum() - 1; j++) {
                sheet.getRow(i + 53).createCell(j);
                sheet.getRow(i + 53).getCell(j).setCellValue(raw.get(i).getRawFilesRow()[j]);
            }

        }
    }

}
