package com.mobiquity.packer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileReaderService {

  /**
   * Reads the file and returns the content as a list of strings.
   *
   * @param filePath the path to the file
   * @return the content of the file as a list of strings
   * @throws IOException if an I/O error occurs
   */
  public List<String> readFile(String filePath) throws IOException {
    Path path = Path.of(filePath);
    return Files.readAllLines(path, StandardCharsets.UTF_8);
  }
}
