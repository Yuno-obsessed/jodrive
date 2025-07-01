package sanity.nil.meta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import sanity.nil.meta.consts.FileState;
import sanity.nil.meta.consts.WsRole;
import sanity.nil.meta.db.routines.NextFileId;
import sanity.nil.meta.db.routines.NextHistoryId;
import sanity.nil.meta.db.tables.FileJournal;
import sanity.nil.meta.db.tables.Users;
import sanity.nil.meta.db.tables.records.FileJournalRecord;
import sanity.nil.meta.dto.file.DeletedFileInfo;
import sanity.nil.meta.dto.file.FileInfo;
import sanity.nil.meta.dto.workspace.WorkspaceUserDTO;
import sanity.nil.meta.model.FileJournalEntity;
import sanity.nil.minio.MinioOperations;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static sanity.nil.meta.consts.Constants.DIRECTORY_CHAR;
import static sanity.nil.meta.db.Tables.USER_WORKSPACES;
import static sanity.nil.meta.db.tables.FileJournal.FILE_JOURNAL;
import static sanity.nil.meta.db.tables.Users.USERS;

@ApplicationScoped
public class FileJournalRepo {

    @Inject
    DSLContext dslContext;
    @Inject
    MinioOperations minioOperations;

    public FileJournalRecord insert(FileJournalEntity journal) {

        var nextIDProcedure = new NextFileId();
        nextIDProcedure.attach(dslContext.configuration());
        nextIDProcedure.setPWsId(journal.getWorkspaceID());
        nextIDProcedure.execute();

        var historyIDProcedure = new NextHistoryId();
        historyIDProcedure.attach(dslContext.configuration());
        historyIDProcedure.setPWsId(journal.getWorkspaceID());
        historyIDProcedure.execute();

        var newJournal = dslContext.newRecord(FILE_JOURNAL);
        newJournal.setFileId(nextIDProcedure.getReturnValue());
        newJournal.setWsId(journal.getWorkspaceID());
        newJournal.setBlocklist(journal.getBlockList());
        newJournal.setUploaderId(journal.getUploaderID());
        newJournal.setCreatedAt(OffsetDateTime.now());
        newJournal.setUpdatedAt(OffsetDateTime.now());
        newJournal.setUpdatedAt(OffsetDateTime.now());
        newJournal.setLatest((short) 1);
        newJournal.setPath(journal.getPath());
        newJournal.setSize(journal.getSize());
        newJournal.setState(journal.getState().name());
        newJournal.setHistoryId(Optional.ofNullable(historyIDProcedure.getReturnValue()).orElse(1));
        newJournal.store();
        return newJournal;
    }

    public Optional<FileJournalRecord> findById(Long fileID, Long wsID) {
        return dslContext.selectFrom(FILE_JOURNAL)
                .where(FILE_JOURNAL.FILE_ID.eq(fileID))
                .and(FILE_JOURNAL.WS_ID.eq(wsID))
                .fetchOptional();
    }

    public Optional<FileJournalRecord> findLatestByPathAndState(Long wsID, String path, FileState fileState) {
        return dslContext.select(FILE_JOURNAL.asterisk())
                .from(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(wsID))
                .and(FILE_JOURNAL.PATH.eq(path))
                .and(FILE_JOURNAL.STATE.eq(fileState.name()))
                .and(FILE_JOURNAL.LATEST.eq((short) 1))
                .orderBy(FILE_JOURNAL.HISTORY_ID.desc())
                .limit(1).fetchOptional()
                .map(r -> dslContext.newRecord(FILE_JOURNAL, r));
    }

    public FileJournalRecord findByIdAndStateIn(Long fileID, Long wsID, FileState state) {
        return dslContext.select(FILE_JOURNAL.asterisk().except(FILE_JOURNAL.BLOCKLIST))
                .from(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(wsID))
                .and(FILE_JOURNAL.FILE_ID.eq(fileID))
                .and(FILE_JOURNAL.STATE.eq(state.name()))
                .orderBy(FILE_JOURNAL.HISTORY_ID.desc())
                .limit(1).fetchOne()
                .into(FileJournalRecord.class);
    }

    public FileJournalRecord findByPathAndVersion(Long wsID, String path, int version) {
        return dslContext.select(FILE_JOURNAL.asterisk().except(FILE_JOURNAL.BLOCKLIST))
                .from(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(wsID))
                .and(FILE_JOURNAL.PATH.eq(path))
                .and(FILE_JOURNAL.STATE.eq(FileState.UPLOADED.name()))
                .orderBy(FILE_JOURNAL.HISTORY_ID.asc())
                .limit(1).offset(version-1).fetchOne()
                .into(FileJournalRecord.class);
    }

