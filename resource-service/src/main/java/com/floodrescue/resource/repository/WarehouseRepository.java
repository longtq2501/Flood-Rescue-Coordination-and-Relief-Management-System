package com.floodrescue.resource.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.floodrescue.resource.domain.entity.Warehouse;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findByManagerId(Long managerId);

    @Query("SELECT w FROM Warehouse w LEFT JOIN FETCH w.items")
    List<Warehouse> findAllWithItems();
}
