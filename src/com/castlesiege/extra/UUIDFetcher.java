package com.castlesiege.extra;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.castlesiege.Main;
import com.google.common.collect.ImmutableList;

public class UUIDFetcher implements Callable<Map<String, UUID>> {
    private static final double PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private final JSONParser jsonParser = new JSONParser();
    private final Main plugin;
    private final List<String> names;
    private final boolean rateLimiting;

    public UUIDFetcher(Main plugin, List<String> names, boolean rateLimiting) {
    	this.plugin = plugin;
        this.names = ImmutableList.copyOf(names);
        this.rateLimiting = rateLimiting;
    }

    public UUIDFetcher(Main plugin, List<String> names) {
        this(plugin, names, true);
    }

    public Map<String, UUID> call() throws Exception {
        Map<String, UUID> uuidMap = new HashMap<String, UUID>();
        
        List<String> namesCache = new ArrayList<String>();
        namesCache.addAll(names);
        if(!plugin.datahandler.cachedNames.isEmpty())
        {
        	for(String name : names)
        	{
        		for(UUID uuid : plugin.datahandler.cachedNames.keySet())
        		{
        			if(plugin.datahandler.cachedNames.get(uuid).equals(name))
        			{
        				namesCache.remove(name);
        				uuidMap.put(name, uuid);
        				break;
        			}
        		}
        	}
        }
        
        if(namesCache.isEmpty()) return uuidMap;
        
        int requests = (int) Math.ceil(namesCache.size() / PROFILES_PER_REQUEST);
        for (int i = 0; i < requests; i++) {
            HttpURLConnection connection = createConnection();
            String body = JSONArray.toJSONString(namesCache.subList(i * 100, Math.min((i + 1) * 100, namesCache.size())));
            writeBody(connection, body);
            JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            for (Object profile : array) {
                JSONObject jsonProfile = (JSONObject) profile;
                String id = (String) jsonProfile.get("id");
                String name = (String) jsonProfile.get("name");
                UUID uuid = UUIDFetcher.getUUID(id);
                uuidMap.put(name, uuid);
                plugin.datahandler.cachedNames.put(uuid, name);
            }
            if (rateLimiting && i != requests - 1) {
                Thread.sleep(100L);
            }
        }
        return uuidMap;
    }

    private static void writeBody(HttpURLConnection connection, String body) throws Exception {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    private static HttpURLConnection createConnection() throws Exception {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" +id.substring(20, 32));
    }

    public static byte[] toBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static UUID fromBytes(byte[] array) {
        if (array.length != 16) {
            throw new IllegalArgumentException("Illegal byte array length: " + array.length);
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        long mostSignificant = byteBuffer.getLong();
        long leastSignificant = byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    public static UUID getUUIDOf(Main plugin, String name) throws Exception {
        return new UUIDFetcher(plugin, Arrays.asList(name)).call().get(name);
    }
}