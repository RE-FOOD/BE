package com.iitp.domains.favorite.service.command;

import com.iitp.domains.favorite.domain.entity.Favorite;
import com.iitp.domains.favorite.dto.Response.FavoriteToggledResponse;
import com.iitp.domains.favorite.repository.FavoriteRepository;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.service.query.StoreQueryService;
import com.iitp.domains.store.validator.StoreValidator;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


public interface FavoriteCommandService {
    FavoriteToggledResponse toggleFavorite(Long memberId, Long storeId);

}
