package de.litona.ipInformation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.TimeZone;

public class IPInformation {

	protected static final CacheMap<String, IPInformation> cache = new CacheMap<>(72000000);

	private String country = "Unknown";
	private String countryCode = "Unknown";
	private String region = "Unknown";
	private String city = "Unknown";
	private String inetProvider = "Unknown";
	private String weather = "Unknown";
	private String forecast = "Unknown";
	private String news = "Unknown";
	private TimeZone timezone = TimeZone.getTimeZone("GMT");

	public static IPInformation get(Player player) {
		return get(player.getAddress().getHostString());
	}

	public static IPInformation get(String ip) {
		IPInformation ci = cache.get(ip);
		if(ci != null)
			return ci;
		return new IPInformation(ip);
	}

	protected IPInformation(String ip) {
		JsonParser jsonParser = new JsonParser();
		JsonObject json;
		JsonObject temp;
		try {
			if((json = jsonParser.parse(getStringFromURL(new URL("http://ip-api.com/json/" + ip))).getAsJsonObject()).get("status").getAsString()
				.equals("success")) {
				country = json.get("country").getAsString();
				countryCode = json.get("countryCode").getAsString();
				region = json.get("region").getAsString();
				city = json.get("city").getAsString();
				inetProvider = json.get("org").getAsString();
				timezone = TimeZone.getTimeZone(json.get("timezone").getAsString());
			}
		} catch(IOException e) {
			Bukkit.getLogger().warning("IPInformation: Error doing the web stuff.");
		} finally {
			try {
				news =
					jsonParser.parse(getStringFromURL(new URL("https://api.cognitive.microsoft.com/bing/v5.0/news/search?q=" + country + "&mkt=en-US"),
						new String[] {"Ocp-Apim-Subscription-Key", Main.getMicrosoftAPIKey()})).getAsJsonObject().get("value").getAsJsonArray().get(1)
						.getAsJsonObject().get("name").getAsString();
			} catch(IOException e) {
				Bukkit.getLogger().warning("IPInformation: Error doing the web stuff. Have you set the keys?");
			} finally {
				try {
					json =
						jsonParser.parse(getStringFromURL(new URL(
							"http://api.openweathermap.org/data/2.5/weather?q=" + city.replace(" ", "&nbsp;") + "," + region + "&appid=" + Main
								.getWeatherAPIKey()))).getAsJsonObject();
					temp = json.get("main").getAsJsonObject();
					weather =
						Math.round((temp.get("temp_min").getAsFloat() - 273.15F) * 10) / 10 + "°C-"
							+ Math.round((temp.get("temp_max").getAsFloat() - 273.15F) * 10) / 10 + "°C, " + json.get("weather").getAsJsonArray().get(0)
							.getAsJsonObject().get("description").getAsString() + " - " + json.get("clouds").getAsJsonObject().get("all").getAsInt()
							+ "% clouds";
					json =
						jsonParser.parse(getStringFromURL(new URL(
							"http://api.openweathermap.org/data/2.5/forecast/daily?q=" + city.replace(" ", "&nbsp;") + "," + region + "&appid=" + Main
								.getWeatherAPIKey()))).getAsJsonObject().get("list").getAsJsonArray().get(2).getAsJsonObject();
					temp = json.get("temp").getAsJsonObject();
					forecast =
						"~" + Math.round((temp.get("day").getAsFloat() - 273.15F) * 10) / 10 + "°C, " + json.get("weather").getAsJsonArray().get(0)
							.getAsJsonObject().get("description").getAsString() + ", " + json.get("clouds").getAsInt() + "% clouds";
				} catch(IOException e) {
					Bukkit.getLogger().warning("IPInformation: Error doing the web stuff. Have you set the keys?");
				}
			}
		}
		cache.put(ip, this);
	}

	public TimeZone getTimeZone() {
		return timezone;
	}

	public String getCountry() {
		return country;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getRegion() {
		return region;
	}

	public String getCity() {
		return city;
	}

	public String getInetProvider() {
		return inetProvider;
	}

	public String getWeather() {
		return weather;
	}

	public String getWeatherForecast() {
		return forecast;
	}

	public String getNews() {
		return news;
	}

	protected static String getStringFromURL(URL url, String[]... requestProps) throws IOException {
		URLConnection connection = url.openConnection();
		for(String[] requestProp : requestProps)
			connection.addRequestProperty(requestProp[0], requestProp[1]);
		connection.connect();
		StringBuilder builder = new StringBuilder();
		try(InputStream stream = connection.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			String line;
			while((line = reader.readLine()) != null)
				builder.append(line);
		}
		return builder.toString();
	}
}