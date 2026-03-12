package com.example.burnchuck.domain.category.controller;

import static com.example.burnchuck.common.enums.SuccessMessage.CATEGORY_GET_SUCCESS;

import com.example.burnchuck.common.dto.CommonResponse;
import com.example.burnchuck.domain.category.dto.response.CategoryListResponse;
import com.example.burnchuck.domain.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@Tag(name = "Category")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리 목록 조회
     */
    @Operation(
            summary = "카테고리 목록 조회",
            description = """
                    모든 카테고리의 목록을 조회합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<CommonResponse<CategoryListResponse>> getCategories() {

        CategoryListResponse response = categoryService.getCategoryList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(CATEGORY_GET_SUCCESS, response));
    }
}
