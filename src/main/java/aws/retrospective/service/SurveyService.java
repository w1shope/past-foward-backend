package aws.retrospective.service;

import aws.retrospective.dto.SurveyDto;
import aws.retrospective.entity.Survey;
import aws.retrospective.entity.User;
import aws.retrospective.repository.SurveyRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;

    // 설문지 결과 등록
    @Transactional
    public void addSurvey(User user, SurveyDto dto) {
        checkIfAlreadySubmitted(user);

        Survey survey = Survey.builder()
            .user(user)
            .age(dto.getAge())
            .gender(dto.getGender())
            .occupation(dto.getOccupation())
            .region(dto.getRegion())
            .source(dto.getSource())
            .purposes(dto.getPurposes())
            .build();

        surveyRepository.save(survey);
    }

    public List<SurveyDto> getAllSurveys() {
        List<Survey> surveys = surveyRepository.findAll();
        return surveys.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    private void checkIfAlreadySubmitted(User user) {
        if (surveyRepository.existsByUser(user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 설문조사를 제출하셨습니다.");
        }

    }

    private SurveyDto convertToDto(Survey survey) {
        return SurveyDto.builder()
            .age(survey.getAge())
            .gender(survey.getGender().toString())
            .occupation(survey.getOccupation())
            .region(survey.getRegion())
            .source(survey.getSource())
            .purposes(survey.getPurposes())
            .build();
    }
}