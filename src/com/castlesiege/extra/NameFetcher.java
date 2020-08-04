package com.castlesiege.extra;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.castlesiege.Main;
import com.google.common.collect.ImmutableList;

public class NameFetcher implements Callable<Map<UUID, String>> {
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private final JSONParser jsonParser = new JSONParser();
    private final Main plugin;
    private final List<UUID> uuids;
    
    public NameFetcher(Main plugin, List<UUID> uuids) {
    	this.plugin = plugin;
        this.uuids = ImmutableList.copyOf(uuids);
    }

    @Override
    public Map<UUID, String> call() throws Exception {
        Map<UUID, String> uuidStringMap = new HashMap<UUID, String>();
        
        List<UUID> uuidCache = new ArrayList<UUID>();
        uuidCache.addAll(uuids);
        if(!plugin.datahandler.cachedNames.isEmpty())
        {
        	for(UUID uuid : uuids)
        	{
        		if(plugin.datahandler.cachedNames.containsKey(uuid))
        		{
        			uuidCache.remove(uuid);
        			uuidStringMap.put(uuid, plugin.datahandler.cachedNames.get(uuid));
        		}
        	}
        }
        
        if(uuidCache.isEmpty()) return uuidStringMap;
        
        for (UUID uuid : uuidCache) {
            HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL+uuid.toString().replace("-", "")).openConnection();
            JSONObject response = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            String name = (String) response.get("name");
            if (name == null) {
                continue;
            }
            String cause = (String) response.get("cause");
            String errorMessage = (String) response.get("errorMessage");
            if (cause != null && cause.length() > 0) {
                throw new IllegalStateException(errorMessage);
            }
            uuidStringMap.put(uuid, name);
            plugin.datahandler.cachedNames.put(uuid, name);
        }
        return uuidStringMap;
    }
}