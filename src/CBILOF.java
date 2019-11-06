import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CBILOF {
	static final int ki = 180; // k value
	static final int initialNum = 20660; // number of data points detected in initialization
	static final int lastrow = 45660; // number of data points
	static final int streamNum = lastrow - initialNum; // number of data points detected in streaming manner
	static final int dim = 9; // dimension of dataset
	static final double gridlen = 0.1; // length of grid
	static final double threshold = 1.0; // threshold of LOF

	public String datasetName = "shuttle_test";
	public String datasetDir = "dataset\\" + datasetName + ".xlsx";
	public String param = ki + "_" + gridlen + "_" + initialNum + "_" + streamNum;
	public String outTime = "result\\" + datasetName + "_CB-ILOF_outTime_" + param + ".xlsx";
	public String outPointLOF = "result\\" + datasetName + "_CB-ILOF_outPointLof_" + param + ".xlsx";
	public String outAccuracy = "result\\" + datasetName + "_CB-ILOF_outAccuracy_" + param + "_" + threshold + ".txt";
	public String outPreError = "result\\" + datasetName + "_CB-ILOF_outPreError_" + param + "_" + threshold + ".xlsx";

	ArrayList<GridNode> gridlist = new ArrayList<>();
	HashMap<Integer, Integer> gridmap = new HashMap<>();
	ArrayList<ArrayList<Double>> streamList = new ArrayList<ArrayList<Double>>(); // grids tested in streaming manner

	ArrayList<Long> begintime = new ArrayList<>();
	ArrayList<Long> endtime = new ArrayList<>();

	ArrayList<Integer> label = new ArrayList<Integer>();
	ArrayList<Integer> datasizeId = new ArrayList<Integer>();
	ArrayList<Double> errorRate = new ArrayList<Double>();

	public static void main(String[] args) throws IOException {
		CBILOF sp = new CBILOF();
		sp.readData();
		sp.initGrid();
		sp.streamProcessing();
		sp.writePointLof();
		sp.writeTime();
		sp.calAccuracy();
		sp.writeError();
	}

	public CBILOF() {
		gridlist.clear();
		streamList.clear();
		begintime.clear();
		endtime.clear();
		gridmap.clear();
	}

	// If the cube to which the new point is mapped does not exist
	void isNewGrid(int stream_i) {
		int ind = gridlist.size();
		ArrayList<Double> com_data = streamList.get(stream_i);

		GridNode gnew = new GridNode();
		gnew.setIndex(ind);
		gnew.setGridcoor(com_data);
		gnew.density = 1;
		gridlist.add(gnew);

		// if initialNum=0
		if (ind == 0) {
			assignValue(gnew);
			return;
		}

		calKNN(gnew);

		// compute the kRNN
		calKRNN(gnew);
	}

	// calculate the kRNN of grid gn
	void calKRNN(GridNode gn) {
		int ind = gn.getIndex();
		ArrayList<Double> com_data = gn.getGridcoor();

		for (int j = 0; j < ind; j++) {
			GridNode gci = gridlist.get(j);
			double rd = getDistance(gci.getGridcoor(), com_data);

			if (gci.density >= ki)
				continue;
			else {
				if (gci.getReachDis() == 0.0) {
					// only have two grids
					grid g = new grid();
					g.setIndex(j);
					g.setCoorvalue(gci.getGridcoor());
					gridlist.get(ind).krnn.add(g);

					grid addg = new grid();
					addg.setIndex(ind);
					addg.setCoorvalue(com_data);
					addg.setDistance(rd);
					gridlist.get(j).knn.add(addg);

					addg.setReachdist(rd);
					addg.setScore((double) Math.min(ki - gci.density, gn.density) / (double) gn.density);
					gridlist.get(j).setReachDis(rd);
				} else {
					if (gci.getReachDis() > rd) {
						grid g = new grid();
						g.setIndex(j);
						g.setCoorvalue(gci.getGridcoor());
						gn.krnn.add(g);

						grid addg = new grid();
						addg.setIndex(ind);
						addg.setCoorvalue(com_data);
						addg.setDistance(rd);
						addg.setScore(1.0);
						addg.setReachdist(gci.getReachDis());
						gci.knn.add(addg);

						Collections.sort(gci.knn, new DistComparator());

						int knnlen = gci.knn.size();
						grid gtemp = gci.knn.get(knnlen - 1);
						int index_temp = gtemp.getIndex();
						int dens = gridlist.get(index_temp).density;
						int sum_k = gci.density;

						for (int k = 0; k < knnlen - 1; k++) {
							int index = gci.knn.get(k).getIndex();
							sum_k += gridlist.get(index).density;
						}
						if (sum_k < ki) {
							double news = (double) Math.min((ki - sum_k), dens) / (double) dens;
							gci.knn.get(knnlen - 1).setScore(news);
						} else {
							gci.knn.remove(knnlen - 1);
							for (int m = 0; m < gridlist.get(index_temp).krnn.size(); m++) {
								if (gridlist.get(index_temp).krnn.get(m).getIndex() == j) {
									gridlist.get(index_temp).krnn.remove(m);
									break;
								}
							}

							int las = gridlist.get(j).knn.size() - 1;
							if (gci.knn.size() == 0) {
								gci.setReachDis(0.0);
							} else {
								gci.setReachDis(gci.knn.get(las).getDistance());
							}
						}
					} else {
						int sum = gci.density;
						for (int k = 0; k < gci.knn.size(); k++) {
							int temp_ind = gci.knn.get(k).getIndex();
							sum += gridlist.get(temp_ind).density;
						}
						// the number of data points (except gn) < ki
						if (sum < ki) {
							grid g = new grid();
							g.setIndex(j);
							g.setCoorvalue(gci.getGridcoor());
							gn.krnn.add(g);

							grid addg = new grid();
							addg.setIndex(ind);
							addg.setCoorvalue(com_data);
							addg.setDistance(rd);

							addg.setReachdist(Math.max(rd, gn.getReachDis()));
							addg.setScore((double) Math.min(ki - sum, gn.density) / (double) gn.density);
							gci.knn.add(addg);
							gci.setReachDis(rd);
						}
					}
				}
			}
		}
	}

	// if the cube to which the new point is mapped exists
	void isOldGrid(int inde) {
		GridNode gn = gridlist.get(inde);
		gn.density++;
		ArrayList<Double> com_data = gridlist.get(inde).getGridcoor();

		int sum = gn.density;
		// dense grid or only one grid
		if (sum > ki || inde == 0) {
			gn.knn.clear();
			assignValue(gn);
			endtime.add(System.currentTimeMillis());
			return;
		}

		int templen = gn.knn.size();

		// update kNN of current grid
		grid gtemp = gn.knn.get(templen - 1);
		for (int k = 0; k < gn.knn.size() - 1; k++) {
			int index = gn.knn.get(k).getIndex();
			sum += gridlist.get(index).density;
		}
		int den = gridlist.get(gtemp.getIndex()).density;

		if (sum < ki) {
			double score = (double) Math.min((ki - sum), den) / (double) den;
			gtemp.setScore(score);
		} else // update kNN and k-distance
		{
			gtemp = gn.knn.remove(templen - 1); // remove last grid
			if (gn.knn.size() == 0) {
				gn.setReachDis(0.0);
			} else {
				grid ge = gn.knn.get(gn.knn.size() - 1);
				gn.setReachDis(getDistance(ge.getCoorvalue(), com_data));
			}

			// Update kRNN of affected grids
			int temp_index = gtemp.getIndex();
			int krnnlen = gridlist.get(temp_index).krnn.size();
			for (int kr = 0; kr < krnnlen; kr++) {
				grid gr = gridlist.get(temp_index).krnn.get(kr);
				if (gr.getIndex() == inde) {
					gridlist.get(temp_index).krnn.remove(kr);
					break;
				}
			}
		}

		// new coming data point may affect the mapped grid's kRNN's kNN and k-distance
		for (int m = 0; m < gn.krnn.size(); m++) {
			grid gm = gn.krnn.get(m);
			int gm_i = gm.getIndex();
			GridNode gn_m = gridlist.get(gm_i);

			int knn_len = gn_m.knn.size();
			grid g_last = gn_m.knn.get(knn_len - 1);
			int num_point = gn_m.density;
			for (int k = 0; k < gn_m.knn.size() - 1; k++) {
				int index = gn_m.knn.get(k).getIndex();
				num_point += gridlist.get(index).density;
			}
			int g_last_den = gridlist.get(g_last.getIndex()).density;
			if (num_point < ki) {
				// kNN does not change, but need to update the score of last grid
				double score = (double) Math.min((ki - num_point), g_last_den) / (double) g_last_den;
				g_last.setScore(score);
			} else {
				gtemp = gn_m.knn.remove(knn_len - 1);
				if (gn_m.knn.size() == 0) {
					gn_m.setReachDis(0.0);
				} else {
					grid ge = gn_m.knn.get(gn_m.knn.size() - 1);
					gn_m.setReachDis(getDistance(ge.getCoorvalue(), gm.getCoorvalue()));
				}

				int krnnlen = gridlist.get(gtemp.getIndex()).krnn.size();
				for (int kr = 0; kr < krnnlen; kr++) {
					grid gr = gridlist.get(gtemp.getIndex()).krnn.get(kr);
					if (gr.getIndex() == gm_i) {
						gridlist.get(gtemp.getIndex()).krnn.remove(kr);
						break;
					}
				}
			}
		}
	}

	// update the reach-dist(gn, gn_kni), gn_kni \in
	void updateReachDist(GridNode gn) {
		int ind = gn.getIndex();
		for (int kni = 0; kni < gridlist.get(ind).knn.size(); kni++) {
			grid g_kni = gn.knn.get(kni);
			int kni_ind = g_kni.getIndex();
			double rd = Math.max(gridlist.get(kni_ind).getReachDis(),
					getDistance(gn.getGridcoor(), g_kni.getCoorvalue()));
			g_kni.setReachdist(rd);
		}
	}

	void calErrorRate(int stream_i) {
		int errorNum = 0;
		for (int i = 0; i < stream_i + 1; i++) {
			int index = gridmap.get(streamList.get(i).toString().hashCode());
			if (gridlist.get(index).getLof() > threshold && label.get(i) == 0) {
				errorNum++;
			}
			if (gridlist.get(index).getLof() <= threshold && label.get(i) == 1) {
				errorNum++;
			}
		}
		int id = stream_i + 1;
		double error_rate = (double) errorNum / (double) id;
		System.out.println("id:" + id + ", num:" + errorNum + ", rate=" + error_rate);
		datasizeId.add(id);
		errorRate.add(error_rate);
	}

	// calculate the lrd update list of gn
	ArrayList<grid> calUpdateLrd(GridNode gn) {
		ArrayList<grid> update_lrd = new ArrayList<grid>();

		update_lrd.addAll(gn.krnn);

		for (int j = 0; j < gn.krnn.size(); j++) {
			grid gj = gn.krnn.get(j);
			int xj = gj.getIndex();
			for (int k = 0; k < gridlist.get(xj).knn.size(); k++) {
				grid gsmall = gridlist.get(xj).knn.get(k);
				if (gsmall.getIndex() != gn.getIndex()) {
					gridlist.get(xj).knn.get(k).setReachdist(gridlist.get(xj).getReachDis());

					for (int gm = 0; gm < gridlist.get(gsmall.getIndex()).knn.size(); gm++) {
						if (gridlist.get(gsmall.getIndex()).knn.get(gm).getIndex() == xj) {
							update_lrd.add(gsmall);
							break;
						}
					}
				}
			}
		}

		return update_lrd;

	}

	public void streamProcessing() {
		System.out.println("Begin streaming computing.");
		for (int i = 0; i < streamList.size(); i++) {
			begintime.add(System.currentTimeMillis());
			System.out.println(initialNum + i);
			ArrayList<Double> com_data = streamList.get(i);
			int com_code = com_data.toString().hashCode();
			int com_ind = 0;

			// if the grid com_code not exists
			if (!(gridmap.containsKey(com_code))) {
				com_ind = gridlist.size();
				gridmap.put(com_code, com_ind);
				isNewGrid(i);
			} else // the grid com_code has existed
			{
				com_ind = gridmap.get(com_code);
				isOldGrid(com_ind);
			}

			GridNode gn_now = gridlist.get(com_ind);

			// update reach-dist(com_ind, kni), kni \in kNN(com_ind)
			updateReachDist(gn_now);

			ArrayList<grid> update_lrd = new ArrayList<grid>();

			update_lrd = calUpdateLrd(gn_now);

			// Update lrd
			ArrayList<grid> update_LOF = new ArrayList<grid>();
			update_LOF.addAll(update_lrd);
			for (int m = 0; m < update_lrd.size(); m++) {
				grid gm = update_lrd.get(m);
				int xm = gm.getIndex();
				gridlist.get(xm).setLrd(calculateLrd(xm));
				for (int kr = 0; kr < gridlist.get(xm).krnn.size(); kr++) {
					int index = gridlist.get(xm).krnn.get(kr).getIndex();
					if (index != com_ind)
						update_LOF.add(gridlist.get(xm).krnn.get(kr));
				}
			}

			// update LOF
			for (int l = 0; l < update_LOF.size(); l++) {
				grid gl = update_LOF.get(l);
				int xl = gl.getIndex();
				gridlist.get(xl).setLof(calculateLof(xl));
			}

			// Update the lrd and LOF of current grid
			if (gn_now.knn.size() == 0) {
				assignValue(gn_now);
			} else {
				gn_now.setLrd(calculateLrd(com_ind));
				gn_now.setLof(calculateLof(com_ind));
			}

			endtime.add(System.currentTimeMillis());

			calErrorRate(i);
		}
	}

	double calculateLrd(int x) {
		double sum_lrd = 0.0;
		for (int i = 0; i < gridlist.get(x).knn.size(); i++) {
			grid gmi = gridlist.get(x).knn.get(i);
			// System.out.println("cal lrd: " + gmi.getReachdist() + ", " + gmi.getScore());
			sum_lrd += gmi.getReachdist() * gmi.getScore() * gridlist.get(gmi.getIndex()).density;
		}
		if (gridlist.get(x).density >= ki)
			return 5.0;
		else
			return (double) (ki - gridlist.get(x).density) / sum_lrd;
	}

	double calculateLof(int i) {
		double suml = 0.0;
		for (int o = 0; o < gridlist.get(i).knn.size(); o++) {
			grid glo = gridlist.get(i).knn.get(o);
			suml += gridlist.get(glo.getIndex()).getLrd();
		}
		if (gridlist.get(i).density >= ki)
			return 1.0;
		else
			return suml / (double) gridlist.get(i).knn.size() / gridlist.get(i).getLrd();
	}

	private double getDistance(ArrayList<Double> a1, ArrayList<Double> a2) {
		double sum = 0;
		for (int i = 0; i < a1.size() && i < a2.size(); i++) {
			sum += (a1.get(i) - a2.get(i)) * (a1.get(i) - a2.get(i));
		}
		return Math.sqrt(sum);
	}

	void assignValue(GridNode gn) {
		if (gn.density >= ki) {
			gn.setReachDis(0.0);
			gn.setLrd(5.0);
			gn.setLof(1.0);
		} else {
			gn.setReachDis(0.0);
			gn.setLrd(1.0);
			gn.setLof(5.0);
		}

	}

	void calKNN(GridNode gn) {
		List<grid> kdList = new ArrayList<grid>();
		kdList.clear();
		int index = gn.getIndex();

		// compute the distance between current grid and other all grids
		for (int j = 0; j < gridlist.size(); j++) {
			if (index == j)
				continue;
			grid g = new grid();
			g.setIndex(j);
			ArrayList<Double> ta = gridlist.get(j).getGridcoor();
			g.setCoorvalue(ta);
			g.setDistance(getDistance(gn.getGridcoor(), ta));
			kdList.add(g);
		}

		// sorting by distance
		Collections.sort(kdList, new DistComparator());

		int sum = gn.density;
		// calculate gn's kNN
		for (int k = 0; k < kdList.size() && sum < ki; k++) {
			grid g = kdList.get(k);
			int den = gridlist.get(g.getIndex()).density;
			// if all points in grid g belong to kNN(gn);
			if (sum + den <= ki) {
				g.setScore(1.0);
				gridlist.get(index).knn.add(g);
			}
			// if only some points in grid g belong to kNN(gn)
			else {
				double s = (double) Math.min((ki - sum), den) / (double) den;
				g.setScore(s);
				gridlist.get(index).knn.add(g);
			}
			sum += den;

			grid gadd = new grid();
			gadd.setIndex(index);
			gadd.setCoorvalue(gn.getGridcoor());
			// if g is one of kNN(gn), then gn is one of kRNN(g)
			gridlist.get(g.getIndex()).krnn.add(gadd);
		}

		// calculate the k-distance of grid gn
		int knnlen = gridlist.get(index).knn.size();
		if (knnlen != 0) {
			gridlist.get(index)
					.setReachDis(getDistance(gn.getGridcoor(), gridlist.get(index).knn.get(knnlen - 1).getCoorvalue()));
		} else
			gridlist.get(index).setReachDis(0.0);
	}

	void calReachDist(GridNode gn) {
		ArrayList<Double> ai = gn.getGridcoor();
		for (int j = 0; j < gn.knn.size(); j++) {
			grid gj = gn.knn.get(j);
			ArrayList<Double> aj = gj.getCoorvalue();
			double rd = getDistance(ai, aj);
			gn.knn.get(j).setReachdist(Math.max(rd, gridlist.get(gj.getIndex()).getReachDis()));
		}
	}

	void initGrid() {
		// initialize grids
		for (int i = 0; i < gridlist.size(); i++) {
			// if all initialNum points are mapped into the same grid, or current grid is
			// dense
			if (gridlist.size() == 1 || gridlist.get(i).density >= ki) {
				assignValue(gridlist.get(i));
				continue;
			}

			calKNN(gridlist.get(i));
		}

		// calculate the reach-dist of kNN of each grid
		for (int i = 0; i < gridlist.size(); i++) {
			calReachDist(gridlist.get(i));
		}

		// Compute lrd
		for (int i = 0; i < gridlist.size(); i++) {
			if (gridlist.get(i).getLrd() > 0)
				continue; // if the grid has been initialized, then continue
			gridlist.get(i).setLrd(calculateLrd(i));
		}

		for (int i = 0; i < gridlist.size(); i++) {
			if (gridlist.get(i).getLof() > 0)
				continue; // if the grid has been initialized, then continue
			gridlist.get(i).setLof(calculateLof(i));
		}

		System.out.println("Finish init");
	}

	void readData() throws IOException {
		Workbook book = null;
		book = getExcelWorkbook(datasetDir);
		Sheet sheet = getSheetByNum(book, 0);

		// Read initialized data points
		for (int i = 0; i < initialNum; i++) {
			System.out.println(i);
			Row row = null;
			row = sheet.getRow(i);
			if (row != null) {
				ArrayList<Double> data_coor = new ArrayList<>();
				data_coor.clear();
				for (int j = 0; j < dim; j++) {
					double temp_data = row.getCell(j).getNumericCellValue();
					// Calculate the grid coordinate
					double temp_coor = (double) Math.floor(temp_data / gridlen) * gridlen + 0.5 * gridlen;
					if (temp_data == 1.0)
						temp_coor = ((double) Math.floor(temp_data / gridlen) - 1) * gridlen + 0.5 * gridlen;
					data_coor.add(temp_coor);
				}
				int newcode = data_coor.toString().hashCode();
				// If the grid exists
				if (gridmap.containsKey(newcode))
					gridlist.get(gridmap.get(newcode)).density++;
				else {
					gridmap.put(newcode, gridlist.size());
					GridNode ge = new GridNode();
					ge.density = 1;
					ge.setGridcoor(data_coor);
					ge.setIndex(gridlist.size());
					gridlist.add(ge);
				}
			}
		}

		System.out.println("Read initialNum over.");

		// Read steam data points
		for (int i = initialNum; i < lastrow; i++) {
			Row row = null;
			row = sheet.getRow(i);
			if (row != null) {
				ArrayList<Double> data_coor = new ArrayList<>();
				data_coor.clear();
				for (int j = 0; j < dim; j++) {
					double temp_data = row.getCell(j).getNumericCellValue();
					double temp_coor = (double) Math.floor(temp_data / gridlen) * gridlen + 0.5 * gridlen;
					if (temp_data == 1.0)
						temp_coor = ((double) Math.floor(temp_data / gridlen) - 1) * gridlen + 0.5 * gridlen;
					data_coor.add(temp_coor);
				}
				streamList.add(data_coor);

				label.add((int) row.getCell(dim).getNumericCellValue());
			}
		}

		System.out.println("Read streamNum over.");
	}

	public Sheet getSheetByNum(Workbook book, int number) {
		Sheet sheet = null;
		try {
			sheet = book.getSheetAt(number);
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
				throw new RuntimeException("file " + filePath + "does not exist");
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

	// write point's coor, grid's coor, lrd, lof and gridId into file
	void writePointLof() throws IOException {
		Workbook book = null;
		book = getExcelWorkbook(datasetDir);
		Sheet sheet = getSheetByNum(book, 0);

		ArrayList<ArrayList<Double>> dataList = new ArrayList<ArrayList<Double>>();

		// Read data
		for (int i = 0; i < streamNum; i++) {
			Row row = null;
			row = sheet.getRow(i + initialNum);
			if (row != null) {
				ArrayList<Double> data_coor = new ArrayList<>();
				data_coor.clear();
				for (int j = 0; j < dim; j++) {
					double temp_data = row.getCell(j).getNumericCellValue();
					data_coor.add(temp_data);
				}
				dataList.add(data_coor);
			}
		}
		book.close();

		// write data
		Workbook wb = new XSSFWorkbook();
		sheet = wb.createSheet();
		Row row = sheet.createRow(0);

		Cell cell;
		for (int i = 0; i < dim; i++) {
			cell = row.createCell(i);
			cell.setCellValue("x" + i);
		}
		for (int i = dim; i < 2 * dim; i++) {
			int temp_i = i - dim;
			cell = row.createCell(i);
			cell.setCellValue("g" + temp_i);
		}

		cell = row.createCell(2 * dim);
		cell.setCellValue("lrd");
		cell = row.createCell(2 * dim + 1);
		cell.setCellValue("LOF");
		cell = row.createCell(2 * dim + 2);
		cell.setCellValue("GridId");

		int k = 1;
		for (int i = 0; i < streamNum; i++) {
			row = sheet.createRow(k++);
			for (int m = 0; m < dim; m++) {
				cell = row.createCell(m);
				cell.setCellValue(dataList.get(i).get(m));
			}

			for (int m = dim; m < 2 * dim; m++) {
				cell = row.createCell(m);
				cell.setCellValue(streamList.get(i).get(m - dim));
			}
			int index = gridmap.get(streamList.get(i).toString().hashCode());
			cell = row.createCell(2 * dim);
			cell.setCellValue(gridlist.get(index).getLrd());
			cell = row.createCell(2 * dim + 1);
			cell.setCellValue(gridlist.get(index).getLof());
			cell = row.createCell(2 * dim + 2);
			cell.setCellValue(index);
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outPointLOF);
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

	void writeTime() throws IOException {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet();
		Row row = sheet.createRow(0);

		Cell cell = row.createCell(0);
		cell.setCellValue("Start");
		cell = row.createCell(1);
		cell.setCellValue("End");
		cell = row.createCell(2);
		cell.setCellValue("Elapsed");
		int k = 1;
		for (int i = 0; i < begintime.size() && i < endtime.size(); i++) {
			row = sheet.createRow(k++);
			for (int m = 0; m < 3; m++) {
				cell = row.createCell(m);
				if (m == 0) {
					cell.setCellValue(begintime.get(i));
				} else if (m == 1) {
					cell.setCellValue(endtime.get(i));
				} else {
					cell.setCellValue(endtime.get(i) - begintime.get(i));
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

	void calAccuracy() throws IOException {
		Workbook resultBook = null;
		resultBook = getExcelWorkbook(outPointLOF);
		Sheet resultSheet = getSheetByNum(resultBook, 0);
		File f1 = new File(outAccuracy);
		if (f1.exists() == false)
			f1.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(f1);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		int NormalErrorNum = 0;
		int NormalNum = 0;
		double NormalRate;
		int OutlierNum = 0;
		int OutlierErrorNum = 0;
		double OutlierRate;
		double Accuracy;

		// for other data sets
		for (int i = 0; i < streamNum; i++) {
			Row resultRow = resultSheet.getRow(i + 1);
			if (label.get(i) == 0) {
				NormalNum++;
				if (threshold < resultRow.getCell(2 * dim + 1).getNumericCellValue()) {
					NormalErrorNum++;
				}
			} else {
				OutlierNum++;
				if (threshold >= resultRow.getCell(2 * dim + 1).getNumericCellValue()) {
					OutlierErrorNum++;
				}
			}
		}

		if (NormalNum + OutlierNum != streamNum)
			System.out.println("Point error!");

		NormalRate = (double) (NormalNum - NormalErrorNum) / (double) (NormalNum);
		OutlierRate = (double) (OutlierNum - OutlierErrorNum) / (double) (OutlierNum);
		Accuracy = (double) (NormalNum - NormalErrorNum + OutlierNum - OutlierErrorNum) / (double) (streamNum);
		bw.write("===============LOF threshold = " + threshold + "===============");
		bw.newLine();
		bw.write("NormalNum=" + NormalNum + ", NormalError=" + NormalErrorNum + ", NormalRate=" + NormalRate);
		bw.newLine();
		bw.write("OutlierNum=" + OutlierNum + ", OutlierError=" + OutlierErrorNum + ", OutlierRate=" + OutlierRate);
		bw.newLine();
		bw.write("Accuracy=" + Accuracy);
		bw.newLine();
		bw.newLine();
		bw.close();
		System.out.println("Calculation Accuracy Over.");
	}

	void writeError() throws IOException {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet();
		Row row = sheet.createRow(0);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("size");
		cell = row.createCell(1);
		cell.setCellValue("preError");

		int k = 1;
		for (int i = 0; i < datasizeId.size(); i++) {
			row = sheet.createRow(k++);
			cell = row.createCell(0);
			cell.setCellValue(datasizeId.get(i));
			cell = row.createCell(1);
			cell.setCellValue(errorRate.get(i));
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outPreError);
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

class DistComparator implements Comparator<grid> {
	public int compare(grid A, grid B) {
		// return A.getDistance() - B.getDistance() < 0 ? -1 : 1;
		if ((A.getDistance() - B.getDistance()) < 0)
			return -1;
		else if ((A.getDistance() - B.getDistance()) > 0)
			return 1;
		else
			return 0;
	}
}

class coordinate {
	private ArrayList<Double> coor = new ArrayList<>();

	public coordinate(ArrayList<Double> co) {
		coor = co;
	}

	public ArrayList<Double> getCoor() {
		return coor;
	}

	public void setCoor(ArrayList<Double> coor) {
		this.coor = coor;
	}

}