package brokcu.ca.bf22wk.a2;

import java.util.Objects;

public class Contact {
    String id;
    String name;
    String phone;
    String email;

    public Contact(String id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String formatContactString(){
        String s = "";

        if (!Objects.equals(name, "")){
            s = s + name;
        }
        if (!Objects.equals(phone, "")){
            s = s + "," + phone;
        }
        if (!Objects.equals(email, "")){
            s = s + "," + email;
        }
        return s;
    }
}
