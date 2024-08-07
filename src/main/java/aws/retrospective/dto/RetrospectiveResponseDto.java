package aws.retrospective.dto;

import aws.retrospective.entity.Retrospective;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RetrospectiveResponseDto {

    private Long id;
    private String title;
    private Long userId;
    private String username;
    private Long teamId;
    private Long templateId;
    private String status;
    private Boolean isBookmarked;
    private UUID thumbnail;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public RetrospectiveResponseDto(Long id, String title, Long userId, String username,
        Long teamId,
        Long templateId,
        String status,
        Boolean isBookmarked,
        UUID thumbnail,
        String description,
        LocalDateTime startDate,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
    ) {
        this.id = id;
        this.title = title;
        this.userId = userId;
        this.username = username;
        this.teamId = teamId;
        this.templateId = templateId;
        this.status = status;
        this.isBookmarked = isBookmarked;
        this.thumbnail = thumbnail;
        this.description = description;
        this.startDate = startDate;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;

    }

    public static RetrospectiveResponseDto of(Retrospective retrospective,
        boolean hasBookmarksByUser) {
        return new RetrospectiveResponseDto(
            retrospective.getId(),
            retrospective.getTitle(),
            retrospective.getUser().getId(),
            retrospective.getUser().getUsername(),
            retrospective.getTeam() != null ? retrospective.getTeam().getId() : null,
            retrospective.getTemplate().getId(),
            retrospective.getStatus().name(),
            hasBookmarksByUser,
            retrospective.getThumbnail(),
            retrospective.getDescription(),
            retrospective.getStartDate(),
            retrospective.getCreatedDate(),
            retrospective.getUpdatedDate()
        );
    }

    public static RetrospectiveResponseDto withoutBookmark(Retrospective retrospective) {
        return new RetrospectiveResponseDto(
            retrospective.getId(),
            retrospective.getTitle(),
            retrospective.getUser().getId(),
            retrospective.getUser().getUsername(),
            retrospective.getTeam() != null ? retrospective.getTeam().getId() : null,
            retrospective.getTemplate().getId(),
            retrospective.getStatus().name(),
            null, // 북마크 여부를 설정하지 않음
            retrospective.getThumbnail(),
            retrospective.getDescription(),
            retrospective.getStartDate(),
            retrospective.getCreatedDate(),
            retrospective.getUpdatedDate()
        );
    }
}
