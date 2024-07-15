package ru.urfu.sv.utils.result;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;

@Value
@JsonSerialize
public class ActionResultResponse {
    boolean success;
    String message;

    public static ActionResultResponse fromActionResult(Object result){
        if(!(result instanceof ActionResult actionResult)) return new ActionResultResponse(true, "Успешно");
        return new ActionResultResponse(actionResult.isSuccess(), actionResult.getFormattedMessage());
    }
}
