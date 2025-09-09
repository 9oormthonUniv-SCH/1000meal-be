package com._1000meal.store.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class SetStoreImageUrlRequest {
    @NotNull
    private Long storeId;

    @NotNull @Size(max = 1024)
    private String imageUrl;
}