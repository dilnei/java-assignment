package com.mobiquity.packer;

import com.mobiquity.exception.APIException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Packer {

  /**
   * Private constructor to prevent instantiation of this class.
   */
  private Packer() {
  }


  /**
   * This method reads the file and returns the optimal items for each package.
   *
   * @param filePath the path to the file
   * @return the optimal items for each package
   * @throws APIException if the file is not found or the content is invalid
   */
  public static String pack(String filePath) throws APIException {

    // Read the input from the file and parse the package weight and item list
    List<String> lines = readFile(filePath);

    StringBuilder result = new StringBuilder();

    for (String line : lines) {
      String[] parts = line.split(" : ");
      int packageLimit = Integer.parseInt(parts[0].trim());
      List<Item> items = parseItems(parts[1]);

      result.append(solveKnapsack(items, packageLimit)).append("\n");
    }

    return result.toString().trim();
  }

  private static List<Item> parseItems(String itemsString) {
    List<Item> items = new ArrayList<>();
    // Regex to extract (index, weight, cost)
    Pattern pattern = Pattern.compile("\\((\\d+),(\\d+\\.\\d+),€(\\d+)\\)");
    Matcher matcher = pattern.matcher(itemsString);

    while (matcher.find()) {
      int index = Integer.parseInt(matcher.group(1));
      double weight = Double.parseDouble(matcher.group(2));
      int cost = Integer.parseInt(matcher.group(3));
      items.add(new Item(index, weight, cost));
    }

    return items;
  }

  private static String solveKnapsack(List<Item> items, int packageLimit) {
    int n = items.size();

    // DP table where dp[i][w] stores the maximum cost with i items and weight limit w
    double[][] dp = new double[n + 1][packageLimit + 1];
    // Additional table to track total weight of the optimal solution
    double[][] totalWeight = new double[n + 1][packageLimit + 1];

    // Populate DP table
    for (int i = 1; i <= n; i++) {
      Item currentItem = items.get(i - 1);
      for (int w = 0; w <= packageLimit; w++) {
        // If we don't include the current item
        dp[i][w] = dp[i - 1][w];
        totalWeight[i][w] = totalWeight[i - 1][w];

        // If we include the current item and it fits within the weight limit
        if (currentItem.weight <= w) {
          double newCost = dp[i - 1][w - (int) currentItem.weight] + currentItem.cost;
          double newWeight = totalWeight[i - 1][w - (int) currentItem.weight] + currentItem.weight;

          // Choose solution with higher cost or lower weight if cost is the same
          if (newCost > dp[i][w] || (newCost == dp[i][w] && newWeight < totalWeight[i][w])) {
            dp[i][w] = newCost;
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
      Item currentItem = items.get(i - 1);
      // Check if the current item is part of the optimal solution
      if (w >= currentItem.weight &&
          dp[i][w] == dp[i - 1][w - (int) currentItem.weight] + currentItem.cost) {
        // This item was included, add its index to the list
        selectedItems.add(currentItem.index);
        // Reduce the weight accordingly
        w -= (int) currentItem.weight;
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

  private static List<String> readFile(String filePath) throws APIException {
    List<String> lines = new ArrayList<>();

    // Load the file from the classpath using the class loader
    try {
      // Get the file from the classpath
      File file = new File(Packer.class.getClassLoader().getResource(filePath).getFile());

      // Ensure the file exists
      if (!file.exists()) {
        throw new APIException("File not found: " + filePath);
      }

      // Read the file using BufferedReader
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

        String line;
        while ((line = reader.readLine()) != null) {
          lines.add(line);
        }
      }
    } catch (IOException | NullPointerException e) {
      throw new APIException("Error reading file: " + filePath, e);
    }

    return lines;
  }

  public static void main(String[] args) {
    try {
      String result = pack("example_input");
      System.out.println(result);
    } catch (APIException e) {
      System.err.println("Error: " + e.getMessage());
    }
  }

}
