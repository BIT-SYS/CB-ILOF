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

public class StreamProcess {
	private static int ki = 180;
	static final double gridlen = 0.01;
	static final int firstBatch = 20660;
	static final int lastrow = 45660;
	static final int streamNum = lastrow-firstBatch;
	static final int dim = 9;
	
	public String prefix = "D:\\BIT\\Paper\\Outlier-detection\\LOF_result_and_program\\LOF\\";
	public String datasetName = "shuttle_test";
	public String dataset = prefix+"test-debug\\"+datasetName+".xlsx";
	public String outLOF = prefix+"Result\\LOF\\"+datasetName+"_gridCoordinateChange_my_outLof_"+ki+"_"+gridlen+"_"+firstBatch+"_"+streamNum+".xlsx";
	public String outTime = prefix+"Result\\time\\"+datasetName+"_gridCoordinateChange_my_outTime_"+ki+"_"+gridlen+"_"+firstBatch+"_"+streamNum+".xlsx";
	public String outPointLOF = prefix+"Result\\point\\"+datasetName+"_gridCoordinateChange_point_my_outLof_"+ki+"_"+gridlen+"_"+firstBatch+"_"+streamNum+".xlsx";
	public String outAccuracy = prefix+"Result\\accuracy\\"+datasetName+"_gridCoordinateChange_my_accuracy_"+ki+"_"+gridlen+"_"+firstBatch+"_"+streamNum+".txt";
	
	String resultSheet = ki + "-" + firstBatch;
	ArrayList<GridNode> gridlist = new ArrayList<>();
	HashMap<Integer, Integer> gridmap = new HashMap<>();
	ArrayList<ArrayList<Double>> streamList = new ArrayList<ArrayList<Double>>();// 鏁版嵁娴�
	
