package sanity.nil.block.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

public record BlockUpload(
        UUID correlationID,
        List<UploadResult> results
) {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadResult {
        public String hash;
        public boolean success;
    }
}