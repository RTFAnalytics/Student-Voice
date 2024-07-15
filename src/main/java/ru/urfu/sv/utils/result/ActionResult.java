package ru.urfu.sv.utils.result;

import lombok.Value;

@Value
public class ActionResult {
    boolean success;
    String message;
    String[] params;

    public ActionResult(boolean success, String message, String... params) {
        this.success = success;
        this.message = message;
        this.params = params;
    }

    public String getFormattedMessage() {
        return params.length == 0 ? message : message.formatted((Object[]) params);
    }
}
