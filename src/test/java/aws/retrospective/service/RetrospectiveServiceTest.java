package aws.retrospective.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aws.retrospective.dto.CreateRetrospectiveDto;
import aws.retrospective.dto.CreateRetrospectiveResponseDto;
import aws.retrospective.dto.GetRetrospectiveResponseDto;
import aws.retrospective.dto.GetRetrospectivesDto;
import aws.retrospective.dto.PaginationResponseDto;
import aws.retrospective.dto.RetrospectiveResponseDto;
import aws.retrospective.dto.RetrospectiveType;
import aws.retrospective.dto.RetrospectivesOrderType;
import aws.retrospective.dto.UpdateRetrospectiveDto;
import aws.retrospective.entity.ProjectStatus;
import aws.retrospective.entity.Retrospective;
import aws.retrospective.entity.RetrospectiveTemplate;
import aws.retrospective.entity.Team;
import aws.retrospective.entity.User;
import aws.retrospective.repository.RetrospectiveRepository;
import aws.retrospective.repository.RetrospectiveTemplateRepository;
import aws.retrospective.repository.TeamRepository;
import aws.retrospective.repository.UserRepository;
import aws.retrospective.repository.UserTeamRepository;
import aws.retrospective.util.TestUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RetrospectiveServiceTest {

    @Mock
    private RetrospectiveRepository retrospectiveRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserTeamRepository userTeamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RetrospectiveTemplateRepository templateRepository;

    @InjectMocks
    private RetrospectiveService retrospectiveService;

    @Test
    void getRetrospectives_ReturnsRetrospectiveList_WhenCalledWithValidDto() {
        // given
        GetRetrospectivesDto dto = new GetRetrospectivesDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setOrder(RetrospectivesOrderType.OLDEST);
        dto.setKeyword("keyword");

        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize(),
            Sort.by(Sort.Direction.ASC, "createdDate"));
        List<Retrospective> retrospectiveList = new ArrayList<>();

        Retrospective retrospective = new Retrospective("New Retro",
            null,
            "some description",
            null,
            ProjectStatus.IN_PROGRESS,
            new Team("Team Name"), new User("user1", "test", "test", "test"),
            new RetrospectiveTemplate("Template Name"),
            LocalDateTime.now());

        ReflectionTestUtils.setField(retrospective, "id", 1L);

        retrospectiveList.add(retrospective);
        Page<Retrospective> retrospectivePage = new PageImpl<>(retrospectiveList, pageable,
            retrospectiveList.size());

        BDDMockito.given(retrospectiveRepository.findAll(any(Specification.class), eq(pageable)))
            .willReturn(retrospectivePage);

        // when
        PaginationResponseDto<RetrospectiveResponseDto> result = retrospectiveService.getRetrospectives(
            new User("user1", "test", "test", "test"),
            dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.nodes()).isNotEmpty();
        assertThat(result.nodes().size()).isEqualTo(retrospectiveList.size());
        assertThat(result.nodes().get(0).getId()).isEqualTo(retrospective.getId());

        verify(retrospectiveRepository).findAll(any(Specification.class), eq(pageable));

    }


    @Test
    void createRetrospective_ReturnsResponseDto_WhenCalledWithValidDto() {
        // given
        User user = new User("user1", "test", "test", "test");
        ReflectionTestUtils.setField(user, "id", 1L);
        BDDMockito.given(userRepository.findById(1L)).willReturn(Optional.of(user));

        Team team = new Team("Team Name");
        ReflectionTestUtils.setField(team, "id", 1L);
        BDDMockito.given(teamRepository.save(any(Team.class))).willReturn(team);

        RetrospectiveTemplate template = new RetrospectiveTemplate("Template Name");
        ReflectionTestUtils.setField(template, "id", 1L);
        BDDMockito.given(templateRepository.findById(1L)).willReturn(Optional.of(template));

        Retrospective retrospective = new Retrospective("New Retro",
            null,
            "some description",
            null,
            ProjectStatus.IN_PROGRESS,
            team, user, template,
            LocalDateTime.now());
        ReflectionTestUtils.setField(retrospective, "id", 1L);
        BDDMockito.given(retrospectiveRepository.save(any(Retrospective.class)))
            .willReturn(retrospective);

        CreateRetrospectiveDto dto = new CreateRetrospectiveDto();
        ReflectionTestUtils.setField(dto, "title", "New Retro");
        ReflectionTestUtils.setField(dto, "type", RetrospectiveType.TEAM);
        ReflectionTestUtils.setField(dto, "templateId", 1L);
        ReflectionTestUtils.setField(dto, "status", ProjectStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(dto, "thumbnail", UUID.randomUUID());
        ReflectionTestUtils.setField(dto, "description", "some description");
        ReflectionTestUtils.setField(dto, "startDate", LocalDateTime.now());

        // when
        CreateRetrospectiveResponseDto response = retrospectiveService.createRetrospective(user,
            dto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(dto.getTitle());
        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getTeamId()).isEqualTo(team.getId());
        assertThat(response.getTemplateId()).isEqualTo(template.getId());
        assertThat(response.getDescription()).isEqualTo(dto.getDescription());
    }

    @Test
    void deleteRetrospective_Success() {
        // Arrange
        User user = TestUtil.createUser();
        Team team = TestUtil.createTeam();
        RetrospectiveTemplate retrospectiveTemplate = TestUtil.createTemplate();
        Retrospective retrospective = TestUtil.createRetrospective(retrospectiveTemplate, user,
            team);

        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(retrospective, "id", 1L);
        ReflectionTestUtils.setField(retrospective, "user", user);

        when(retrospectiveRepository.findById(1L)).thenReturn(Optional.of(retrospective));
        doNothing().when(retrospectiveRepository).deleteById(1L);

        // Act
        retrospectiveService.deleteRetrospective(1L, user);

        // Assert
        verify(retrospectiveRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRetrospective_Failure_UserNotAuthorized() {
        // Arrange
        User authorizedUser = TestUtil.createUser();
        Team team = TestUtil.createTeam();
        RetrospectiveTemplate retrospectiveTemplate = TestUtil.createTemplate();
        ReflectionTestUtils.setField(authorizedUser, "id", 1L);

        User unauthorizedUser = TestUtil.createUser();
        ReflectionTestUtils.setField(unauthorizedUser, "id", 123L);

        Retrospective unauthorizedRetrospective = TestUtil.createRetrospective(
            retrospectiveTemplate, unauthorizedUser,
            team);
        ReflectionTestUtils.setField(unauthorizedRetrospective, "id", 1L);
        ReflectionTestUtils.setField(unauthorizedRetrospective, "user", authorizedUser);

        when(retrospectiveRepository.findById(1L)).thenReturn(
            Optional.of(unauthorizedRetrospective));

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            retrospectiveService.deleteRetrospective(1L, unauthorizedUser);
        });

        assertTrue(thrown.getMessage().contains("Not allowed to delete retrospective"));
    }

    @Test
    void deleteRetrospective_Failure_RetrospectiveNotFound() {
        // Arrange
        User user = TestUtil.createUser();

        when(retrospectiveRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException thrown = assertThrows(NoSuchElementException.class, () -> {
            retrospectiveService.deleteRetrospective(1L, user);
        });

        assertTrue(thrown.getMessage().contains("Not found retrospective"));
    }

    @Test
    void updateRetrospective_Success() {
        // Arrange
        User user = TestUtil.createUser();
        Team team = TestUtil.createTeam();
        RetrospectiveTemplate retrospectiveTemplate = TestUtil.createTemplate();
        Retrospective retrospective = TestUtil.createRetrospective(retrospectiveTemplate, user,
            team);

        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(retrospective, "id", 1L);
        ReflectionTestUtils.setField(retrospective, "user", user);

        UpdateRetrospectiveDto dto = new UpdateRetrospectiveDto();
        ReflectionTestUtils.setField(dto, "title", "New Retro");
        ReflectionTestUtils.setField(dto, "teamId", 1L);
        ReflectionTestUtils.setField(dto, "status", ProjectStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(dto, "thumbnail", UUID.randomUUID());
        ReflectionTestUtils.setField(dto, "description", "New Description");

        when(retrospectiveRepository.findById(1L)).thenReturn(Optional.of(retrospective));

        // Act
        RetrospectiveResponseDto response = retrospectiveService.updateRetrospective(user,
            1L,
            dto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(retrospective.getId());
        assertThat(response.getThumbnail()).isEqualTo(retrospective.getThumbnail());
    }

    @Test
    void updateRetrospective_Failure_RetrospectiveNotFound() {
        // Arrange
        User user = TestUtil.createUser();
        when(retrospectiveRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException thrown = assertThrows(NoSuchElementException.class, () -> {
            retrospectiveService.updateRetrospective(user, 1L, new UpdateRetrospectiveDto());
        });

        assertTrue(thrown.getMessage().contains("Not found retrospective"));
    }

    @Test
    @DisplayName("단일 회고를 조회할 수 있다.")
    void findRetrospective() {
        // given
        Long userId = 1L;
        User user = TestUtil.createUser();
        ReflectionTestUtils.setField(user, "id", userId);

        Long teamId = 2L;
        Team team = TestUtil.createTeam();
        ReflectionTestUtils.setField(team, "id", teamId);

        Long templateId = 3L;
        RetrospectiveTemplate retrospectiveTemplate = TestUtil.createTemplate(); // KPT
        ReflectionTestUtils.setField(retrospectiveTemplate, "id", templateId);

        Long retrospectiveId = 4L;
        Retrospective retrospective = TestUtil.createRetrospective(retrospectiveTemplate, user,
            team);
        ReflectionTestUtils.setField(retrospective, "id", retrospectiveId);
        when(retrospectiveRepository.findRetrospectiveById(retrospectiveId)).thenReturn(
            Optional.of(retrospective));

        // when
        GetRetrospectiveResponseDto findRetrospective = retrospectiveService.getRetrospective(
            user,
            retrospectiveId);

        // then
        assertThat(findRetrospective.getRetrospectiveId()).isEqualTo(retrospectiveId);
        assertThat(findRetrospective.getTitle()).isEqualTo("test");
        assertThat(findRetrospective.getTemplateId()).isEqualTo(templateId);
        assertThat(findRetrospective.getType()).isEqualTo(RetrospectiveType.TEAM);
        assertThat(findRetrospective.getUserId()).isEqualTo(userId);
        assertThat(findRetrospective.getLeaderName()).isEqualTo("test");
        assertThat(findRetrospective.getDescription()).isEqualTo("test");
        assertThat(findRetrospective.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(findRetrospective.getThumbnail()).isEqualTo(retrospective.getThumbnail());

    }


}
