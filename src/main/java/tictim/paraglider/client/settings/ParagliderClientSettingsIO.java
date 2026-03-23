package tictim.paraglider.client.settings;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Util;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderClientMod;
import tictim.paraglider.ParagliderMod;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@NullMarked
public class ParagliderClientSettingsIO {
	public static final Path FILEPATH = FMLPaths.GAMEDIR.get().resolve("paragliderSettings.nbt");

	public static void save(ParagliderClientSettings settings, @Nullable BooleanConsumer callback) {
		Util.ioPool().execute(() -> {
			boolean result = saveInternal(settings);
			if (callback != null) callback.accept(result);
		});
	}

	private static boolean saveInternal(ParagliderClientSettings settings) {
		DataResult<Tag> result = ParagliderClientSettings.CODEC.encodeStart(NbtOps.INSTANCE, settings);

		return result.mapOrElse(t -> {
			if (!(t instanceof CompoundTag compoundTag)) {
				ParagliderMod.LOGGER.error("Error occurred while saving paraglider settings: Expected compound tag");
				return false;
			}

			try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(FILEPATH, StandardOpenOption.CREATE))) {
				NbtIo.write(compoundTag, dos);
				return true;
			} catch (RuntimeException | IOException ex) {
				ParagliderMod.LOGGER.error("Error occurred while saving paraglider settings: ", ex);
				return false;
			}
		}, e -> {
			ParagliderMod.LOGGER.error("Error occurred while saving paraglider settings: {}", e.message());
			return false;
		});
	}

	public static void load(@Nullable BooleanConsumer callback) {
		Util.ioPool().execute(() -> {
			Pair<Boolean, @Nullable ParagliderClientSettings> pair = loadInternal();
			if (callback != null) callback.accept(pair.getFirst().booleanValue());
			ParagliderClientMod.instance().setSettings(pair.getSecond());
		});
	}

	private static Pair<Boolean, @Nullable ParagliderClientSettings> loadInternal() {
		try (DataInputStream dis = new DataInputStream(Files.newInputStream(FILEPATH))) {
			DataResult<ParagliderClientSettings> result = ParagliderClientSettings.CODEC.parse(NbtOps.INSTANCE, NbtIo.read(dis));
			return result.mapOrElse(s -> Pair.of(true, s), e -> {
				ParagliderMod.LOGGER.error("Error occurred while loading paraglider settings: {}", e.message());
				return Pair.of(false, e.partialValue().orElse(null));
			});
		} catch (NoSuchFileException ignored) {
			// no-op
		} catch (RuntimeException | IOException ex) {
			ParagliderMod.LOGGER.error("Error occurred while loading paraglider settings: ", ex);
		}
		return Pair.of(false, null);
	}
}
