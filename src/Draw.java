import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class Draw {
    String fileName = "/home/zzy/Documents/LOF/test-debug/kddcup.data_10_percent_corrected";
    String outFileName = "/home/zzy/Documents/LOF/symbol.xlsx";

    public static void main(String[] args){
        Draw draw = new Draw();
        File file = new File(draw.fileName);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("symbol");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("symbol");


        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s;
            int index = 1;
            while((s = br.readLine()) != null){
                String[] strs = s.split(",");
                String symbol = strs[strs.length-1];
                Row row1 = sheet.createRow(index++);
                Cell cell1 = row1.createCell(0);
                cell1.setCellValue(symbol.equals("normal.")?"normal":"unnormal");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(draw.outFileName);
            workbook.write(fos);
            fos.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
