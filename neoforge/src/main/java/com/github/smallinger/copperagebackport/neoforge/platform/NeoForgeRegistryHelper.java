package com.github.smallinger.copperagebackport.neoforge.platform;

import com.github.smallinger.copperagebackport.Constants;
import com.github.smallinger.copperagebackport.registry.RegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * NeoForge implementation of the registry helper using DeferredRegister.
 * Supports registering under both minecraft: and copperagebackport: namespaces.
 */
public class NeoForgeRegistryHelper extends RegistryHelper {

    private final Map<String, Map<ResourceKey<?>, DeferredRegister<?>>> namespaceRegisters = new HashMap<>();
    private final IEventBus modEventBus;

    public NeoForgeRegistryHelper(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> register(ResourceKey<? extends Registry<? super T>> registryKey,
                                    String name,
                                    Supplier<T> supplier) {
        return registerWithNamespace(registryKey, Constants.MOD_ID, name, supplier);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> registerWithNamespace(ResourceKey<? extends Registry<? super T>> registryKey,
                                                  String namespace,
                                                  String name,
                                                  Supplier<T> supplier) {
        Map<ResourceKey<?>, DeferredRegister<?>> registers = namespaceRegisters.computeIfAbsent(
            namespace, k -> new HashMap<>()
        );
        
        DeferredRegister<T> register = (DeferredRegister<T>) registers.computeIfAbsent(registryKey, key -> {
            ResourceKey<? extends Registry<T>> typedKey = (ResourceKey<? extends Registry<T>>) key;
            DeferredRegister<T> newRegister = DeferredRegister.create(typedKey, namespace);
            newRegister.register(modEventBus);
            Constants.LOG.debug("Created DeferredRegister for namespace {} and registry {}", namespace, registryKey);
            return newRegister;
        });

        return register.register(name, supplier);
    }

    @Override
    public void fireRegistrationCallbacks() {
        super.fireRegistrationCallbacks();
    }

    @Override
    public Holder<ArmorMaterial> registerArmorMaterial(String name, Supplier<ArmorMaterial> materialSupplier) {
        DeferredRegister<ArmorMaterial> deferredRegister = DeferredRegister.create(Registries.ARMOR_MATERIAL, MINECRAFT_NAMESPACE);
        deferredRegister.register(modEventBus);

        return deferredRegister.register(name, materialSupplier);
    }

    @Override
    public Holder<SoundEvent> registerSoundEvent(String name, Supplier<SoundEvent> soundEventSupplier) {
        DeferredRegister<SoundEvent> deferredRegister = DeferredRegister.create(Registries.SOUND_EVENT, MINECRAFT_NAMESPACE);
        deferredRegister.register(modEventBus);

        return deferredRegister.register(name, soundEventSupplier);
    }
}
