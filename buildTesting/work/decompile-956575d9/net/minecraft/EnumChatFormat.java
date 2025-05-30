package net.minecraft;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.INamable;
import org.jetbrains.annotations.Contract;

public enum EnumChatFormat implements INamable {

    BLACK("BLACK", '0', 0, 0), DARK_BLUE("DARK_BLUE", '1', 1, 170), DARK_GREEN("DARK_GREEN", '2', 2, 43520), DARK_AQUA("DARK_AQUA", '3', 3, 43690), DARK_RED("DARK_RED", '4', 4, 11141120), DARK_PURPLE("DARK_PURPLE", '5', 5, 11141290), GOLD("GOLD", '6', 6, 16755200), GRAY("GRAY", '7', 7, 11184810), DARK_GRAY("DARK_GRAY", '8', 8, 5592405), BLUE("BLUE", '9', 9, 5592575), GREEN("GREEN", 'a', 10, 5635925), AQUA("AQUA", 'b', 11, 5636095), RED("RED", 'c', 12, 16733525), LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13, 16733695), YELLOW("YELLOW", 'e', 14, 16777045), WHITE("WHITE", 'f', 15, 16777215), OBFUSCATED("OBFUSCATED", 'k', true), BOLD("BOLD", 'l', true), STRIKETHROUGH("STRIKETHROUGH", 'm', true), UNDERLINE("UNDERLINE", 'n', true), ITALIC("ITALIC", 'o', true), RESET("RESET", 'r', -1, (Integer) null);

    public static final Codec<EnumChatFormat> CODEC = INamable.<EnumChatFormat>fromEnum(EnumChatFormat::values);
    public static final Codec<EnumChatFormat> COLOR_CODEC = EnumChatFormat.CODEC.validate((enumchatformat) -> {
        return enumchatformat.isFormat() ? DataResult.error(() -> {
            return "Formatting was not a valid color: " + String.valueOf(enumchatformat);
        }) : DataResult.success(enumchatformat);
    });
    public static final char PREFIX_CODE = '\u00a7';
    private static final Map<String, EnumChatFormat> FORMATTING_BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((enumchatformat) -> {
        return cleanName(enumchatformat.name);
    }, (enumchatformat) -> {
        return enumchatformat;
    }));
    private static final Pattern STRIP_FORMATTING_PATTERN = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");
    private final String name;
    public final char code;
    private final boolean isFormat;
    private final String toString;
    private final int id;
    @Nullable
    private final Integer color;

    private static String cleanName(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    private EnumChatFormat(final String s, final char c0, final int i, final Integer integer) {
        this(s, c0, false, i, integer);
    }

    private EnumChatFormat(final String s, final char c0, final boolean flag) {
        this(s, c0, flag, -1, (Integer) null);
    }

    private EnumChatFormat(final String s, final char c0, final boolean flag, final int i, final Integer integer) {
        this.name = s;
        this.code = c0;
        this.isFormat = flag;
        this.id = i;
        this.color = integer;
        this.toString = "\u00a7" + String.valueOf(c0);
    }

    public char getChar() {
        return this.code;
    }

    public int getId() {
        return this.id;
    }

    public boolean isFormat() {
        return this.isFormat;
    }

    public boolean isColor() {
        return !this.isFormat && this != EnumChatFormat.RESET;
    }

    @Nullable
    public Integer getColor() {
        return this.color;
    }

    public String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public String toString() {
        return this.toString;
    }

    @Nullable
    @Contract("!null->!null;_->_")
    public static String stripFormatting(@Nullable String s) {
        return s == null ? null : EnumChatFormat.STRIP_FORMATTING_PATTERN.matcher(s).replaceAll("");
    }

    @Nullable
    public static EnumChatFormat getByName(@Nullable String s) {
        return s == null ? null : (EnumChatFormat) EnumChatFormat.FORMATTING_BY_NAME.get(cleanName(s));
    }

    @Nullable
    public static EnumChatFormat getById(int i) {
        if (i < 0) {
            return EnumChatFormat.RESET;
        } else {
            for (EnumChatFormat enumchatformat : values()) {
                if (enumchatformat.getId() == i) {
                    return enumchatformat;
                }
            }

            return null;
        }
    }

    @Nullable
    public static EnumChatFormat getByCode(char c0) {
        char c1 = Character.toLowerCase(c0);

        for (EnumChatFormat enumchatformat : values()) {
            if (enumchatformat.code == c1) {
                return enumchatformat;
            }
        }

        return null;
    }

    public static Collection<String> getNames(boolean flag, boolean flag1) {
        List<String> list = Lists.newArrayList();

        for (EnumChatFormat enumchatformat : values()) {
            if ((!enumchatformat.isColor() || flag) && (!enumchatformat.isFormat() || flag1)) {
                list.add(enumchatformat.getName());
            }
        }

        return list;
    }

    @Override
    public String getSerializedName() {
        return this.getName();
    }
}
