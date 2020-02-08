package org.golde.discordbot.supportserver.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class Database {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	private static List<SimpleUser> USERS = new ArrayList<SimpleUser>();
	private static List<UsernameCache> USERNAME_CACHE = new ArrayList<UsernameCache>();
	
	public static final String USERNAME_CACHE_FILE = "username-cache";
	public static final String USERS_FILE = "user-data";
	
	public static void saveToFile(List<?> list, String filename) {

		try {
			GSON.toJson(list, new OutputStreamWriter(new FileOutputStream(new File("res/" + filename + ".json")), StandardCharsets.UTF_8));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static <T> List<T> loadFromFile(String filename, Class<T[]> clazz) {
		
		try {
			T[] arr = new Gson().fromJson(new FileReader("res/" + filename + ".json"), clazz);
		    return new ArrayList<>(Arrays.asList(arr)); //Arrays.asList returns a fixed size list. Jolly. https://stackoverflow.com/questions/5755477/java-list-add-unsupportedoperationexception
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	
		return null;
		
	}
	
	public static void loadAllFromFile() {
		USERNAME_CACHE = loadFromFile(USERNAME_CACHE_FILE, UsernameCache[].class);
		//USERS = loadFromFile(USERS_FILE, SimpleUser[].class);
	}
	
	public static List<SimpleUser> getAllUsers() {
		return USERS;
	}
	
	public static SimpleUser getUser(long snowflake) {
		
		for(SimpleUser u : USERS) {
			if(u.getUser().equals(getUsernameCache(snowflake))) {
				return u;
			}
		}
		
		SimpleUser u = new SimpleUser(getUsernameCache(snowflake));
		USERS.add(u);
		return u;
		
	}
	
	public static UsernameCache getUsernameCache(long snowflake) {
		
		for(UsernameCache c : USERNAME_CACHE) {
			if(c.getSnowflake() == snowflake) {
				return c;
			}
		}
		
		UsernameCache c = new UsernameCache(snowflake);
		USERNAME_CACHE.add(c);
		saveToFile(USERNAME_CACHE, USERNAME_CACHE_FILE);
		return c;
		
	}
	
	public static void updateUsername(long snowflake, String name) {
		getUsernameCache(snowflake).setUsername(name);
		saveToFile(USERNAME_CACHE, USERNAME_CACHE_FILE);
	}
	
}
