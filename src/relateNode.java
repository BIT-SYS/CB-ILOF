import java.util.ArrayList;

public class relateNode {
	private int index;
	private ArrayList<Double> data = new ArrayList<>();
	private double distance = 0.0;
	private double reachdist = 0.0;
	private double reachdistTo = 0.0;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getReachdist() {
		return reachdist;
	}

	public void setReachdist(double reachdist) {
		this.reachdist = reachdist;
	}

	public double getReachdistTo() {
		return reachdistTo;
	}

	public void setReachdistTo(double reachdistTo) {
		this.reachdistTo = reachdistTo;
	}

	public ArrayList<Double> getData() {
		return data;
	}

	public void setData(ArrayList<Double> data) {
		this.data = data;
	}

}
