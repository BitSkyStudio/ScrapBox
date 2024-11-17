package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Joint;

public interface IGearJoinable {
    Joint getGearJoint();
    Body getGearJointBody();
}
