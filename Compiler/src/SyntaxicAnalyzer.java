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

        StringBuilder result = new StringBuilder("Résultat de l'analyse syntaxique :\n");
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

            // Lignes vides : ignorées
            if (line.isEmpty()) {
                continue;
            }

            // Commentaires
            if (line.startsWith("##")) {
                result.append("Commentaire détecté : ").append(line).append("\n");
                continue;
            }

            // Premier mot de la ligne (comparé en entier, pas avec startsWith)
            Matcher km = KEYWORD_P.matcher(line);
            String keyword = km.find() ? km.group() : "";
            String rest = line.substring(keyword.length()).trim();

            if (ended) {
                error(result, n, "instruction après 'Snk_End' : " + line);
                continue;
            }

            if (!firstStatementSeen) {
                firstStatementSeen = true;
                if (!keyword.equals("Snk_Begin")) {
                    error(result, n, "le programme doit commencer par 'Snk_Begin'.");
                    beginProblemReported = true;
                }
            }

            switch (keyword) {
            case "Snk_Begin":
                if (!rest.isEmpty()) {
                    error(result, n, "'Snk_Begin' doit être seul sur sa ligne : " + line);
                } else if (begun) {
                    error(result, n, "'Snk_Begin' déjà présent.");
                } else {
                    result.append("Début du programme détecté.\n");
                }
                begun = true;
                break;

            case "Snk_End":
                if (!rest.isEmpty()) {
                    error(result, n, "'Snk_End' doit être seul sur sa ligne : " + line);
                } else {
                    result.append("Fin du programme détectée.\n");
                }
                ended = true;
                break;

            case "Begin":
                if (!rest.isEmpty()) {
                    error(result, n, "'Begin' doit être seul sur sa ligne : " + line);
                } else {
                    blockDepth++;
                    result.append("Début de bloc détecté.\n");
                }
                break;

            case "End":
                if (!rest.isEmpty()) {
                    error(result, n, "'End' doit être seul sur sa ligne : " + line);
                } else if (blockDepth == 0) {
                    error(result, n, "'End' sans 'Begin' correspondant.");
                } else {
                    blockDepth--;
                    result.append("Fin de bloc détectée.\n");
                }
                break;

            case "Else":
                if (!rest.isEmpty()) {
                    error(result, n, "'Else' doit être seul sur sa ligne : " + line);
                } else if (pendingIfs == 0) {
                    error(result, n, "'Else' sans 'If' correspondant.");
                } else {
                    pendingIfs--;
                    result.append("Bloc Else détecté.\n");
                }
                break;

            case "If": {
                pendingIfs++;
                Matcher m = COND_P.matcher(rest);
                if (!m.matches()) {
                    error(result, n, "condition incorrecte (attendu : If [ a < b ]) : " + line);
                } else {
                    boolean ok = checkDeclared(result, n, m.group(1)) & checkDeclared(result, n, m.group(3));
                    if (ok) {
                        result.append("Condition If détectée : ").append(line).append("\n");
                    }
                }
                break;
            }

            case "Snk_Int":
            case "Snk_Real":
            case "Snk_Strg":
                if (!line.endsWith("#")) {
                    error(result, n, "'#' manquant à la fin de la déclaration : " + line);
                } else {
                    declaration(result, n, line.substring(keyword.length(), line.length() - 1).trim(), line);
                }
                break;

            case "Set":
                if (!line.endsWith("#")) {
                    error(result, n, "'#' manquant à la fin de l'affectation : " + line);
                } else {
                    String body = line.substring(keyword.length(), line.length() - 1).trim();
                    Matcher m = ASSIGN_P.matcher(body);
                    if (!m.matches()) {
                        error(result, n, "affectation incorrecte (attendu : Set x valeur #) : " + line);
                    } else {
                        boolean ok = checkDeclared(result, n, m.group(1)) & checkDeclared(result, n, m.group(2));
                        if (ok) {
                            result.append("Affectation détectée : ").append(line).append("\n");
                        }
                    }
                }
                break;

            case "Get":
                if (!line.endsWith("#")) {
                    error(result, n, "'#' manquant à la fin de l'instruction Get : " + line);
                } else {
                    String body = line.substring(keyword.length(), line.length() - 1).trim();
                    Matcher m = GET_P.matcher(body);
                    if (!m.matches()) {
                        error(result, n, "instruction Get incorrecte (attendu : Get x from y #) : " + line);
                    } else {
                        boolean ok = checkDeclared(result, n, m.group(1)) & checkDeclared(result, n, m.group(2));
                        if (ok) {
                            result.append("Instruction Get détectée : ").append(line).append("\n");
                        }
                    }
                }
                break;

            case "Snk_Print":
                if (!line.endsWith("#")) {
                    error(result, n, "'#' manquant à la fin de l'instruction d'affichage : " + line);
                } else {
                    String body = line.substring(keyword.length(), line.length() - 1).trim();
                    if (!PRINT_P.matcher(body).matches()) {
                        error(result, n, "affichage incorrect (attendu : chaînes ou variables séparées par des virgules) : " + line);
                    } else {
                        boolean ok = true;
                        Matcher items = ITEM_P.matcher(body);
                        while (items.find()) {
                            ok &= checkDeclared(result, n, items.group());
                        }
                        if (ok) {
                            result.append("Instruction d'affichage détectée : ").append(line).append("\n");
                        }
                    }
                }
                break;

            default:
                error(result, n, "ligne inconnue ou syntaxe incorrecte : " + line);
            }
        }

        // Vérifications globales
        if (!begun && !beginProblemReported) {
            error(result, 0, "le programme doit commencer par 'Snk_Begin'.");
        }
        if (!ended) {
            error(result, 0, "le programme doit se terminer par 'Snk_End'.");
        }
        if (blockDepth > 0) {
            error(result, 0, blockDepth + " bloc(s) 'Begin' jamais fermé(s) par 'End'.");
        }

        result.append(errors == 0 ? "Aucune erreur syntaxique.\n" : errors + " erreur(s) syntaxique(s).\n");
        return result.toString();
    }

    private void declaration(StringBuilder result, int n, String body, String line) {
        if (body.isEmpty()) {
            error(result, n, "déclaration incorrecte (aucun identifiant) : " + line);
            return;
        }
        for (String variable : body.split(",", -1)) {
            variable = variable.trim();
            if (!IDENT_P.matcher(variable).matches()) {
                error(result, n, "identifiant invalide : '" + variable + "'");
            } else if (RESERVED.contains(variable)) {
                error(result, n, "'" + variable + "' est un mot réservé et ne peut pas être une variable.");
            } else {
                declaredVariables.add(variable);
                result.append("Déclaration détectée : ").append(variable).append("\n");
            }
        }
    }

    /** Returns false (and reports an error) if the operand is an identifier that was not declared. */
    private boolean checkDeclared(StringBuilder result, int n, String operand) {
        if (IDENT_P.matcher(operand).matches() && !declaredVariables.contains(operand)) {
            error(result, n, "la variable '" + operand + "' n'est pas déclarée.");
            return false;
        }
        return true;
    }

    private void error(StringBuilder result, int n, String message) {
        errors++;
        result.append(n > 0 ? "Erreur (ligne " + n + ") : " : "Erreur : ").append(message).append("\n");
    }
}
