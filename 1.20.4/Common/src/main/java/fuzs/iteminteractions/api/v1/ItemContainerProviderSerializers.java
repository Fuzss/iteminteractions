package fuzs.iteminteractions.api.v1;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fuzs.iteminteractions.api.v1.provider.ItemContainerProvider;
import fuzs.iteminteractions.impl.ItemInteractions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * main class for managing item provider serializers
 */
public class ItemContainerProviderSerializers {
    private static final Map<ResourceLocation, Serializer> SERIALIZERS_BY_ID = Collections.synchronizedMap(Maps.newHashMap());
    private static final Map<Class<? extends ItemContainerProvider>, Serializer> SERIALIZERS_BY_TYPE = Collections.synchronizedMap(Maps.newHashMap());

    /**
     * register a custom item provider serializer for a custom serializer
     *
     * @param clazz        the provider class, is checked via identity
     * @param id           provider id as resource location, used to add the provider type to the json file when serializing
     * @param deserializer the deserializer function
     */
    public static void register(Class<? extends ItemContainerProvider> clazz, ResourceLocation id, Function<JsonElement, ItemContainerProvider> deserializer) {
        Serializer serializer = new Serializer(clazz, id, deserializer);
        SERIALIZERS_BY_ID.put(id, serializer);
        SERIALIZERS_BY_TYPE.put(clazz, serializer);
    }

    /**
     * serialize an {@link ItemContainerProvider} to json
     *
     * @param provider the provider to serialize
     * @return json element containing all provider data
     */
    public static JsonElement serialize(ItemContainerProvider provider) {
        Serializer serializer = SERIALIZERS_BY_TYPE.get(provider.getClass());
        Objects.requireNonNull(serializer, "no serializer registered for class %s".formatted(provider.getClass()));
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", serializer.id().toString());
        provider.toJson(jsonObject);
        return jsonObject;
    }

    /**
     * deserialize an {@link ItemContainerProvider} from json
     *
     * @param jsonObject json object containing provider data
     * @return the provider
     */
    @Nullable
    public static ItemContainerProvider deserialize(JsonObject jsonObject) {
        ResourceLocation identifier = new ResourceLocation(GsonHelper.getAsString(jsonObject, "type"));
        Serializer serializer = SERIALIZERS_BY_ID.get(identifier);
        Objects.requireNonNull(serializer, "no serializer registered for identifier %s".formatted(identifier));
        return serializer.deserializer().apply(jsonObject);
    }

    static {
        Serializer serializer = new Serializer(null, ItemInteractions.id("none"), $ -> null);
        SERIALIZERS_BY_ID.put(serializer.id(), serializer);
    }

    private record Serializer(Class<? extends ItemContainerProvider> clazz, ResourceLocation id,
                              Function<JsonElement, @Nullable ItemContainerProvider> deserializer) {

    }
}
