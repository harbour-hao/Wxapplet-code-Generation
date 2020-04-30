package com.example.springboot.Model;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    public Border val;
    public int parent;
    public List<TreeNode> children=new ArrayList<>();
    public boolean selected=false;
    public TreeNode(Border border){
        this.val=border;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public void addChildren(TreeNode node){
        this.children.add(node);
    }
}
