package com.p1nero.cobblebashscreen.mixin;

import com.cobblemon.mod.common.api.dialogue.DialogueText;
import com.cobblemon.mod.common.api.dialogue.WrappedDialogueText;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.nore.cobblebash.dialogue.GymTrainerDialogue", remap = false)
public abstract class GymTrainerDialogueMixin {
    @Inject(
            method = "text(Ljava/lang/String;)Lcom/cobblemon/mod/common/api/dialogue/DialogueText;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void cobblebashScreen$translateKey(
            String text,
            CallbackInfoReturnable<DialogueText> callback
    ) {
        if (text.startsWith("cobblebash.dialogue.")) {
            callback.setReturnValue(new WrappedDialogueText(Component.translatable(text)));
        }
    }
}
