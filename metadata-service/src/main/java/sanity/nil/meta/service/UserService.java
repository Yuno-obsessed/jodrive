package sanity.nil.meta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.jbosslog.JBossLog;
import org.mindrot.jbcrypt.BCrypt;
import sanity.nil.meta.dto.user.CreateUserDTO;
import sanity.nil.meta.dto.user.UserBaseDTO;
import sanity.nil.meta.mappers.StatisticsMapper;
import sanity.nil.meta.mappers.SubscriptionMapper;
import sanity.nil.meta.model.StatisticsModel;
import sanity.nil.meta.model.UserModel;
import sanity.nil.meta.model.UserStatisticsModel;
import sanity.nil.meta.model.UserSubscriptionModel;

import java.util.List;
import java.util.UUID;

@JBossLog
@ApplicationScoped
public class UserService {

    @Inject
    EntityManager entityManager;
    @Inject
    SubscriptionMapper subscriptionMapper;
    @Inject
    StatisticsMapper statisticsMapper;
    private final String DEFAULT_VALUE = "0";

    @Transactional
    public UUID createUser(CreateUserDTO dto) {
        var defaultSubscription = entityManager.find(UserSubscriptionModel.class, (short) 1);

        String hashedPassword = BCrypt.hashpw(dto.password(), BCrypt.gensalt());
        var newUser = new UserModel(dto.username(), dto.email(), hashedPassword, defaultSubscription);
        var defaultStatistics = entityManager.createQuery("SELECT s FROM StatisticsModel s " +
                "WHERE s.id IN :id", StatisticsModel.class)
                .setParameter("id", List.of((short) 1, (short) 2))
                .getResultList();
        entityManager.persist(newUser);
        for (var statistics : defaultStatistics) {
            var userStatistic = new UserStatisticsModel(newUser, statistics, DEFAULT_VALUE);
            entityManager.persist(userStatistic);
        }
        return newUser.getId();
    }

    public UserBaseDTO getUser(UUID id) {
        var user = entityManager.createQuery("SELECT u FROM UserModel u " +
                "WHERE u.id = :id", UserModel.class)
                .setParameter("id", id)
                .getSingleResult();
        var statisticsModel = entityManager.createQuery("SELECT s FROM UserStatisticsModel s " +
                "WHERE s.id.userID = :id", UserStatisticsModel.class)
                .setParameter("id", id)
                .getResultList();

        var subscriptionDTO = subscriptionMapper.entityToDTO(user.getSubscription());
        var statisticsDTOs = statisticsModel.stream().map(statisticsMapper::entityToDTO).toList();
        return new UserBaseDTO(user.getId(), user.getUsername(), user.getEmail(), subscriptionDTO, statisticsDTOs, user.getCreatedAt());
    }
}
