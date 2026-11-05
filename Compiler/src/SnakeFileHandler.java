import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class SnakeFileHandler {

	/**
	 * Reads a SNAKE source file.
	 * - tries UTF-8 first, falls back to Windows-1252 (files saved by old Windows editors
	 *   with accents would otherwise make Files.readString throw MalformedInputException)
	 * - removes a UTF-8 BOM, which would otherwise glue itself to "Snk_Begin"
	 */
	public static String readFile(File file) throws IOException {
		byte[] bytes = Files.readAllBytes(file.toPath());
		String text;
		try {
			text = StandardCharsets.UTF_8.newDecoder()
					.onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException e) {
			text = new String(bytes, Charset.forName("windows-1252"));
		}
		if (text.startsWith("﻿")) {
			text = text.substring(1);
		}
		return text;
	}
}
