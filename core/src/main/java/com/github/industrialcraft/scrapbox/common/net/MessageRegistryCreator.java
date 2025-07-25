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
        messageRegistry.register(23, SendConnectionListData.createDescriptor());
        messageRegistry.register(24, DestroyJoint.createDescriptor());
        messageRegistry.register(25, RequestControllerState.createDescriptor());
        messageRegistry.register(26, ResponseControllerState.createDescriptor());
        messageRegistry.register(27, DestroyValueConnection.createDescriptor());
        messageRegistry.register(28, CloseGameObjectEditUI.createDescriptor());
        messageRegistry.register(29, GamePausedState.createDescriptor());
        messageRegistry.register(30, ChangeObjectHealth.createDescriptor());
        messageRegistry.register(31, UpdateBuildableAreas.createDescriptor());
        messageRegistry.register(32, SetGameState.createDescriptor());
        messageRegistry.register(33, SubmitPassword.createDescriptor());
        messageRegistry.register(34, DisconnectMessage.createDescriptor());
        messageRegistry.register(35, CreateGearConnection.createDescriptor());
        messageRegistry.register(36, DestroyGearConnection.createDescriptor());
        messageRegistry.register(37, UpdateInventory.createDescriptor());
        messageRegistry.register(38, PlaySoundMessage.createDescriptor());
        messageRegistry.register(39, StopSoundMessage.createDescriptor());
        messageRegistry.register(40, PlayerTeamUpdate.createDescriptor());
        messageRegistry.register(41, PlayerTeamList.createDescriptor());
        messageRegistry.register(42, ToggleSaveState.createDescriptor());
        messageRegistry.register(43, GameSaveStateActive.createDescriptor());
        return messageRegistry;
    }
}
