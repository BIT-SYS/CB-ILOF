import java.util.ArrayList;

public class grid {
	private int index = 0;
	private ArrayList<Double> coorvalue = new ArrayList<>(); // coordinate of cube
	private double distance = 0.0; // real distance
	private double score = 0.0; // contribution factor
	private double reachdist = 0.0; // reachability distance between two cubes

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getReachdist() {
		return reachdist;
	}

	public void setReachdist(double reachdist) {
		this.reachdist = reachdist;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public ArrayList<Double> getCoorvalue() {
		return coorvalue;
	}

	public void setCoorvalue(ArrayList<Double> coorvalue) {
		this.coorvalue = coorvalue;
	}

}
