
import java.util.ArrayList;
import java.util.List;

public class GridNode {
	private int index = 0;
	private ArrayList<Double> gridcoor = new ArrayList<>();  //缃戞牸鐨勫潗鏍�
	public int density = 0;  //瀵嗗害
	List<grid> knn = new ArrayList<grid>();
	List<grid> krnn = new ArrayList<grid>();

	private double lrd = 0.0;// 
	private double reachDis = 0.0;// 
	private double lof = 0.0;

	public GridNode() {
		this.density = 0;
		this.knn = new ArrayList<grid>();
		this.krnn = new ArrayList<grid>();
		this.lrd = 0.0;
		this.reachDis = 0.0;
		this.lof = 0.0;
	}

	/*
	 * public double getScore() { return score; }
	 * 
	 * public void setScore(double score) { this.score = score; }
	 */

	public double getLrd() {
		return lrd;
	}

	public void setLrd(double reachDensity) {
		this.lrd = reachDensity;
	}

	public double getReachDis() {
		return reachDis;
	}

	public void setReachDis(double reachDis) {
		this.reachDis = reachDis;
	}

	public double getLof() {
		return lof;
	}

	public void setLof(double lof) {
		this.lof = lof;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public ArrayList<Double> getGridcoor() {
		return gridcoor;
	}

	public void setGridcoor(ArrayList<Double> gridcoor) {
		this.gridcoor = gridcoor;
	}

}
