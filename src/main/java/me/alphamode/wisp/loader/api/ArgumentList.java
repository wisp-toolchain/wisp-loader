package me.alphamode.wisp.loader.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgumentList {
    private static final String DOUBLE_HYPHEN = "--";
    private final List<String> arguments;
    public ArgumentList(String[] args) {
        this.arguments = new ArrayList<>(List.of(args));
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void replace(String key, String value) {
        for (int i = 0; i < arguments.size(); i++) {
            if (arguments.get(i).equals(DOUBLE_HYPHEN + key))
                arguments.set(i + 1, value);
        }
    }

    public String getArg(String key) {
        for (int i = 0; i < arguments.size(); i++) {
            if (arguments.get(i).equals(DOUBLE_HYPHEN + key))
                return arguments.get(i + 1);
        }
        throw new RuntimeException("Argument %s not found!".formatted(key));
    }

    public String[] toArray() {
        return arguments.toArray(String[]::new);
    }
}