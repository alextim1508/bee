package com.alextim.bee.frontend.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public abstract class AbstractDialogController {

    protected List<String> errors = new ArrayList<>();

    abstract protected boolean check();

    public String getErrors() {
        StringJoiner joiner = new StringJoiner(", ");
        errors.forEach(joiner::add);
        return joiner.toString();
    }
}
