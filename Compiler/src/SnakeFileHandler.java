import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SnakeFileHandler {
	public static String readFile(File file) throws IOException {
		return Files.readString(file.toPath());
	}
}
