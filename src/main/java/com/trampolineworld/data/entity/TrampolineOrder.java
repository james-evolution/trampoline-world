package com.trampolineworld.data.entity;

import java.time.LocalDate;
import javax.persistence.Entity;

@Entity
public class TrampolineOrder extends AbstractEntity {

    private boolean status;
    private Integer orderId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String orderDescription;
    private String measurements;
    private Integer price;
    private Integer subtotal;
    private Integer total;
    private LocalDate date;

    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
    public Integer getOrderId() {
        return orderId;
    }
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getOrderDescription() {
        return orderDescription;
    }
    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
    }
    public String getMeasurements() {
        return measurements;
    }
    public void setMeasurements(String measurements) {
        this.measurements = measurements;
    }
    public Integer getPrice() {
        return price;
    }
    public void setPrice(Integer price) {
        this.price = price;
    }
    public Integer getSubtotal() {
        return subtotal;
    }
    public void setSubtotal(Integer subtotal) {
        this.subtotal = subtotal;
    }
    public Integer getTotal() {
        return total;
    }
    public void setTotal(Integer total) {
        this.total = total;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

}
