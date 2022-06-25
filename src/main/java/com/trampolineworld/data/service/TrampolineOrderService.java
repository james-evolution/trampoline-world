package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.TrampolineOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TrampolineOrderService {

  private final TrampolineOrderRepository repository;

  @Autowired
  public TrampolineOrderService(TrampolineOrderRepository repository) {
    this.repository = repository;
  }

  public Optional<TrampolineOrder> get(Long id) {
    return repository.findById(id);
  }

  public TrampolineOrder update(TrampolineOrder entity) {
    return repository.save(entity);
  }

  /*
   * This is no longer a true deletion operation. Instead, we're marking orders as
   * deleted via a boolean property. Orders marked as deleted will be omitted from
   * findAll() results and will not show up in the grid. This way, they remain
   * archived for record-keeping purposes but appear to be deleted on the
   * front-end.
   */
  public void delete(Long id) {
    TrampolineOrder orderToDelete = this.get(id).get();
    orderToDelete.setDeleted(true);
    repository.save(orderToDelete);
//        repository.deleteById(id);
  }

  public Page<TrampolineOrder> list(Pageable pageable) {
    return repository.findAll(pageable);
  }

  public int count() {
    return (int) repository.count();
  }
  
  private class SortByDate implements Comparator<TrampolineOrder> {
    // Sort by the most recent date.
    public int compare(TrampolineOrder a, TrampolineOrder b) {
      return (int) (b.getDate().compareTo(a.getDate()));
    }
  }
  
  public List<TrampolineOrder> findAllArchived(String filterText) {
    if (filterText == null || filterText.isEmpty()) {
      List<TrampolineOrder> allOrders = repository.findAll();
      List<TrampolineOrder> deletedOrders = new ArrayList<TrampolineOrder>();
      
      // Loop through existing orders, copy deleted orders to a separate list.
      for (TrampolineOrder order : allOrders) {
        if (order.isDeleted()) {
          deletedOrders.add(order);
        }
      }
      Collections.sort(deletedOrders, new SortByDate());
      return deletedOrders;
    } else {
      List<TrampolineOrder> allFilteredOrders = repository.search(filterText);
      List<TrampolineOrder> allFilteredDeletedOrders = new ArrayList<TrampolineOrder>();
      // Loop through existing orders, copy deleted orders to a separate list.
      for (TrampolineOrder order : allFilteredOrders) {
        if (order.isDeleted()) {
          allFilteredDeletedOrders.add(order);
        }
      }
      Collections.sort(allFilteredDeletedOrders, new SortByDate());
      return allFilteredDeletedOrders;
    }
  }
  
  public List<TrampolineOrder> findAllArchivedNoFilter() {
    List<TrampolineOrder> allOrders = repository.findAll();
    List<TrampolineOrder> allDeletedOrders = new ArrayList<TrampolineOrder>();

    // Loop through existing orders, add ones marked as deleted to a separate list
    // of ones to delete.
    for (TrampolineOrder order : allOrders) {
      if (order.isDeleted()) {
        allDeletedOrders.add(order);
      }
    }
    Collections.sort(allDeletedOrders, new SortByDate());
    return allDeletedOrders;
  }

  public List<TrampolineOrder> findAll(String filterText) {
    if (filterText == null || filterText.isEmpty()) {
      List<TrampolineOrder> allOrders = repository.findAll();
      List<TrampolineOrder> allOrdersToDelete = new ArrayList<TrampolineOrder>();
      // Loop through existing orders, add ones marked as deleted to a separate list
      // of ones to delete.
      for (TrampolineOrder order : allOrders) {
        if (order.isDeleted()) {
          allOrdersToDelete.add(order);
        }
      }
      // Loop through "allOrdersToDelete" list, remove all these orders from the
      // "allOrders" list.
      for (TrampolineOrder orderToDelete : allOrdersToDelete) {
        allOrders.remove(orderToDelete);
      }
      Collections.sort(allOrders, new SortByDate());
      return allOrders;
    } else {
      List<TrampolineOrder> allFilteredOrders = repository.search(filterText);
      List<TrampolineOrder> allFilteredOrdersToDelete = new ArrayList<TrampolineOrder>();
      // Loop through existing orders, add ones marked as deleted to a separate list
      // of ones to delete.
      for (TrampolineOrder order : allFilteredOrders) {
        if (order.isDeleted()) {
          allFilteredOrdersToDelete.add(order);
        }
      }
      // Loop through "allOrdersToDelete" list, remove all these orders from the
      // "allOrders" list.
      for (TrampolineOrder orderToDelete : allFilteredOrdersToDelete) {
        allFilteredOrders.remove(orderToDelete);
      }

      Collections.sort(allFilteredOrders, new SortByDate());
      return allFilteredOrders;
    }
  }

  public List<TrampolineOrder> findAllNoFilter() {
    List<TrampolineOrder> allOrders = repository.findAll();
    List<TrampolineOrder> allOrdersToDelete = new ArrayList<TrampolineOrder>();

    // Loop through existing orders, add ones marked as deleted to a separate list
    // of ones to delete.
    for (TrampolineOrder order : allOrders) {
      if (order.isDeleted()) {
        allOrdersToDelete.add(order);
      }
    }
    // Loop through "allOrdersToDelete" list, remove all these orders from the
    // "allOrders" list.
    for (TrampolineOrder orderToDelete : allOrdersToDelete) {
      allOrders.remove(orderToDelete);
    }
    Collections.sort(allOrders, new SortByDate());
    return allOrders;
  }
}