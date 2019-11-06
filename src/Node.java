
import java.util.ArrayList;
import java.util.List;

public class Node {
	private int index = 0;
	ArrayList<Double> dataValue = new ArrayList<>();
	List<relateNode> knn = new ArrayList<relateNode>();
	List<relateNode> krnn = new ArrayList<relateNode>();

	private double lrd = 0.0;
	private double reachDis = 0.0;
	private double lof = 0.0;

	public Node() {

	}

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
