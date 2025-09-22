package ru.practicum.interaction.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationDto {

    Long eventId;

    @JsonProperty(value = "lat")
    Double latitude;

    @JsonProperty(value = "lon")
    Double longitude;
}
