package sanity.nil.meta.service;

import io.minio.PutObjectArgs;
import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jooq.DSLContext;
import sanity.nil.exceptions.StorageException;
import sanity.nil.meta.db.tables.Statistics;
import sanity.nil.meta.db.tables.UserSubscriptions;
import sanity.nil.meta.db.tables.records.UsersRecord;
import sanity.nil.meta.dto.user.StatisticsDTO;
import sanity.nil.meta.dto.user.UserBaseDTO;
import sanity.nil.meta.mappers.SubscriptionMapper;
import sanity.nil.meta.mappers.UserMapper;
import sanity.nil.minio.MinioOperations;
import sanity.nil.security.IdentityProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static sanity.nil.meta.db.Tables.*;
import static sanity.nil.meta.db.tables.UserStatistics.USER_STATISTICS;

@JBossLog
@ApplicationScoped
public class UserService {

    @Inject
    DSLContext dslContext;
    @Inject
    SubscriptionMapper subscriptionMapper;
    @Inject
    UserMapper userMapper;
    @Inject
    @Named("keycloakIdentityProvider")
    IdentityProvider identityProvider;
    @Inject
    MinioOperations minioOperations;
    private final String DEFAULT_STATISTICS_VALUE = "0";
    private final String AVATAR_BUCKET = "user.avatars";

    @Transactional
    public UserBaseDTO getUser(UUID id) {
        var identity = identityProvider.getCheckedIdentity();
        var userOp = dslContext.selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .fetchOptional();
        var user = userOp.orElse(null);
        if (userOp.isEmpty() && id.equals(identity.getUserID())) {
            // in case such user doesn't exist, means that it's first getInfo request after register
            // so we can create a new user
            var defaultSubscription = dslContext.selectFrom(UserSubscriptions.USER_SUBSCRIPTIONS)
                    .where(USER_SUBSCRIPTIONS.ID.eq((short) 1))
                    .fetchOne();
            user = new UsersRecord(id, identity.getEmail(), identity.getUsername(), null,
                    defaultSubscription.getId(), OffsetDateTime.now(), OffsetDateTime.now());
            var defaultStatistics = dslContext.selectFrom(Statistics.STATISTICS)
                    .where(STATISTICS.ID.in(List.of((short) 1, (short) 2)))
                    .fetch();
            dslContext.attach(user);
            user.store();
            for (var statistics : defaultStatistics) {
                var userStatistic = dslContext.newRecord(USER_STATISTICS);
                userStatistic.setStatisticsId(statistics.getId());
                userStatistic.setUserId(user.getId());
                userStatistic.setValue(DEFAULT_STATISTICS_VALUE);
                userStatistic.store();
            }
        }
        var subscription = dslContext.selectFrom(USER_SUBSCRIPTIONS)
                .where(USER_SUBSCRIPTIONS.ID.eq(user.getSubscriptionId()))
                .fetchOne();
        var subscriptionDTO = subscriptionMapper.entityToDTO(subscription);

        String avatarURL = null;
        if (StringUtils.isNotBlank(user.getAvatar())) {
            avatarURL = minioOperations.getObjectURL(AVATAR_BUCKET, user.getAvatar());
        }

        if (id.equals(identity.getUserID())) {
            var statistics = dslContext.select(USER_STATISTICS.asterisk(), STATISTICS.asterisk())
                    .from(USER_STATISTICS)
                    .join(STATISTICS).on(USER_STATISTICS.STATISTICS_ID.eq(STATISTICS.ID))
                    .where(USER_STATISTICS.USER_ID.eq(user.getId()))
                    .fetch().map(record -> {
                        return new StatisticsDTO(record.get(STATISTICS.ID), record.get(STATISTICS.QUOTA),
                                record.get(STATISTICS.DESCRIPTION), record.get(USER_STATISTICS.VALUE));
                    });
            var userDTO = userMapper.entityToExtendedDTO(user);
            userDTO.subscription = subscriptionDTO;
            userDTO.statistics = statistics;
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
                            .bucket(AVATAR_BUCKET)
                            .stream(new FileInputStream(photo.uploadedFile().toFile()), photo.size(), -1).build()
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
        dslContext.update(USERS).set(USERS.AVATAR, photo)
                .where(USERS.ID.eq(identity.getUserID()))
                .execute();
        return minioOperations.getObjectURL(AVATAR_BUCKET, photo);
    }
}
