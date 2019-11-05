
import java.util.ArrayList;
import java.util.List;

//用于LOF算法的数据点
public class Node {
	private int index = 0;  //
	ArrayList<Double> dataValue = new ArrayList<>();  //数据点的坐标值
	List<relateNode> knn = new ArrayList<relateNode>();
	List<relateNode> krnn = new ArrayList<relateNode>();

	private double lrd = 0.0;// 局部可达密度
	private double reachDis = 0.0;// 可达距离
	private double lof = 0.0;  //局部异常因子

	public Node() {

	}

	/*
	 * public double getScore() { return score; }
	 * 
	 * public void setScore(double score) { this.score = score; }
	 */

	public double getLrd() {
		return lrd;
	}

	public void setLrd(double lrd) {
		this.lrd = lrd;
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

	public ArrayList<Double> getDataValue() {
		return dataValue;
	}

	public void setDataValue(ArrayList<Double> dataValue) {
		this.dataValue = dataValue;
	}
}
