package machinelearning.ne.neat.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JComponent;

import data.tuple.Tuple2D;
import machinelearning.ne.neat.genome.Genome;
import machinelearning.ne.neat.network.NeuralNetwork;
import machinelearning.ne.neat.network.Neuron;
import math.AKMath;

public class VisualNEATNetworkPanel extends JComponent {

	private static final long serialVersionUID = 5395346094940056438L;

	private NeuralNetwork network;

	private final Map<Neuron, Tuple2D<Double, Double>> nodeLocations;

	private Double fitness;

	public VisualNEATNetworkPanel(NeuralNetwork network) {
		this.nodeLocations = new HashMap<>();
		this.setNetwork(network);
		this.fitness = null;
	}

	public VisualNEATNetworkPanel(Genome geno) {
		this(new NeuralNetwork(geno, null));
		this.fitness = geno.fitness;
	}

	private Tuple2D<Integer, Integer> locationOf(Neuron neuron) {
		Tuple2D<Double, Double> loc = this.nodeLocations.get(neuron);
		return new Tuple2D<>((int) (loc.getA() * this.getWidth()), (int) (loc.getB() * this.getHeight()));
	}

	@Override
	public void paintComponent(Graphics g) {
		if (this.network == null) {
			return;
		}
		g.setColor(Color.BLACK);
		g.drawString("" + this.network.networkID, 0, 15);

		if (this.fitness != null) {
			g.setColor(Color.MAGENTA);
			g.drawString("" + this.fitness, 3 * this.getWidth() / 5, 15);
		}

		for (int layer = 0; layer < 3; layer++) {
			List<Neuron> neurons = layer == 0 ? this.network.getInputNeurons()
					: layer == 1 ? this.network.getHiddenNeurons() : this.network.getOutputNeurons();

			for (int nodeID = 0; nodeID < neurons.size(); nodeID++) {
				int x = (int) AKMath.scale(layer, -0.5, 2.5, 0, this.getWidth());
				int y = (int) AKMath.scale(nodeID, -1, neurons.size(), 0, this.getHeight());

				x = this.locationOf(neurons.get(nodeID)).getA();
				y = this.locationOf(neurons.get(nodeID)).getB();

				int circledia = Math.max(10,
						Math.min(this.getWidth() / 4 / (3 + 1), this.getHeight() / 4 / neurons.size()));

				double activation = neurons.get(nodeID).getActivation();
				int grayscaleAct = (int) AKMath.scale(activation, 0, 1, 0, 255);
				g.setColor(new Color(grayscaleAct, grayscaleAct, grayscaleAct));
				g.fillOval(x - circledia / 2, y - circledia / 2, circledia, circledia);

				int realNodeID = nodeID;
				if (layer >= 1) {
					realNodeID += this.network.getInputNeurons().size();
				}
				if (layer == 1) {
					realNodeID += this.network.getOutputNeurons().size();
				}
				if (!this.network.hasBias()) {
					realNodeID += 1;
				}

				// first drawString call takes a long time, this is the delay
				int fontSize = 20;
				g.setColor(Color.RED);
				g.drawString("" + realNodeID, x - fontSize / 2, y);

			}

		}
		for (int i = 0; i < this.network.getInputNeurons().size() + this.network.getHiddenNeurons().size()
				+ this.network.getOutputNeurons().size(); i++) {
			Neuron neuron = i < this.network.getInputNeurons().size() ? this.network.getInputNeurons().get(i)
					: i < this.network.getInputNeurons().size() + this.network.getHiddenNeurons().size()
							? this.network.getHiddenNeurons().get(i - this.network.getInputNeurons().size())
							: this.network.getOutputNeurons().get(
									i - this.network.getInputNeurons().size() - this.network.getHiddenNeurons().size());
			for (Neuron prevNeuron : neuron.getPrevConnections().keySet()) {
				Tuple2D<Integer, Integer> locNeuron = this.locationOf(neuron);
				Tuple2D<Integer, Integer> locPrevNeuron = this.locationOf(prevNeuron);

				int midx = (locNeuron.getA() + locPrevNeuron.getA()) / 2,
						midy = (locNeuron.getB() + locPrevNeuron.getB()) / 2;

				g.setColor(Color.ORANGE);
				g.drawLine(locPrevNeuron.getA(), locPrevNeuron.getB(), midx, midy);

				g.setColor(Color.GREEN);
				g.drawLine(midx, midy, locNeuron.getA(), locNeuron.getB());
			}

		}

	}

	public NeuralNetwork getNetwork() {
		return this.network;
	}

	public void setNetwork(NeuralNetwork network) {
		this.network = network;

		this.nodeLocations.clear();

		if (network == null) {
			return;
		}
		List<Neuron> inputNeurons = network.getInputNeurons();
		List<Neuron> hiddenNeurons = network.getHiddenNeurons();
		List<Neuron> outputNeurons = network.getOutputNeurons();

		// Random random = new Random(network.networkID);
		Random random = new Random(44324324);

		for (int i = 0; i < inputNeurons.size(); i++) {
			Neuron neuron = inputNeurons.get(i);
			double x = 0.1;
			double y = AKMath.scale(i, -0.5, inputNeurons.size() - 0.5, 0, 1);
			Tuple2D<Double, Double> location = new Tuple2D<>(x, y);
			this.nodeLocations.put(neuron, location);
		}
		for (int i = 0; i < outputNeurons.size(); i++) {
			Neuron neuron = outputNeurons.get(i);
			double x = 0.9;
			double y = AKMath.scale(i, -0.5, outputNeurons.size() - 0.5, 0, 1);
			Tuple2D<Double, Double> location = new Tuple2D<>(x, y);
			this.nodeLocations.put(neuron, location);
		}

		for (int i = 0; i < hiddenNeurons.size(); i++) {
			Neuron neuron = hiddenNeurons.get(i);
			double x = AKMath.scale(i, -1.5, hiddenNeurons.size() + 0.5, 0, 1);
			double y = AKMath.scale(random.nextDouble(), 0, 1, 0.1, 0.9);
			Tuple2D<Double, Double> location = new Tuple2D<>(x, y);
			this.nodeLocations.put(neuron, location);
		}
	}

}
