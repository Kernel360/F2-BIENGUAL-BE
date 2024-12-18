package com.biengual.userapi.image.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biengual.userapi.image.domain.ImageReader;
import com.biengual.userapi.image.domain.ImageService;
import com.biengual.userapi.image.domain.ImageStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final ImageStore imageStore;
    private final ImageReader imageReader;

    @Override
    @Transactional
    public void save(Long contentId) {
        imageStore.saveImage(contentId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getImage(Long contentId) {
        return imageReader.getImage(contentId);
    }

    // TODO: PROD 적용 후 삭제 예정
    @Override
    @Transactional
    public void saveAllToS3() {
        imageStore.saveAllImagesToS3();
    }
}
