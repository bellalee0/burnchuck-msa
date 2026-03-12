package com.example.burnchuck.domain.category.service;

import com.example.burnchuck.common.entity.Category;
import com.example.burnchuck.domain.category.dto.response.CategoryListResponse;
import com.example.burnchuck.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 목록 조회
     */
    public CategoryListResponse getCategoryList() {

        List<Category> categoryList = categoryRepository.findAllByOrderByIdAsc();

        return CategoryListResponse.from(categoryList);
    }
}
