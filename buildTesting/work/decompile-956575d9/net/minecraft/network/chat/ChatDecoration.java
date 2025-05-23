package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;

public record ChatDecoration(String translationKey, List<ChatDecoration.a> parameters, ChatModifier style) {

    public static final Codec<ChatDecoration> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.STRING.fieldOf("translation_key").forGetter(ChatDecoration::translationKey), ChatDecoration.a.CODEC.listOf().fieldOf("parameters").forGetter(ChatDecoration::parameters), ChatModifier.ChatModifierSerializer.CODEC.optionalFieldOf("style", ChatModifier.EMPTY).forGetter(ChatDecoration::style)).apply(instance, ChatDecoration::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ChatDecoration> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ChatDecoration::translationKey, ChatDecoration.a.STREAM_CODEC.apply(ByteBufCodecs.list()), ChatDecoration::parameters, ChatModifier.ChatModifierSerializer.TRUSTED_STREAM_CODEC, ChatDecoration::style, ChatDecoration::new);

    public static ChatDecoration withSender(String s) {
        return new ChatDecoration(s, List.of(ChatDecoration.a.SENDER, ChatDecoration.a.CONTENT), ChatModifier.EMPTY);
    }

    public static ChatDecoration incomingDirectMessage(String s) {
        ChatModifier chatmodifier = ChatModifier.EMPTY.withColor(EnumChatFormat.GRAY).withItalic(true);

        return new ChatDecoration(s, List.of(ChatDecoration.a.SENDER, ChatDecoration.a.CONTENT), chatmodifier);
    }

    public static ChatDecoration outgoingDirectMessage(String s) {
        ChatModifier chatmodifier = ChatModifier.EMPTY.withColor(EnumChatFormat.GRAY).withItalic(true);

        return new ChatDecoration(s, List.of(ChatDecoration.a.TARGET, ChatDecoration.a.CONTENT), chatmodifier);
    }

    public static ChatDecoration teamMessage(String s) {
        return new ChatDecoration(s, List.of(ChatDecoration.a.TARGET, ChatDecoration.a.SENDER, ChatDecoration.a.CONTENT), ChatModifier.EMPTY);
    }

    public IChatBaseComponent decorate(IChatBaseComponent ichatbasecomponent, ChatMessageType.a chatmessagetype_a) {
        Object[] aobject = this.resolveParameters(ichatbasecomponent, chatmessagetype_a);

        return IChatBaseComponent.translatable(this.translationKey, aobject).withStyle(this.style);
    }

    private IChatBaseComponent[] resolveParameters(IChatBaseComponent ichatbasecomponent, ChatMessageType.a chatmessagetype_a) {
        IChatBaseComponent[] aichatbasecomponent = new IChatBaseComponent[this.parameters.size()];

        for (int i = 0; i < aichatbasecomponent.length; ++i) {
            ChatDecoration.a chatdecoration_a = (ChatDecoration.a) this.parameters.get(i);

            aichatbasecomponent[i] = chatdecoration_a.select(ichatbasecomponent, chatmessagetype_a);
        }

        return aichatbasecomponent;
    }

    public static enum a implements INamable {

        SENDER(0, "sender", (ichatbasecomponent, chatmessagetype_a) -> {
            return chatmessagetype_a.name();
        }), TARGET(1, "target", (ichatbasecomponent, chatmessagetype_a) -> {
            return (IChatBaseComponent) chatmessagetype_a.targetName().orElse(CommonComponents.EMPTY);
        }), CONTENT(2, "content", (ichatbasecomponent, chatmessagetype_a) -> {
            return ichatbasecomponent;
        });

        private static final IntFunction<ChatDecoration.a> BY_ID = ByIdMap.<ChatDecoration.a>continuous((chatdecoration_a) -> {
            return chatdecoration_a.id;
        }, values(), ByIdMap.a.ZERO);
        public static final Codec<ChatDecoration.a> CODEC = INamable.<ChatDecoration.a>fromEnum(ChatDecoration.a::values);
        public static final StreamCodec<ByteBuf, ChatDecoration.a> STREAM_CODEC = ByteBufCodecs.idMapper(ChatDecoration.a.BY_ID, (chatdecoration_a) -> {
            return chatdecoration_a.id;
        });
        private final int id;
        private final String name;
        private final ChatDecoration.a.a selector;

        private a(final int i, final String s, final ChatDecoration.a.a chatdecoration_a_a) {
            this.id = i;
            this.name = s;
            this.selector = chatdecoration_a_a;
        }

        public IChatBaseComponent select(IChatBaseComponent ichatbasecomponent, ChatMessageType.a chatmessagetype_a) {
            return this.selector.select(ichatbasecomponent, chatmessagetype_a);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public interface a {

            IChatBaseComponent select(IChatBaseComponent ichatbasecomponent, ChatMessageType.a chatmessagetype_a);
        }
    }
}
