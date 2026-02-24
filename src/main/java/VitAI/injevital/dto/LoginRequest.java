package VitAI.injevital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class LoginRequest {
    @NotBlank
    @Size(min = 3 , max = 50)
    private String memberId;

    @NotBlank
    @Size(min = 3 , max = 100)
    private String memberPassword;


}
