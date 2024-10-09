# Delivery
Important: The assigment must be completed by pushing your code into master.

# Constraints

You should write a class `com.mobiquity.packer.Packer` with a static API method named pack. This method accepts the absolute path to a test file as a String. The test file will be in UTF-8 format. The pack method returns the solution as a String.

Your method should throw an `com.mobiquity.exception.APIException` if incorrect parameters are being passed.  Therefore, your signature should look like 

```java
public static String pack(String filePath) throws APIException
```
