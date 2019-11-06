import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// data normalization
public class DataNormalization {
	static double[] min = new double[41];
	static double[] max = new double[41];

	public int dim = 2;
	public int lastrow = 11000;
	public String datasetName = "RandomSet11000";
	public String datasetDir = "dataset\\" + datasetName + ".xlsx";
	public String normalizedDataDir = "dataset\\" + datasetName + "_normalized.xlsx";

	public static void main(String[] args) throws IOException {
		DataNormalization dp = new DataNormalization();
		dp.readDataFromTxtFile();
		dp.normalizationTxt();
		// dp.readDataFromExcelFile();
		// dp.normalizationExcel();

	}

	public void normalizationTxt() throws IOException {
		File file = new File(datasetDir);
		BufferedReader reader = null;

		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet();
		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		int k = 0;

		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 0;
			while ((tempString = reader.readLine()) != null && line < lastrow) {
				String[] str = tempString.split(",");
				int m = 0;
				row = sheet.createRow(k++);
				for (int i = 0; i < str.length; i++) {
					double d = Double.parseDouble(str[i]);
					d = (d - min[i]) * 1.0 / (max[i] - min[i]);
					cell = row.createCell(m++);
					cell.setCellValue(d);
				}
				line++;
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

		System.out.println("Data have been normalized.");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(normalizedDataDir);
			try {
				wb.write(fos);
				wb.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	void normalizationExcel() throws IOException {
		Workbook wb = new XSSFWorkbook();
		Sheet write_sheet = wb.createSheet();

		CBILOF cb = new CBILOF();
		Workbook book = null;
		book = cb.getExcelWorkbook(datasetDir);
		Sheet read_sheet = cb.getSheetByNum(book, 0);
		Cell cell = null;

		for (int i = 0; i < lastrow; i++) {
			Row read_row = read_sheet.getRow(i);
			if (read_row != null) {
				Row write_row = write_sheet.createRow(i);
				for (int j = 0; j < dim; j++) {
					double temp_data = read_row.getCell(j).getNumericCellValue();
					temp_data = (temp_data - min[j]) * 1.0 / (max[j] - min[j]);
					cell = write_row.createCell(j);
					cell.setCellValue(temp_data);
				}
			}
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(normalizedDataDir);
			wb.write(fos);
			wb.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	public void readDataFromTxtFile() throws IOException {
		File file = new File(datasetDir);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 0;
			while ((tempString = reader.readLine()) != null && line < lastrow) {
				String[] str = tempString.split(",");
				for (int i = 0; i < str.length; i++) {
					double d = Double.parseDouble(str[i]);
					if (min[i] > d)
						min[i] = d;
					if (max[i] < d)
						max[i] = d;
				}
				line++;
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
	}

	public void readDataFromExcelFile() throws IOException {
		CBILOF cb = new CBILOF();
		Workbook book = null;
		book = cb.getExcelWorkbook(datasetDir);
		Sheet sheet = cb.getSheetByNum(book, 0);

		for (int i = 0; i < lastrow; i++) {
			Row row = null;
			row = sheet.getRow(i);
			if (row != null) {
				for (int j = 0; j < dim; j++) {
					double temp_data = row.getCell(j).getNumericCellValue();
					if (min[j] > temp_data)
						min[j] = temp_data;
					if (max[j] < temp_data)
						max[j] = temp_data;
				}
			}
		}
		System.out.println("maximum and minmum have been calculated");
	}

}
