
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class LOFStreamProcess {
	private static int ki = 190;
	final int firstBatch = 25000;
	final int lastrow = 50000;
	final int streamNum = lastrow - firstBatch;
	final int dim = 34;
	
	public String prefix = "D:\\BIT\\Paper\\Outlier-detection\\LOF_result_and_program\\LOF\\";
	public String datasetName = "CUP_KDD_50000_daluan";
	public String dataset = prefix+"test-debug\\"+datasetName+".xlsx";
	public String outLOF = prefix+"Result\\LOF\\"+datasetName+"_outLof_"+ki+"_"+firstBatch+"_"+streamNum+".xlsx";
	public String outTime = prefix+"Result\\time\\"+datasetName+"_outTime_"+ki+"_"+firstBatch+"_"+streamNum+".xlsx";
	
	ArrayList<Long> begintime = new ArrayList<>();
	ArrayList<Long> endtime = new ArrayList<>();
	
	String resultSheet = ki + "-" + firstBatch;
	ArrayList<ArrayList<Double>> streamList = new ArrayList<ArrayList<Double>>();// 鏁版嵁娴佒�
	ArrayList<Node> dataList = new ArrayList<Node>();  //鏁版嵁閾�

	public static void main(String[] args) throws IOException {
		LOFStreamProcess sp = new LOFStreamProcess();
		sp.readData();
		sp.initLof();
		System.out.println("InitLof Over!!");
		System.in.read();
		sp.OutlierProcessing();
		sp.writeResult();
		sp.writeTimeResult();
	}
	
	public LOFStreamProcess(){
		dataList.clear();
		streamList.clear();
		begintime.clear();
		endtime.clear();
	}

	public void OutlierProcessing() {
		// 妯℃嫙鏂扮殑鏁版嵁鐐圭殑鍒版潵
		for (int i = 0; i < streamList.size(); i++) {
			System.out.println(i);
			begintime.add(System.currentTimeMillis());
//			double x = xList.get(i);
//			double y = yList.get(i);
			ArrayList<Double> com_data = streamList.get(i);
			int index = dataList.size();
			
			Node new_node = new Node();
			new_node.setIndex(index);
			new_node.setDataValue(com_data);
			dataList.add(new_node);

			// 璁＄畻knn
			List<relateNode> kdList = new ArrayList<relateNode>();
			for (int j = 0; j < index; j++) {
				ArrayList<Double> dl = dataList.get(j).getDataValue();
				relateNode rn = new relateNode();
				rn.setIndex(j);
				rn.setData(dl);
				rn.setDistance(getDis(com_data, dl));
				kdList.add(rn);
			}
			//鎺掑簭
			Collections.sort(kdList, new NodeDisComparator());
			//寰楀埌knn
			for (int k = 0; k < kdList.size() && k < ki; k++) {
				relateNode e = kdList.get(k);
				dataList.get(index).knn.add(e);
				//
				relateNode gadd = new relateNode();
				gadd.setIndex(index);
				gadd.setData(com_data);
				dataList.get(e.getIndex()).krnn.add(gadd);
			}

			//璁＄畻鍏跺彲杈捐窛绂�
			int kmin = Math.min(ki, kdList.size()) - 1;
//			if (kdList.size() == 0)
//			{
//				dataList.get(index).setReachDis(0);
//			}
//			else
//			{
//				dataList.get(index).setReachDis(kdList.get(kmin).getDistance());
//			}
			dataList.get(index).setReachDis(kdList.get(kmin).getDistance());

			// 璁＄畻krnn
			for (int j = 0; j < index; j++) {
				ArrayList<Double> krd = dataList.get(j).getDataValue();
				if (dataList.get(j).getReachDis() > getDis(krd, com_data)) {
					relateNode rn = new relateNode();
					rn.setIndex(j);
					rn.setData(krd);
					dataList.get(index).krnn.add(rn);

					// 鏇存柊j鐨刱nn
					relateNode addn = new relateNode();
					addn.setIndex(index);
					addn.setData(com_data);
					addn.setDistance(getDis(krd, com_data));
					dataList.get(j).knn.add(addn);
					Collections.sort(dataList.get(j).knn, new NodeDisComparator());
					if (dataList.get(j).knn.size() <= ki)
					{
						int lastone = dataList.get(j).knn.size()-1;
						dataList.get(j).setReachDis(dataList.get(j).knn.get(lastone).getDistance());
						continue;
					}
					int lasti = dataList.get(j).knn.size() - 1;
					relateNode deleten = dataList.get(j).knn.remove(lasti);
					//j鐨刱nn绉昏蛋浜嗕竴涓猯asti锛屽垯lasti鐨刱rnn搴旇绉绘帀j
					int delid = deleten.getIndex();
					for(int m = 0; m < dataList.get(delid).krnn.size(); m++){
						if(dataList.get(delid).krnn.get(m).getIndex() == j){
							dataList.get(delid).krnn.remove(m);
							break;
						}
					}
					lasti = dataList.get(j).knn.size() - 1;
					// 鏇存柊j鐨勫彲杈捐窛绂�
					dataList.get(j).setReachDis(dataList.get(j).knn.get(lasti).getDistance());
				}
				else if (dataList.get(j).knn.size() < ki)
				{
					relateNode rn = new relateNode();
					rn.setIndex(j);
					rn.setData(krd);
					dataList.get(index).krnn.add(rn);

					// 鏇存柊j鐨刱nn
					relateNode addn = new relateNode();
					addn.setIndex(index);
					addn.setData(com_data);
					addn.setDistance(getDis(krd, com_data));
					dataList.get(j).knn.add(addn);
					Collections.sort(dataList.get(j).knn, new NodeDisComparator());
				}
			}

			//瀵逛簬鏂版潵鐨勭偣鐨刱nn涓殑姣忎竴涓偣锛岃绠梤each-dist鍜宺each-distTo
			for (int j = 0; j < dataList.get(index).knn.size(); j++) {
				relateNode gi = dataList.get(index).knn.get(j);
				int ii = gi.getIndex();
				double rd = Math.max(dataList.get(ii).getReachDis(), getDis(gi.getData(),com_data));
				dataList.get(index).knn.get(j).setReachdist(rd);
			}

			ArrayList<relateNode> update_lrd = new ArrayList<relateNode>();
			update_lrd.addAll(dataList.get(index).krnn);
			for (int j = 0; j < dataList.get(index).krnn.size(); j++) {
				relateNode gj = dataList.get(index).krnn.get(j);
				int ii = gj.getIndex();
				for (int k = 0; k < dataList.get(ii).knn.size(); k++) {
					relateNode gsmall = dataList.get(ii).knn.get(k);
					if (gsmall.getIndex() != index) {
						dataList.get(ii).knn.get(k).setReachdistTo(dataList.get(ii).getReachDis());
					}
					if (hasNode(dataList.get(gsmall.getIndex()).knn, ii)) {
						update_lrd.add(gsmall);
					}
				}
			}
			// 锟斤拷锟斤拷lrd
			ArrayList<relateNode> update_LOF = new ArrayList<relateNode>();
			update_LOF.addAll(update_lrd);
			for (int m = 0; m < update_lrd.size(); m++) {
				relateNode gm = update_lrd.get(m);
				int im = gm.getIndex();
				// 锟斤拷锟斤拷lrd
				dataList.get(im).setLrd(calculateLrd(im));
				update_LOF.addAll(dataList.get(im).krnn);
			}
			// 锟斤拷锟节硷拷锟斤拷update_LOF锟叫碉拷锟斤拷锟今，革拷锟斤拷锟斤拷锟斤拷LOF
			for (int l = 0; l < update_LOF.size(); l++) {
				relateNode gl = update_LOF.get(l);
				int il = gl.getIndex();
				dataList.get(il).setLof(calculateLof(il));
			}
			// 锟斤拷锟斤拷锟铰诧拷锟斤拷锟斤拷lrd
			dataList.get(index).setLrd(calculateLrd(index));
			// 锟斤拷锟斤拷锟铰诧拷锟斤拷锟斤拷lof
			dataList.get(index).setLof(calculateLof(index));

			endtime.add(System.currentTimeMillis());
		}
	}

	// 璁＄畻lrd
	double calculateLrd(int index) {
		double sum_lrd = 0.0;
		for (int i = 0; i < dataList.get(index).knn.size(); i++) {
			relateNode gmi = dataList.get(index).knn.get(i);
			sum_lrd += gmi.getReachdist();
		}
		
		if (sum_lrd == 0.0)
			sum_lrd = ki;
		return ki / sum_lrd;
	}

	// 璁＄畻LOF
	double calculateLof(int index) {
		double suml = 0.0;
		for (int o = 0; o < dataList.get(index).knn.size(); o++) {
			relateNode glo = dataList.get(index).knn.get(o);
			suml += dataList.get(glo.getIndex()).getLrd();
		}
		return suml / ki / dataList.get(index).getLrd();
	}

	boolean hasNode(List<relateNode> glist, int x) {
		for (relateNode g : glist) {
			if (g.getIndex() == x) {
				return true;
			}
		}
		return false;

	}

	//寰楀埌涓や釜鐐逛箣闂寸殑璺濈
	double getDis(ArrayList<Double> a1, ArrayList<Double> a2) {
		double sum = 0;
		for(int i = 0; i < a1.size() && i < a2.size(); i++){
			sum += (a1.get(i) - a2.get(i)) * (a1.get(i) - a2.get(i));
		}
		return Math.sqrt(sum);
	}

	//瀵逛簬璇荤殑绗竴鎵规暟鎹紝杩涜LOF妫�娴�
	void initLof() {
		// 璁＄畻knn鍜宬rnn
		System.out.println("initLof");
		for (int i = 0; i < dataList.size(); i++) {
			System.out.println(i);
			if (dataList.size() == 1)
			{
				dataList.get(i).setReachDis(0.0);
				dataList.get(i).setLrd(1.0);
				dataList.get(i).setLof(5.0);
			}
			List<relateNode> kdList = new ArrayList<relateNode>();
			kdList.clear();
			for (int j = 0; j < dataList.size(); j++) {
				if (i == j)
					continue;
				relateNode rn = new relateNode();
				rn.setIndex(j);
				ArrayList<Double> tdata = dataList.get(j).getDataValue();
				rn.setData(tdata);
				rn.setDistance(getDis(tdata, dataList.get(i).getDataValue()));
				kdList.add(rn);
			}
			
			Collections.sort(kdList, new NodeDisComparator());

			// 璁＄畻knn, krnn
			for (int k = 0; k < ki && k < kdList.size(); k++) {
				relateNode newnode = new relateNode();
				newnode.setIndex(kdList.get(k).getIndex());
				newnode.setData(kdList.get(k).getData());
				dataList.get(i).knn.add(newnode);
				
				relateNode gadd = new relateNode();
				gadd.setIndex(i);
				gadd.setData(dataList.get(i).getDataValue());
				dataList.get(kdList.get(k).getIndex()).krnn.add(gadd);
			}

			// 鏇存柊鐐圭殑鍙揪璺濈
			int kmin = Math.min(ki, kdList.size()) - 1;
			dataList.get(i).setReachDis(kdList.get(kmin).getDistance());
		}
		for (int i = 0; i < dataList.size(); i++) {
			// 瀵规瘡涓�涓偣鐨刱杩戦偦鐐癸紝鏇存柊鍏跺埌杩欎釜鐐圭殑璺濈锛屽彲杩欎釜鐐瑰埌k杩戦偦鐐圭殑璺濈
			for (int j = 0; j < dataList.get(i).knn.size(); j++) {
				int in = dataList.get(i).knn.get(j).getIndex();
				double rd = getDis(dataList.get(i).getDataValue(),dataList.get(in).getDataValue());
				dataList.get(i).knn.get(j).setReachdist(Math.max(dataList.get(in).getReachDis(), rd));
//				dataList.get(i).knn.get(j).setReachdistTo(Math.max(dataList.get(i).getReachDis(), rd));
			}
		}
		//璁＄畻Lrd
		for (int i = 0; i < dataList.size(); i++) {
			dataList.get(i).setLrd(calculateLrd(i));
		}
		//璁＄畻lof
		for (int i = 0; i < dataList.size(); i++) {
			dataList.get(i).setLof(calculateLof(i));
		}
	}

	void readData() throws IOException {
		Workbook book = null;
		book = getExcelWorkbook(dataset);
		Sheet sheet = getSheetByNum(book, 0);
		//璇荤涓�鎵规暟鎹�
		for (int i = 0; i < firstBatch; i++) { 
			System.out.println("aaaaaa" + i);
			Row row = null;
			row = sheet.getRow(i);
			if (row != null) {
				Node e = new Node();
				e.setIndex(i);
				ArrayList<Double> perdata = new ArrayList<>();
				perdata.clear();
				for(int j = 0; j < dim; j++){
					perdata.add(row.getCell(j).getNumericCellValue());
				}
				e.setDataValue(perdata);
				dataList.add(e);
			}
		} 
		//缂撳瓨浣欎笅鐨勬暟鎹�
		for (int i = firstBatch; i < lastrow; i++) {
			System.out.println("aaa" + i);
			Row row = null;
			row = sheet.getRow(i);
			if (row != null) {
				ArrayList<Double> perdata = new ArrayList<>();
				perdata.clear();
				for(int j = 0; j < dim; j++){
					perdata.add(row.getCell(j).getNumericCellValue());
				}
				streamList.add(perdata);
			}
		}
		System.out.println("streamlist size is " + streamList.size());
		System.out.println("datalist size is " + dataList.size());
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

	public Workbook getExcelWorkbook(String filePath) throws IOException {
		Workbook book = null;
		File file = null;
		FileInputStream fis = null;

		try {
			file = new File(filePath);
			if (!file.exists()) {
				throw new RuntimeException("锟侥硷拷锟斤拷锟斤拷锟斤拷");
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

	void writeResult() throws IOException {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet(resultSheet);		
		Row row = sheet.createRow(0);
		Cell cell;
		for (int i = 0; i < dim; i++)
		{
			cell = row.createCell(i);
			cell.setCellValue("x"+i);
		}
		cell = row.createCell(dim);
		cell.setCellValue("lrd");
		cell = row.createCell(dim+1);
		cell.setCellValue("LOF");
		int k = 1;
		for (int i = firstBatch; i < dataList.size(); i++) {
			row = sheet.createRow(k++);
			for (int m = 0; m < dim; m++) {
				cell = row.createCell(m);
				cell.setCellValue(dataList.get(i).getDataValue().get(m));
			}
			cell = row.createCell(dim);
			cell.setCellValue(dataList.get(i).getLrd());
			cell = row.createCell(dim+1);
			cell.setCellValue(dataList.get(i).getLof());
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outLOF);
			wb.write(fos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}
	
	void writeTimeResult() throws IOException {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet(resultSheet);
		Row row = sheet.createRow(0);

		Cell cell = row.createCell(0);
		cell.setCellValue("start");
		cell = row.createCell(1);
		cell.setCellValue("end");
		int k = 1;
		for (int i = 0; i < begintime.size(); i++) {
			row = sheet.createRow(k++);
			for (int m = 0; m < 2; m++) {
				cell = row.createCell(m);
				if (m == 0) {
					cell.setCellValue(begintime.get(i));
				} else if (m == 1) {
					cell.setCellValue(endtime.get(i));
				}
			}
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outTime);
			wb.write(fos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}
}

class NodeDisComparator implements Comparator<relateNode> {
	public int compare(relateNode A, relateNode B) {
		// return A.getDistance() - B.getDistance() < 0 ? -1 : 1;
		if ((A.getDistance() - B.getDistance()) < 0)
			return -1;
		else if ((A.getDistance() - B.getDistance()) > 0)
			return 1;
		else
			return 0;
	}
}