	ArrayList<Long> begintime = new ArrayList<>();
	ArrayList<Long> endtime = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		StreamProcess sp = new StreamProcess();
		sp.readData();
		sp.initGrid();
		System.in.read();
		sp.OutlierProcessing();
		sp.writeResult();
		sp.writePointResult();
		sp.writeTimeResult();
//		sp.calAccuracy();
		System.out.println("over");
//		Date now = new Date();
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		String hehe = dateFormat.format( now );
//		System.out.println(hehe);
	}
	public StreamProcess(){
		gridlist.clear();
		streamList.clear();
		begintime.clear();
		endtime.clear();
		gridmap.clear();
	}

	public void OutlierProcessing() {
		// 锟铰诧拷锟斤拷一锟斤拷锟斤拷
		for (int i = 0; i < streamList.size(); i++) {
			
//			if(i == 8316)
//				System.out.println();
			System.out.println(firstBatch+i);
			begintime.add(System.currentTimeMillis());
			ArrayList<Double> com_data = streamList.get(i);
			int com_code = com_data.toString().hashCode();
			int com_ind = 0;
			//if current mapped cube not exists
			if (!(gridmap.containsKey(com_code))) {
				// 鎻掑叆缃戞牸
				int ind = gridlist.size();
				com_ind = ind;
				// System.out.println("global_index="+global_index+", grid_index="+com_ind);
				GridNode gnew = new GridNode();
				gnew.setIndex(ind);
				gnew.setGridcoor(com_data);
				gnew.density = 1;
				gridmap.put(com_code, ind);
				gridlist.add(gnew);
				if(ind == 0)
				{
					gridlist.get(ind).setReachDis(0.0);
					gridlist.get(ind).setLrd(1.0);
					gridlist.get(ind).setLof(5.0);
					continue;
				}
				
				// 鏇存柊knn
				List<grid> kdList = new ArrayList<grid>();
				for(int co = 0; co < ind; co++){
					grid g = new grid();
					g.setIndex(co);
					g.setCoorvalue(gridlist.get(co).getGridcoor());
					g.setDistance(getDistance(com_data, g.getCoorvalue()));
					kdList.add(g);
				}
				Collections.sort(kdList, new DistComparator());
				int sum = 1; //鏂版潵缃戞牸鐨勫瘑搴�
				for (int k = 0; k < kdList.size() && sum < ki; k++) {
					grid g = kdList.get(k);
					if (sum + gridlist.get(g.getIndex()).density <= ki) {
						g.setScore(1.0);
						gridlist.get(ind).knn.add(g);
					} else {
						double s = (double) Math.min((ki - sum),gridlist.get(g.getIndex()).density) / (double) gridlist.get(g.getIndex()).density;
						g.setScore(s);
						gridlist.get(ind).knn.add(g);
					}
					sum += gridlist.get(g.getIndex()).density;

					grid gadd = new grid();
					gadd.setIndex(ind);
					gadd.setCoorvalue(com_data);
					gridlist.get(g.getIndex()).krnn.add(gadd);
				}
				//compute the k-dist of current cube 
				int knnlen = gridlist.get(ind).knn.size();
				if(knnlen != 0){
					gridlist.get(ind).setReachDis(getDistance(com_data,
							gridlist.get(ind).knn.get(knnlen - 1).getCoorvalue()));
				}
				//compute the kRNN of current code
				for(int j = 0; j < ind; j++){
					GridNode gci = gridlist.get(j);
					if(gci.getReachDis() > getDistance(gci.getGridcoor(), com_data) || (gci.getReachDis() == 0.0 && gci.density < ki)){
						grid g = new grid();
						g.setIndex(j);
						g.setCoorvalue(gci.getGridcoor());
						gridlist.get(ind).krnn.add(g);

						//Update the knn of gci
						grid addg = new grid();
						addg.setIndex(ind);
						addg.setCoorvalue(com_data);
						addg.setDistance(getDistance(gci.getGridcoor(), com_data));
						gridlist.get(j).knn.add(addg);

						if(gci.getReachDis() == 0.0 && gci.density < ki) {
							addg.setReachdist(getDistance(gci.getGridcoor(), com_data));
							addg.setScore((double)Math.min(ki-gci.density,gnew.density)/(double)ki);
							gridlist.get(j).setReachDis(getDistance(gci.getGridcoor(),com_data));
							continue;
						}
						
						Collections.sort(gridlist.get(j).knn, new DistComparator());
						
						int lasti = gridlist.get(j).knn.size() - 1;
						grid lastgrid = gridlist.get(j).knn.get(lasti);
						int lastindex = lastgrid.getIndex();
						int dens = gridlist.get(lastindex).density;
						int sum_k = gridlist.get(j).density;
						for (int k = 0; k < lasti; k++)
						{
							int index = gridlist.get(j).knn.get(k).getIndex();
							sum_k += gridlist.get(index).density;
						}
						if(sum_k < ki){
							double news = (double) Math.min((ki - sum_k), dens) / (double) dens;
							gridlist.get(j).knn.get(lasti).setScore(news);
						}else{
							//绉婚櫎锛屼笖绉婚櫎鎺夌殑缃戞牸鐨刱rnn涔熷簲璇ユ洿鏂�
							gridlist.get(j).knn.remove(lasti);
							for(int m = 0; m < gridlist.get(lastindex).krnn.size(); m++){
								if(gridlist.get(lastindex).krnn.get(m).getIndex() == j){
									gridlist.get(lastindex).krnn.remove(m);
									break;
								}
							}
							//鏇存柊gci鐨勫彲杈捐窛绂�
							int las = gridlist.get(j).knn.size() - 1;
							if(gridlist.get(j).knn.size() == 0) {
								gridlist.get(j).setReachDis(0.0);
							}else{
								gridlist.get(j).setReachDis(gridlist.get(j).knn.get(las).getDistance());
							}
						}
					}
					else
					{
						int sum_k = gci.density;
						for (int k = 0; k < gci.knn.size(); k++)
							sum_k += gridlist.get(gci.knn.get(k).getIndex()).density;
						if (sum_k < ki)
						{
							grid g = new grid();
							g.setIndex(j);
							g.setCoorvalue(gci.getGridcoor());
							gridlist.get(ind).krnn.add(g);

							grid addg = new grid();
							addg.setIndex(ind);
							addg.setCoorvalue(com_data);
							addg.setDistance(getDistance(gci.getGridcoor(), com_data));
							addg.setScore(1.0);
							gridlist.get(j).knn.add(addg);
							Collections.sort(gridlist.get(j).knn, new DistComparator());
							int las = gridlist.get(j).knn.size() - 1;
							gridlist.get(j).setReachDis(gridlist.get(j).knn.get(las).getDistance());
						}
						
					}
				}
			} else {
				// update 涔嬪墠杩欎釜缃戞牸閲屾湁鏁版嵁鐐癸紝鏇存柊鍗冲彲
				// 鏇存柊瀵嗗害
				int inde = gridmap.get(com_code);
				com_ind = inde;
				gridlist.get(inde).density++;

				
				int sum = gridlist.get(inde).density;
				if (sum > ki) {
					System.out.println("Number of points included in grid " + gridlist.get(inde).getIndex()+" is greater than "+ki);
					gridlist.get(inde).knn.clear(); 
					gridlist.get(inde).setReachDis(0.0);
					gridlist.get(inde).setLrd(5.0);
					gridlist.get(inde).setLof(1.0);
					endtime.add(System.currentTimeMillis());
					continue;

				} else {
					int templen = gridlist.get(inde).knn.size();
					if(templen == 0){
						if (sum < ki)
						{
							gridlist.get(inde).setReachDis(0.0);
							gridlist.get(inde).setLrd(1.0);
							gridlist.get(inde).setLof(5.0);
						}
						else if (sum == ki)
						{
							gridlist.get(inde).setReachDis(0.0);
							gridlist.get(inde).setLrd(5.0);
							gridlist.get(inde).setLof(1.0);
						}
//						else
//						{
//							List<grid> kdList = new ArrayList<grid>();
//							for(int co = 0; co < gridlist.size(); co++){
//								if(co == inde) continue;
//								grid g = new grid();
//								g.setIndex(co);
//								g.setCoorvalue(gridlist.get(co).getGridcoor());
//								g.setDistance(getDistance(com_data, g.getCoorvalue()));
//								kdList.add(g);
//							}
//							Collections.sort(kdList, new DistComparator());
//							int sum1 = sum; //鏂版潵缃戞牸鐨勫瘑搴�
//							for (int k = 0; k < kdList.size() && sum1 < ki; k++) {
//								grid g = kdList.get(k);
//								if (sum1 + gridlist.get(g.getIndex()).density <= ki) {
//									gridlist.get(inde).knn.add(g);
//								} else {
//									double s = (double) (ki - sum1) / (double) gridlist.get(g.getIndex()).density;
//									g.setScore(s);
//									gridlist.get(inde).knn.add(g);
//								}
//								sum1 += gridlist.get(g.getIndex()).density;
//	
//								grid gadd = new grid();
//								gadd.setIndex(inde);
//								gadd.setCoorvalue(com_data);
//								gridlist.get(g.getIndex()).krnn.add(gadd);
//							}
//							//鏇存柊i鐨勫彲杈捐窛绂�
//							int knnlen = gridlist.get(inde).knn.size();
//							if(knnlen != 0){
//								gridlist.get(inde).setReachDis(getDistance(com_data,
//										gridlist.get(inde).knn.get(knnlen - 1).getCoorvalue()));
//							}
//						}
					}
					else{  //update kNN of current cube
						grid gtemp = gridlist.get(inde).knn.get(templen - 1);
						int num_point = sum;
						for(int k = 0; k < gridlist.get(inde).knn.size()-1; k++)
						{
							int index = gridlist.get(inde).knn.get(k).getIndex();
							num_point += gridlist.get(index).density;
						}
						int den = gridlist.get(gtemp.getIndex()).density;
//						double sc = gtemp.getScore();
//						if (den * sc > 1 && sum < ki) {
						if (num_point < ki) {
							double score = (double) (ki-num_point)/ (double)den;
							gridlist.get(inde).knn.get(templen - 1).setScore(score);
						} 
						else { // num_point == ki
							gtemp = gridlist.get(inde).knn.remove(templen - 1); // remove last cube
							if (gridlist.get(inde).knn.size() == 0) {
								gridlist.get(inde).setReachDis(0.0);
							} 
							else {
								grid ge = gridlist.get(inde).knn.get(gridlist.get(inde).knn.size() - 1);
								// Update reach-dist
								gridlist.get(inde).setReachDis(getDistance(ge.getCoorvalue(), com_data));
							}
							// Update kRNN of affected cubes
							int krlen = gtemp.getIndex();
							int krnnlen = gridlist.get(krlen).krnn.size();
							for (int kr = 0; kr < krnnlen; kr++) {
								grid gr = gridlist.get(krlen).krnn.get(kr);
								if (gr.getIndex() == inde) {
									gridlist.get(krlen).krnn.remove(kr);
									break;
								}
							}

						}
					}
				}
				// new coming data point may affect the mapped cube's kRNN's kNN
				for (int m = 0; m < gridlist.get(inde).krnn.size(); m++) {
					grid gk = gridlist.get(inde).krnn.get(m);
					int gi = gk.getIndex();

					int templen = gridlist.get(gi).knn.size();
					if (templen == 0)
						continue;
					grid gtemp = gridlist.get(gi).knn.get(templen - 1);
					int num_point = gridlist.get(gi).density;
					for(int k = 0; k < gridlist.get(gi).knn.size()-1; k++)
					{
						int index = gridlist.get(gi).knn.get(k).getIndex();
						num_point += gridlist.get(index).density;
					}
					int den = gridlist.get(gtemp.getIndex()).density;
					if( num_point < ki){
						// kNN does not change, but need to update the score of last cube
						double score = (double) (ki-num_point) / (double) den;
						gridlist.get(gi).knn.get(templen - 1).setScore(score);
					}else{
						gtemp = gridlist.get(gi).knn.remove(templen - 1);
						if(gridlist.get(gi).knn.size() == 0){
							gridlist.get(gi).setReachDis(0.0);

						}else{
							grid ge = gridlist.get(gi).knn.get(gridlist.get(gi).knn.size() - 1);
							gridlist.get(gi).setReachDis(getDistance(ge.getCoorvalue(), gk.getCoorvalue()));
						}

						//鏇存柊绉婚櫎缃戞牸鐨刱rnn
						int krnnlen = gridlist.get(gtemp.getIndex()).krnn.size();
						for(int kr = 0; kr < krnnlen; kr++){
							grid gr = gridlist.get(gtemp.getIndex()).krnn.get(kr);
							if(gr.getIndex() == gi){
								gridlist.get(gtemp.getIndex()).krnn.remove(kr);
								break;
							}
						}

					}
				}
			} /////////////// end else

			// update reach-dist(com_ind, kni), kni \in kNN
			for (int kni = 0; kni < gridlist.get(com_ind).knn.size(); kni++) {
				grid gi = gridlist.get(com_ind).knn.get(kni);
				int xi = gi.getIndex();
				double rd = Math.max(gridlist.get(xi).getReachDis(), getDistance(com_data, gi.getCoorvalue()));
				gridlist.get(com_ind).knn.get(kni).setReachdist(rd);
			}

			ArrayList<grid> update_lrd = new ArrayList<grid>();
			update_lrd.addAll(gridlist.get(com_ind).krnn);
			for (int j = 0; j < gridlist.get(com_ind).krnn.size(); j++) {
				grid gj = gridlist.get(com_ind).krnn.get(j);
				int xj = gj.getIndex();
				for (int k = 0; k < gridlist.get(xj).knn.size(); k++) {
					grid gsmall = gridlist.get(xj).knn.get(k);
					if (gsmall.getIndex() != com_ind) {
//						gridlist.get(xj).knn.get(k).setReachdistTo(gridlist.get(xj).getReachDis());
						gridlist.get(xj).knn.get(k).setReachdist(gridlist.get(xj).getReachDis());
						for(int gm = 0; gm < gridlist.get(gsmall.getIndex()).knn.size(); gm++){
							if(gridlist.get(gsmall.getIndex()).knn.get(gm).getIndex() == xj){
								update_lrd.add(gsmall);
								break;
							}
						}
					}
				}
			}
			// Update Lrd
			ArrayList<grid> update_LOF = new ArrayList<grid>();
			update_LOF.addAll(update_lrd);
			for (int m = 0; m < update_lrd.size(); m++) {
				grid gm = update_lrd.get(m);
				int xm = gm.getIndex();
				gridlist.get(xm).setLrd(calculateLrd(xm));
				for (int kr = 0; kr < gridlist.get(xm).krnn.size(); kr++)
				{
					int index = gridlist.get(xm).krnn.get(kr).getIndex();
					if (index != com_ind)
						update_LOF.add(gridlist.get(xm).krnn.get(kr));
				}
			}
			// delete the same cubes
//			for(int m = 0; m < update_LOF.size(); m++)
//			{
//				// except current cube
//				
//				for (int k = m+1; k < update_LOF.size(); k++)
//				{
//					if(update_LOF.get(m).getIndex() == update_LOF.get(k).getIndex())
//					{
//						update_LOF.remove(k);
//						k--;
//					}
//				}
//				if (update_LOF.get(m).getIndex() == com_ind)
//				{
//					update_LOF.remove(m);
//					m--;
//					continue;
//				}
//			}
			// update the LOF of all affected cubes
			for (int l = 0; l < update_LOF.size(); l++) {
				grid gl = update_LOF.get(l);
				int xl = gl.getIndex();
				
				gridlist.get(xl).setLof(calculateLof(xl));
			}
			// Update the lrd and LOF of current cube
			if(gridlist.get(com_ind).knn.size() == 0 )
			{
				if ( gridlist.get(com_ind).density >= ki){
					gridlist.get(com_ind).setLrd(5.0);
					gridlist.get(com_ind).setLof(1.0);
				}
				else{
					gridlist.get(com_ind).setLrd(1.0);
					gridlist.get(com_ind).setLof(5.0);
				}
			}
			else {
				gridlist.get(com_ind).setLrd(calculateLrd(com_ind));
				gridlist.get(com_ind).setLof(calculateLof(com_ind));
			}
			
			System.out.println("number of points:"+gridlist.get(com_ind).density);
//			if(gridlist.size() > 2)
//				System.out.println(gridlist.get(2).getLof());
			endtime.add(System.currentTimeMillis());

		}
	}

	// 锟斤拷锟斤拷lrd
	double calculateLrd(int x) {
		// double sum_lrd = kd[xm][ym].density * kd[xm][ym].getReachDis();
		double sum_lrd = 0.0;
		for (int i = 0; i < gridlist.get(x).knn.size(); i++) {
			grid gmi = gridlist.get(x).knn.get(i);
//			System.out.println("cal lrd: " + gmi.getReachdist() + ", " + gmi.getScore());
			sum_lrd += gmi.getReachdist() * gmi.getScore() * gridlist.get(gmi.getIndex()).density;
		}
		if(gridlist.get(x).density >= ki)
			return 5.0;
		else
		{
//			if(sum_lrd == 0.0)
//			{
////				System.out.println(x + "hi");
//				
//			}
			return (double) (ki - gridlist.get(x).density) / sum_lrd;
		}
	}

	// 锟斤拷锟斤拷LOF
	double calculateLof(int i) {
		double suml = 0.0;
		for (int o = 0; o < gridlist.get(i).knn.size(); o++) {
			grid glo = gridlist.get(i).knn.get(o);
			suml += gridlist.get(glo.getIndex()).getLrd();
		}
		if(gridlist.get(i).density >= ki)
			return 1.0;
		else
		{
//			if(gridlist.get(i).knn.size() == 0 || gridlist.get(i).getLrd() == 0.0)
//			{
//				System.out.println(i);
//			}
			return suml / (double)gridlist.get(i).knn.size() / gridlist.get(i).getLrd();
		}
	}


	private double getDistance(ArrayList<Double> a1, ArrayList<Double> a2) {
		double sum = 0;
		for(int i = 0; i < a1.size() && i < a2.size(); i++){
			sum += (a1.get(i) -a2.get(i)) * (a1.get(i) - a2.get(i));
		}
		return Math.sqrt(sum);
	}

	void initGrid() {
		// initialize cubes
		for(int i = 0; i < gridlist.size(); i++){
			System.out.println(i);
			// if all initialized points are mapped into one cube
			if (gridlist.size() == 1)
			{
				if(gridlist.get(i).density >= ki)
				{
					gridlist.get(i).setReachDis(0.0);
					gridlist.get(i).setLrd(5.0);
					gridlist.get(i).setLof(1.0);
					continue;
				}
				else
				{
					gridlist.get(i).setReachDis(0.0);
					gridlist.get(i).setLrd(1.0);
					gridlist.get(i).setLof(5.0);
					continue;
				}
			}
			// if current cube is dense
			if(gridlist.get(i).density >= ki)
			{
				gridlist.get(i).setReachDis(0.0);
				gridlist.get(i).setLrd(1.0);
				gridlist.get(i).setLof(1.0);
				continue;
			}
			GridNode gn = gridlist.get(i);
			List<grid> kdList = new ArrayList<grid>();
			kdList.clear();
			// compute the distance between current cube and other all cubes
			for (int j = 0; j < gridlist.size(); j++) {
				if(i == j) continue;
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
			// compute the kNN list
			for (int k = 0; k < kdList.size() && sum < ki; k++) {
				grid g = kdList.get(k);

				if (sum + gridlist.get(g.getIndex()).density <= ki) {
					g.setScore(1.0);
					gridlist.get(i).knn.add(g);
				} else {
					double s = (double)(ki - sum) / (double)gridlist.get(g.getIndex()).density;
					g.setScore(s);
					gridlist.get(i).knn.add(g);
				}
				sum += gridlist.get(g.getIndex()).density;

				grid gadd = new grid();
				gadd.setIndex(i);
				gadd.setCoorvalue(gn.getGridcoor());
				gridlist.get(g.getIndex()).krnn.add(gadd);
			}
			//鏇存柊i鐨勫彲杈捐窛绂�
			int knnlen = gridlist.get(i).knn.size();
			if(knnlen != 0){
				gridlist.get(i).setReachDis(getDistance(gn.getGridcoor(),
						gridlist.get(i).knn.get(knnlen - 1).getCoorvalue()));
			}

		}

		// update the reach-dist of knn of current cube
		for(int i = 0; i < gridlist.size(); i++){
			ArrayList<Double> ai = gridlist.get(i).getGridcoor();
			for (int j = 0; j < gridlist.get(i).knn.size(); j++) {
				grid gj = gridlist.get(i).knn.get(j);
				ArrayList<Double> aj = gj.getCoorvalue();
				double rd = getDistance(ai, aj);
				gridlist.get(i).knn.get(j).setReachdist(
						Math.max(rd, gridlist.get(gj.getIndex()).getReachDis()));
//				gridlist.get(i).knn.get(j).setReachdistTo(
//						Math.max(rd, gridlist.get(i).getReachDis()));
			}
		}
		// Compute lrd
		for (int i = 0; i < gridlist.size(); i++) {
			if(gridlist.get(i).getLrd() > 0) continue;	// if the grid has been initialized, then continue
			gridlist.get(i).setLrd(calculateLrd(i));
		}
		
		for (int i = 0; i < gridlist.size(); i++) {
			if(gridlist.get(i).getLof() > 0) continue;
			gridlist.get(i).setLof(calculateLof(i));
		}
		System.out.println("Finish init");
	}

	void readData() throws IOException {
		Workbook book = null;
		book = getExcelWorkbook(dataset);
		Sheet sheet = getSheetByNum(book, 0);
		int lastRowNum = sheet.getLastRowNum();
		System.out.println("last number is " + lastRowNum);
		// 锟斤拷始锟斤拷一锟斤拷锟斤拷
		for (int i = 0; i < firstBatch; i++) {
			System.out.println(i);
			Row row = null;
			row = sheet.getRow(i);
			if (row != null) {
				ArrayList<Double> neda = new ArrayList<>();
				neda.clear();
				for(int j = 0; j < dim; j++){
					double temp = row.getCell(j).getNumericCellValue();
					double tempint = (double) Math.floor(temp / gridlen) * gridlen + 0.5*gridlen;  //缃戞牸杈�
					if (temp == 1.0) tempint = ((double) Math.floor(temp / gridlen) - 1) * gridlen + 0.5*gridlen;
					neda.add(tempint);
				}
				int newcode = neda.toString().hashCode();
				if(gridmap.containsKey(newcode)){
					gridlist.get(gridmap.get(newcode)).density++;
				}else{
					gridmap.put(newcode, gridlist.size());
					GridNode ge = new GridNode();
					ge.density = 1;
					ge.setGridcoor(neda);
					ge.setIndex(gridlist.size());
					gridlist.add(ge);
				}
			}
		}
		for (int i = firstBatch; i < lastrow; i++) {
			Row row = null;
			row = sheet.getRow(i);
			if (row != null) {
				ArrayList<Double> neda = new ArrayList<>();
				neda.clear();
				for(int j = 0; j < dim; j++){
					double temp = row.getCell(j).getNumericCellValue();
					double tempint = (double) Math.floor(temp / gridlen) * gridlen + 0.5*gridlen;
					if (temp == 1.0) tempint = ((double) Math.floor(temp / gridlen) - 1) * gridlen + 0.5*gridlen;
					neda.add(tempint);
				}
				streamList.add(neda);
			}

		}
		System.out.println("Finish read");
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
				throw new RuntimeException("瓒呮椂");
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
//		Cell cell = row.createCell(0);
//		cell.setCellValue("x");
//		cell = row.createCell(1);
//		cell.setCellValue("y");
		cell = row.createCell(dim);
		cell.setCellValue("lrd");
		cell = row.createCell(dim+1);
		cell.setCellValue("LOF");
		int k = 1;
		for (int i = 0; i < gridlist.size(); i++) {
			row = sheet.createRow(k++);
			for (int m = 0; m < dim; m++) {
				cell = row.createCell(m);
				cell.setCellValue(gridlist.get(i).getGridcoor().get(m));
			}
			cell = row.createCell(dim);
			cell.setCellValue(gridlist.get(i).getLrd());
			cell = row.createCell(dim+1);
			cell.setCellValue(gridlist.get(i).getLof());
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

	//write the lrd and lof of each point into file
	void writePointResult() throws IOException {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet(resultSheet);
		Row row = sheet.createRow(0);

		Cell cell;
		for (int i = 0; i < dim; i++)
		{
			cell = row.createCell(i);
			cell.setCellValue("x"+i);
		}
//		Cell cell = row.createCell(0);
//		cell.setCellValue("x");
//		cell = row.createCell(1);
//		cell.setCellValue("y");
		cell = row.createCell(dim);
		cell.setCellValue("lrd");
		cell = row.createCell(dim+1);
		cell.setCellValue("LOF");
		cell = row.createCell(dim+2);
		cell.setCellValue("Grid");
		int k = 1;
		for (int i = 0; i < streamList.size(); i++) {
			row = sheet.createRow(k++);
			for (int m = 0; m < dim; m++) {
				cell = row.createCell(m);
				cell.setCellValue(streamList.get(i).get(m));
			}
			cell = row.createCell(dim);
			cell.setCellValue(gridlist.get(gridmap.get(streamList.get(i).toString().hashCode())).getLrd());
			cell = row.createCell(dim+1);
			cell.setCellValue(gridlist.get(gridmap.get(streamList.get(i).toString().hashCode())).getLof());
			cell = row.createCell(dim+2);
			cell.setCellValue(gridmap.get(streamList.get(i).toString().hashCode()));
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

	void writeTimeResult() throws IOException {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet(resultSheet);
		Row row = sheet.createRow(0);

		Cell cell = row.createCell(0);
		cell.setCellValue("start");
		cell = row.createCell(1);
		cell.setCellValue("end");
		System.out.println("begintime " + begintime.size());
		System.out.println("endtime" + endtime.size());
		int k = 1;
		for (int i = 0; i < begintime.size() && i < endtime.size(); i++) {
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

class coordinate{
	private ArrayList<Double> coor = new ArrayList<>();
	public coordinate(ArrayList<Double> co){
		coor = co;
	}
	public ArrayList<Double> getCoor() {
		return coor;
	}
	public void setCoor(ArrayList<Double> coor) {
		this.coor = coor;
	}

}