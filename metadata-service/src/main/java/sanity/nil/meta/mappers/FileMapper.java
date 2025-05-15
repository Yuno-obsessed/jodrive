package sanity.nil.meta.mappers;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sanity.nil.meta.cache.model.FileMetadata;
import sanity.nil.meta.dto.file.FileInfo;
import sanity.nil.meta.model.FileJournalModel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static sanity.nil.meta.consts.Constants.DIRECTORY_CHAR;

@Mapper(componentModel = "cdi")
public interface FileMapper {

    @Mapping(target = "path", source = "path")
    @Mapping(target = "blocks", expression = "java(getBlocksFromBlockList(journal.getBlocklist()))")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "state", source = "state")
    FileMetadata journalToMetadata(FileJournalModel journal);

    @Mapping(target = "id", source = "id.fileID")
    @Mapping(target = "workspaceID", source = "id.workspaceID")
    @Mapping(target = "name", qualifiedByName = "extractFilename", source = "path")
    @Mapping(target = "isDirectory", expression = "java(isFileDirectory(journal.getPath()))")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "uploader", source = "uploader.id")
    @Mapping(target = "uploadedAt", source = "createdAt")
    FileInfo journalToInfo(FileJournalModel journal);

    default boolean isFileDirectory(String path) {
        return path.charAt(path.length()-1) == DIRECTORY_CHAR;
    }

    @Named("extractFilename")
    default String extractFilename(String path) {
        return StringUtils.substringAfterLast(path, DIRECTORY_CHAR);
    }

    default List<String> getBlocksFromBlockList(String blocklist) {
        return Arrays.stream(blocklist.split(",")).collect(Collectors.toList());
    }
}
