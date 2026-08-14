package com.p1nero.cobblebashscreen.progress;

import com.p1nero.cobblebashscreen.CobbleBash;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class TrainingSimulatorData implements INBTSerializable<CompoundTag> {
    private static final String UNLOCK_MASK_KEY = "unlockMask";
    private static final String CLEAR_COUNTS_KEY = "clearCounts";
    private static final String CHALLENGES_STARTED_KEY = "challengesStarted";

    private int unlockMask;
    private CompoundTag clearCounts = new CompoundTag();
    private int challengesStarted;

    public static TrainingSimulatorData get(ServerPlayer player) {
        return player.getData(CobbleBash.TRAINING_SIMULATOR_DATA);
    }

    public int getUnlockMask() {
        return unlockMask;
    }

    public boolean isUnlocked(int challengeIndex) {
        return challengeIndex >= 0 && challengeIndex < Integer.SIZE
                && (unlockMask & (1 << challengeIndex)) != 0;
    }

    public boolean unlock(int challengeIndex) {
        if (challengeIndex < 0 || challengeIndex >= Integer.SIZE || isUnlocked(challengeIndex)) {
            return false;
        }
        unlockMask |= 1 << challengeIndex;
        return true;
    }

    public int getClearCount(String gymType) {
        return Math.max(0, clearCounts.getInt(gymType));
    }

    public int incrementClearCount(String gymType) {
        int updated = incrementSaturated(getClearCount(gymType));
        clearCounts.putInt(gymType, updated);
        return updated;
    }

    public int getChallengesStarted() {
        return Math.max(0, challengesStarted);
    }

    public int incrementChallengesStarted() {
        int updated = incrementSaturated(getChallengesStarted());
        challengesStarted = updated;
        return updated;
    }

    private static int incrementSaturated(int value) {
        return value == Integer.MAX_VALUE ? value : value + 1;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(UNLOCK_MASK_KEY, unlockMask);
        tag.put(CLEAR_COUNTS_KEY, clearCounts.copy());
        tag.putInt(CHALLENGES_STARTED_KEY, challengesStarted);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        unlockMask = tag.getInt(UNLOCK_MASK_KEY);
        clearCounts = tag.contains(CLEAR_COUNTS_KEY, Tag.TAG_COMPOUND)
                ? tag.getCompound(CLEAR_COUNTS_KEY).copy()
                : new CompoundTag();
        challengesStarted = Math.max(0, tag.getInt(CHALLENGES_STARTED_KEY));
    }
}
