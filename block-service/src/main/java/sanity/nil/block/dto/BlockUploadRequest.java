package sanity.nil.block.dto;

import java.util.UUID;

public record BlockUploadRequest(
        UUID correlationID
) { }