package snake;

public class SemanticAnalyzer {
    private final java.util.Map<String, String> variableTypes = new java.util.HashMap<>(); // Store declared variable types
    private final java.util.regex.Pattern intPattern = java.util.regex.Pattern.compile("\\d+"); // Pattern for integers
    private final java.util.regex.Pattern realPattern = java.util.regex.Pattern.compile("\\d+\\.\\d+"); // Pattern for reals
    private final java.util.regex.Pattern stringPattern = java.util.regex.Pattern.compile("\"[^\"]*\""); // Pattern for strings
    private final java.util.regex.Pattern identifierPattern = java.util.regex.Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*"); // Valid identifier

    public String analyze(String code) {
        StringBuilder result = new StringBuilder("Semantic Analysis Result:\n");
        String[] lines = code.split("\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Snk_Int") || line.startsWith("Snk_Real") || line.startsWith("Snk_Strg")) {
                handleDeclaration(line, result);
            } else if (line.startsWith("Set")) {
                handleAssignment(line, result);
            } else if (line.startsWith("Get")) {
                handleGet(line, result);
            } else if (line.startsWith("Snk_Print")) {
                handlePrint(line, result);
            }
        }

        return result.toString();
    }

    private void handleDeclaration(String line, StringBuilder result) {
        String type;
        if (line.startsWith("Snk_Int")) {
            type = "int";
        } else if (line.startsWith("Snk_Real")) {
            type = "real";
        } else if (line.startsWith("Snk_Strg")) {
            type = "string";
        } else {
            result.append("Error: Invalid declaration syntax. Line: ").append(line).append("\n");
            return;
        }

        String[] parts = line.substring(line.indexOf(' ')).split(",");
        for (String part : parts) {
            String identifier = part.trim().replace("#", "").trim();
            if (identifierPattern.matcher(identifier).matches()) {
                if (!variableTypes.containsKey(identifier)) {
                    variableTypes.put(identifier, type);
                    result.append("Declaration detected: ").append(identifier).append(" as ").append(type).append("\n");
                } else {
                    result.append("Error: Variable ").append(identifier).append(" is already declared. Line: ").append(line).append("\n");
                }
            } else {
                result.append("Error: Invalid identifier '").append(identifier).append("' in declaration. Line: ").append(line).append("\n");
            }
        }
    }

    private void handleAssignment(String line, StringBuilder result) {
        String[] parts = line.split(" ");
        if (parts.length < 3) {
            result.append("Error: Invalid assignment syntax. Line: ").append(line).append("\n");
            return;
        }

        String variable = parts[1];
        String value = parts[2].replace("#", "").trim();

        if (!variableTypes.containsKey(variable)) {
            result.append("Error: Variable '").append(variable).append("' is not declared before assignment. Line: ").append(line).append("\n");
            return;
        }

        String type = variableTypes.get(variable);

        if ((type.equals("int") && intPattern.matcher(value).matches()) ||
            (type.equals("real") && realPattern.matcher(value).matches()) ||
            (type.equals("string") && stringPattern.matcher(value).matches())) {
            result.append("Assignment detected: ").append(variable).append(" = ").append(value).append("\n");
        } else {
            result.append("Error: Type mismatch in assignment. Expected ").append(type).append(" but got '").append(value).append("'. Line: ").append(line).append("\n");
        }
    }

    private void handleGet(String line, StringBuilder result) {
        String[] parts = line.split(" ");
        if (parts.length < 4 || !parts[2].equals("from")) {
            result.append("Error: Invalid Get syntax. Line: ").append(line).append("\n");
            return;
        }

        String variable1 = parts[1];
        String variable2 = parts[3].replace("#", "").trim();

        if (!variableTypes.containsKey(variable1)) {
            result.append("Error: Variable '").append(variable1).append("' is not declared. Line: ").append(line).append("\n");
        }
        if (!variableTypes.containsKey(variable2)) {
            result.append("Error: Variable '").append(variable2).append("' is not declared. Line: ").append(line).append("\n");
        }

        if (variableTypes.containsKey(variable1) && variableTypes.containsKey(variable2)) {
            result.append("Get instruction detected: ").append(variable1).append(" from ").append(variable2).append("\n");
        }
    }

    private void handlePrint(String line, StringBuilder result) {
        String[] parts = line.substring(line.indexOf(' ') + 1).replace("#", "").trim().split(",");
        for (String content : parts) {
            content = content.trim();
            if (stringPattern.matcher(content).matches() || variableTypes.containsKey(content)) {
                result.append("Print instruction detected: ").append(content).append("\n");
            } else {
                result.append("Error: Invalid print content '").append(content).append("'. Line: ").append(line).append("\n");
            }
        }
    }
}
