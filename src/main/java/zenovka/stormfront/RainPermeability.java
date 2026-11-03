package zenovka.stormfront;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;

/** User overrides for decorative blocks that should not shelter the ground. */
public final class RainPermeability {
    private static final Set<String> ALL_META = new HashSet<String>();
    private static final Map<String, Set<Integer>> META = new HashMap<String, Set<Integer>>();

    private RainPermeability() {}

    public static void configure(String[] entries) {
        ALL_META.clear();
        META.clear();
        if (entries == null) return;
        for (String entry : entries) {
            if (entry == null) continue;
            String value = entry.trim().toLowerCase(Locale.ROOT);
            if (value.length() == 0 || value.startsWith("#")) continue;
            int separator = value.lastIndexOf('@');
            String id = separator < 0 ? value : value.substring(0, separator).trim();
            if (id.indexOf(':') <= 0 || id.endsWith(":")) {
                System.err.println("[StormFront] Ignoring invalid rain-permeable block ID: " + entry);
                continue;
            }
            if (separator < 0 || value.substring(separator + 1).trim().equals("*")) {
                ALL_META.add(id);
                continue;
            }
            try {
                int meta = Integer.parseInt(value.substring(separator + 1).trim());
                if (meta < 0 || meta > 15) throw new NumberFormatException();
                Set<Integer> values = META.get(id);
                if (values == null) {
                    values = new HashSet<Integer>();
                    META.put(id, values);
                }
                values.add(meta);
            } catch (NumberFormatException ex) {
                System.err.println("[StormFront] Ignoring invalid rain-permeable metadata: " + entry);
            }
        }
    }

    public static boolean matches(Block block, int metadata) {
        Object key = Block.blockRegistry.getNameForObject(block);
        if (key == null) return false;
        String id = key.toString().toLowerCase(Locale.ROOT);
        if (ALL_META.contains(id)) return true;
        Set<Integer> values = META.get(id);
        return values != null && values.contains(metadata);
    }
}
