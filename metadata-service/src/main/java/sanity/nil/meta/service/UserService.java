package sanity.nil.meta.service;

import io.minio.PutObjectArgs;
import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import sanity.nil.exceptions.StorageException;
import sanity.nil.meta.dto.user.UserBaseDTO;
import sanity.nil.meta.mappers.StatisticsMapper;
import sanity.nil.meta.mappers.SubscriptionMapper;
import sanity.nil.meta.mappers.UserMapper;
import sanity.nil.meta.model.StatisticsModel;
import sanity.nil.meta.model.UserModel;
import sanity.nil.meta.model.UserStatisticsModel;
import sanity.nil.meta.model.UserSubscriptionModel;
import sanity.nil.minio.MinioOperations;
import sanity.nil.security.IdentityProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    @Inject
    UserMapper userMapper;
    @Inject
    @Named("keycloakIdentityProvider")
    IdentityProvider identityProvider;
    @Inject
    MinioOperations minioOperations;
    private final String DEFAULT_STATISTICS_VALUE = "0";

    @Transactional
    public UserBaseDTO getUser(UUID id) {
        var identity = identityProvider.getCheckedIdentity();
        var user = new UserModel();
        try {
            user = entityManager.createQuery("SELECT u FROM UserModel u " +
                            "WHERE u.id = :id", UserModel.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            // in case such user doesn't exist, means that it's first getInfo request after register
            // so we can create a new user
            var defaultSubscription = entityManager.find(UserSubscriptionModel.class, (short) 1);
            user = new UserModel(identity.getUserID(), identity.getUsername(), identity.getEmail(), defaultSubscription);
            var defaultStatistics = entityManager.createQuery("SELECT s FROM StatisticsModel s " +
                            "WHERE s.id IN :id", StatisticsModel.class)
                    .setParameter("id", List.of((short) 1, (short) 2))
                    .getResultList();
            entityManager.persist(user);
            for (var statistics : defaultStatistics) {
                var userStatistic = new UserStatisticsModel(user, statistics, DEFAULT_STATISTICS_VALUE);
                entityManager.persist(userStatistic);
            }
        }
        var subscriptionDTO = subscriptionMapper.entityToDTO(user.getSubscription());

        String avatarURL = null;
        if (StringUtils.isNotBlank(user.getAvatar())) {
            avatarURL = minioOperations.getObjectURL(user.getAvatar());
        }

        if (id.equals(identity.getUserID())) {
            var statisticsModel = entityManager.createQuery("SELECT s FROM UserStatisticsModel s " +
                            "WHERE s.id.userID = :id", UserStatisticsModel.class)
                    .setParameter("id", id)
                    .getResultList();
            var statisticsDTOs = statisticsModel.stream().map(statisticsMapper::entityToDTO).toList();
            var userDTO = userMapper.entityToExtendedDTO(user);
            userDTO.subscription = subscriptionDTO;
            userDTO.statistics = statisticsDTOs;
            userDTO.avatarURL = avatarURL;
            return userDTO;
        } else {
            var userDTO = userMapper.entityToDTO(user);
            userDTO.subscription = subscriptionDTO;
            userDTO.avatarURL = avatarURL;
            return userDTO;
        }
    }

    public String uploadFile(FileUpload photo) {
        var identity = identityProvider.getCheckedIdentity();
        var filename = identity.getUserID().toString().substring(0, 10) + photo.fileName();
        try {
            minioOperations.putObject(
                    PutObjectArgs.builder().contentType(photo.contentType())
                            .object(filename)
                            .stream(new FileInputStream(photo.uploadedFile().toFile()), photo.size(), -1)
            );
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            throw new StorageException(e.getMessage());
        }
        return filename;
    }

    @Transactional
    public String changeUserAvatar(String userID, String photo) {
        var identity = identityProvider.getCheckedIdentity();
        if (!userID.equals(identity.getUserID().toString())) {
            throw new ForbiddenException();
        }
        var user = entityManager.find(UserModel.class, identity.getUserID());
        user.setAvatar(photo);
        entityManager.persist(user);
        return minioOperations.getObjectURL(photo);
    }
}
