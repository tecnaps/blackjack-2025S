package json;

import utilities.enums.Role;

public class RegREQ {

    public String type = "register";
    public Role role;
    public int credit;
    public String name;

    public RegREQ(Role role, int credit, String name){
        this.role = role;
        this.credit = credit;
        this.name = name;
    }

}
