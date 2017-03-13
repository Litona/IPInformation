package de.litona.ipInformation;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public final class Main extends JavaPlugin {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	private static String microsoftAPIKey;
	private static String weatherAPIKey;

	@Override
	public void onEnable() {
		if(!getDataFolder().exists())
			getDataFolder().mkdirs();
		if(!new File(getDataFolder() + File.pathSeparator + "config.yml").exists())
			saveDefaultConfig();
		microsoftAPIKey = getConfig().getString("microsoft-API-key");
		weatherAPIKey = getConfig().getString("weather-API-key");
		getCommand("ipinfo").setExecutor(((sender, command, label, args) -> {
			if(args.length == 1) {
				Player found = Bukkit.getPlayer(args[0]);
				if(found != null)
					Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
						IPInformation info = IPInformation.get(found);
						TimeZone timeZone = info.getTimeZone();
						try {
							sender.sendMessage(formatMessage(found,
								new String[] {"Ping", "Timezone", "Time", "Internet Provider", "Country", "Region", "City", "Weather", "Forecast", "News"},
								new String[] {"" + getHandleField(found, "ping"), timeZone.getID(),
									dateFormat.format(Calendar.getInstance(timeZone).getTime()), info.getInetProvider(), info.getCountry(),
									info.getRegion(), info.getCity(), info.getWeather(), info.getWeatherForecast(), info.getNews()}));
						} catch(NoSuchFieldException | NoSuchMethodException e) {
							e.printStackTrace();
						}
					});
				else
					sender.sendMessage("§bIPInformation §7>> §cOh.. §l" + args[0] + "§r§c couldn't be found :(");
			} else
				sender.sendMessage("§bIPInformation §7>> §cSyntax: /ipinfo (Player)");
			return true;
		}));
		getCommand("ipinfokey").setExecutor(((sender, command, label, args) -> {
			if(args.length == 2) {
				if(args[0].equalsIgnoreCase("m"))
					microsoftAPIKey = args[1];
				else if(args[0].equalsIgnoreCase("w"))
					weatherAPIKey = args[1];
				else {
					sender.sendMessage("§bIPInformation §7>> §cSyntax: /ipinfokey <m/w> (Key)");
					return true;
				}
				getConfig().set("microsoft-API-key", microsoftAPIKey);
				getConfig().set("weather-API-key", weatherAPIKey);
				saveConfig();
				sender.sendMessage("§bIPInformation §7>> §cKey set. Reload the server after setting all keys!");
			} else
				sender.sendMessage("§bIPInformation §7>> §cSyntax: /ipinfokey <m/w> (Key)");
			return true;
		}));
	}

	static String getMicrosoftAPIKey() {
		return microsoftAPIKey;
	}

	static String getWeatherAPIKey() {
		return weatherAPIKey;
	}

	private static Object getHandleField(Object p, String field) throws NoSuchFieldException, NoSuchMethodException {
		try {
			Object handle = p.getClass().getMethod("getHandle").invoke(p);
			return handle.getClass().getField(field).get(handle);
		} catch(IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String formatMessage(Player player, String[] headers, String[] values) {
		StringBuilder stringBuilder = new StringBuilder("§bIPInformation §7>> §efor §l");
		stringBuilder.append(player.getName());
		stringBuilder.append("\n");
		for(byte i = 0; i < headers.length; i++) {
			stringBuilder.append("\n§b§l");
			stringBuilder.append(headers[i]);
			stringBuilder.append(":§r§e ");
			stringBuilder.append(values[i]);
		}
		return stringBuilder.toString();
	}
}