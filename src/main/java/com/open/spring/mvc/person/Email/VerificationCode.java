package com.open.spring.mvc.person.Email;

import java.time.Instant;
import java.util.ArrayList;

public class VerificationCode {
    public static char RandomCharacter(){
        char random = (char)(int)(Math.random()*43 + 48);
        while((int)random >= 58 && (int)random <= 64){
            random = (char)(int)(Math.random()*43 + 48);
        }
        
        return random;
    }

    public static ArrayList<String[]> codes = new ArrayList<String[]>();

    public static String GenerateVerificationCode(String uid){
        for (int i = 0; i< codes.size(); i++) { //remove expired codes
            String[] array = codes.get(i);
            if(Instant.now().isAfter(Instant.parse(array[2]))){
                codes.remove(i);
                continue;
            }
        }

        if (codes.stream().anyMatch(code -> uid.equals(code[0]))){ //return null if a code has already been sent
            return null;
        }

        //generate temporary code
        boolean different = false;
        String code = "";
        while(different == false){ 
            for (int i=0; i<6; i++){
                code += RandomCharacter();
            };
            different = true;
            for (String[] strings : codes) {
                if( strings[1] == code){
                    different = false;
                    break;
                }
            }
        }

        //create an array to store temporary code
        String[] array = new String[3];
        array[0] = uid;
        array[1] = code;
        array[2] = Instant.now().plusSeconds(60*5).toString();// set expiration to 5 minutes after now

        //add the array to the ArrayList of temporary codes
        codes.add(array);

        return code;
    }

    public static String getCodeForUid(String uid){
        for (int i = 0; i< codes.size(); i++) { 
            String[] array = codes.get(i);
            if(Instant.now().isAfter(Instant.parse(array[2]))){ //remove expired codes
                codes.remove(i);
                continue;
            }
            if(uid.equals(array[0])){
                return array[1];
            }
        }
        return null; //not found
    }

    public static void removeCodeByUid(String uid){
        for (int i = 0; i< codes.size(); i++) { 
            String[] array = codes.get(i);
            if(Instant.now().isAfter(Instant.parse(array[2]))){ //remove expired codes
                codes.remove(i);
                continue;
            }
            if(uid.equals(array[0])){
                codes.remove(i);
            }
        }
    }
}
