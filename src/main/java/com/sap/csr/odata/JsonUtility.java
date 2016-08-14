package com.sap.csr.odata;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.stream.JsonReader;

//provide some static utils to read the array from an string. Here we use the gson because it is used by Olingo
public class JsonUtility {

	static public Map<String, Object> readMapFromString(String content) throws IOException {
		JsonReader reader = new JsonReader( new StringReader(content));
		return readMap(reader);
	}
	
	static public List<Map<String, Object>> readMapArrayFromString(String content) throws IOException {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		JsonReader reader = new JsonReader( new StringReader(content));
		reader.beginArray();
	    while (reader.hasNext()) {
	       list.add( readMap(reader));
	    }
	    reader.endArray();
		return list;
	}

	//??now just return as string, need revise later
	static private  Map<String, Object> readMap(JsonReader reader) throws IOException {
		Map <String, Object> map = new HashMap<String, Object>();

	     reader.beginObject();
	     while (reader.hasNext()) {
	       String name = reader.nextName();
	       map.put( name, reader.nextString());
	     }
	     reader.endObject();
	     return map;
	}
	
	public static void main(String []args) {
//		String json = "{\"name\": \"lucky\"}";
		String json="[ {\"name\": \"lucky\", \"age\": \"12\"}, {\"name\": \"lucky2\"}]";
		try {
			List<Map<String, Object>> list = readMapArrayFromString(json);
			for (Map<String,Object> map:  list) {
				System.out.println(map);
			}
			
//			Map<String, Object>  map = readMapFromString(json);
//			System.out.println(map.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
}
	
	
