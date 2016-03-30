package de.unidue.ltl.majoritytagger;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class Dictionary
{
    protected Map<String, String> map = new HashMap<String, String>();
    protected String defaultTag;
    
    public Dictionary(String defaultTag, String path) throws IOException{
        this.defaultTag = defaultTag;
        List<String> readLines = FileUtils.readLines(new File(path));
        for(String line : readLines){
            String[] split = line.split("\t");
            String word = split[0];
            String tag = split[1];
            
            map.put(word, tag);
        }
    }
    
    public String getTag(String token){
        String string = map.get(token);
        if(string!=null){
            return string;
        }
        return defaultTag;
    }

}
