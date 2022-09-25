package io.th0rgal.oraxen.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.TextArgument;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.font.FontManager;
import io.th0rgal.oraxen.font.Glyph;
import io.th0rgal.oraxen.utils.Utils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class PrintGlyphCommand {
    private final FontManager fontManager = OraxenPlugin.get().getFontManager();
    public CommandAPICommand getPrintGlyphCommand() {
        List<String> glyphnames = new ArrayList<>();
        glyphnames.add("all");
        OraxenPlugin.get().getFontManager().getGlyphs().forEach(glyph -> glyphnames.add(glyph.getName()));
        return new CommandAPICommand("printglyph")
                .withPermission("oraxen.command.printglyph")
                .withArguments(new TextArgument("glyphname").replaceSuggestions(ArgumentSuggestions.strings(glyphnames.toArray(new String[0]))))
                .executes(((commandSender, args) -> {
                    Audience audience = OraxenPlugin.get().getAudience().sender(commandSender);
                    audience.sendMessage(Utils.MINI_MESSAGE.deserialize("<red><b>Click one of the glyph-ids below to copy the unicode!"));
                    if (fontManager.getGlyphFromName(String.valueOf(args[0])) != null
                            ||
                            String.valueOf(args[0]).equals("all")) {

                        printGlyph(audience, (String) args[0]);

                    } else printUnicode(audience, (String) args[0]);
                }));
    }

    private void printGlyph(Audience audience, String glyphName) {
        Component component = Component.text("");
        if (glyphName.equals("all")) {
            int i = 0;
            for (Glyph glyph : fontManager.getGlyphs()) {
                component = component.append(printClickableMsg("<reset>[<green>" + glyph.getName() + "<reset>] ", glyph.getCharacter(), String.valueOf(glyph.getCharacter())));
                if (i % 3 == 0) {
                    audience.sendMessage(component);
                    component = Component.empty();
                }
                i++;
            }
        } else {
            Glyph g = fontManager.getGlyphs().stream().filter(glyph -> glyph.getName().equals(glyphName)).findFirst().orElse(null);
            if (g == null) return;
            component = printClickableMsg("<white>" + g.getName(), g.getCharacter(), "<reset>" + g.getCharacter());
        }
        audience.sendMessage(component);
    }

    /**
     * Parses code input to print a unicode list with decimal version of UTF-16
     * the format is {hex unicode id}+{range to display} like this: "E000+10"
     * @param audience audience to send the message to
     * @param code unicode symbol with formatted range
     */
    private void printUnicode(Audience audience, String code) {
        try {
            char utf;
            int range = 1;
            if (code.matches("[A-Za-z0-9]{4}\\+[0-9]+")) {
                String[] splitted = code.split("\\+");
                utf = new String(Hex.decodeHex(splitted[0].toCharArray()), StandardCharsets.UTF_16BE).toCharArray()[0];
                range = Integer.parseInt(splitted[1]);
            } else {
                utf = new String(Hex.decodeHex(code.toCharArray()), StandardCharsets.UTF_16BE).toCharArray()[0];
            }
            Component component = Component.text("");
            for (int i = 0; i < range; i++) {
                component = component.append(printClickableMsg("<white>[<aqua>U+" + Integer.toHexString(utf).toUpperCase() + "," + ((int) utf) + "(dec)<white>] ",
                        utf, "<white>" + utf));
                if (i == 2) {
                    audience.sendMessage(component);
                    component = Component.empty();
                }
                utf++;
            }
            audience.sendMessage(component);

        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }

    private Component printClickableMsg(String text, char unicode, String hoverText) {
        return Utils.MINI_MESSAGE.deserialize(text)
                .hoverEvent(HoverEvent.showText(Utils.MINI_MESSAGE.deserialize(hoverText)))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(unicode)));

    }
}