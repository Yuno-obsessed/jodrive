package sanity.nil.meta.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.keys.KeyScanArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import sanity.nil.meta.cache.model.FileMetadata;
import sanity.nil.meta.dto.user.SubscriptionDTO;
import sanity.nil.meta.model.FileJournalModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Startup
@JBossLog
@ApplicationScoped
public class FileMetadataCache {

    KeyCommands<String> keys;
    ValueCommands<String, FileMetadata> values;

    private final String ANY = "*";

    public FileMetadataCache(RedisDataSource redisDataSource) {
        keys = redisDataSource.key(String.class);
        values = redisDataSource.value(FileMetadata.class);
    }

    public List<FileMetadata> getByParams(Long wsID, Long fileID, String path, int limit) {
        List<FileMetadata> res = new ArrayList<>();
        var cursor = keys.scan(new KeyScanArgs()
                .count(limit)
                .match(constructKey(wsID, fileID, path))
        );
        while (cursor.hasNext()) {
            for (var foundEntry : cursor.next()) {
                log.info(foundEntry);
                res.add(values.get(foundEntry));
            }
        }
        return res;
    }

    public void persistFileMetadata(FileMetadata fileMetadata, Long wsID, Long fileID) {
        var key = constructKey(wsID, fileID, fileMetadata.path());
        values.set(key, fileMetadata);
    }

    private String constructKey(Long wsID, Long fileID, String path) {
        var key = new StringBuilder();
        key.append(wsID != null ? wsID : ANY).append(".");
        key.append(fileID != null ? fileID : ANY).append(".");
        key.append(path != null ? path : ANY);
        return key.toString();
    }
}
