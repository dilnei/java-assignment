package com.mobiquity.packer;

import com.mobiquity.exception.APIException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Packer {

  private final FileReaderService fileReaderService = new FileReaderService();

  /**
   * This method reads the file and returns the optimal items for each package.
   *
   * @param filePath the path to the file
   * @return the optimal items for each package
   * @throws APIException if the file is not found or the content is invalid
   */
  public String pack(String filePath) throws APIException {
    // Read the input from the file, process each line, and join the results using Stream API
    return readFile(filePath).stream()
        .map(line -> {
          // Split the line into package weight limit and item list
          String[] parts = line.split(" : ");
          int packageLimit = Integer.parseInt(parts[0].trim());
          List<Item> items = parseItems(parts[1]);

          // Solve the knapsack problem for each line
          return solveKnapsack(items, packageLimit);
        })
        .collect(Collectors.joining("\n"));
  }

  /**
   * Parses the items from the input string.
   *
   * @param itemsString the string containing the items
   * @return the list of items
   */
  public static List<Item> parseItems(String itemsString) {
    // Regex to extract (index, weight, cost)
    Pattern pattern = Pattern.compile("\\((\\d+),(\\d+\\.\\d+),€(\\d+)\\)");

    // Use a stream to find all matches and map them to Item objects
    return pattern.matcher(itemsString)
        .results()  // Stream of MatchResult
        .map(match -> new Item(
            Integer.parseInt(match.group(1)),    // index
            Double.parseDouble(match.group(2)),  // weight
            Integer.parseInt(match.group(3))))   // cost
        .toList();  // Collect the results into a List
  }

  /**
   * Solves the 0/1 knapsack problem using dynamic programming.
   *
   * @param items        the list of items
   * @param packageLimit the weight limit of the package
   * @return the indices of the selected items as a comma-separated string
   */
  public static String solveKnapsack(List<Item> items, int packageLimit) {
    int n = items.size();

    // DP table where dp[i][w] stores the maximum cost with i items and weight limit w
    double[][] dp = new double[n + 1][packageLimit + 1];

    // Additional table to track total weight of the optimal solution
    double[][] totalWeight = new double[n + 1][packageLimit + 1];

    // Populate DP (two-dimensional) table
    for (int i = 1; i <= n; i++) {

      // Get the current item
      Item currentItem = items.get(i - 1);

      // Iterate over the weight limit
      for (int w = 0; w <= packageLimit; w++) {

        // If we don't include the current item
        dp[i][w] = dp[i - 1][w];

        // Copy the total weight from the previous row
        totalWeight[i][w] = totalWeight[i - 1][w];

        // If we include the current item and it fits within the weight limit
        if (currentItem.weight() <= w) {

          // Calculate the new cost and weight
          double newCost = dp[i - 1][w - (int) currentItem.weight()] + currentItem.cost();

          // Calculate the new weight
          double newWeight =
              totalWeight[i - 1][w - (int) currentItem.weight()] + currentItem.weight();

          // Choose solution with higher cost or lower weight if cost is the same
          if (newCost > dp[i][w] || (newCost == dp[i][w] && newWeight < totalWeight[i][w])) {

            // Update the DP table and total weight table
            dp[i][w] = newCost;

            // Update the total weight
            totalWeight[i][w] = newWeight;
          }
        }
      }
    }

// Improved Backtracking to find the selected items accurately
    List<Integer> selectedItems = new ArrayList<>();

    int w = packageLimit;

// Trace back the items to find which ones are part of the optimal solution
    for (int i = n; i > 0 && w >= 0; i--) {

      // Get the current item
      Item currentItem = items.get(i - 1);

      // Check if the current item is part of the optimal solution
      if (w >= currentItem.weight() &&

          // Check if the current item was included in the optimal solution
          dp[i][w] == dp[i - 1][w - (int) currentItem.weight()] + currentItem.cost()) {

        // This item was included, add its index to the list
        selectedItems.add(currentItem.index());

        // Reduce the weight accordingly
        w -= (int) currentItem.weight();
      }
    }

// Sort the indices of selected items in ascending order
    Collections.sort(selectedItems);

// If no items were selected, return "-"
    if (selectedItems.isEmpty()) {
      return "-";
    }

// Return the indices of the selected items as a comma-separated string
    return selectedItems.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(","));


  }

  /**
   * Reads the file from the classpath and returns the lines as a list of strings.
   *
   * @param filePath the path to the file
   * @return the lines of the file as a list of strings
   * @throws APIException if the file is not found or an error occurs while reading the file
   */
  public List<String> readFile(String filePath) throws APIException {
    try {
      // Load the file from the classpath using the class loader
      var resource = Optional.ofNullable(Packer.class.getClassLoader().getResource(filePath))
          .orElseThrow(() -> new APIException("File not found: " + filePath));

      // Read all lines from the file using Files.readAllLines with UTF-8 encoding
//      return Files.readAllLines(Path.of(resource.toURI()), StandardCharsets.UTF_8);
      return fileReaderService.readFile(String.valueOf(Path.of(resource.toURI())));
    } catch (IOException e) {
      throw new APIException("Error reading file: " + filePath, e);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
