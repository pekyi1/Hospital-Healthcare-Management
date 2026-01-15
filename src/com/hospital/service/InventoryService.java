package com.hospital.service;

import com.hospital.dao.InventoryDAO;
import com.hospital.model.MedicalInventory;
import com.hospital.util.PerformanceLogger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryService {

    private final InventoryDAO inventoryDAO;
    private Map<Integer, MedicalInventory> inventoryCache = new HashMap<>();

    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
    }

    public void addInventoryItem(MedicalInventory item) throws SQLException {
        long start = System.currentTimeMillis();
        inventoryDAO.addItem(item);
        inventoryCache.put(item.getId(), item);
        PerformanceLogger.log("addInventoryItem", start);
    }

    public List<MedicalInventory> getAllInventoryItems() throws SQLException {
        long start = System.currentTimeMillis();
        List<MedicalInventory> items = inventoryDAO.getAllItems();
        inventoryCache.clear();
        for (MedicalInventory item : items) {
            inventoryCache.put(item.getId(), item);
        }
        PerformanceLogger.log("getAllInventoryItems", start);
        return items;
    }

    public void updateInventoryItem(MedicalInventory item) throws SQLException {
        long start = System.currentTimeMillis();
        inventoryDAO.updateItem(item);
        inventoryCache.put(item.getId(), item);
        PerformanceLogger.log("updateInventoryItem", start);
    }

    public void deleteInventoryItem(int id) throws SQLException {
        long start = System.currentTimeMillis();
        inventoryDAO.deleteItem(id);
        inventoryCache.remove(id);
        PerformanceLogger.log("deleteInventoryItem", start);
    }
}
