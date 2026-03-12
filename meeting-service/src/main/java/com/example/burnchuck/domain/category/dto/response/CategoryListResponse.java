package com.example.burnchuck.domain.category.dto.response;

import com.example.burnchuck.common.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CategoryListResponse {

    private List<CategoryResponse> categoryResponseList;

    public static CategoryListResponse from(List<Category> categoryList) {
        return new CategoryListResponse(
                categoryList.stream()
                        .map(CategoryResponse::from)
                        .toList()
        );
    }
}
