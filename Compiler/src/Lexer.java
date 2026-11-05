import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

	private static final Map<String, String> EXPLANATIONS = new HashMap<>();

	static {
		// SNAKE keywords
		EXPLANATIONS.put("Snk_Begin", "program start keyword");
		EXPLANATIONS.put("Snk_End", "program end keyword");
		EXPLANATIONS.put("Snk_Int", "integer type declaration keyword");
		EXPLANATIONS.put("Snk_Real", "real type declaration keyword");
		EXPLANATIONS.put("Snk_Strg", "string type declaration keyword");
		EXPLANATIONS.put("Set", "assignment keyword");
		EXPLANATIONS.put("Get", "Get instruction keyword");
		EXPLANATIONS.put("from", "Get instruction keyword");
		EXPLANATIONS.put("If", "conditional keyword");
		EXPLANATIONS.put("Else", "else keyword");
		EXPLANATIONS.put("Begin", "block start");
		EXPLANATIONS.put("End", "block end");
		EXPLANATIONS.put("Snk_Print", "print keyword");

		// Symbols
		EXPLANATIONS.put(",", "separator");
		EXPLANATIONS.put("#", "end of instruction");
		EXPLANATIONS.put("[", "condition start");
		EXPLANATIONS.put("]", "condition end");

		// Operators
		EXPLANATIONS.put("<", "comparison operator");
		EXPLANATIONS.put(">", "comparison operator");
		EXPLANATIONS.put("=", "comparison operator");
		EXPLANATIONS.put("!=", "comparison operator");
	}

	// Order matters: comment line, string, real, integer, identifier, "!=", single symbols,
	// and finally any other non-blank character (reported as "unknown" instead of being skipped).
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
			return "comment";
		}
		String known = EXPLANATIONS.get(token);
		if (known != null) {
			return known;
		}
		if (token.matches("\\d+")) {
			return "integer number";
		} else if (token.matches("\\d+\\.\\d+")) {
			return "real number";
		} else if (token.matches("\"[^\"]*\"")) {
			return "string literal";
		} else if (token.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
			return "identifier";
		} else if (token.equals("\"")) {
			return "unterminated string";
		}
		return "unknown";
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
