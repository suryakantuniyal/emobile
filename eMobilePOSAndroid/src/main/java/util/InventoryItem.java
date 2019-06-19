package util;

public class InventoryItem {

    private String name;

    private String address;

    private String qty;

    public InventoryItem(String name, String address, String qty) {
        this.name = name;
        this.address = address;
        this.qty = qty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }




}
