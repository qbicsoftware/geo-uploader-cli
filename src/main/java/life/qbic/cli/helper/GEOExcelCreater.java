package life.qbic.cli.helper;

import life.qbic.cli.model.geo.RawDataGEO;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import life.qbic.cli.model.geo.SampleGEO;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class GEOExcelCreater {

  public GEOExcelCreater(List<SampleGEO> samples, List<RawDataGEO> raws, String outPath,
      String fileName) throws IOException, InvalidFormatException {
    //InputStream in = getClass().getResourceAsStream("/geo_template.xlsx");

    //Get first sheet from the workbook
    XSSFWorkbook wb;
    wb = new XSSFWorkbook(new File("/home/tlucas/IdeaProjects/geo-uploader-cli/src/main/resources/geo_template.xlsx"));
    XSSFSheet sheet = (XSSFSheet) wb.getSheetAt(0);

    System.out.println("Writing " + samples.size() + " Samples to Excel File ...");
    addRawFilesRows(sheet, raws);
    try {
      adaptSampleHeader(sheet, samples);
    } catch (IndexOutOfBoundsException e) {
      System.out.println("No samples found. Please check your input and try again.");
      System.exit(1);
    }
    try{
    addSampleRows(sheet, samples);}
    catch(ArrayIndexOutOfBoundsException e){}



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
    if(samples.get(0).getCharacteristics() != null)
    for (String key : samples.get(0).getCharacteristics().keySet()) {
      charLabels = "characteristics: " + key + "\t" + charLabels;
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
      for (int j = 0; j < sheet.getRow(19).getLastCellNum(); j++) {
        sheet.getRow(i + 20).createCell(j);
        System.out.println(samples.get(i).getSampleRow()[j]);
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
        try {
          sheet.getRow(i + 53).getCell(j).setCellValue(raw.get(i).getRawFilesRow()[j]);}

        catch(ArrayIndexOutOfBoundsException e){
          System.out.println("Array out of bounds");
        }
      }

    }
  }

}
