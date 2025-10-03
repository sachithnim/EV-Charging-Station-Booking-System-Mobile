package com.example.ev_mobile.Models;

public class EVOwner {
    private String nic;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String password;
    private boolean isActive;

    public EVOwner() {}

    public EVOwner(String nic, String name, String email, String phone,String address, String password, boolean isActive) {
        this.nic = nic;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
        this.isActive = isActive;
    }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
