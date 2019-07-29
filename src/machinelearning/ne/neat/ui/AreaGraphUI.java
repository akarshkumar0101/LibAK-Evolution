package machinelearning.ne.neat.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import data.function.Function2D;
import math.AKMath;
import math.AKRandom;

//S is for Species (type)
public class AreaGraphUI<S> extends JComponent {

	private static final long serialVersionUID = 8034352058463249483L;

	private Set<S> species;

	private final Map<S, Color> speciesColors;

	private final Function2D<Integer, S, Double> getNumInSpecies;

	private int numGenerations;

	public AreaGraphUI(List<Map<S, Double>> data) {
		this.numGenerations = data.size();

		this.species = new HashSet<>();

		for (Map<S, Double> generationData : data) {
			this.species.addAll(generationData.keySet());
		}

		this.getNumInSpecies = (generation, spec) -> {
			Map<S, Double> gen = data.get(generation);
			return gen.containsKey(spec) ? gen.get(spec) : 0.0;
		};

		this.speciesColors = this.getRandomSpeciesColors(this.species);
	}

	public AreaGraphUI(int numGenerations, Collection<S> species, Function2D<Integer, S, Double> getNumInSpecies) {
		this(numGenerations, new HashSet<>(species), getNumInSpecies);
	}

	public AreaGraphUI(int numGenerations, Set<S> species, Function2D<Integer, S, Double> getNumInSpecies) {
		this.numGenerations = numGenerations;
		this.getNumInSpecies = getNumInSpecies;
		this.species = species;

		this.speciesColors = this.getRandomSpeciesColors(species);
	}

	private Map<S, Color> getRandomSpeciesColors(Set<S> species) {
		Map<S, Color> speciesColors = new HashMap<>();
		for (S spec : species) {
			Color color = new Color((int) AKRandom.randomNumber(0.0, 255), (int) AKRandom.randomNumber(0.0, 255),
					(int) AKRandom.randomNumber(0.0, 255));
			speciesColors.put(spec, color);
		}
		return speciesColors;
	}

	private double getNumInSpecies(int generation, S spec) {
		return this.getNumInSpecies.evaluate(generation, spec);
	}

	private double sumOrganismsInGeneration(int generation) {
		double sum = 0.0;
		for (S spec : this.species) {
			sum += this.getNumInSpecies(generation, spec);
		}

		return sum;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int numGenerations = this.numGenerations;

		for (int generation = 0; generation < numGenerations - 1; generation++) {
			double sumLeft = this.sumOrganismsInGeneration(generation);
			double sumRight = this.sumOrganismsInGeneration(generation + 1);

			double currentLeft = 0.0;
			double currentRight = 0.0;

			for (S spec : this.species) {
				double numInSpeciesLeft = this.getNumInSpecies(generation, spec);
				double numInSpeciesRight = this.getNumInSpecies(generation + 1, spec);

				double nextCurrentLeft = currentLeft + numInSpeciesLeft;
				double nextCurrentRight = currentRight + numInSpeciesRight;

				int leftX = (int) AKMath.scale(generation, 0.0, numGenerations - 1, 0.0, this.getWidth());
				int rightX = (int) AKMath.scale(generation + 1, 0.0, numGenerations - 1, 0.0, this.getWidth());

				int topLeftY = (int) AKMath.scale(currentLeft, 0.0, sumLeft, this.getHeight(), 0.0);
				int topRightY = (int) AKMath.scale(currentRight, 0.0, sumRight, this.getHeight(), 0.0);
				int botRightY = (int) AKMath.scale(nextCurrentRight, 0.0, sumRight, this.getHeight(), 0.0);
				int botLeftY = (int) AKMath.scale(nextCurrentLeft, 0.0, sumLeft, this.getHeight(), 0.0);

				// points defined as top left, top right, bot right, bot left
				int[] xPoints = { leftX, rightX, rightX, leftX };
				int[] yPoints = { topLeftY, topRightY, botRightY, botLeftY };

				g.setColor(this.speciesColors.get(spec));
				g.fillPolygon(xPoints, yPoints, 4);
				g.setColor(Color.BLACK);
				g.drawLine(leftX, topLeftY, rightX, topRightY);
				g.drawLine(leftX, botLeftY, rightX, botRightY);

				currentLeft = nextCurrentLeft;
				currentRight = nextCurrentRight;
			}

		}

	}
}
