package VitAI.injevital.repository;

import VitAI.injevital.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // 특정 부위의 모든 운동 찾기
    List<Exercise> findByPart(String part);

    // 특정 부위와 난이도의 운동 찾기
    List<Exercise> findByPartAndDifficulty(String part, String difficulty);

    // 특정 부위의 운동을 찾되, 주어진 제한사항을 가진 운동은 제외
    @Query(nativeQuery = true, value =
            "SELECT DISTINCT e.* FROM exercise_table e " +
                    "WHERE e.part = :part " +
                    "AND e.contraindications NOT REGEXP :conditions " +
                    "ORDER BY RAND() " +
                    "LIMIT :limit")
    List<Exercise> findSafeExercises(
            @Param("part") String part,
            @Param("conditions") String conditions,
            @Param("limit") int limit
    );

    // 특정 운동기구를 사용하는 운동 찾기
    List<Exercise> findByEquipment(String equipment);

    // 운동 이름으로 검색 (부분 일치)
    List<Exercise> findByNameContaining(String namePart);

    // 특정 난이도의 운동만 찾기
    List<Exercise> findByDifficulty(String difficulty);

    // 특정 부위의 운동을 난이도순으로 정렬
    @Query("SELECT e FROM Exercise e WHERE e.part = :part ORDER BY " +
            "CASE e.difficulty " +
            "WHEN '초급' THEN 1 " +
            "WHEN '중급' THEN 2 " +
            "WHEN '고급' THEN 3 END")
    List<Exercise> findByPartOrderByDifficulty(@Param("part") String part);
}