package che.statues20.client;

import che.statues20.Statues20;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Recreates the defining texture trick from the classic Statues mod: the player's skin supplies
 * light/dark detail, while the sculpted block texture supplies the material. Bedrock is the old
 * sentinel for a painted/full-colour statue, so no material blend is applied in that case.
 */
public final class StatueTextureManager {
    private StatueTextureManager() {}

    private record Key(String player, String state) {}
    private static final Map<Key, ResourceLocation> READY = new ConcurrentHashMap<>();
    private static final Map<Key, Boolean> PENDING = new ConcurrentHashMap<>();
    private static final ResourceLocation DEFAULT = DefaultPlayerSkin.getDefaultSkin(new UUID(0L, 0L));

    public static ResourceLocation texture(String playerName, BlockState sourceState) {
        String name = playerName == null ? "" : playerName.trim();
        Key key = new Key(name.toLowerCase(Locale.ROOT), sourceState.toString());
        ResourceLocation ready = READY.get(key);
        if (ready != null) return ready;

        if (PENDING.putIfAbsent(key, Boolean.TRUE) == null) {
            if (name.isBlank()) {
                Minecraft.getInstance().execute(() -> buildFromResource(key, DEFAULT, sourceState));
            } else {
                CompletableFuture.runAsync(() -> downloadAndBuild(key, name, sourceState));
            }
        }
        return DEFAULT;
    }

    private static void buildFromResource(Key key, ResourceLocation skin, BlockState sourceState) {
        try (InputStream in = Minecraft.getInstance().getResourceManager().open(skin);
             NativeImage image = NativeImage.read(in)) {
            NativeImage normalized = normalize(image);
            registerBuiltTexture(key, normalized, sourceState);
        } catch (Exception ex) {
            PENDING.remove(key);
        }
    }

    private static void downloadAndBuild(Key key, String name, BlockState sourceState) {
        try {
            String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8);
            JsonObject profile = readJson("https://api.mojang.com/users/profiles/minecraft/" + encoded);
            if (profile == null || !profile.has("id")) throw new IllegalStateException("No Mojang profile");
            String rawUuid = profile.get("id").getAsString();
            JsonObject session = readJson("https://sessionserver.mojang.com/session/minecraft/profile/" + rawUuid + "?unsigned=true");
            String value = null;
            if (session != null && session.has("properties")) {
                for (var el : session.getAsJsonArray("properties")) {
                    JsonObject p = el.getAsJsonObject();
                    if ("textures".equals(p.get("name").getAsString())) { value = p.get("value").getAsString(); break; }
                }
            }
            if (value == null) throw new IllegalStateException("No textures property");
            JsonObject payload = JsonParser.parseString(new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8)).getAsJsonObject();
            String skinUrl = payload.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();

            HttpURLConnection con = (HttpURLConnection) URI.create(skinUrl).toURL().openConnection();
            con.setConnectTimeout(5000); con.setReadTimeout(8000); con.setRequestProperty("User-Agent", "Statues20/0.4");
            try (InputStream in = con.getInputStream(); NativeImage image = NativeImage.read(in)) {
                NativeImage normalized = normalize(image);
                Minecraft.getInstance().execute(() -> registerBuiltTexture(key, normalized, sourceState));
            }
        } catch (Exception ex) {
            Minecraft.getInstance().execute(() -> buildFromResource(key, DEFAULT, sourceState));
        }
    }

    private static JsonObject readJson(String url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) URI.create(url).toURL().openConnection();
        con.setConnectTimeout(5000); con.setReadTimeout(8000); con.setRequestProperty("User-Agent", "Statues20/0.4");
        try (InputStream in = con.getInputStream(); java.io.InputStreamReader reader = new java.io.InputStreamReader(in, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static NativeImage normalize(NativeImage source) {
        if (source.getWidth() == 64 && source.getHeight() == 64) {
            NativeImage copy = new NativeImage(64, 64, true); copy.copyFrom(source); return copy;
        }
        NativeImage out = new NativeImage(64, 64, true);
        source.resizeSubRectTo(0, 0, source.getWidth(), source.getHeight(), out);
        return out;
    }

    private static void registerBuiltTexture(Key key, NativeImage skin, BlockState sourceState) {
        try {
            NativeImage result;
            if (sourceState.is(Blocks.BEDROCK)) {
                result = skin; // Palette / painted statue in the original.
            } else {
                TextureAtlasSprite sprite = findSprite(sourceState);
                if (sprite == null) { result = skin; }
                else {
                    result = new NativeImage(64, 64, true);
                    int sw = Math.max(1, sprite.contents().width());
                    int sh = Math.max(1, sprite.contents().height());
                    for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
                        int skinPixel = skin.getPixelRGBA(x, y);
                        int blockPixel = sprite.getPixelRGBA(0, x % sw, y % sh);
                        result.setPixelRGBA(x, y, overlay(skinPixel, blockPixel));
                    }
                    skin.close();
                }
            }
            DynamicTexture dynamic = new DynamicTexture(result);
            ResourceLocation loc = Minecraft.getInstance().getTextureManager().register("statues20_statue", dynamic);
            READY.put(key, loc);
        } catch (Throwable t) {
            try { skin.close(); } catch (Throwable ignored) {}
        } finally {
            PENDING.remove(key);
        }
    }

    private static TextureAtlasSprite findSprite(BlockState state) {
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        RandomSource random = RandomSource.create(0L);
        Direction[] preferred = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP, Direction.DOWN};
        for (Direction direction : preferred) {
            List<BakedQuad> quads = model.getQuads(state, direction, random);
            if (!quads.isEmpty()) return quads.get(0).getSprite();
        }
        List<BakedQuad> unculled = model.getQuads(state, null, random);
        return unculled.isEmpty() ? null : unculled.get(0).getSprite();
    }

    /** Exact overlay-style blend used by ImageStatueBufferDownload in the old mod. */
    private static int overlay(int skin, int material) {
        int sr = skin & 255, sg = (skin >>> 8) & 255, sb = (skin >>> 16) & 255, sa = (skin >>> 24) & 255;
        int mr = material & 255, mg = (material >>> 8) & 255, mb = (material >>> 16) & 255, ma = (material >>> 24) & 255;
        double luminance = (0.2125 * sr + 0.7154 * sg + 0.0721 * sb) / 255.0;
        luminance = Math.min(1.0, luminance * 4.0 / 3.0);
        int r = blendChannel(mr, luminance), g = blendChannel(mg, luminance), b = blendChannel(mb, luminance);
        int a = sa * ma / 255;
        return r | (g << 8) | (b << 16) | (a << 24);
    }

    private static int blendChannel(int value, double luminance) {
        double c = value / 255.0;
        double out = c < 0.5 ? 2.0 * c * luminance : 1.0 - 2.0 * (1.0 - c) * (1.0 - luminance);
        return Math.max(0, Math.min(255, (int)Math.round(out * 255.0)));
    }
}
