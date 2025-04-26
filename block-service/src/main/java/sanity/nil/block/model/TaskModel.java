package sanity.nil.block.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonNodeBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import sanity.nil.block.consts.TaskStatus;
import sanity.nil.block.consts.TaskType;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
@Entity
public class TaskModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "object_id")
    private String objectID;

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private TaskType action;

    @Column(name = "metadata", columnDefinition = "jsonb")
    @Type(JsonNodeBinaryType.class)
    private JsonNode metadata;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private Short retries;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private LocalDateTime updatedAt;

    public TaskModel(String objectID, TaskType action, JsonNode metadata, TaskStatus status) {
        this.objectID = objectID;
        this.action = action;
        this.metadata = metadata;
        this.status = status;
        this.retries = 0;
    }
}
