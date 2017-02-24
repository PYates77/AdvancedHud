package com.projecttango.examples.java.AdvancedHUD;

import com.google.atap.tangoservice.TangoPoseData;

public class HUD_User {
    private float[] position;
    private float[] orientation;

    public void update_pose(TangoPoseData pose) {
        float[] translation = pose.getTranslationAsFloats();
        float[] orientation = pose.getRotationAsFloats();

        for (int i = 0; i < 3; i++) {
            position[i] = translation[i];
            this.orientation[i] = orientation[i];
        }

        this.orientation[3] = orientation[3];
    }
}