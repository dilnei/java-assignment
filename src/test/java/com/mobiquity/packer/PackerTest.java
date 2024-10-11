package com.mobiquity.packer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.mobiquity.exception.APIException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PackerTest {

  private final Path mockPath = mock(Path.class);

  @BeforeEach
  void setUp() {
    // Reset any mock or setup before each test
    Mockito.reset();
  }

  @Test
  void testPackTest() throws APIException {
    String result = new Packer().pack("example_input");
    assertEquals("4\n-\n2,7\n8,9", result);
  }

  @Test
  void testParseItems() {
    String itemsString = "(1,53.38,€45) (2,88.62,€98) (3,78.48,€3)";
    List<Item> items = Packer.parseItems(itemsString);

    assertEquals(3, items.size());
    assertEquals(new Item(1, 53.38, 45), items.get(0));
    assertEquals(new Item(2, 88.62, 98), items.get(1));
    assertEquals(new Item(3, 78.48, 3), items.get(2));
  }

  @Test
  void testSolveKnapsack() {
    List<Item> items = List.of(
        new Item(1, 53.38, 45),
        new Item(2, 88.62, 98),
        new Item(3, 78.48, 3),
        new Item(4, 72.30, 76),
        new Item(5, 30.18, 9),
        new Item(6, 46.34, 48)
    );

    String result = Packer.solveKnapsack(items, 81);
    assertEquals("4", result);
  }

  @Test
  void testReadFileWithTempFile() throws IOException {
    // Create a temporary file for testing
    Path tempFile = Files.createTempFile("testFile", ".txt");
    List<String> mockLines = List.of("81 : (1,53.38,€45) (2,88.62,€98)");
    Files.write(tempFile, mockLines, StandardCharsets.UTF_8);

    // Call the method that reads the file
    List<String> lines = Files.readAllLines(tempFile, StandardCharsets.UTF_8);

    // Verify that the lines match the expected data
    assertEquals(mockLines, lines);

    // Clean up by deleting the temporary file
    Files.deleteIfExists(tempFile);
  }

  @Test
  void testReadFileException() {
    assertThrows(APIException.class, () -> new Packer().readFile("nonExistentFilePath"));
  }
}
