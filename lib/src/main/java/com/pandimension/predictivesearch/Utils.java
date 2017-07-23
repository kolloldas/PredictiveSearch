/*
 * Copyright 2017 Kollol Das
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pandimension.predictivesearch;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal static class containing utility methods that help the Predictor
 */
final class Utils {
    public static final int MAX_KEYS_NUM_KEYPAD = 10 + 3;
    public static final int MAX_KEYS_QWERTY_KEYPAD = 36 + 3;

    public static String mapToKeypad(String source, boolean includeSymbols, List<Integer> positions){
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for(char c: source.toLowerCase().toCharArray()){
            char cc = 0;
            switch(c){
                //case ' ': cc = '1'; break;
                case 'a': case 'á': case 'à': case 'â': case 'ä': case 'ã':
                case 'b': case 'c': case 'ç': cc = '2'; break;
                case 'd':
                case 'e': case 'é': case 'è': case 'ê': case 'ë':
                case 'f': cc = '3'; break;
                case 'g': case 'h':
                case 'i': case 'í': case 'î': cc = '4'; break;
                case 'j': case 'k': case 'l': cc = '5'; break;
                case 'm': case 'n': case 'ñ':
                case 'o': case 'ó': case 'ò':case 'ô' :case 'ö': case 'õ': cc = '6'; break;
                case 'p': case 'q': case 'r': case 's': cc = '7'; break;
                case 't':
                case 'u': case 'ú': case 'ù': case 'ü':
                case 'v': cc = '8'; break;
                case 'w': case 'x': case 'y': case 'z':cc = '9'; break;
                case '+': case '*': case '#': if(includeSymbols) cc = c; break;
                default: if(Character.isDigit(c)) cc = c;
            }

            if(cc != 0){
                sb.append(cc);
                if(positions != null)
                    positions.add(pos);
            }
            pos++;
        }

        return sb.toString();
    }

    public static String mapToAlphaNum(String source, boolean includeSymbols, List<Integer> positions){
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for(char c: source.toLowerCase().toCharArray()){
            char cc = 0;
            switch(c){
                case 'á': case 'à': case 'â': case 'ä': case 'ã': cc = 'a'; break;
                case 'ç': cc = 'c'; break;
                case 'é': case 'è': case 'ê': case 'ë': cc = 'e'; break;
                case 'í': case 'î': cc = 'i'; break;
                case 'ñ': cc = 'n'; break;
                case 'ó': case 'ò':case 'ô' :case 'ö': case 'õ': cc = 'o'; break;
                case 'ú': case 'ù': case 'ü': cc = 'u'; break;
                case '.': case ',': case '@': if(includeSymbols) cc = c; break;
                default: if(Character.isLetterOrDigit(c)) cc = c;
            }

            if(cc != 0){
                sb.append(cc);
                if(positions != null)
                    positions.add(pos);
            }
            pos++;
        }

        return sb.toString();
    }

    public static int mapKey(char key){
        switch(key){
            case '0': return 0;
            case '1': return 1;
            case '2': return 2;
            case '3': return 3;
            case '4': return 4;
            case '5': return 5;
            case '6': return 6;
            case '7': return 7;
            case '8': return 8;
            case '9': return 9;
            case '*': return 10;
            case '+': return 11;
            case '#': return 12;
            default: return -1;
        }
    }

    public static int mapAlphaNum(char key){
        if(key >= 'a' && key <= 'z') return key - 'a';
        if(key >= '0' && key <= '9') return key - '0' + 26;
        switch(key){
            case 'á': case 'à': case 'â': case 'ä': case 'ã': return 0;
            case 'ç': return 2;
            case 'é': case 'è': case 'ê': case 'ë': return 4;
            case 'í': case 'î': return 8;
            case 'ñ': return 13;
            case 'ó': case 'ò':case 'ô' :case 'ö': case 'õ': return 14;
            case 'ú': case 'ù': case 'ü': return 20;
            case ',': return 36;
            case '.': return 37;
            case '@': return 38;
            default: return -1;
        }
    }


    final static String FILLER = new String(new char[500]).replace("\0", "_");

    public static List<String> variantsOfText(String text, boolean includeNumbers){
        List<String> items = new ArrayList<>();
        if(includeNumbers)
            variantsOfText("", text, "[^a-zA-Z0-9]+", items);
        else
            variantsOfText("", text, "[^a-zA-Z]+", items);
        return items;
    }

    private static void variantsOfText(String prefix, String suffix, String pattern, List<String> results){
        results.add(prefix + suffix);

        String[] split = suffix.split(pattern, 2);
        if(split.length == 2 && !split[0].isEmpty() && !split[1].isEmpty()){
            String blank = FILLER.substring(0, suffix.length() - split[1].length());
            String initial = "" + split[0].charAt(0) + blank.substring(1);

            variantsOfText(prefix + blank, split[1], pattern, results);
            variantsOfText(prefix + initial, split[1], pattern, results);

            /* Uncomment if you want separations at non-alphabets */
            /*String blankedSuffix = split[1].replaceAll("[^a-zA-Z]", "_");
            variantsOfText(prefix + blank, blankedSuffix, pattern, results);
            variantsOfText(prefix + initial, blankedSuffix, pattern, results);*/
        }
    }

    final static int MAX_PREFIX_LEN = 4;

    public static List<String> variantsOfNumber(String number){
        List<String> items = new ArrayList<String>();

        items.add(number);
        if(number.length() > 4){
            // Since we don't assume any country codes, we'll add first 4 suffixes
            for(int i = 1; i < MAX_PREFIX_LEN; i++)
                items.add(FILLER.substring(0, i) + number.substring(i));

            // For long numbers add the last 4 numbers so that users can do a quick check
            if(number.length() >= 8) {
                int prefixLength = number.length() - 4;
                items.add(FILLER.substring(0, prefixLength) + number.substring(prefixLength));
            }
        }
        return items;
    }
}
