package com.example.burnchuck.domain.category.repository;

import static com.example.burnchuck.common.enums.ErrorCode.CATEGORY_NOT_FOUND;

import com.example.burnchuck.common.entity.Category;
import com.example.burnchuck.common.exception.CustomException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByIdAsc();

    Optional<Category> findByCode(String code);

    default Category findCategoryByCode(String code) {
        return findByCode(code)
            .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));
    }
}
