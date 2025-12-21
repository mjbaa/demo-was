package config;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ConfigLoader {

    private static final String CONFIG_PATH = "config.json";

    public static ServerConfig load() {
        try{
            ObjectMapper mapper = new ObjectMapper();

            InputStream is = ConfigLoader.class
                    .getClassLoader()
                    .getResourceAsStream(CONFIG_PATH);

            if (is == null) {
                throw new IllegalStateException("config.json not found in classpath");
            }

            return mapper.readValue(is, ServerConfig.class);

//            ObjectMapper mapper = new ObjectMapper();
//            return mapper.readValue(new File(CONFIG_PATH), ServerConfig.class);
        }catch(Exception e){
            throw new RuntimeException("Failed to load config.json", e);
        }
    }
}
