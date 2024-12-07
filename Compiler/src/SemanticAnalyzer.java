public class SemanticAnalyzer {
	public String analyze(String code) {
		StringBuilder result = new StringBuilder("Semantic Analysis Result:\n");
		String[] lines = code.split("\n");

		boolean variableDeclared = false;

		for (String line : lines) {
			if (line.startsWith("Snk_Int") || line.startsWith("Snk_Real")) {
				variableDeclared = true;
				result.append("Variable declaration detected: ").append(line.trim()).append("\n");
			} else if (line.contains("Set") && !variableDeclared) {
				result.append("Error: Variable used before declaration. Line: ").append(line.trim()).append("\n");
			}
		}

		return result.toString();
	}
}