    public FileInfo getFileInfo(Condition[] conditions) {
        return dslContext
                .select(FILE_JOURNAL.asterisk(), USERS.AVATAR, USERS.USERNAME)
                .from(FILE_JOURNAL)
                .leftJoin(Users.USERS).on(FILE_JOURNAL.UPLOADER_ID.eq(Users.USERS.ID))
                .where(conditions)
                .orderBy(FILE_JOURNAL.UPDATED_AT.desc())
                .limit(1)
                .fetchOne(record -> {
                    var fj = record.into(FILE_JOURNAL);
                    return journalToFileInfo(fj, record.get(USERS.USERNAME), record.get(USERS.AVATAR));
                });
    }

    public List<FileInfo> getFileInfoList(Condition condition, Integer limit, Integer offset) {
        return dslContext
                .select(FILE_JOURNAL.asterisk(), USERS.AVATAR, USERS.USERNAME)
                .from(FILE_JOURNAL)
                .leftJoin(Users.USERS).on(FILE_JOURNAL.UPLOADER_ID.eq(Users.USERS.ID))
                .where(condition)
                .orderBy(FILE_JOURNAL.UPDATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch(record -> {
                    var fj = record.into(FILE_JOURNAL);
                    return journalToFileInfo(fj, record.get(USERS.USERNAME), record.get(USERS.AVATAR));
                });
    }

    public DeletedFileInfo getDeletedFileInfo(Condition[] conditions) {
        return dslContext
                .select(FILE_JOURNAL.asterisk(), USERS.AVATAR, USERS.USERNAME, USER_WORKSPACES.ROLE, USER_WORKSPACES.JOINED_AT)
                .from(FILE_JOURNAL)
                .leftJoin(Users.USERS).on(FILE_JOURNAL.UPLOADER_ID.eq(Users.USERS.ID))
                .leftJoin(USER_WORKSPACES).on(Users.USERS.ID.eq(USER_WORKSPACES.USER_ID), FILE_JOURNAL.WS_ID.eq(USER_WORKSPACES.WS_ID))
                .where(conditions)
                .orderBy(FILE_JOURNAL.UPDATED_AT.desc())
                .limit(1)
                .fetchOne(record -> {
                    var fj = record.into(FILE_JOURNAL);
                    var deletedBy = new WorkspaceUserDTO(fj.getUpdatedBy(), record.get(USERS.USERNAME),
                            record.get(USERS.AVATAR), WsRole.valueOf(record.get(USER_WORKSPACES.ROLE)), record.get(USER_WORKSPACES.JOINED_AT));
                    return journalToDeletedFileInfo(fj, deletedBy);
                });
    }

    public List<DeletedFileInfo> getDeletedFileInfoList(Condition[] conditions) {
        return dslContext
                .select(FILE_JOURNAL.asterisk(), USERS.AVATAR, USERS.USERNAME, USER_WORKSPACES.ROLE, USER_WORKSPACES.JOINED_AT)
                .from(FILE_JOURNAL)
                .leftJoin(Users.USERS).on(FILE_JOURNAL.UPLOADER_ID.eq(Users.USERS.ID))
                .leftJoin(USER_WORKSPACES).on(Users.USERS.ID.eq(USER_WORKSPACES.USER_ID), FILE_JOURNAL.WS_ID.eq(USER_WORKSPACES.WS_ID))
                .where(conditions)
                .orderBy(FILE_JOURNAL.UPDATED_AT.desc())
                .fetch(record -> {
                    var fj = record.into(FILE_JOURNAL);
                    var deletedBy = new WorkspaceUserDTO(fj.getUpdatedBy(), record.get(USERS.USERNAME),
                            record.get(USERS.AVATAR), WsRole.valueOf(record.get(USER_WORKSPACES.ROLE)), record.get(USER_WORKSPACES.JOINED_AT));
                    return journalToDeletedFileInfo(fj, deletedBy);
                });
    }

    public String findPathByID(Long wsID, Long id) {
        return dslContext.select(FILE_JOURNAL.PATH)
                .from(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(wsID))
                .and(FILE_JOURNAL.FILE_ID.eq(id))
                .and(FILE_JOURNAL.STATE.eq(FileState.UPLOADED.name()))
                .orderBy(FILE_JOURNAL.HISTORY_ID.desc())
                .limit(1).fetchOne()
                .into(String.class);
    }

    public List<Long> getVersionsByPath(Long wsID, String path) {
        return dslContext.select(FILE_JOURNAL.FILE_ID)
                .from(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(wsID))
                .and(FILE_JOURNAL.PATH.eq(path))
                .and(FILE_JOURNAL.STATE.eq(FileState.UPLOADED.name()))
                .orderBy(FILE_JOURNAL.HISTORY_ID.asc())
                .fetchInto(Long.class);
    }

    public void updateStateAndNameByPath(String updatedName, FileState updatedState, Long wsID, String path) {
        if (updatedName == null && updatedState == null) {
            return;
        }

        var update = dslContext.update(FILE_JOURNAL);
        UpdateSetMoreStep u = null;
        if (updatedName != null) {
            u = update.set(FILE_JOURNAL.PATH, updatedName);
        }
        if (updatedState != null) {
            u = update.set(FILE_JOURNAL.STATE, updatedState.name());
        }
        u.where(
                FILE_JOURNAL.PATH.eq(path),
                FILE_JOURNAL.WS_ID.eq(wsID)
        ).execute();
    }

    public Condition buildConditionsFromParams(FileJournal fileJournal, Long wsID, String pathLike) {

        String normalizedPath;
        if (!pathLike.endsWith("/")) {
            normalizedPath = pathLike + "/";
        } else {
            normalizedPath = pathLike + "_";
        }
        String upperPath = normalizedPath.toUpperCase();
        Field<String> upperPathField = DSL.upper(fileJournal.PATH);
        Field<String> subPath = DSL.substring(upperPathField, upperPath.length() + 1);

        return FILE_JOURNAL.WS_ID.eq(wsID)
            .and(DSL.upper(fileJournal.PATH).like(upperPath + "%"))
            .and(subPath.notLike("%/_%"))
            .and(FILE_JOURNAL.LATEST.eq((short) 1))
            .and(FILE_JOURNAL.STATE.eq(FileState.UPLOADED.name()));
    }

    public List<FileInfo> getFileNodesByFilters(Long wsID, String path) {
        String pathLike = path.endsWith("/") ? path + "%" : null;

        var query = dslContext.select(
                        FILE_JOURNAL.asterisk().except(FILE_JOURNAL.BLOCKLIST),
                        USERS.USERNAME,
                        USERS.AVATAR
                )
                .from(FILE_JOURNAL)
                .join(USERS).on(FILE_JOURNAL.UPLOADER_ID.eq(USERS.ID))
                .where(FILE_JOURNAL.WS_ID.eq(wsID))
                .and(pathLike == null
                        ? DSL.trueCondition()
                        : FILE_JOURNAL.PATH.like(pathLike))
                .and(FILE_JOURNAL.STATE.eq(FileState.UPLOADED.name()));

        return query.fetch(record -> {
            var fj = record.into(FILE_JOURNAL);
            var fileInfo = journalToFileInfo(fj, record.get(USERS.USERNAME), record.get(USERS.AVATAR));
            fileInfo.name = fj.getPath(); // override default name substr
            return fileInfo;
        });
    }

    public FileInfo journalToFileInfo(FileJournalRecord fileJournal) {
        var userInfo = dslContext.select(USERS.USERNAME, USERS.AVATAR).from(USERS)
                .where(USERS.ID.eq(fileJournal.getUploaderId()))
                .fetchOne();
        return journalToFileInfo(fileJournal, userInfo.get(USERS.USERNAME), userInfo.get(USERS.AVATAR));
    }

    private FileInfo journalToFileInfo(FileJournalRecord fileJournal, String uploaderName, String uploaderAvatar) {
        var fileInfo = new FileInfo(fileJournal.getFileId(), fileJournal.getWsId(), extractFilename(fileJournal.getPath()),
                isFileDirectory(fileJournal.getPath()), fileJournal.getSize(), fileJournal.getUploaderId(),
                uploaderName, null, fileJournal.getUpdatedAt()
        );
        if (uploaderAvatar != null) {
            fileInfo.uploaderAvatar = minioOperations.getObjectURL(uploaderAvatar);
        }
        return fileInfo;
    }

    private DeletedFileInfo journalToDeletedFileInfo(FileJournalRecord fileJournal, WorkspaceUserDTO workspaceUser) {
        var fileInfo = new DeletedFileInfo(extractPath(fileJournal.getPath()), 0, workspaceUser, fileJournal.getUpdatedAt());
        fileInfo.id = fileJournal.getFileId();
        fileInfo.workspaceID = fileJournal.getWsId();
        fileInfo.isDirectory = isFileDirectory(fileJournal.getPath());
        fileInfo.size = fileJournal.getSize();
        fileInfo.uploader = fileJournal.getUploaderId();
        if (workspaceUser.avatarURL != null) {
            workspaceUser.avatarURL = minioOperations.getObjectURL(workspaceUser.avatarURL);
        }
        return fileInfo;
    }

    private boolean isFileDirectory(String path) {
        return path.charAt(path.length()-1) == DIRECTORY_CHAR;
    }

    private String extractFilename(String path) {
        if (path.charAt(path.length()-1) == DIRECTORY_CHAR) {
            int lastDirChar = path.lastIndexOf('/');
            int secondLastDirChar = path.lastIndexOf('/', lastDirChar - 1);
            return path.substring(secondLastDirChar + 1, lastDirChar + 1);
        }
        return StringUtils.substringAfterLast(path, DIRECTORY_CHAR);
    }

    private String extractPath(String path) {
        Path filePath = Paths.get(path);
        Path parent = filePath.getParent();
        if (parent != null && parent.getNameCount() > 0) {
            return parent.getFileName().toString() + "/";
        }
        return "/";
    }
}
