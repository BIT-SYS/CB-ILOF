

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class dataprocessing {
	static double[] min = new double[41];
	static double[] max = new double[41];

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		dataprocessing dp = new dataprocessing();
		dp.readDataFromFile();
		
		File file = new File("kddcup.newtestdata_10_percent_unlabeled");
        BufferedReader reader = null;
        
        Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet0");
		Row row = sheet.createRow(0);

		Cell cell = row.createCell(0);
		int k = 0;
		
        try {
        //    System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            int line = 0;
            while ((tempString = reader.readLine()) != null && line < 50000) {
            	System.out.println(line++);
                // 显示行号
//                System.out.println("line " + line + ": " + tempString);
            	String[] str = tempString.split(",");
            	int m = 0;
            	row = sheet.createRow(k++);
            	for(int i = 0; i < str.length; i++){
            		if(i == 1 || i == 2) continue;
            		if(i == 3 || i == 6) continue;
            		if(i == 11 || i == 20) continue;
            		if(i == 21) continue;
            		double d = Double.parseDouble(str[i]); 	
            		if(i != 19){
            			d = (d - min[i]) * 1.0 / (max[i] - min[i]);
            			System.out.println(d);
            		}
        			cell = row.createCell(m++);
        			cell.setCellValue(d);
        		}
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    
	

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("KDD_50000.xlsx");
			wb.write(fos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		
	    System.out.println("over!!!!!");

	}
	//读取数据文件，获得每个数值属性的最大值和最小值
	public void readDataFromFile() throws IOException {
		File file = new File("kddcup.newtestdata_10_percent_unlabeled");
		
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 0;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null && line < 50000) {
                // 显示行号
//                System.out.println("line " + line + ": " + tempString);
            	String[] str = tempString.split(",");
            	for(int i = 0; i < str.length; i++){
            		if(i > 0 && i < 4) continue;
            		double d = Double.parseDouble(str[i]);
            		if(min[i] > d) min[i] = d;
            		if(max[i] < d) max[i] = d;
            	}
                line++;
            }
            //System.out.println(line);
            //for(int i = 0; i < 41; i++){
            //	System.out.println(max[i] + ", " + min[i]);
           // }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        System.out.println("over");
	}


}
