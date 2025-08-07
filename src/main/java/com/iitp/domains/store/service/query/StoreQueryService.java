package com.iitp.domains.store.service.query;

import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.SortType;
import com.iitp.domains.store.dto.response.StoreListResponse;
import com.iitp.domains.store.repository.mapper.StoreListQueryResult;
import com.iitp.domains.store.repository.store.StoreImageRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.common.constants.Constants;
import com.iitp.global.exception.NotFoundException;
import com.iitp.imageUpload.service.query.ImageGetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

import static com.iitp.global.exception.ExceptionMessage.DATA_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StoreQueryService {
    private final StoreRepository storeRepository;
    private final ImageGetService imageGetService;

    public List<StoreListResponse> findStores(Category category, String keyword, SortType sort, Long cursorId){

        List<StoreListQueryResult> results = storeRepository.findStores(category,keyword,sort,cursorId,Constants.PAGING_LIMIT);

        List<StoreListResponse> stores = new  ArrayList<>();

        // TODO :: 리뷰 연동되면 수정
        int percent = 10;
        double rating = 3.5;
        int count = 100;
        double distance = 5.5;

        results.stream()
                        .forEach(result -> {
                            // S3 이미지 경로 호출
                            String imageUrl = imageGetService.getGetS3Url(result.imageKey()).preSignedUrl();
                            stores.add(StoreListResponse.fromQueryResult(result, imageUrl,percent,rating,count,distance));
                        });

        return stores;
    }
}
