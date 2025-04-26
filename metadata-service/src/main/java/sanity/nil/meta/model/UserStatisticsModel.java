package sanity.nil.meta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_statistics")
@Entity
public class UserStatisticsModel {

    @EmbeddedId
    private UserStatisticsIDModel id;

    @ManyToOne
    @MapsId("userID")
    @JoinColumn(name = "user_id")
    private UserModel user;

    @ManyToOne
    @MapsId("statisticsID")
    @JoinColumn(name = "statistics_id", columnDefinition = "smallint")
    private StatisticsModel statistics;

    private String value;

    @Embeddable
    public record UserStatisticsIDModel(
            @Column(name = "user_id")
            UUID userID,
            @Column(name = "statistics_id")
            Short statisticsID
    ) {}

    public UserStatisticsModel(UserModel user, StatisticsModel statistics, String value) {
        id = new UserStatisticsIDModel(user.getId(), statistics.getId());
        this.user = user;
        this.statistics = statistics;
        this.value = value;
    }
}
