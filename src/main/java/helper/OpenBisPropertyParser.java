package helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OpenBisPropertyParser {

    public OpenBisPropertyParser() {

    }

    public static Map<String, String> parseProperty(String xml, String property) {
        HashMap<String, String> properties = new HashMap<>();
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(xml.split("\n")));
        for (String line : lines) {
            if (property.contains(property)) {
                String label = line.trim().split(" ")[1].replace("label=", "").replace("/>", "").replace("\"", "");
                String value = line.trim().split(" ")[2].replace("value=", "").replace("/>", "").replace("\"", "");
                properties.put(label, value);
            }
        }

        return properties;
    }

}

