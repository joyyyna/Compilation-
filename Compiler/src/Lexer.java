import java.util.regex.*;
import java.util.HashSet;

public class Lexer {

	private static final HashSet<String> KEYWORDS = new HashSet<>();
	private static final HashSet<String> SYMBOLS = new HashSet<>();
	private static final HashSet<String> OPERATORS = new HashSet<>();

	static {
		// Add SNAKE language keywords
		KEYWORDS.add("Snk_Begin");
		KEYWORDS.add("Snk_End");
		KEYWORDS.add("Begin");
		KEYWORDS.add("Snk_Int");
		KEYWORDS.add("Snk_Real");
		KEYWORDS.add("Set");
		KEYWORDS.add("If");
		KEYWORDS.add("Else");
		KEYWORDS.add("End");
		KEYWORDS.add("Snk_Print");

		// Add symbols
		SYMBOLS.add(",");
		SYMBOLS.add("#");
		SYMBOLS.add("[");
		SYMBOLS.add("]");

		// Add operators
		OPERATORS.add("<");
		OPERATORS.add(">");
		OPERATORS.add("=");
		OPERATORS.add("!=");
	}

	private String getTokenExplanation(String token) {
		// Return the explanation for each token
		switch (token) {
		case "Snk_Begin":
			return "mot clé de début de programme";
		case "Snk_End":
			return "mot clé de fin de programme";
		case "Snk_Real":
			return "mot clé de déclaration du type réel";
		case "Snk_Int":
			return "mot clé de déclaration du type entier";
		case "Set":
			return "mot clé pour affectation d’une valeur";
		case "If":
			return "mot clé pour conditionnel";
		case "Snk_Print":
			return "mot clé d'affichage";
		case "Begin":
			return "début de bloc";
		case "Else":
			return "mot clé pour sinon";
		case "End":
			return "mot clé";
		case ",":
			return "séparateur";
		case "#":
			return "fin d’instruction";
		case "[":
		case "]":
			return "début/fin de condition";
		case "<":
		case ">":
		case "=":
		case "!=":
			return "opérateur de comparaison";
		default:
			if (token.matches("\\d+")) {
				return "nombre entier";
			} else if (token.matches("\\d+\\.\\d+")) {
				return "nombre réel";
			} else if (token.matches("\"[^\"]*\"")) {
				return "chaîne de caractères";
			} else if (token.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
				return "identificateur";
			}
			return "inconnu";
		}
	}

	public String tokenize(String input) {
		StringBuilder result = new StringBuilder();

		String regex = "\"[^\"]*\"|\\d+\\.\\d+|\\d+|[a-zA-Z][a-zA-Z0-9_]*|[<>!=#\\[\\],]";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);

		while (matcher.find()) {
			String token = matcher.group();
			// First check if the token is a keyword
			if (KEYWORDS.contains(token)) {
				result.append(token).append(": ").append(getTokenExplanation(token)).append("\n");
			}
			// Then check if the token is a symbol, operator, or identifier
			else if (SYMBOLS.contains(token)) {
				result.append(token).append(": ").append(getTokenExplanation(token)).append("\n");
			} else if (OPERATORS.contains(token)) {
				result.append(token).append(": ").append(getTokenExplanation(token)).append("\n");
			} else {
				result.append(token).append(": ").append(getTokenExplanation(token)).append("\n");
			}
		}
		return result.toString();
	}

	public static void main(String[] args) {
		Lexer lexer = new Lexer();

		String testCode = "Snk_Begin\n" + "Snk_Int i, j\n" + "# fin d’instruction\n" + "Snk_Real x1\n"
				+ "# fin d’instruction\n" + "Set i 10\n" + "# fin d’instruction\n" + "If [ i < 20 ]\n" + "Set j 5\n"
				+ "# fin d’instruction\n" + "Else\n" + "Begin\n" + "Set x1 15.5\n" + "# fin d’instruction\n" + "End\n"
				+ "Snk_Print \"Hello, World!\"\n" + "# fin d’instruction\n" + "Snk_End";

		// Tokenize and display the results
		String result = lexer.tokenize(testCode);
		System.out.println(result);
	}
}
