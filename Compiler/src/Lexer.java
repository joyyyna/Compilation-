import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

	private static final Map<String, String> EXPLANATIONS = new HashMap<>();

	static {
		// SNAKE keywords
		EXPLANATIONS.put("Snk_Begin", "mot clé de début de programme");
		EXPLANATIONS.put("Snk_End", "mot clé de fin de programme");
		EXPLANATIONS.put("Snk_Int", "mot clé de déclaration du type entier");
		EXPLANATIONS.put("Snk_Real", "mot clé de déclaration du type réel");
		EXPLANATIONS.put("Snk_Strg", "mot clé de déclaration du type chaîne");
		EXPLANATIONS.put("Set", "mot clé pour affectation d’une valeur");
		EXPLANATIONS.put("Get", "mot clé de l'instruction Get");
		EXPLANATIONS.put("from", "mot clé de l'instruction Get");
		EXPLANATIONS.put("If", "mot clé pour conditionnel");
		EXPLANATIONS.put("Else", "mot clé pour sinon");
		EXPLANATIONS.put("Begin", "début de bloc");
		EXPLANATIONS.put("End", "fin de bloc");
		EXPLANATIONS.put("Snk_Print", "mot clé d'affichage");

		// Symbols
		EXPLANATIONS.put(",", "séparateur");
		EXPLANATIONS.put("#", "fin d’instruction");
		EXPLANATIONS.put("[", "début de condition");
		EXPLANATIONS.put("]", "fin de condition");

		// Operators
		EXPLANATIONS.put("<", "opérateur de comparaison");
		EXPLANATIONS.put(">", "opérateur de comparaison");
		EXPLANATIONS.put("=", "opérateur de comparaison");
		EXPLANATIONS.put("!=", "opérateur de comparaison");
	}

	// Order matters: comment line, string, real, integer, identifier, "!=", single symbols,
	// and finally any other non-blank character (reported as "inconnu" instead of being skipped).
	private static final Pattern TOKEN = Pattern.compile(
			"(?m)^[ \\t]*##[^\\r\\n]*"
			+ "|\"[^\"\\r\\n]*\""
			+ "|\\d+\\.\\d+"
			+ "|\\d+"
			+ "|[a-zA-Z][a-zA-Z0-9_]*"
			+ "|!="
			+ "|[<>=#\\[\\],]"
			+ "|\\S");

	private String getTokenExplanation(String token) {
		if (token.startsWith("##")) {
			return "commentaire";
		}
		String known = EXPLANATIONS.get(token);
		if (known != null) {
			return known;
		}
		if (token.matches("\\d+")) {
			return "nombre entier";
		} else if (token.matches("\\d+\\.\\d+")) {
			return "nombre réel";
		} else if (token.matches("\"[^\"]*\"")) {
			return "chaîne de caractères";
		} else if (token.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
			return "identificateur";
		} else if (token.equals("\"")) {
			return "chaîne non terminée";
		}
		return "inconnu";
	}

	public String tokenize(String input) {
		StringBuilder result = new StringBuilder();
		Matcher matcher = TOKEN.matcher(input);

		while (matcher.find()) {
			String token = matcher.group().trim();
			result.append(token).append(": ").append(getTokenExplanation(token)).append("\n");
		}
		return result.toString();
	}

	public static void main(String[] args) {
		Lexer lexer = new Lexer();

		String testCode = "Snk_Begin\n"
				+ "Snk_Int i, j #\n"
				+ "Snk_Real x1 #\n"
				+ "Snk_Strg s #\n"
				+ "Set i 10 #\n"
				+ "If [ i != 20 ]\n"
				+ "Set j 5 #\n"
				+ "Else\n"
				+ "Begin\n"
				+ "Set x1 15.5 #\n"
				+ "End\n"
				+ "Snk_Print \"Hello, World!\" #\n"
				+ "Snk_End";

		System.out.println(lexer.tokenize(testCode));
	}
}
