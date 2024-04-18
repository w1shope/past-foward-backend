package aws.retrospective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @Column(name = "comment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String content; // 작성 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section; // 어떤 Section의 게시물에서 작성된 댓글인지

    @CreatedDate
    private LocalDateTime createDate; // 댓글 작성 일자

    private LocalDateTime deletedDate; // 삭제 일자

    @Builder
    public Comment(Long id, String content, User user, Section section, LocalDateTime deletedDate, LocalDateTime createDate) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.section = section;
        this.createDate = createDate;
        this.deletedDate = deletedDate;
    }

    public void updateContent(String content) {
        this.content = content;
    }

}