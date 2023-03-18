package com.vanoma.api.order.businesshours;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessHourRepository extends JpaRepository<BusinessHour, Integer> {
    List<BusinessHour> findAllByOrderByWeekDayAsc();
}
