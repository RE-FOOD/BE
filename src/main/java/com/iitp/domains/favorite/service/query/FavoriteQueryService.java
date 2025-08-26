package com.iitp.domains.favorite.service.query;

import com.iitp.domains.favorite.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FavoriteQueryService {
    private final FavoriteRepository favoriteRepository;

    public boolean isFavoriteExists(Long memberId, Long storeId) {
        return favoriteRepository.existsByMemberIdAndStoreId(memberId, storeId);
    }
}
