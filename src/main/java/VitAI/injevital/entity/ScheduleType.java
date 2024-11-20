package VitAI.injevital.entity;

public enum ScheduleType {
    EXERCISE("운동", "#4A90E2"),    // 파란색
    DIET("식단", "#2ECC71"),        // 초록색
    OTHER("기타", "#95A5A6");       // 회색

    private final String description;
    private final String color;

    ScheduleType(String description, String color) {
        this.description = description;
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }
}