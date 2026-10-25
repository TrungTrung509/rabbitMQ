package testmq.demo.dto.response.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventResponseDTO {
    UUID evenId;
    String name;
    String description;
    Integer number;
    Date startDate;
}
