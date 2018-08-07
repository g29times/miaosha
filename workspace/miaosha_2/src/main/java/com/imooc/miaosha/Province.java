package com.imooc.miaosha;

import java.util.List;

public class Province {

    private String id;
    private String name;
    private String cname;
    private List<City> citys;

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ", \"name\":" + name +
                ", \"cname\":" + (cname.equals("") ? "\"\"" : cname) +
                ", \"citys\":" + citys +
                '}';
    }

    public Province() {
    }

    public Province(String id, String name, String cname) {
        this.id = id;
        this.name = name;
        this.cname = cname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public List<City> getCitys() {
        return citys;
    }

    public void setCitys(List<City> citys) {
        this.citys = citys;
    }
}
