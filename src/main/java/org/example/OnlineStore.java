package org.example;

public class OnlineStore {
    private int customerId;
    private String customerName;
    private int goodId;
    private String goodName;
    private int price;

    public OnlineStore(int customerId, String customerName, int goodId, String goodName, int price) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.goodId = goodId;
        this.goodName = goodName;
        this.price = price;
    }

    public int getCustomerId() { return customerId; }
    public String getСustomerName() { return customerName; }
    public int getGoodId() { return goodId; }
    public String getGoodName() { return goodName; }
    public int getPrice() { return price; }

    @Override
    public String toString() {
        return "Id покупателя: " + customerId + ", Имя покупателя: "  + customerName + ", Id товара: " + goodId + ", Нзвание товара: " + goodName + ", Цена: " + price ;
    }
}
