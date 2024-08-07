package aws.retrospective.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aws.retrospective.dto.SurveyDto;
import aws.retrospective.entity.Survey;
import aws.retrospective.entity.Survey.Gender;
import aws.retrospective.entity.User;
import aws.retrospective.repository.SurveyRepository;
import aws.retrospective.repository.UserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SurveyServiceTest {


    @InjectMocks
    private SurveyService surveyService;

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("설문조사 결과 추가")
    void addSurveyTest() {
        // Given
        User user = User.builder()
            .email("test@example.com")
            .username("testuser")
            .phone("123-456-7890")
            .tenantId("tenant1")
            .isAdministrator(false)
            .isEmailConsent(true)
            .build();

        List<String> purposes = Arrays.asList("purpose1", "purpose2", "purpose3");
        SurveyDto surveyDto = SurveyDto.builder()
            .age(22)
            .gender("FEMALE")
            .occupation("student")
            .region("Korea")
            .source("internet")
            .purposes(purposes)
            .emailConsents(true) // 추가된 필드
            .build();

        // When
        surveyService.addSurvey(user, surveyDto);

        // Then
        ArgumentCaptor<Survey> surveyCaptor = ArgumentCaptor.forClass(Survey.class);
        verify(surveyRepository).save(surveyCaptor.capture());
        Survey savedSurvey = surveyCaptor.getValue();

        assertThat(savedSurvey.getAge()).isEqualTo(surveyDto.getAge());
        assertThat(savedSurvey.getGender().name()).isEqualTo(surveyDto.getGender());
        assertThat(savedSurvey.getOccupation()).isEqualTo(surveyDto.getOccupation());
        assertThat(savedSurvey.getRegion()).isEqualTo(surveyDto.getRegion());
        assertThat(savedSurvey.getSource()).isEqualTo(surveyDto.getSource());
        assertThat(savedSurvey.getPurposes()).isEqualTo(surveyDto.getPurposes());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.isEmailConsent()).isEqualTo(surveyDto.getEmailConsents());
    }

    @Test
    void getAllSurveys() {
        // 가짜 데이터 생성
        List<String> purposes = Arrays.asList("purpose1", "purpose2", "purpose3");
        User user = User.builder()
            .isEmailConsent(true)
            .build();
        List<Survey> surveys = new ArrayList<>();
        surveys.add(Survey.builder()
            .age(30)
            .gender(String.valueOf(Gender.valueOf("MALE")))
            .occupation("Engineer")
            .region("Seoul")
            .source("Internet")
            .purposes(purposes)
            .user(user)
            .build());
        // Mock 객체 설정
        when(surveyRepository.findAll()).thenReturn(surveys);

        // 테스트 실행
        List<SurveyDto> response = surveyService.getAllSurveys();

        // 결과 확인
        assertEquals(surveys.size(), response.size());

        // 추가적인 검증
        SurveyDto surveyDto = response.get(0);
        assertEquals(30, surveyDto.getAge());
        assertEquals("MALE", surveyDto.getGender());
        assertEquals("Engineer", surveyDto.getOccupation());
        assertEquals("Seoul", surveyDto.getRegion());
        assertEquals("Internet", surveyDto.getSource());
        assertEquals(purposes, surveyDto.getPurposes());
        assertTrue(surveyDto.getEmailConsents());
    }

    @Test
    public void testGetGenderAndAgeSurveys() {
        // 가짜 데이터 생성
        List<String> purposes = Arrays.asList("purpose1", "purpose2", "purpose3");
        List<Survey> surveys = new ArrayList<>();
        surveys.add(Survey.builder()
            .age(30)
            .gender(String.valueOf(Gender.valueOf("MALE")))
            .occupation("Engineer")
            .region("Seoul")
            .source("Internet")
            .purposes(purposes)
            .build());

        // Mock 객체 설정
        when(surveyRepository.findAll()).thenReturn(surveys);

        // 테스트 수행
        List<SurveyDto> surveyDtos = surveyService.getGenderAndAgeSurveys();

        // 결과 검증
        assertEquals(1, surveyDtos.size());

        SurveyDto surveyDto = surveyDtos.get(0);
        assertEquals(30, surveyDto.getAge());
        assertEquals("MALE", surveyDto.getGender());

        // findAll 메서드가 한 번 호출되었는지 검증
        verify(surveyRepository, times(1)).findAll();
    }

    @Test
    public void testGetOccupationAndRegionSurveys() {
        // 가짜 데이터 생성
        List<String> purposes = Arrays.asList("purpose1", "purpose2", "purpose3");
        User user = User.builder()
            .isEmailConsent(true)
            .build();
        List<Survey> surveys = new ArrayList<>();
        surveys.add(Survey.builder()
            .age(30)
            .gender("MALE")
            .occupation("Engineer")
            .region("Seoul")
            .source("Internet")
            .purposes(purposes)
            .user(user)
            .build());

        // Mock 객체 설정
        when(surveyRepository.findAll()).thenReturn(surveys);

        // 테스트 수행
        List<SurveyDto> surveyDtos = surveyService.getOccupationAndRegionSurveys();

        // 결과 검증
        assertEquals(1, surveyDtos.size());

        SurveyDto surveyDto = surveyDtos.get(0);
        assertEquals("Engineer", surveyDto.getOccupation());
        assertEquals("Seoul", surveyDto.getRegion());

        // findAll 메서드가 한 번 호출되었는지 검증
        verify(surveyRepository, times(1)).findAll();
    }

    @Test
    public void testGetSourceAndPurposeSurveys() {
        // 가짜 데이터 생성
        List<String> purposes = Arrays.asList("purpose1", "purpose2", "purpose3");
        User user = User.builder()
            .isEmailConsent(true)
            .build();
        List<Survey> surveys = new ArrayList<>();
        surveys.add(Survey.builder()
            .age(30)
            .gender("MALE")
            .occupation("Engineer")
            .region("Seoul")
            .source("Internet")
            .purposes(purposes)
            .user(user)
            .build());

        // Mock 객체 설정
        when(surveyRepository.findAll()).thenReturn(surveys);

        // 테스트 수행
        List<SurveyDto> surveyDtos = surveyService.getSourceAndPurposeSurveys();

        // 결과 검증
        assertEquals(1, surveyDtos.size());

        SurveyDto surveyDto = surveyDtos.get(0);
        assertEquals("Internet", surveyDto.getSource());
        assertEquals(purposes, surveyDto.getPurposes());
        assertEquals(true, surveyDto.getEmailConsents());

        // findAll 메서드가 한 번 호출되었는지 검증
        verify(surveyRepository, times(1)).findAll();
    }
}
