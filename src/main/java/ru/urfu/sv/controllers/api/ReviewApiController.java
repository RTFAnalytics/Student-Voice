package ru.urfu.sv.controllers.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urfu.sv.controllers.ReviewController;
import ru.urfu.sv.utils.result.ActionResultResponse;

import java.util.Map;

import static ru.urfu.sv.utils.consts.Parameters.RESULT;
import static ru.urfu.sv.utils.result.ActionResultResponse.fromActionResult;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewApiController {
    private final ReviewController reviewController;

    @PostMapping("save")
    @Parameters(value = {
            @Parameter(name = "sessionId", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "studentFullName", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "reviewValue", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "comment", in = ParameterIn.QUERY, required = true)
    })
    public ResponseEntity<Map<String, Object>> createCourse(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        reviewController.saveReview(request, model);

        ActionResultResponse result = fromActionResult(model.getAttribute(RESULT));
        return ResponseEntity.ok().body(Map.of(RESULT, result));
    }
}
