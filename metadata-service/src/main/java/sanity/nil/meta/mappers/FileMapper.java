package sanity.nil.meta.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sanity.nil.meta.cache.model.FileMetadata;
import sanity.nil.meta.db.tables.records.FileJournalRecord;
import sanity.nil.meta.dto.file.FileInfo;
import sanity.nil.meta.dto.file.VersionedFileInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi")
public interface FileMapper {

    @Mapping(target = "path", source = "path")
    @Mapping(target = "blocks", expression = "java(getBlocksFromBlockList(journal.getBlocklist()))")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "state", source = "state")
    FileMetadata journalToMetadata(FileJournalRecord journal);

    VersionedFileInfo fileInfoToVersionedFile(FileInfo fileInfo);

    default List<String> getBlocksFromBlockList(String blocklist) {
        return Arrays.stream(blocklist.split(",")).collect(Collectors.toCollection(ArrayList::new));
    }
}
