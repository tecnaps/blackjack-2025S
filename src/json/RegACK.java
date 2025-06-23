package json;

import utilities.enums.Role;

public class RegACK {

    public String type = "ACK";
    public Role role;
    public int credit;
    public String name;

public RegACK (){}

public RegACK(Role role, int credit, String name){
    this.role = role;
    this.credit = credit;
    this.name = name;
}

}

