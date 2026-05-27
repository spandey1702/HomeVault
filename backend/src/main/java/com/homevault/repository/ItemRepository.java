package com.homevault.repository;

import com.homevault.entity.Household;
import com.homevault.entity.Item;
import com.homevault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwner(User owner);
    List<Item> findByHousehold(Household household);
    List<Item> findByCategory(String category);
    List<Item> findByLocation(String location);

    /** Items expiring in a window for a specific user (personal view). */
    @Query("SELECT i FROM Item i WHERE i.owner = :owner " +
           "AND i.expiryDate BETWEEN :startDate AND :endDate")
    List<Item> findExpiringItems(@Param("owner") User owner,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    /** Items expiring in a window for an entire household (shared view). */
    @Query("SELECT i FROM Item i WHERE i.household = :household " +
           "AND i.expiryDate BETWEEN :startDate AND :endDate")
    List<Item> findExpiringItemsInHousehold(@Param("household") Household household,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /** All items expiring in a window across the whole app — used by the notification scheduler. */
    @Query("SELECT i FROM Item i WHERE i.expiryDate IS NOT NULL " +
           "AND i.expiryDate BETWEEN :startDate AND :endDate")
    List<Item> findAllExpiringItems(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    List<Item> findByHouseholdId(Long householdId);

    long countByOwner(User owner);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.household = :household")
    long countByHousehold(@Param("household") Household household);
}
