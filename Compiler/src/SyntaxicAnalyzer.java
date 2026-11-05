import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SyntaxicAnalyzer {

    private static final String IDENT = "[a-zA-Z][a-zA-Z0-9_]*";
    private static final String VALUE = "\"[^\"]*\"|\\d+\\.\\d+|\\d+|" + IDENT;
    private static final String ITEM = "\"[^\"]*\"|" + IDENT;

    private static final Pattern IDENT_P = Pattern.compile(IDENT);
    private static final Pattern KEYWORD_P = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*");
    private static final Pattern ASSIGN_P = Pattern.compile("(" + IDENT + ")\\s+(" + VALUE + ")");
    private static final Pattern GET_P = Pattern.compile("(" + IDENT + ")\\s+from\\s+(" + IDENT + ")");
    private static final Pattern COND_P = Pattern.compile("\\[\\s*(" + VALUE + ")\\s*(!=|<|>|=)\\s*(" + VALUE + ")\\s*\\]");
    private static final Pattern PRINT_P = Pattern.compile("(?:" + ITEM + ")(?:\\s*,\\s*(?:" + ITEM + "))*");
    private static final Pattern ITEM_P = Pattern.compile(ITEM);

    private static final Set<String> RESERVED = new HashSet<>(Arrays.asList(
            "Snk_Begin", "Snk_End", "Snk_Int", "Snk_Real", "Snk_Strg", "Snk_Print",
            "Set", "Get", "from", "If", "Else", "Begin", "End"));

    private final Set<String> declaredVariables = new HashSet<>();
    private int errors;

    public String analyze(String code) {
        declaredVariables.clear();
        errors = 0;

        StringBuilder result = new StringBuilder("Syntax Analysis Result:\n");
        String[] lines = code.split("\\R", -1);

        boolean begun = false;
        boolean ended = false;
        boolean firstStatementSeen = false;
        boolean beginProblemReported = false;
        int blockDepth = 0;
        int pendingIfs = 0;

        for (int i = 0; i < lines.length; i++) {
            int n = i + 1;
            String line = lines[i].trim();

            // Blank lines: ignored
            if (line.isEmpty()) {
                continue;
            }

            // Comments
            if (line.startsWith("##")) {
                result.append("Comment detected: ").append(line).append("\n");
                continue;
            }

            // First word of the line (compared as a whole word, not with startsWith)
            Matcher km = KEYWORD_P.matcher(line);
            String keyword = km.find() ? km.group() : "";
            String rest = line.substring(keyword.length()).trim();

            if (ended) {
                error(result, n, "instruction after 'Snk_End': " + line);
                continue;
            }

            if (!firstStatementSeen) {
                firstStatementSeen = true;
                if (!keyword.equals("Snk_Begin")) {
                    error(result, n, "the program must start with 'Snk_Begin'.");
                    beginProblemReported = true;
                }
            }

            switch (keyword) {
            case "Snk_Begin":
                if (!rest.isEmpty()) {
                    error(result, n, "'Snk_Begin' must be alone on its line: " + line);
                } else if (begun) {
                    error(result, n, "'Snk_Begin' is already present.");
                } else {
                    result.append("Program start detected.\n");
                }
                begun = true;
                break;

            case "Snk_End":
                if (!rest.isEmpty()) {
                    error(result, n, "'Snk_End' must be alone on its line: " + line);
                } else {
                    result.append("Program end detected.\n");
                }
                ended = true;
                break;

            case "Begin":
                if (!rest.isEmpty()) {
                    error(result, n, "'Begin' must be alone on its line: " + line);
                } else {
                    blockDepth++;
                    result.append("Block start detected.\n");
                }
                break;

            case "End":
                if (!rest.isEmpty()) {
                    error(result, n, "'End' must be alone on its line: " + line);
                } else if (blockDepth == 0) {
                    error(result, n, "'End' without a matching 'Begin'.");
                } else {
                    blockDepth--;
                    result.append("Block end detected.\n");
                }
                break;

            case "Else":
                if (!rest.isEmpty()) {
                    error(result, n, "'Else' must be alone on its line: " + line);
                } else if (pendingIfs == 0) {
                    error(result, n, "'Else' without a matching 'If'.");
                } else {
                    pendingIfs--;
                    result.append("Else block detected.\n");
                }
                break;

            case "If": {
                pendingIfs++;
                Matcher m = COND_P.matcher(rest);
                if (!m.matches()) {
                    error(result, n, "invalid condition (expected: If [ a < b ]): " + line);
                } else {
                    boolean ok = checkDeclared(result, n, m.group(1)) & checkDeclared(result, n, m.group(3));
                    if (ok) {
                        result.append("If condition detected: ").append(line).append("\n");
                    }
                }
                break;
            }

            case "Snk_Int":
            case "Snk_Real":
            case "Snk_Strg":
                if (!line.endsWith("#")) {
                    error(result, n, "missing '#' at the end of the declaration: " + line);
                } else {
                    declaration(result, n, line.substring(keyword.length(), line.length() - 1).trim(), line);
                }
                break;

            case "Set":
                if (!line.endsWith("#")) {
                    error(result, n, "missing '#' at the end of the assignment: " + line);
                } else {
                    String body = line.substring(keyword.length(), line.length() - 1).trim();
                    Matcher m = ASSIGN_P.matcher(body);
                    if (!m.matches()) {
                        error(result, n, "invalid assignment (expected: Set x value #): " + line);
                    } else {
                        boolean ok = checkDeclared(result, n, m.group(1)) & checkDeclared(result, n, m.group(2));
                        if (ok) {
                            result.append("Assignment detected: ").append(line).append("\n");
                        }
                    }
                }
                break;

            case "Get":
                if (!line.endsWith("#")) {
                    error(result, n, "missing '#' at the end of the Get instruction: " + line);
                } else {
                    String body = line.substring(keyword.length(), line.length() - 1).trim();
                    Matcher m = GET_P.matcher(body);
                    if (!m.matches()) {
                        error(result, n, "invalid Get instruction (expected: Get x from y #): " + line);
                    } else {
                        boolean ok = checkDeclared(result, n, m.group(1)) & checkDeclared(result, n, m.group(2));
                        if (ok) {
                            result.append("Get instruction detected: ").append(line).append("\n");
                        }
                    }
                }
                break;

            case "Snk_Print":
                if (!line.endsWith("#")) {
                    error(result, n, "missing '#' at the end of the print instruction: " + line);
                } else {
                    String body = line.substring(keyword.length(), line.length() - 1).trim();
                    if (!PRINT_P.matcher(body).matches()) {
                        error(result, n, "invalid print (expected strings or variables separated by commas): " + line);
                    } else {
                        boolean ok = true;
                        Matcher items = ITEM_P.matcher(body);
                        while (items.find()) {
                            ok &= checkDeclared(result, n, items.group());
                        }
                        if (ok) {
                            result.append("Print instruction detected: ").append(line).append("\n");
                        }
                    }
                }
                break;

            default:
                error(result, n, "unknown instruction or invalid syntax: " + line);
            }
        }

        // Global checks
        if (!begun && !beginProblemReported) {
            error(result, 0, "the program must start with 'Snk_Begin'.");
        }
        if (!ended) {
            error(result, 0, "the program must end with 'Snk_End'.");
        }
        if (blockDepth > 0) {
            error(result, 0, blockDepth + " 'Begin' block(s) never closed by 'End'.");
        }

        result.append(errors == 0 ? "No syntax errors.\n" : errors + " syntax error(s).\n");
        return result.toString();
    }

    private void declaration(StringBuilder result, int n, String body, String line) {
        if (body.isEmpty()) {
            error(result, n, "invalid declaration (no identifier): " + line);
            return;
        }
        for (String variable : body.split(",", -1)) {
            variable = variable.trim();
            if (!IDENT_P.matcher(variable).matches()) {
                error(result, n, "invalid identifier: '" + variable + "'");
            } else if (RESERVED.contains(variable)) {
                error(result, n, "'" + variable + "' is a reserved word and cannot be a variable name.");
            } else {
                declaredVariables.add(variable);
                result.append("Declaration detected: ").append(variable).append("\n");
            }
        }
    }

    /** Returns false (and reports an error) if the operand is an identifier that was not declared. */
    private boolean checkDeclared(StringBuilder result, int n, String operand) {
        if (IDENT_P.matcher(operand).matches() && !declaredVariables.contains(operand)) {
            error(result, n, "variable '" + operand + "' is not declared.");
            return false;
        }
        return true;
    }

    private void error(StringBuilder result, int n, String message) {
        errors++;
        result.append(n > 0 ? "Error (line " + n + "): " : "Error: ").append(message).append("\n");
    }
}
