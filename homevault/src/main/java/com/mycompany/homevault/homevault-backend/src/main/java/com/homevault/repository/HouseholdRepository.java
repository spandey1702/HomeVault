package com.homevault.repository;

import com.homevault.entity.Household;
import com.homevault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {
    List<Household> findByOwner(User owner);
    
    @Query("SELECT h FROM Household h JOIN h.members m WHERE m.id = :userId")
    List<Household> findByMemberId(@Param("userId") Long userId);
}