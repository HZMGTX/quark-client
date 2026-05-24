package com.ghostclient.module.modules.render;

import com.ghostclient.module.Category;
import com.ghostclient.module.Module;

/**
 * NoHurtCam - cancels the camera shake (hurt animation) when the player takes damage.
 *
 * The actual cancellation is applied via MixinGameRenderer which checks
 * NoHurtCam.INSTANCE.isEnabled() before computing the hurt camera tilt.
 *
 * This module itself has no event handlers; it is purely a flag for the mixin.
 */
public class NoHurtCam extends Module {

    public static NoHurtCam INSTANCE;

    public NoHurtCam() {
        super("NoHurtCam", "Removes screen shake when taking damage", Category.RENDER);
        INSTANCE = this;
    }
}
