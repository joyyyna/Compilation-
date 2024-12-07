import java.util.HashSet;
import java.util.Set;

public class SyntaxicAnalyzer {
    private Set<String> declaredVariables = new HashSet<>();

    public String analyze(String code) {
        StringBuilder result = new StringBuilder("Résultat de l'analyse syntaxique :\n");
        String[] lines = code.split("\n");

        for (String line : lines) {
            String trimmedLine = line.trim();

            // Vérifier si la ligne est une des instructions qui n'exigent pas de #
            if (trimmedLine.startsWith("Snk_Begin")) {
                result.append("Début du programme détecté.\n");
                continue;
            }

            if (trimmedLine.startsWith("Snk_End")) {
                result.append("Fin du programme détectée.\n");
                continue;
            }

            if (trimmedLine.startsWith("If")) {
                result.append("Condition If détectée : ").append(trimmedLine).append("\n");
                continue;
            }

            if (trimmedLine.startsWith("Else")) {
                result.append("Bloc Else détecté.\n");
                continue;
            }

            if (trimmedLine.startsWith("Begin")) {
                result.append("Début de bloc détecté.\n");
                continue;
            }

            if (trimmedLine.startsWith("End")) {
                result.append("Fin de bloc détectée.\n");
                continue;
            }

            // Vérifier si c'est une ligne de commentaire
            if (trimmedLine.startsWith("##")) {
                result.append("Commentaire détecté : ").append(trimmedLine).append("\n");
                continue;
            }

            // Gestion des déclarations (Snk_Int, Snk_Real, Snk_Strg)
            if (trimmedLine.startsWith("Snk_Int") || trimmedLine.startsWith("Snk_Real") || trimmedLine.startsWith("Snk_Strg")) {
                if (!trimmedLine.endsWith("#")) {
                    result.append("Erreur de syntaxe dans la déclaration : ").append(trimmedLine).append("\n");
                } else {
                    // Extraire les identifiants déclarés
                    String[] parts = trimmedLine.split("\\s+", 2);
                    if (parts.length < 2) {
                        result.append("Erreur : Déclaration incorrecte. Ligne : ").append(trimmedLine).append("\n");
                    } else {
                        String[] variables = parts[1].replace("#", "").split(",");
                        for (String variable : variables) {
                            variable = variable.trim();
                            if (variable.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                                declaredVariables.add(variable);
                                result.append("Déclaration détectée : ").append(variable).append("\n");
                            } else {
                                result.append("Erreur : Identifiant invalide : ").append(variable).append("\n");
                            }
                        }
                    }
                }
                continue;
            }

            // Gestion des affectations (Set)
            if (trimmedLine.startsWith("Set")) {
                if (!trimmedLine.endsWith("#")) {
                    result.append("Erreur de syntaxe dans l'affectation : ").append(trimmedLine).append("\n");
                } else {
                    String[] parts = trimmedLine.split("\\s+");
                    if (parts.length < 3) {
                        result.append("Erreur : Affectation incorrecte. Ligne : ").append(trimmedLine).append("\n");
                    } else {
                        String variable = parts[1];
                        if (declaredVariables.contains(variable)) {
                            result.append("Affectation détectée : ").append(trimmedLine).append("\n");
                        } else {
                            result.append("Erreur : La variable '").append(variable).append("' n'est pas déclarée. Ligne : ").append(trimmedLine).append("\n");
                        }
                    }
                }
                continue;
            }

            // Gestion des instructions Get
            if (trimmedLine.startsWith("Get")) {
                if (!trimmedLine.endsWith("#")) {
                    result.append("Erreur de syntaxe dans l'instruction Get : ").append(trimmedLine).append("\n");
                } else {
                    String[] parts = trimmedLine.split("\\s+");
                    if (parts.length < 4 || !"from".equals(parts[2])) {
                        result.append("Erreur : Instruction Get incorrecte. Ligne : ").append(trimmedLine).append("\n");
                    } else {
                        String variable1 = parts[1];
                        String variable2 = parts[3].replace("#", "");
                        if (declaredVariables.contains(variable1) && declaredVariables.contains(variable2)) {
                            result.append("Instruction Get détectée : ").append(trimmedLine).append("\n");
                        } else {
                            result.append("Erreur : Variables utilisées non déclarées dans Get. Ligne : ").append(trimmedLine).append("\n");
                        }
                    }
                }
                continue;
            }

            // Gestion des affichages (Snk_Print)
            if (trimmedLine.startsWith("Snk_Print")) {
                if (!trimmedLine.endsWith("#")) {
                    result.append("Erreur de syntaxe dans l'instruction d'affichage : ").append(trimmedLine).append("\n");
                } else {
                    result.append("Instruction d'affichage détectée : ").append(trimmedLine).append("\n");
                }
                continue;
            }

            // Ligne non reconnue
            result.append("Ligne inconnue ou syntaxe incorrecte : ").append(trimmedLine).append("\n");
        }

        // Vérification globale du début et de la fin du programme
        if (!code.contains("Snk_Begin")) {
            result.append("Erreur : Le programme doit commencer par 'Snk_Begin'.\n");
        }
        if (!code.contains("Snk_End")) {
            result.append("Erreur : Le programme doit se terminer par 'Snk_End'.\n");
        }

        return result.toString();
    }
}
