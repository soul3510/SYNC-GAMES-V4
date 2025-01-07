package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class ReadConfig {
    public static String userDir = System.getProperty("user.dir");
    public String convertJsonToString() throws Exception {
        String filePath = userDir + "\\src\\test\\java\\tests\\config.json";
        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        return new String(encoded, StandardCharsets.UTF_8);
    }
    public Map<String, Object> parseJsonToObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> config = mapper.readValue(convertJsonToString(), Map.class);
        return config;
    }
}