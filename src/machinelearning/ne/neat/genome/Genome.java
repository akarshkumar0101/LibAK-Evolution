package machinelearning.ne.neat.genome;

import java.util.ArrayList;
import java.util.List;

import data.tuple.Tuple2D;

public class Genome extends ArrayList<ConnectionGene> {

	private static final long serialVersionUID = -5784317383291473781L;

	public final long ID;

	private final BaseTemplate baseTemplate;

	// private final List<ConnectionGene> genes;

	// bias here is 0

	// inputs are 1..numInputNodes

	// outputs are numInputNodes+1..numInputNodes+numOutputNodes

	// hidden are
	// numInputNodes+numOutputNodes+1..numInputNodes+numOutputNodes+numHiddenNodes
	private int numHiddenNodes;

	public double fitness = 0.0;

	/*
	 * public Genome(BaseTemplate baseTemplate, int numHiddenNodes) { super();
	 * this.baseTemplate = baseTemplate; this.numHiddenNodes = numHiddenNodes; //
	 * this.nodeGenes = new ArrayList<>();
	 *
	 * this.ID = Genome.GenomeIDGenerator++; }
	 */

	public Genome(long ID, BaseTemplate baseTemplate, int numHiddenNodes) {
		super();
		this.baseTemplate = baseTemplate;
		this.numHiddenNodes = numHiddenNodes;

		this.ID = ID;
		this.fitness = Double.NaN;
	}

	public Genome(long ID, Genome startingPoint) {
		super();
		this.baseTemplate = startingPoint.baseTemplate;
		this.numHiddenNodes = startingPoint.numHiddenNodes;

		this.addAll(startingPoint);

		this.ID = ID;
		this.fitness = Double.NaN;
	}

	public boolean hasConnection(int inputNodeID, int outputNodeID) {
		for (ConnectionGene cg : this) {
			if (cg.getInputNodeID() == inputNodeID && cg.getOutputNodeID() == outputNodeID) {
				return true;
			}
		}
		return false;
	}

	public Tuple2D<Integer, Integer> complexity() {
		return new Tuple2D<>(this.numHiddenNodes, this.size());
	}

	public void calculateNumHiddenNodes() {
		int numHiddenNodes = 0;

		for (ConnectionGene cg : this) {
			numHiddenNodes = Math.max(numHiddenNodes, cg.getInputNodeID());
			numHiddenNodes = Math.max(numHiddenNodes, cg.getOutputNodeID());
		}
		numHiddenNodes -= this.baseTemplate.numInputNodes() + this.baseTemplate.numOutputNodes();

		this.numHiddenNodes = numHiddenNodes;
	}

	public void cleanup() {
		this.sort((o1, o2) -> o1.getInnovationNumber() - o2.getInnovationNumber());
		this.calculateNumHiddenNodes();
	}

	/**
	 * @return the id of the new hidden node
	 */
	public int addNewHiddenNode() {
		int newNodeID = this.getNumTotalNodes();
		if (!this.baseTemplate.hasBias()) {
			newNodeID++;
		}
		this.numHiddenNodes++;

		return newNodeID;
	}

	public int layerOf(int nodeID) {
		int layer = 0;
		if (nodeID > this.baseTemplate.numInputNodes()) {
			layer = 2;
		}
		if (nodeID > this.baseTemplate.numInputNodes() + this.baseTemplate.numOutputNodes()) {
			layer = 1;
		}
		return layer;
	}

	@Override
	public String toString() {
		return this.toString(false, true);
	}

