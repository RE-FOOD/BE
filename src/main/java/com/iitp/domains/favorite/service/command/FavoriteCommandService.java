package com.iitp.domains.favorite.service.command;

import com.iitp.domains.favorite.domain.entity.Favorite;
import com.iitp.domains.favorite.dto.Response.FavoriteToggledResponse;
import com.iitp.domains.favorite.repository.FavoriteRepository;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.service.query.StoreQueryService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FavoriteCommandService {
    private final FavoriteRepository favoriteRepository;
    private final MemberQueryService memberQueryService;
    private final StoreQueryService storeQueryService;

    public FavoriteToggledResponse toggleFavorite(Long memberId, Long storeId) {
        Member member = memberQueryService.findMemberById(memberId);
        Store store = storeQueryService.findExistingStore(storeId);

        Optional<Favorite> optionalFavorite = favoriteRepository.findByMemberIdAndStoreId(member.getId(), storeId);
        boolean isFavored;
        if (optionalFavorite.isEmpty()) {
            member.addFavorite(Favorite.builder()
                    .member(member)
                    .store(store)
                    .build()
            );
            isFavored = true;
        } else {
            member.removeFavorite(optionalFavorite.get());
            isFavored = false;
        }

        return FavoriteToggledResponse.of(store.getId(), isFavored);
    }

}
