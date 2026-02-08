package com.ankinbt.nbt;

import net.minecraft.nbt.*;

import com.ankinbt.compat.VersionCompat;
import java.util.ArrayList;
import java.util.List;

/**
 * Tree node for NBT editing. Follows NBTEdit's NbtTree.Node pattern:
 * each node holds a key, a tag, and children. The tree can be converted
 * back to a CompoundTag via toCompoundTag() for saving.
 */
public class NbtTreeNode {

    private String key;
    private Tag tag;
    private final NbtTreeNode parent;
    private final List<NbtTreeNode> children = new ArrayList<>();
    private boolean expanded;
    private final int depth;

    public NbtTreeNode(String key, Tag tag, NbtTreeNode parent, boolean expandByDefault) {
        this.key = key;
        this.tag = tag;
        this.parent = parent;
        this.depth = parent == null ? 0 : parent.depth + 1;
        this.expanded = expandByDefault;
        buildChildren(expandByDefault);
    }

    private void buildChildren(boolean expandByDefault) {
        children.clear();
        if (tag instanceof CompoundTag compound) {
            for (String childKey : VersionCompat.get().getCompoundKeys(compound)) {
                children.add(new NbtTreeNode(childKey, compound.get(childKey), this, expandByDefault));
            }
        } else if (tag instanceof ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                children.add(new NbtTreeNode("[" + i + "]", list.get(i), this, expandByDefault));
            }
        }
    }

    public void rebuild(boolean expandByDefault) {
        buildChildren(expandByDefault);
    }

    // ==================== Getters/Setters ====================

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public Tag getTag() { return tag; }
    public void setTag(Tag tag) { this.tag = tag; }
    public NbtTreeNode getParent() { return parent; }
    public List<NbtTreeNode> getChildren() { return children; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
    public void toggleExpanded() { this.expanded = !this.expanded; }
    public int getDepth() { return depth; }
    public boolean isLeaf() { return children.isEmpty(); }
    public boolean isCompound() { return tag instanceof CompoundTag; }
    public boolean isList() { return tag instanceof ListTag; }

    public String getTypeName() { return NbtHelper.getTagTypeName(tag); }

    public String getDisplayValue() { return NbtHelper.getValueAsString(tag); }

    // ==================== Tree Operations ====================

    public void collectVisible(List<NbtTreeNode> out) {
        out.add(this);
        if (expanded) {
            for (NbtTreeNode child : children) {
                child.collectVisible(out);
            }
        }
    }

    /**
     * Writes this node's value back into the parent's tag.
     */
    public void applyToParent() {
        if (parent == null) return;
        Tag parentTag = parent.getTag();
        if (parentTag instanceof CompoundTag compound) {
            compound.put(key, tag);
        } else if (parentTag instanceof ListTag list) {
            int idx = parseListIndex(key);
            if (idx >= 0 && idx < list.size()) {
                list.set(idx, tag);
            }
        }
    }

    /**
     * Adds a new child tag. For CompoundTag parents, uses the given key.
     * For ListTag parents, appends to the end.
     */
    public NbtTreeNode addChild(String childKey, Tag childTag, boolean expandByDefault) {
        if (tag instanceof CompoundTag compound) {
            compound.put(childKey, childTag);
        } else if (tag instanceof ListTag list) {
            list.add(childTag);
            childKey = "[" + (list.size() - 1) + "]";
        }
        NbtTreeNode child = new NbtTreeNode(childKey, childTag, this, expandByDefault);
        children.add(child);
        return child;
    }

    /**
     * Removes a child node.
     */
    public void removeChild(NbtTreeNode child) {
        Tag pt = this.tag;
        if (pt instanceof CompoundTag compound) {
            compound.remove(child.getKey());
        } else if (pt instanceof ListTag list) {
            int idx = parseListIndex(child.getKey());
            if (idx >= 0 && idx < list.size()) {
                list.remove(idx);
            }
        }
        children.remove(child);
        // Re-index list children
        if (pt instanceof ListTag) {
            for (int i = 0; i < children.size(); i++) {
                children.get(i).key = "[" + i + "]";
            }
        }
    }

    // ==================== Conversion ====================

    /**
     * Recursively converts this node tree back to a CompoundTag.
     * Following NBTEdit's NbtTree.toCompound() pattern.
     */
    public CompoundTag toCompoundTag() {
        return buildCompound(this);
    }

    private static CompoundTag buildCompound(NbtTreeNode node) {
        CompoundTag result = new CompoundTag();
        for (NbtTreeNode child : node.getChildren()) {
            String name = child.getKey();
            Tag childTag = child.getTag();
            if (childTag instanceof CompoundTag) {
                result.put(name, buildCompound(child));
            } else if (childTag instanceof ListTag) {
                result.put(name, buildList(child));
            } else {
                result.put(name, childTag);
            }
        }
        return result;
    }

    private static ListTag buildList(NbtTreeNode node) {
        ListTag result = new ListTag();
        for (NbtTreeNode child : node.getChildren()) {
            Tag childTag = child.getTag();
            if (childTag instanceof CompoundTag) {
                result.add(buildCompound(child));
            } else if (childTag instanceof ListTag) {
                result.add(buildList(child));
            } else {
                result.add(childTag);
            }
        }
        return result;
    }

    private static int parseListIndex(String key) {
        try {
            if (key.startsWith("[") && key.endsWith("]")) {
                return Integer.parseInt(key.substring(1, key.length() - 1));
            }
        } catch (NumberFormatException ignored) {}
        return -1;
    }
}
