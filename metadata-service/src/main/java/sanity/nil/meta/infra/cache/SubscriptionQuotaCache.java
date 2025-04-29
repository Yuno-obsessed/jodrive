package sanity.nil.meta.infra.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.extern.jbosslog.JBossLog;
import sanity.nil.meta.consts.Quota;
import sanity.nil.meta.dto.user.SubscriptionDTO;
import sanity.nil.meta.mappers.SubscriptionMapper;
import sanity.nil.meta.model.UserSubscriptionModel;
import sanity.nil.util.CollectionUtils;

@JBossLog
@ApplicationScoped
public class SubscriptionQuotaCache {

    ValueCommands<Short, SubscriptionDTO> valueCommands;
    @Inject
    Instance<EntityManager> entityManagerProxy;
    @Inject
    SubscriptionMapper subscriptionMapper;

    public SubscriptionDTO getByID(Short id) {
        return valueCommands.get(id);
    }

    public SubscriptionDTO getByQuota(Quota quota) {
        return valueCommands.get(quota.id());
    }

    public SubscriptionQuotaCache(RedisDataSource redisDataSource) {
        valueCommands = redisDataSource.value(Short.class, SubscriptionDTO.class);
    }

    @PostConstruct
    public void init() {
        loadFromDB();
    }

    public void loadFromDB() {
        EntityManager entityManager = entityManagerProxy.get();
        var subscriptions = entityManager.createQuery("SELECT s FROM UserSubscriptionModel s", UserSubscriptionModel.class)
                .getResultList();

        if (CollectionUtils.isEmpty(subscriptions)) {
            subscriptions.forEach(subscription -> {
                valueCommands.set(subscription.getId(), subscriptionMapper.entityToDTO(subscription));
            });
        }
        log.debug("Loaded " + subscriptions.size() + " subscriptions from database");
    }
}
