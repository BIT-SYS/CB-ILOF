
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author zouzhongfan
 *
 */
public class DataNode {
	private String nodeName; // ��������
	private double[] dimensioin; // �������ά��
	private double kDistance; // k-����
	private List<DataNode> kNeighbor = new ArrayList<DataNode>();// k-����
	private double distance; // ���������ŷ����þ���
	private double reachDensity;// �ɴ��ܶ�
	private double reachDis;// �ɴ����
	private double lof;// �ֲ���Ⱥ����

	public DataNode() {

	}

	public DataNode(String nodeName, double[] dimensioin) {
		this.nodeName = nodeName;
		this.dimensioin = dimensioin;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public double[] getDimensioin() {
		return dimensioin;
	}

	public void setDimensioin(double[] dimensioin) {
		this.dimensioin = dimensioin;
	}

	public double getkDistance() {
		return kDistance;
	}

	public void setkDistance(double kDistance) {
		this.kDistance = kDistance;
	}

	public List<DataNode> getkNeighbor() {
		return kNeighbor;
	}

	public void setkNeighbor(List<DataNode> kNeighbor) {
		this.kNeighbor = kNeighbor;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getReachDensity() {
		return reachDensity;
	}

	public void setReachDensity(double reachDensity) {
		this.reachDensity = reachDensity;
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

}