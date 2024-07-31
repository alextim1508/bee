package com.alextim.bee.context;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
public class AppState {

    private final File file;
    private final Map<String, String> params = new HashMap<>();

    public String getParam(String param) {
        return params.get(param);
    }

    public void putParam(String key, String value) {
        params.put(key, value);
    }

    @SneakyThrows
    public void saveParam() {
        log.info("saveParam to file");

        @Cleanup
        FileWriter fileWriter = new FileWriter(file);

        params.forEach(new BiConsumer<String, String>() {
            @SneakyThrows
            @Override
            public void accept(String key, String value) {
                fileWriter.append(key).append(":").append(value).append(System.lineSeparator());
            }
        });

        fileWriter.flush();

        log.info("saveParam to file OK");
    }

    @SneakyThrows
    public void readParam() {
        log.info("readParam to file");

        if(!file.exists()) {
            boolean isCreated = file.createNewFile();
            log.info("Is create new param file: {}", isCreated);
            return;
        }

        @Cleanup
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split(":");
            if (split.length == 2) {
                params.put(split[0], split[1]);
            }
        }

        log.info("readParam to file OK");
    }
}
