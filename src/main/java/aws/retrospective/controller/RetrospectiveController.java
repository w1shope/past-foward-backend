package aws.retrospective.controller;


import aws.retrospective.dto.CreateRetrospectiveDto;
import aws.retrospective.dto.CreateRetrospectiveResponseDto;
import aws.retrospective.service.RetrospectiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/retrospectives")
@Tag(name = "retrospectives")
public class RetrospectiveController {

    private final RetrospectiveService retrospectiveService;


    @Operation(summary = "회고 생성")
    @PostMapping()
    public CreateRetrospectiveResponseDto createRetrospective(
        @RequestBody CreateRetrospectiveDto createRetrospectiveDto) {
        return retrospectiveService.createRetrospective(createRetrospectiveDto);
    }
}