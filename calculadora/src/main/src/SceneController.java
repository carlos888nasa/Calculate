import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;

public class SceneController {

    @FXML
    private Label displayLabel;
    private final StringBuilder currentText = new StringBuilder();
    private boolean lastWasResult = false;
    private boolean lastPressedAns = false;
    private double lastAnswer = 0;

    @FXML
    private void handleNumberButton(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String input = clickedButton.getText();

        switch (input) {
            case "AC":
                currentText.setLength(0);
                displayLabel.setText("");
                lastWasResult = false;
                lastPressedAns = false;
                break;

            case "DEL":
                if (!currentText.isEmpty()) {
                    currentText.deleteCharAt(currentText.length() - 1);
                }
                lastPressedAns = false;
                displayLabel.setText(currentText.toString());
                break;

            case "=":
                try {
                    String expression = currentText.toString();

                    if (expression.contains("Ans")) {
                        expression = expression.replace("Ans", String.valueOf(lastAnswer));
                    }

                    double result = calculateExpression(expression);
                    String formatted = getString(result, displayLabel);

                    displayLabel.setText(formatted);
                    currentText.setLength(0);
                    currentText.append(result);
                    lastAnswer = result;
                    lastWasResult = true;
                    lastPressedAns = false;

                } catch (Exception e) {
                    displayLabel.setText("Error");
                    currentText.setLength(0);
                    lastWasResult = false;
                    lastPressedAns = false;
                }
                break;

            case "Ans":
                if (lastWasResult) {
                    currentText.setLength(0);
                    lastWasResult = false;
                }
                currentText.append("Ans");
                displayLabel.setText(currentText.toString());
                lastPressedAns = true;
                break;

            case ".":
                if (currentText.isEmpty() || endsWithOperator()) {
                    currentText.append("0.");
                } else if (!lastNumberHasDecimal()) {
                    currentText.append(".");
                }
                displayLabel.setText(currentText.toString());
                lastPressedAns = false;
                break;

            default:
                if (lastWasResult) {
                    if (isOperator(input)) {
                        String ansExpression = "Ans" + input;
                        currentText.setLength(0);
                        currentText.append(ansExpression);
                        lastWasResult = false;
                        lastPressedAns = true;
                    } else {
                        currentText.setLength(0);
                        currentText.append(input);
                        lastWasResult = false;
                    }
                } else if (lastPressedAns) {
                    currentText.append(input);
                    lastPressedAns = false;
                } else {
                    currentText.append(input);
                }
                displayLabel.setText(currentText.toString());
                break;
        }
    }

    private static String getString(double result, Label label) {
        java.text.DecimalFormat normalFormat = new java.text.DecimalFormat("0.##########");
        normalFormat.setMaximumFractionDigits(10);
        normalFormat.setMinimumFractionDigits(0);

        java.text.DecimalFormat sciFormat = new java.text.DecimalFormat("0.#########E0");

        String candidate;
        if (result % 1 == 0) {
            candidate = String.valueOf((long) result);
        } else {
            candidate = normalFormat.format(result);
        }

        Text textNode = new Text(candidate);
        textNode.setFont(label.getFont());

        if (textNode.getLayoutBounds().getWidth() > label.getWidth()) {
            candidate = sciFormat.format(result);
        }

        return candidate;
    }

    private boolean isOperator(String text) {
        return text.equals("+") || text.equals("-") || text.equals("x") || text.equals("÷");
    }

    private double calculateExpression(String expr) {
        String processedExpr = expr.replace(" ", "");
        if (processedExpr.startsWith("-")) {
            processedExpr = "0" + processedExpr;
        }
        processedExpr = processedExpr.replace("(-", "(0-");

        String[] parts = processedExpr.split("(?=[+-])|(?<=[+-])");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                tokens.add(part.trim());
            }
        }

        double total = 0;
        String currentOperator = "+";

        for (String token : tokens) {
            if (token.equals("+") || token.equals("-")) {
                currentOperator = token;
            } else {
                double value = multiplyDivide(token);
                if (currentOperator.equals("+")) {
                    total += value;
                } else {
                    total -= value;
                }
            }
        }
        return total;
    }

    private double multiplyDivide(String expr) {
        String[] parts = expr.split("(?=[x÷])|(?<=[x÷])");
        double result = 1.0;
        String currentOperator = "";

        for (String part : parts) {
            part = part.trim();
            if (part.equals("x") || part.equals("÷")) {
                currentOperator = part;
            } else if (!part.isEmpty()) {
                double value = Double.parseDouble(part);
                switch (currentOperator) {
                    case "" -> result = value;
                    case "x" -> result *= value;
                    case "÷" -> {
                        if (value == 0) throw new ArithmeticException("División por cero");
                        result /= value;
                    }
                }
            }
        }
        return result;
    }

    private boolean endsWithOperator() {
        if (currentText.isEmpty()) return false;
        char last = currentText.charAt(currentText.length() - 1);
        return "+-x÷".indexOf(last) != -1;
    }

    private boolean lastNumberHasDecimal() {
        String[] parts = currentText.toString().split("[+\\-x÷]");
        if (parts.length == 0) return false;
        String lastPart = parts[parts.length - 1];
        return lastPart.contains(".");
    }
}