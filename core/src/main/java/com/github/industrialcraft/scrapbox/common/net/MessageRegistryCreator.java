package com.github.industrialcraft.scrapbox.common.net;

import com.github.industrialcraft.netx.MessageRegistry;
import com.github.industrialcraft.scrapbox.common.net.msg.*;

public class MessageRegistryCreator {
    public static MessageRegistry create(){
        MessageRegistry messageRegistry = new MessageRegistry();
        messageRegistry.register(1, AddGameObjectMessage.createDescriptor());
        messageRegistry.register(2, CommitWeld.createDescriptor());
        messageRegistry.register(3, DeleteGameObject.createDescriptor());
        messageRegistry.register(4, GameObjectPinch.createDescriptor());
        messageRegistry.register(5, GameObjectRelease.createDescriptor());
        messageRegistry.register(6, LockGameObject.createDescriptor());
        messageRegistry.register(7, MouseMoved.createDescriptor());
        messageRegistry.register(8, MoveGameObjectMessage.createDescriptor());
        messageRegistry.register(9, PinchingRotate.createDescriptor());
        messageRegistry.register(10, PinchingGhostToggle.createDescriptor());
        messageRegistry.register(11, PlaceTerrain.createDescriptor());
        messageRegistry.register(12, ShowActivePossibleWelds.createDescriptor());
        messageRegistry.register(13, TakeObject.createDescriptor());
        messageRegistry.register(14, TakeObjectResponse.createDescriptor());
        messageRegistry.register(15, TerrainShapeMessage.createDescriptor());
        messageRegistry.register(16, ToggleGamePaused.createDescriptor());
        messageRegistry.register(17, TrashObject.createDescriptor());
        messageRegistry.register(18, OpenGameObjectEditUI.createDescriptor());
        messageRegistry.register(19, SetGameObjectEditUIData.createDescriptor());
        messageRegistry.register(20, CreateValueConnection.createDescriptor());
        messageRegistry.register(21, ControllerInput.createDescriptor());
        messageRegistry.register(22, EditorUIInput.createDescriptor());
        return messageRegistry;
    }
}
