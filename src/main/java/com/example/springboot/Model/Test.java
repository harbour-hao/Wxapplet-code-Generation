package com.example.springboot.Model;

import java.util.ArrayList;
import java.util.List;

public class Test {
    private String val;
    private int parent;
    private List<String> children=new ArrayList<>();
    private boolean selected=false;

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
