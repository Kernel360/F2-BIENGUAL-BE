package com.biengual.userapi.category.infrastructure;

import com.biengual.userapi.annotation.DataProvider;
import com.biengual.userapi.category.domain.CategoryInfo;
import com.biengual.userapi.category.domain.CategoryReader;
import com.biengual.userapi.category.repository.CategoryCustomRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@DataProvider
@RequiredArgsConstructor
public class CategoryReaderImpl implements CategoryReader {
    private final CategoryCustomRepository categoryCustomRepository;

    @Override
    public List<CategoryInfo.Category> findAllCategories() {
        return categoryCustomRepository.findAllCategories();
    }
}