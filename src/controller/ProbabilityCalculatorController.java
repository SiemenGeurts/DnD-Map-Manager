package controller;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.function.Function;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ProbabilityCalculatorController {

	@FXML
	private VBox vbox;
	@FXML
	private TextField txtDice;
	@FXML
	private TextArea txtOutput;
	@FXML
	private CheckBox chkboxHigher;
	@FXML
	private CheckBox chkboxLower;
	@FXML
	private Button btnCalculate;

	private BarChart<String, Number> barchart;
	private final XYChart.Series<String, Number> dataSeries = new XYChart.Series<String, Number>();

	@FXML
	void initialize() {
		chkboxHigher.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal)
				chkboxLower.setSelected(false);
		});
		chkboxLower.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal)
				chkboxHigher.setSelected(false);
		});
		CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel("Roll outcome");

		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Probability");

		barchart = new BarChart<String, Number>(xAxis, yAxis);
		vbox.getChildren().add(barchart);
		VBox.setVgrow(barchart, Priority.SOMETIMES);
		barchart.setVisible(false);
		barchart.setCategoryGap(1);
		barchart.setBarGap(1);
		barchart.getData().add(dataSeries);
	}

	final int GREATER = 1, LESS = 2, EQUAL = 4;

	@FXML
	void onBtnCalculateClicked(ActionEvent event) {
		StringBuilder output = new StringBuilder();
		String input = txtDice.getText().replaceAll("\\s+", "");
		String operator = "";
		int condition = 0;
		int flags = 0;
		int index = -1;
		if (input.matches(".+>\\d*")) {
			flags |= GREATER;
			index = input.indexOf(">");
			operator = ">";
			chkboxHigher.setSelected(true);
		} else if (input.matches(".+>=\\d*")) {
			flags |= GREATER | EQUAL;
			index = input.indexOf(">=");
			operator = "\u2265";
			chkboxHigher.setSelected(true);
		} else if (input.matches(".+<\\d*")) {
			flags |= LESS;
			index = input.indexOf("<");
			operator = "<";
			chkboxLower.setSelected(true);
		} else if (input.matches(".+<=\\d*")) {
			flags |= LESS | EQUAL;
			index = input.indexOf("<=");
			operator = "\u2264";
			chkboxLower.setSelected(true);
		}
		if(index>0) {
			condition = Integer.valueOf(input.substring(index + ((flags & EQUAL) == EQUAL ? 2 : 1)));
			input = input.substring(0, index);
		}
		if (input.length() == 0)
			return;
		
		String[] parts = input.split("\\+");
		int shift = 0;
		ArrayList<Die> dices = new ArrayList<>();
		for (int i = 0; i < parts.length; i++) {
			try {
				shift += Integer.valueOf(parts[i]);
			} catch (NumberFormatException e) {
				if (parts[i].matches("\\d+d\\d+")) {
					index = parts[i].indexOf("d");
					int sides = Integer.valueOf(parts[i].substring(index + 1));
					if ((sides > 3 && sides < 13 && sides % 2 == 0) || sides == 20 || sides == 100)
						dices.add(new Die(sides, Integer.valueOf(parts[i].substring(0, index))));
					else
						output.append(parts[i]).append(" is not a valid roll");
				} else
					output.append(parts[i]).append(" is not a valid roll");
			}
		}
		if (output.length() == 0) {
			float avg = shift;
			int min = shift;
			int max = shift;
			float[] g = null;
			for (Die die : dices) {
				avg += die.avg();
				min += die.min();
				max += die.max();
				g = multiply_generators(g, die.generator());
			}
			output.append("avg=").append(String.format("%.2f", avg)).append(System.lineSeparator());
			output.append("min=").append(String.format("%d", min)).append(System.lineSeparator());
			output.append("max=").append(String.format("%d", max)).append(System.lineSeparator())
					.append(System.lineSeparator());

			int sum = 0;
			for (int i = 1; i < g.length; i++)
				sum += g[i];
			
			if(flags!=0) {
				output.append("P(").append(operator).append(condition).append(")=");
				if((flags & GREATER) == GREATER && (flags & EQUAL) == EQUAL)
					condition--;
				else if((flags & LESS) == LESS && (flags & EQUAL) != EQUAL)
					condition--;
				int posibilities = 0;
				for(int i = 1; i < condition; i++)
					posibilities += g[i];
				if((flags & GREATER) == GREATER)
					output.append((1-posibilities/(float)sum)*100);
				if((flags & LESS) == LESS)
					output.append(posibilities/(float)sum*100);
				output.append("%").append(System.lineSeparator()).append(System.lineSeparator());
			}
			
			dataSeries.setName(input);
			dataSeries.getData().clear();
			barchart.getData().clear();
			if (chkboxHigher.isSelected()) {
				int totalProb = 0;
				for (int i = 1; i < g.length; i++) {
					float probs = (1 - totalProb / (float) sum) * 100;
					if (probs != 0) {
						output.append(String.format("P(\u2265%d)=%.3f", i + shift, probs));
						output.append('%').append(System.lineSeparator());
						dataSeries.getData().add(new XYChart.Data<>(String.format("\u2265%d", i + shift), probs));
					}
					totalProb += g[i];
				}
			} else if (chkboxLower.isSelected()) {
				int totalProb = 0;
				for (int i = 1; i < g.length; i++) {
					totalProb += g[i];
					float probs = totalProb / (float) sum * 100;
					if (totalProb != 0) {
						output.append(String.format("P(\u2264%d)=%.3f", i + shift, probs));
						output.append('%').append(System.lineSeparator());
						dataSeries.getData().add(new XYChart.Data<>(String.format("\u2264%d", i + shift), probs));
					}
				}
			} else {
				for (int i = 1; i < g.length; i++) {
					g[i] /= sum;
					float probs = g[i] * 100;
					if (g[i] != 0) {
						output.append(String.format("P(%d)=%.3f", i + shift, probs));
						output.append('%').append(System.lineSeparator());
						dataSeries.getData().add(new XYChart.Data<>(String.format("%d", i + shift), probs));
					}
				}
			}
			barchart.getData().add(dataSeries);
			barchart.setVisible(true);
		}
		txtOutput.setText(output.toString());
	}

	public static BigInteger binom(int x, int y) {
		if (y < 0 || y > x)
			return BigInteger.ZERO;
		if (y == 0 || y == x)
			return BigInteger.ONE;

		BigInteger answer = BigInteger.ONE;
		for (int i = x - y + 1; i <= x; i++)
			answer = answer.multiply(BigInteger.valueOf(i));
		for (int j = 1; j <= y; j++)
			answer = answer.divide(BigInteger.valueOf(j));
		return answer;
	}

	private float[] multiply_generators(float[] a, float[] b) {
		if (a == null)
			return b;
		float[] c = new float[a.length + b.length - 1];
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < b.length; j++)
				c[i + j] += a[i] * b[j];
		return c;
	}

	class Die {
		private int sides, amount;

		public Die(int sides, int amount) {
			this.sides = sides;
			this.amount = amount;
		}

		public int max() {
			return sides * amount;
		}

		public int min() {
			return amount;
		}

		public float avg() {
			return ((sides + 1) / 2) * amount;
		}

		public double std() {
			double sum = 0;
			for (float i = avg() / amount - 1; i > 0; i--)
				sum += i * i;
			sum *= 2 / sides * amount;
			return Math.sqrt(sum);
		}

		// def generator_generator(self):
		// return lambda p : np.sum([(-1)**k*binom(self.amount,
		// k)*binom(p-self.sides*k-1, self.amount-1) for k in range(0,
		// int(np.floor((p-self.amount)/self.sides))+1)])/self.sides**self.amount
		private Function<Integer, Integer> generatorGenerator() {
			return p -> {
				BigInteger sum = BigInteger.ZERO;
				for (int k = 0; k < Math.floor((p - amount) / sides) + 1; k++) {
					BigInteger term = binom(amount, k).multiply(binom(p - sides * k - 1, amount - 1));
					if (k % 2 == 0)
						sum = sum.add(term);
					else
						sum = sum.subtract(term);
				}
				return sum.intValue();
			};
		}

		public float[] generator() {
			float[] g = new float[sides * amount + 1];
			Function<Integer, Integer> generator = generatorGenerator();
			for (int i = min(); i <= max(); i++)
				g[i] = generator.apply(i);
			return g;
		}

		@Override
		public String toString() {
			return amount + "d" + sides;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Die)
				return ((Die) other).sides == sides && ((Die) other).amount == amount;
			return false;
		}
	}
}