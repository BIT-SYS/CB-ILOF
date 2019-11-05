import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Iterator;
import java.util.List;

public class CompareResult {
	private static int ki = 180;
	static final double gridlen = 0.05;
	static final int firstBatch = 25000;
	static final int lastrow = 50000;
	static final int streamNum = lastrow-firstBatch;
	static final int dim = 3;
	
	public String prefix = "D:\\BIT\\Paper\\Outlier-detection\\LOF_result_and_program\\LOF\\";
	public String datasetName = "smtp_test";
	public String dataset = prefix+"test-debug\\"+datasetName+".xlsx";
	
	// CB-ILOF
	public String outPointLOF = prefix+"Result\\point\\"+datasetName+"_gridCoordinateChange_point_my_outLof_"+ki+"_"+gridlen+"_"+firstBatch+"_"+streamNum+".xlsx";
	public String outAccuracy = prefix+"Result\\accuracy\\"+datasetName+"_gridCoordinateChange_my_accuracy_"+ki+"_"+gridlen+"_"+firstBatch+"_"+streamNum+".txt";
	
	// ILOF
//	public String outPointLOF = prefix+"Result\\LOF\\"+datasetName+"_outLof_"+ki+"_"+firstBatch+"_"+streamNum+".xlsx";
//	public String outAccuracy = prefix+"Result\\accuracy\\"+datasetName+"_accuracy_"+ki+"_"+firstBatch+"_"+streamNum+".txt";
    
    public static void main(String[] args) throws IOException{
    	CompareResult cp = new CompareResult();
    	cp.calAccuracy();
    }

    void calAccuracy() throws IOException
	{
		Workbook resultBook = null;
        resultBook = getExcelWorkbook(outPointLOF);
        Sheet resultSheet = getSheetByNum(resultBook, 0);
        Workbook compareBook = null;
        compareBook = getExcelWorkbook(dataset);
        Sheet compareSheet = getSheetByNum(compareBook, 0);
        File f1 = new File(outAccuracy);
        if (f1.exists() == false)
        	f1.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(f1);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos)); 

        // 测试多组LOF阈值对应的准确率
        for(double j = 0 ; j<=2 ; j+=0.2){
            double LOFNumber = 1.0 + j;	// LOF threshold
            int NormalError = 0;
            int NormalNum = 0;
            double NormalRate;
            int OutlierNum = 0;
            int OutlierError = 0;
            double OutlierRate;
            double Accuracy;
            
            // for other data sets
            for(int i = 0;i < streamNum;i ++){
                Row resultRow = resultSheet.getRow(i + 1);
                Row compareRow = compareSheet.getRow(i + firstBatch);
                if(compareRow.getCell(dim).getNumericCellValue( ) == 0){
                    NormalNum ++;
                    System.out.println(i);
                    if( LOFNumber < resultRow.getCell(dim+1).getNumericCellValue()){
                        NormalError ++;
                    }
                }
                else
                {
                    OutlierNum ++;
                    if( LOFNumber >= resultRow.getCell(dim+1).getNumericCellValue()){
                        OutlierError ++;
                    }
                }
            }
            
            if(NormalNum + OutlierNum != streamNum)	
            	System.out.println("Point error!");
            
            NormalRate = (double)(NormalNum-NormalError)/(double)(NormalNum);
            OutlierRate = (double)(OutlierNum-OutlierError)/(double)(OutlierNum);
            Accuracy = (double)(NormalNum-NormalError+OutlierNum-OutlierError)/(double)(streamNum);
            bw.write("===============LOF threshold = "+LOFNumber+"===============");
            bw.newLine();
            bw.write("NormalNum="+NormalNum+", NormalError="+NormalError+", NormalRate="+NormalRate);
            bw.newLine();
            bw.write("OutlierNum="+OutlierNum+", OutlierError="+OutlierError+", OutlierRate="+OutlierRate);
            bw.newLine();
            bw.write("Accuracy="+Accuracy);
            bw.newLine();
            bw.newLine();
        }
        bw.close();
        System.out.println("Calculation Accuracy Over.");
	}
    public Workbook getExcelWorkbook(String filePath) throws IOException {
        Workbook book = null;
        File file = null;
        FileInputStream fis = null;

        try {
            file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("Runtime Error!");
            } else {
                fis = new FileInputStream(file);
                book = WorkbookFactory.create(fis);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return book;
    }

    public Sheet getSheetByNum(Workbook book, int number) {
        Sheet sheet = null;
        try {
            sheet = book.getSheetAt(number);
            // if(sheet == null){
            // sheet = book.createSheet("Sheet"+number);
            // }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return sheet;
    }


}
