import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SemanticAnalyzer {
    private static final String IDENT = "[a-zA-Z][a-zA-Z0-9_]*";
    private static final String VALUE = "\"[^\"]*\"|\\d+\\.\\d+|\\d+|" + IDENT;
    private static final String ITEM = "\"[^\"]*\"|" + IDENT;

    private static final Pattern intPattern = Pattern.compile("\\d+");                // Pattern for integers
    private static final Pattern realPattern = Pattern.compile("\\d+\\.\\d+");        // Pattern for reals
    private static final Pattern stringPattern = Pattern.compile("\"[^\"]*\"");       // Pattern for strings
    private static final Pattern identifierPattern = Pattern.compile(IDENT);          // Valid identifier

    private static final Pattern KEYWORD_P = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*");
    private static final Pattern ASSIGN_P = Pattern.compile("(" + IDENT + ")\\s+(" + VALUE + ")");
    private static final Pattern GET_P = Pattern.compile("(" + IDENT + ")\\s+from\\s+(" + IDENT + ")");
    private static final Pattern COND_P = Pattern.compile("\\[\\s*(" + VALUE + ")\\s*(!=|<|>|=)\\s*(" + VALUE + ")\\s*\\]");
    private static final Pattern PRINT_P = Pattern.compile("(?:" + ITEM + ")(?:\\s*,\\s*(?:" + ITEM + "))*");
    private static final Pattern ITEM_P = Pattern.compile(ITEM);

    private static final Set<String> RESERVED = new HashSet<>(Arrays.asList(
            "Snk_Begin", "Snk_End", "Snk_Int", "Snk_Real", "Snk_Strg", "Snk_Print",
            "Set", "Get", "from", "If", "Else", "Begin", "End"));

    private final Map<String, String> variableTypes = new HashMap<>(); // Store declared variable types
    private int errors;

    public String analyze(String code) {
        variableTypes.clear();
        errors = 0;

        StringBuilder result = new StringBuilder("Semantic Analysis Result:\n");
        String[] lines = code.split("\\R", -1);

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("##")) {
                continue;
            }

            Matcher km = KEYWORD_P.matcher(line);
            String keyword = km.find() ? km.group() : "";
            // Instruction body without the keyword and without the final '#'
            // (a missing '#' is a syntax error, reported by the syntax analysis)
            String body = line.substring(keyword.length()).trim();
            if (body.endsWith("#")) {
                body = body.substring(0, body.length() - 1).trim();
            }

            switch (keyword) {
            case "Snk_Int":
                handleDeclaration("int", body, line, result);
                break;
            case "Snk_Real":
                handleDeclaration("real", body, line, result);
                break;
            case "Snk_Strg":
                handleDeclaration("string", body, line, result);
                break;
            case "Set":
                handleAssignment(body, line, result);
                break;
            case "Get":
                handleGet(body, line, result);
                break;
            case "If":
                handleCondition(body, line, result);
                break;
            case "Snk_Print":
                handlePrint(body, line, result);
                break;
            default:
                break; // Snk_Begin, Snk_End, Begin, End, Else, unknown lines: nothing to check here
            }
        }

        result.append(errors == 0 ? "No semantic errors.\n" : errors + " semantic error(s).\n");
        return result.toString();
    }

    private void handleDeclaration(String type, String body, String line, StringBuilder result) {
        if (body.isEmpty()) {
            error(result, "Invalid declaration syntax", line);
            return;
        }

        for (String part : body.split(",", -1)) {
            String identifier = part.trim();
            if (!identifierPattern.matcher(identifier).matches() || RESERVED.contains(identifier)) {
                error(result, "Invalid identifier '" + identifier + "' in declaration", line);
            } else if (variableTypes.containsKey(identifier)) {
                error(result, "Variable " + identifier + " is already declared", line);
            } else {
                variableTypes.put(identifier, type);
                result.append("Declaration detected: ").append(identifier).append(" as ").append(type).append("\n");
            }
        }
    }

    private void handleAssignment(String body, String line, StringBuilder result) {
        Matcher m = ASSIGN_P.matcher(body);
        if (!m.matches()) {
            error(result, "Invalid assignment syntax", line);
            return;
        }

        String variable = m.group(1);
        String value = m.group(2);

        if (!variableTypes.containsKey(variable)) {
            error(result, "Variable '" + variable + "' is not declared before assignment", line);
            return;
        }

        String expected = variableTypes.get(variable);
        String actual = typeOf(value);

        if (actual == null) {
            error(result, "Variable '" + value + "' is not declared before assignment", line);
        } else if (actual.equals(expected)) {
            result.append("Assignment detected: ").append(variable).append(" = ").append(value).append("\n");
        } else {
            // for a variable on the right-hand side, also show its type
            String got = identifierPattern.matcher(value).matches() ? value + "' (" + actual + ")" : value + "'";
            error(result, "Type mismatch in assignment. Expected " + expected + " but got '" + got, line);
        }
    }

    private void handleGet(String body, String line, StringBuilder result) {
        Matcher m = GET_P.matcher(body);
        if (!m.matches()) {
            error(result, "Invalid Get syntax", line);
            return;
        }

        String variable1 = m.group(1);
        String variable2 = m.group(2);

        if (!variableTypes.containsKey(variable1)) {
            error(result, "Variable '" + variable1 + "' is not declared", line);
        }
        if (!variableTypes.containsKey(variable2)) {
            error(result, "Variable '" + variable2 + "' is not declared", line);
        }

        if (variableTypes.containsKey(variable1) && variableTypes.containsKey(variable2)) {
            result.append("Get instruction detected: ").append(variable1).append(" from ").append(variable2).append("\n");
        }
    }

    private void handleCondition(String body, String line, StringBuilder result) {
        Matcher m = COND_P.matcher(body);
        if (!m.matches()) {
            error(result, "Invalid condition syntax", line);
            return;
        }

        String left = m.group(1);
        String operator = m.group(2);
        String right = m.group(3);
        String leftType = typeOf(left);
        String rightType = typeOf(right);

        if (leftType == null) {
            error(result, "Variable '" + left + "' is not declared", line);
        }
        if (rightType == null) {
            error(result, "Variable '" + right + "' is not declared", line);
        }
        if (leftType == null || rightType == null) {
            return;
        }

        // int and real can be compared with each other; a string only with a string
        boolean compatible = leftType.equals(rightType) || (isNumeric(leftType) && isNumeric(rightType));
        if (compatible) {
            result.append("Condition detected: ").append(left).append(" ").append(operator).append(" ").append(right).append("\n");
        } else {
            error(result, "Type mismatch in condition. Cannot compare " + leftType + " with " + rightType, line);
        }
    }

    private void handlePrint(String body, String line, StringBuilder result) {
        if (!PRINT_P.matcher(body).matches()) {
            error(result, "Invalid print content '" + body + "'", line);
            return;
        }

        // Items are matched one by one, so a comma inside a string ("Hello, World") is not a separator
        Matcher items = ITEM_P.matcher(body);
        while (items.find()) {
            String content = items.group();
            if (stringPattern.matcher(content).matches() || variableTypes.containsKey(content)) {
                result.append("Print instruction detected: ").append(content).append("\n");
            } else {
                error(result, "Invalid print content '" + content + "' (variable not declared)", line);
            }
        }
    }

    /** Type of a literal or of a declared variable; null if it is an undeclared variable. */
    private String typeOf(String operand) {
        if (intPattern.matcher(operand).matches()) {
            return "int";
        }
        if (realPattern.matcher(operand).matches()) {
            return "real";
        }
        if (stringPattern.matcher(operand).matches()) {
            return "string";
        }
        return variableTypes.get(operand);
    }

    private boolean isNumeric(String type) {
        return type.equals("int") || type.equals("real");
    }

    private void error(StringBuilder result, String message, String line) {
        errors++;
        result.append("Error: ").append(message).append(". Line: ").append(line).append("\n");
    }
}
