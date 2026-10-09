package com.talentFlow.data.dto;

import com.talentFlow.data.Enums.MaterialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UploadMaterialRequest(
        @NotBlank @Size(max = 180) String title,
        @NotNull MaterialType materialType,
        @NotBlank @Size(max = 500) String contentUrl
) {
}
