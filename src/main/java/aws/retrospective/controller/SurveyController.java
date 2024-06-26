package aws.retrospective.controller;

import aws.retrospective.common.CommonApiResponse;
import aws.retrospective.common.CurrentUser;
import aws.retrospective.dto.SurveyDto;
import aws.retrospective.entity.User;
import aws.retrospective.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/surveys")
@Tag(name = "surveys")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping("/responses")
    @Operation(summary = "설문조사 저장", description = "설문조사 결과를 DB에 저장하는 API")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "설문조사가 성공적으로 저장됨")})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addSurvey(@CurrentUser User user, @Valid @RequestBody SurveyDto surveyDto) {
        surveyService.addSurvey(user, surveyDto);
    }

    @Operation(summary = "설문조사 조회", description = "설문조사 데이터를 조회하는 API")
    @ApiResponse(responseCode = "200", description = "설문조사가 성공적으로 조회됨")
    @GetMapping
    public CommonApiResponse<List<SurveyDto>> getAllSurveys() {
        List<SurveyDto> surveys = surveyService.getAllSurveys();

        // 정상적으로 데이터를 조회한 경우 successResponse 메서드로 응답을 구성
        return CommonApiResponse.successResponse(HttpStatus.OK, surveys);
    }
}
