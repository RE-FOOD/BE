package com.iitp.domains.favorite.service.command;

import com.iitp.domains.favorite.domain.entity.Favorite;
import com.iitp.domains.favorite.dto.Response.FavoriteToggledResponse;
import com.iitp.domains.favorite.repository.FavoriteRepository;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.validator.StoreValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FavoriteCommandServiceImpl implements FavoriteCommandService {
    private final FavoriteRepository favoriteRepository;
    private final MemberQueryService memberQueryService;
    private final StoreValidator storeValidator;

    public FavoriteToggledResponse toggleFavorite(Long memberId, Long storeId) {
        Member member = memberQueryService.findMemberById(memberId);
        Store store = storeValidator.validateStoreExists(storeId);

        Optional<Favorite> optionalFavorite = favoriteRepository.findByMemberIdAndStoreId(member.getId(), storeId);
        boolean isFavored;
        if (optionalFavorite.isEmpty()) {
            Favorite favorite = Favorite.builder()
                    .member(member)
                    .store(store)
                    .build();
            member.addFavorite(favorite);
            store.addFavorite(favorite);
            isFavored = true;
        } else {
            member.removeFavorite(optionalFavorite.get());
            store.removeFavorite(optionalFavorite.get());
            isFavored = false;
        }

        return FavoriteToggledResponse.of(store.getId(), isFavored);
    }

}
