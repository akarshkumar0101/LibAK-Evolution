package machinelearning.ne.neat.genome;

public class BaseTemplate {
	// bias here is 0
	private final boolean hasBias;
	// inputs are 1..numInputNodes
	private final int numInputNodes;
	// outputs are numInputNodes+1..numInputNodes+numOutputNodes
	private final int numOutputNodes;

	public BaseTemplate(int numInputNodes, int numOutputNodes) {
		this(true, numInputNodes, numOutputNodes);
	}

	public BaseTemplate(boolean hasBias, int numInputNodes, int numOutputNodes) {
		this.hasBias = hasBias;
		this.numInputNodes = numInputNodes;
		this.numOutputNodes = numOutputNodes;
	}

	public boolean hasBias() {
		return this.hasBias;
	}

	public int numInputNodes() {
		return this.numInputNodes;
	}

	public int numOutputNodes() {
		return this.numOutputNodes;
	}

	@Override
	public boolean equals(Object another) {
		if (another instanceof BaseTemplate) {
			BaseTemplate anotherBT = (BaseTemplate) another;
			if (this.hasBias == anotherBT.hasBias && this.numInputNodes == anotherBT.numInputNodes
					&& this.numOutputNodes == anotherBT.numOutputNodes)
				return true;
		}
		return false;
	}
}
