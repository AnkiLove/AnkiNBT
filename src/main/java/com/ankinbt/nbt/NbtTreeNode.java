/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 */
package com.ankinbt.nbt;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.nbt.NbtHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class NbtTreeNode {
    private String key;
    private Tag tag;
    private final NbtTreeNode parent;
    private final List<NbtTreeNode> children = new ArrayList<NbtTreeNode>();
    private boolean expanded;
    private final int depth;

    public NbtTreeNode(String key, Tag tag, NbtTreeNode parent, boolean expandByDefault) {
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
                if (!(object instanceof CompoundTag)) break block2;
                CompoundTag compound = (CompoundTag)object;
                for (String childKey : VersionCompat.get().getCompoundKeys(compound)) {
                    this.children.add(new NbtTreeNode(childKey, compound.get(childKey), this, expandByDefault));
                }
                break block3;
            }
            object = this.tag;
            if (!(object instanceof ListTag)) break block3;
            ListTag list = (ListTag)object;
            for (int i = 0; i < list.size(); ++i) {
                this.children.add(new NbtTreeNode("[" + i + "]", list.get(i), this, expandByDefault));
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

    public Tag getTag() {
        return this.tag;
    }

    public void setTag(Tag tag) {
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
        return this.tag instanceof CompoundTag;
    }

    public boolean isList() {
        return this.tag instanceof ListTag;
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
        Tag parentTag = this.parent.getTag();
        if (parentTag instanceof CompoundTag) {
            CompoundTag compound = (CompoundTag)parentTag;
            compound.put(this.key, this.tag);
        } else if (parentTag instanceof ListTag) {
            ListTag list = (ListTag)parentTag;
            int idx = NbtTreeNode.parseListIndex(this.key);
            if (idx >= 0 && idx < list.size()) {
                list.set(idx, this.tag);
            }
        }
    }

    public NbtTreeNode addChild(String childKey, Tag childTag, boolean expandByDefault) {
        Tag tag = this.tag;
        if (tag instanceof CompoundTag) {
            CompoundTag compound = (CompoundTag)tag;
            compound.put((String)childKey, childTag);
        } else {
            tag = this.tag;
            if (tag instanceof ListTag) {
                ListTag list = (ListTag)tag;
                list.add(childTag);
                childKey = "[" + (list.size() - 1) + "]";
            }
        }
        NbtTreeNode child = new NbtTreeNode((String)childKey, childTag, this, expandByDefault);
        this.children.add(child);
        return child;
    }

    public void removeChild(NbtTreeNode child) {
        Tag pt = this.tag;
        if (pt instanceof CompoundTag) {
            CompoundTag compound = (CompoundTag)pt;
            compound.remove(child.getKey());
        } else if (pt instanceof ListTag) {
            ListTag list = (ListTag)pt;
            int idx = NbtTreeNode.parseListIndex(child.getKey());
            if (idx >= 0 && idx < list.size()) {
                list.remove(idx);
            }
        }
        this.children.remove(child);
        if (pt instanceof ListTag) {
            for (int i = 0; i < this.children.size(); ++i) {
                this.children.get((int)i).key = "[" + i + "]";
            }
        }
    }

    public CompoundTag toCompoundTag() {
        return NbtTreeNode.buildCompound(this);
    }

    private static CompoundTag buildCompound(NbtTreeNode node) {
        CompoundTag result = new CompoundTag();
        for (NbtTreeNode child : node.getChildren()) {
            String name = child.getKey();
            Tag childTag = child.getTag();
            if (childTag instanceof CompoundTag) {
                result.put(name, (Tag)NbtTreeNode.buildCompound(child));
                continue;
            }
            if (childTag instanceof ListTag) {
                result.put(name, (Tag)NbtTreeNode.buildList(child));
                continue;
            }
            result.put(name, childTag);
        }
        return result;
    }

    private static ListTag buildList(NbtTreeNode node) {
        ListTag result = new ListTag();
        for (NbtTreeNode child : node.getChildren()) {
            Tag childTag = child.getTag();
            if (childTag instanceof CompoundTag) {
                result.add(NbtTreeNode.buildCompound(child));
                continue;
            }
            if (childTag instanceof ListTag) {
                result.add(NbtTreeNode.buildList(child));
                continue;
            }
            result.add(childTag);
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
