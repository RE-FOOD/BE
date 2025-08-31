package com.iitp.domains.store.dto.response;

public record MonthlyRevenueDto(
        String yearMonth,
        Integer totalRevenue
){
}
