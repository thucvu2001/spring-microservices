package com.thucvu.inventoryservice.repository;

import com.thucvu.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findBySkuCodeIn(List<String> skuCode); // nếu 1 thành phần trong list không có trong db thì sẽ trả về list rỗng
}
