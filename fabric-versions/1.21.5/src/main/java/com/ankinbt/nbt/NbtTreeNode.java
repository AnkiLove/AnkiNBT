/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.class_2487
 *  net.minecraft.class_2499
 *  net.minecraft.class_2520
 */
package com.ankinbt.nbt;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.nbt.NbtHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2520;

public class NbtTreeNode {
    private String key;
    private class_2520 tag;
    private final NbtTreeNode parent;
    private final List<NbtTreeNode> children = new ArrayList<NbtTreeNode>();
    private boolean expanded;
    private final int depth;

    public NbtTreeNode(String key, class_2520 tag, NbtTreeNode parent, boolean expandByDefault) {
        this.key = key;
        this.tag = tag;
        this.parent = parent;
        this.depth = parent == null ? 0 : parent.depth + 1;
        this.expanded = expandByDefault;
        this.buildChildren(expandByDefault);
    }

    private void buildChildren(boolean expandByDefault) {
        block3: {
            Object object;
            block2: {
                this.children.clear();
                object = this.tag;
                if (!(object instanceof class_2487)) break block2;
                class_2487 compound = (class_2487)object;
                for (String childKey : VersionCompat.get().getCompoundKeys(compound)) {
                    this.children.add(new NbtTreeNode(childKey, compound.method_10580(childKey), this, expandByDefault));
                }
                break block3;
            }
            object = this.tag;
            if (!(object instanceof class_2499)) break block3;
            class_2499 list = (class_2499)object;
            for (int i = 0; i < list.size(); ++i) {
                this.children.add(new NbtTreeNode("[" + i + "]", list.method_10534(i), this, expandByDefault));
            }
        }
    }

    public void rebuild(boolean expandByDefault) {
        this.buildChildren(expandByDefault);
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public class_2520 getTag() {
        return this.tag;
    }

    public void setTag(class_2520 tag) {
        this.tag = tag;
    }

    public NbtTreeNode getParent() {
        return this.parent;
    }

    public List<NbtTreeNode> getChildren() {
        return this.children;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }

    public int getDepth() {
        return this.depth;
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public boolean isCompound() {
        return this.tag instanceof class_2487;
    }

    public boolean isList() {
        return this.tag instanceof class_2499;
    }

    public String getTypeName() {
        return NbtHelper.getTagTypeName(this.tag);
    }

    public String getDisplayValue() {
        return NbtHelper.getValueAsString(this.tag);
    }

    public void collectVisible(List<NbtTreeNode> out) {
        out.add(this);
        if (this.expanded) {
            for (NbtTreeNode child : this.children) {
                child.collectVisible(out);
            }
        }
    }

    public void applyToParent() {
        if (this.parent == null) {
            return;
        }
        class_2520 parentTag = this.parent.getTag();
        if (parentTag instanceof class_2487) {
            class_2487 compound = (class_2487)parentTag;
            compound.method_10566(this.key, this.tag);
        } else if (parentTag instanceof class_2499) {
            class_2499 list = (class_2499)parentTag;
            int idx = NbtTreeNode.parseListIndex(this.key);
            if (idx >= 0 && idx < list.size()) {
                list.method_10606(idx, this.tag);
            }
        }
    }

    public NbtTreeNode addChild(String childKey, class_2520 childTag, boolean expandByDefault) {
        class_2520 class_25202 = this.tag;
        if (class_25202 instanceof class_2487) {
            class_2487 compound = (class_2487)class_25202;
            compound.method_10566((String)childKey, childTag);
        } else {
            class_25202 = this.tag;
            if (class_25202 instanceof class_2499) {
                class_2499 list = (class_2499)class_25202;
                list.add((Object)childTag);
                childKey = "[" + (list.size() - 1) + "]";
            }
        }
        NbtTreeNode child = new NbtTreeNode((String)childKey, childTag, this, expandByDefault);
        this.children.add(child);
        return child;
    }

    public void removeChild(NbtTreeNode child) {
        class_2520 pt = this.tag;
        if (pt instanceof class_2487) {
            class_2487 compound = (class_2487)pt;
            compound.method_10551(child.getKey());
        } else if (pt instanceof class_2499) {
            class_2499 list = (class_2499)pt;
            int idx = NbtTreeNode.parseListIndex(child.getKey());
            if (idx >= 0 && idx < list.size()) {
                list.method_10536(idx);
            }
        }
        this.children.remove(child);
        if (pt instanceof class_2499) {
            for (int i = 0; i < this.children.size(); ++i) {
                this.children.get((int)i).key = "[" + i + "]";
            }
        }
    }

    public class_2487 toCompoundTag() {
        return NbtTreeNode.buildCompound(this);
    }

    private static class_2487 buildCompound(NbtTreeNode node) {
        class_2487 result = new class_2487();
        for (NbtTreeNode child : node.getChildren()) {
            String name = child.getKey();
            class_2520 childTag = child.getTag();
            if (childTag instanceof class_2487) {
                result.method_10566(name, (class_2520)NbtTreeNode.buildCompound(child));
                continue;
            }
            if (childTag instanceof class_2499) {
                result.method_10566(name, (class_2520)NbtTreeNode.buildList(child));
                continue;
            }
            result.method_10566(name, childTag);
        }
        return result;
    }

    private static class_2499 buildList(NbtTreeNode node) {
        class_2499 result = new class_2499();
        for (NbtTreeNode child : node.getChildren()) {
            class_2520 childTag = child.getTag();
            if (childTag instanceof class_2487) {
                result.add((Object)NbtTreeNode.buildCompound(child));
                continue;
            }
            if (childTag instanceof class_2499) {
                result.add((Object)NbtTreeNode.buildList(child));
                continue;
            }
            result.add((Object)childTag);
        }
        return result;
    }

    private static int parseListIndex(String key) {
        try {
            if (key.startsWith("[") && key.endsWith("]")) {
                return Integer.parseInt(key.substring(1, key.length() - 1));
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        return -1;
    }
}

