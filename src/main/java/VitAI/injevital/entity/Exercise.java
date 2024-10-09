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

    private String equipment;       // 필요한 운동기구

    @Column(columnDefinition = "TEXT")
    private String contraindications; // 금기사항 (쉼표로 구분: "허리통증,어깨통증")

    private String defaultSets;     // 기본 세트 수 추천

    private String defaultReps;     // 기본 반복 수 추천

    private String defaultRestTime; // 기본 휴식 시간
}