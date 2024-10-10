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

    // Create a StringBuilder to store the result
    StringBuilder result = new StringBuilder();

    // Process each line in the file
    for (String line : lines) {

      // Split the line into package weight limit and item list
      String[] parts = line.split(" : ");

      // Parse the package weight limit and items
      int packageLimit = Integer.parseInt(parts[0].trim());

      // Parse the items
      List<Item> items = parseItems(parts[1]);

      // Solve the knapsack problem and append the result to the StringBuilder
      result.append(solveKnapsack(items, packageLimit)).append("\n");
    }

    return result.toString().trim();
  }

  /**
   * Parses the items from the input string.
   *
   * @param itemsString the string containing the items
   * @return the list of items
   */
  private static List<Item> parseItems(String itemsString) {

    List<Item> items = new ArrayList<>();

    // Regex to extract (index, weight, cost)
    Pattern pattern = Pattern.compile("\\((\\d+),(\\d+\\.\\d+),€(\\d+)\\)");
    Matcher matcher = pattern.matcher(itemsString);

    // Parse each item and add it to the list
    while (matcher.find()) {

      // Extract the index, weight, and cost of the item
      int index = Integer.parseInt(matcher.group(1));

      // Convert weight to double
      double weight = Double.parseDouble(matcher.group(2));

      // Convert cost to integer
      int cost = Integer.parseInt(matcher.group(3));

      // Add the item to the list
      items.add(new Item(index, weight, cost));
    }

    return items;
  }

  /**
   * Solves the 0/1 knapsack problem using dynamic programming.
   *
   * @param items        the list of items
   * @param packageLimit the weight limit of the package
   * @return the indices of the selected items as a comma-separated string
   */
  private static String solveKnapsack(List<Item> items, int packageLimit) {
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
        if (currentItem.weight <= w) {

          // Calculate the new cost and weight
          double newCost = dp[i - 1][w - (int) currentItem.weight] + currentItem.cost;

          // Calculate the new weight
          double newWeight = totalWeight[i - 1][w - (int) currentItem.weight] + currentItem.weight;

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
      if (w >= currentItem.weight &&

          // Check if the current item was included in the optimal solution
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

  /**
   * Reads the file from the classpath and returns the lines as a list of strings.
   *
   * @param filePath the path to the file
   * @return the lines of the file as a list of strings
   * @throws APIException if the file is not found or an error occurs while reading the file
   */
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