	public String toString(boolean crossOverString, boolean showGenes) {
		String str = "";

		str += "Genome ID: " + this.ID + ", hidden nodes: " + this.numHiddenNodes + ", connections: " + this.size();
		str += '\n';

		str += "Fitness: " + this.fitness;
		str += '\n';

		if (!showGenes) {
			return str;
		}

		List<ConnectionGene> genes = new ArrayList<>();
		int listI = 0;
		for (int innov = 0; innov <= this.get(this.size() - 1).getInnovationNumber(); innov++) {
			if (innov == this.get(listI).getInnovationNumber()) {
				genes.add(this.get(listI));
				listI++;
			} else {
				if (crossOverString) {
					genes.add(null);
				}
			}

		}

		for (int i = 0; i < genes.size() * 7 + 1; i++) {
			str += "_";
		}
		str += '\n';

		for (int i = 0; i < genes.size(); i++) {
			str += "|";
			str += "  ";
			if (genes.get(i) != null) {
				str += String.format("%2d", genes.get(i).getInnovationNumber());
			} else {
				str += "  ";
			}
			str += "  ";
		}
		str += "|";
		str += '\n';

		for (int i = 0; i < genes.size(); i++) {
			str += "|";
			if (genes.get(i) != null) {
				str += String.format("%2d", genes.get(i).getInputNodeID());
			} else {
				str += "  ";
			}
			if (genes.get(i) != null) {
				str += "->";
			} else {
				str += "  ";
			}
			if (genes.get(i) != null) {
				str += String.format("%2d", genes.get(i).getOutputNodeID());
			} else {
				str += "  ";
			}
		}
		str += "|";
		str += '\n';

		for (int i = 0; i < genes.size(); i++) {
			str += "|";

			if (genes.get(i) != null) {
				if (genes.get(i).getConnectionWeight() >= 0) {
					str += " ";
				}
				str += String.format("%,.2f", genes.get(i).getConnectionWeight());
			} else {
				str += "     ";
			}
			str += " ";

		}
		str += "|";
		str += '\n';

		for (int i = 0; i < genes.size(); i++) {
			str += "|";
			str += "  ";

			if (genes.get(i) != null) {
				str += genes.get(i).isEnabled() ? "  " : " D";
			} else {
				str += "  ";
			}
			str += "  ";
		}
		str += "|";
		str += '\n';

		for (int i = 0; i < genes.size() * 7 + 1; i++) {
			str += "-";
		}
		str += '\n';

		return str;
	}

	public void insertGeneInOrder(ConnectionGene cg) {
		int numHiddenNodes = this.numHiddenNodes;
		numHiddenNodes = Math.max(numHiddenNodes, cg.getInputNodeID());
		numHiddenNodes = Math.max(numHiddenNodes, cg.getOutputNodeID());

		numHiddenNodes -= this.baseTemplate.numInputNodes() + this.baseTemplate.numOutputNodes();
		this.numHiddenNodes = numHiddenNodes;

		if (this.isEmpty()) {
			this.add(cg);
		} else {
			// check before case
			ConnectionGene first = this.get(0);
			ConnectionGene last = this.get(this.size() - 1);
			if (cg.getInnovationNumber() <= first.getInnovationNumber()) {
				this.add(0, cg);
			}
			// check after case

			else if (cg.getInnovationNumber() > last.getInnovationNumber()) {
				this.add(this.size(), cg);
			}

			// in between case
			else {
				for (int i = 1; i < this.size(); i++) {
					ConnectionGene before = this.get(i - 1);
					ConnectionGene after = this.get(i);
					if (cg.getInnovationNumber() > before.getInnovationNumber()
							&& cg.getInnovationNumber() <= after.getInnovationNumber()) {
						this.add(i, cg);
						break;
					}
				}
			}

		}
	}

	public List<ConnectionGene> getConnectionGenes() {
		return this;
	}

	public BaseTemplate getBaseTemplate() {
		return this.baseTemplate;
	}

	public void setNumHiddenNodes(int numHiddenNodes) {
		this.numHiddenNodes = numHiddenNodes;
	}

	public int getNumHiddenNodes() {
		return this.numHiddenNodes;
	}

	public int getNumTotalNodes() {
		return this.baseTemplate.numInputNodes() + this.numHiddenNodes + this.baseTemplate.numOutputNodes()
				+ (this.baseTemplate.hasBias() ? 1 : 0);
	}

	/*
	 * @Override public Genome clone() { Genome geno = new Genome(this.ID, this);
	 *
	 * geno.fitness = this.fitness; return geno; }
	 */

	@Override
	public boolean equals(Object another) {
		if (another instanceof Genome) {
			if (((Genome) another).ID == this.ID) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) this.ID;
	}
}
