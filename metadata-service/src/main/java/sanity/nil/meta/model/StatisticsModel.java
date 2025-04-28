package sanity.nil.meta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sanity.nil.meta.consts.Quota;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "statistics")
@Entity
public class StatisticsModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;
    @Enumerated(EnumType.STRING)
    private Quota quota;
    private String description;
}
