import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainApp {
	private JFrame frame;
	private JTextArea resultArea;
	private File selectedFile;

	public MainApp() {
		initializeUI();
	}

	private void initializeUI() {
		frame = new JFrame("SNAKE Compiler");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);

		JButton loadFileButton = new JButton("Load File");
		JButton lexicalAnalysisButton = new JButton("Lexical Analysis");
		JButton syntaxAnalysisButton = new JButton("Syntax Analysis");
		JButton semanticAnalysisButton = new JButton("Semantic Analysis");

		resultArea = new JTextArea();
		resultArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(resultArea);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 4));
		buttonPanel.add(loadFileButton);
		buttonPanel.add(lexicalAnalysisButton);
		buttonPanel.add(syntaxAnalysisButton);
		buttonPanel.add(semanticAnalysisButton);

		frame.setLayout(new BorderLayout());
		frame.add(buttonPanel, BorderLayout.NORTH);
		frame.add(scrollPane, BorderLayout.CENTER);

		loadFileButton.addActionListener(e -> loadFile());
		lexicalAnalysisButton.addActionListener(e -> performLexicalAnalysis());
		syntaxAnalysisButton.addActionListener(e -> performSyntaxAnalysis());
		semanticAnalysisButton.addActionListener(e -> performSemanticAnalysis());

		frame.setVisible(true);
	}

	private void loadFile() {
		JFileChooser fileChooser = new JFileChooser();
		int returnValue = fileChooser.showOpenDialog(frame);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			resultArea.setText("Loaded file: " + selectedFile.getAbsolutePath());
		}
	}

	private void performLexicalAnalysis() {
		if (selectedFile == null) {
			showError("Please load a file first.");
			return;
		}

		try {
			String content = SnakeFileHandler.readFile(selectedFile);
			Lexer lexer = new Lexer();
			String result = lexer.tokenize(content);
			resultArea.setText(result);
		} catch (Exception e) {
			showError("Error during lexical analysis: " + e.getMessage());
		}
	}

	private void performSyntaxAnalysis() {
	    if (selectedFile == null) {
	        showError("Please load a file first.");
	        return;
	    }

	    try {
	        String content = SnakeFileHandler.readFile(selectedFile);
	        SyntaxicAnalyzer analyzer = new SyntaxicAnalyzer(); // Remplacement de Parser par SyntaxicAnalyzer
	        String result = analyzer.analyze(content);          // Remplacement de parse() par analyze()
	        resultArea.setText(result);
	    } catch (Exception e) {
	        showError("Error during syntax analysis: " + e.getMessage());
	    }
	}


	private void performSemanticAnalysis() {
		if (selectedFile == null) {
			showError("Please load a file first.");
			return;
		}

		try {
			String content = SnakeFileHandler.readFile(selectedFile);
			SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
			String result = semanticAnalyzer.analyze(content);
			resultArea.setText(result);
		} catch (Exception e) {
			showError("Error during semantic analysis: " + e.getMessage());
		}
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(MainApp::new);
	}
}
