package ru.practicum.analyzer.similarity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

// jpa
@Entity
@Table(name = "similarities",
        indexes = {@Index(name = "events_idx", columnList = "eventA, eventB"), // при добавлении
                @Index(name = "event_a_idx", columnList = "eventA"), // при выборке по событию А
                @Index(name = "event_b_idx", columnList = "eventB")}) // при выборке по событию Б
// lombok
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Similarity implements Comparable<Similarity> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "event1", nullable = false)
    Long eventA;

    @Column(name = "event2", nullable = false)
    Long eventB;

    @Column(name = "rating", nullable = false)
    double rating;

    @Column(name = "ts", nullable = false)
    Instant timestamp;

    @Override
    public int compareTo(Similarity another) {
        return Double.compare(this.rating, another.rating);
    }
}
