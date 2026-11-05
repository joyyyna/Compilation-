import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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
		frame.setLocationRelativeTo(null);

		JButton loadFileButton = new JButton("Load File");
		JButton lexicalAnalysisButton = new JButton("Lexical Analysis");
		JButton syntaxAnalysisButton = new JButton("Syntax Analysis");
		JButton semanticAnalysisButton = new JButton("Semantic Analysis");

		resultArea = new JTextArea();
		resultArea.setEditable(false);
		resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
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
		fileChooser.setFileFilter(new FileNameExtensionFilter("SNAKE programs (*.snk, *.txt)", "snk", "txt"));

		File examples = new File("../examples");
		if (examples.isDirectory()) {
			fileChooser.setCurrentDirectory(examples);
		}

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
			showResult(lexer.tokenize(content));
		} catch (Exception e) {
			showError("Error during lexical analysis: " + describe(e));
		}
	}

	private void performSyntaxAnalysis() {
		if (selectedFile == null) {
			showError("Please load a file first.");
			return;
		}

		try {
			String content = SnakeFileHandler.readFile(selectedFile);
			SyntaxicAnalyzer analyzer = new SyntaxicAnalyzer();
			showResult(analyzer.analyze(content));
		} catch (Exception e) {
			showError("Error during syntax analysis: " + describe(e));
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
			showResult(semanticAnalyzer.analyze(content));
		} catch (Exception e) {
			showError("Error during semantic analysis: " + describe(e));
		}
	}

	private void showResult(String result) {
		resultArea.setText(result);
		resultArea.setCaretPosition(0); // scroll back to the top
	}

	/** getMessage() can be null (e.g. NullPointerException), so fall back to the exception type. */
	private String describe(Exception e) {
		return e.getMessage() != null ? e.getMessage() : e.toString();
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(MainApp::new);
	}
}
