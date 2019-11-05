import java.util.ArrayList;

public class grid {
	private int index = 0; //琛ㄧず鍦╣ridlist涓槸绗嚑涓�
	private ArrayList<Double> coorvalue = new ArrayList<>();
	private double distance = 0.0;// 璺濈
	private double score = 0.0; // 鍗犵殑姣旈噸
	private double reachdist = 0.0; // 鍒拌繖涓偣鐨勫彲杈捐窛绂�
	private double reachdistTo = 0.0; // 锟斤拷锟斤拷愕斤拷锟侥碉拷目纱锟斤拷锟斤拷


//	public grid() {
//		System.out.println("hi");
//	}
	
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

	public double getReachdistTo() {
		return reachdistTo;
	}

	public void setReachdistTo(double reachdistTo) {
		this.reachdistTo = reachdistTo;
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

	// 锟斤拷锟斤拷锟斤拷确指睿瑇, y锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷牡悖瑇_r锟斤拷y_r锟街憋拷锟斤拷锟斤拷锟斤拷维锟饺的半径
	// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷姆锟轿�(x-x_r, x+x_r)(y-y_r,y+y_r)
	// double x;
	// double y;
	// double x_r;
	// double y_r;

}
