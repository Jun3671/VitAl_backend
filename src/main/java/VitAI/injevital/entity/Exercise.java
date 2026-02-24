// Exercise.java
package VitAI.injevital.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exercise_table")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;            // 운동 이름

    @Column(nullable = false)
    private String part;            // 운동 부위 (어깨, 등, 가슴, 하체, 팔, 복근)

    @Column(nullable = false)
    private String difficulty;      // 난이도 (초급, 중급, 고급)

    @Column(columnDefinition = "TEXT")
    private String description;     // 운동 설명

    @Column(columnDefinition = "TEXT")
    private String benefits;        // 운동 효과

    @Column(columnDefinition = "TEXT")
    private String caution;         // 주의사항

    private String youtubeUrl;      // 운동 영상 URL

    // YouTube URL을 설정할 때 자동으로 검증하고 표준화하는 메소드
    public void setYoutubeUrl(String url) {
        if (url != null && !url.trim().isEmpty()) {
            // URL이 유효한지 확인
            try {
                String videoId = extractYoutubeVideoId(url);
                if (videoId != null) {
                    // 표준 형식으로 저장
                    this.youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;
                } else {
                    throw new IllegalArgumentException("유효하지 않은 YouTube URL입니다.");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("YouTube URL 처리 중 오류가 발생했습니다: " + e.getMessage());
            }
        } else {
            this.youtubeUrl = null;
        }
    }

    // YouTube 비디오 ID 추출 메소드
    private String extractYoutubeVideoId(String url) {
        if (url == null) return null;

        String videoId = null;
        try {
            if (url.contains("youtube.com")) {
                String[] urlParts = url.split("v=");
                if (urlParts.length >= 2) {
                    videoId = urlParts[1];
                    int ampersandIndex = videoId.indexOf('&');
                    if (ampersandIndex != -1) {
                        videoId = videoId.substring(0, ampersandIndex);
                    }
                }
            } else if (url.contains("youtu.be")) {
                String[] urlParts = url.split("youtu.be/");
                if (urlParts.length >= 2) {
                    videoId = urlParts[1];
                }
            }
        } catch (Exception e) {
            return null;
        }

        // 유효한 video ID 길이 확인 (YouTube video ID는 11자)
        return (videoId != null && videoId.length() == 11) ? videoId : null;
    }

    private String equipment;       // 필요한 운동기구

    @Column(columnDefinition = "TEXT")
    private String contraindications; // 금기사항 (쉼표로 구분: "허리통증,어깨통증")

    private String defaultSets;     // 기본 세트 수 추천

    private String defaultReps;     // 기본 반복 수 추천

    private String defaultRestTime; // 기본 휴식 시간
}