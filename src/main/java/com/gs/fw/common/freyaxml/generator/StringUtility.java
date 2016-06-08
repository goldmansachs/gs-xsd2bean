/*
Copyright 2016 Goldman Sachs.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package com.gs.fw.common.freyaxml.generator;

import java.util.*;

public class StringUtility
{
    public static String[] javaKeywords = {"abstract","assert","boolean","break","byte","case","catch","char","class","const","continue","default","do","double","else","enum","extends","final","finally","float","for","goto","if","implements","import","instanceof","int","interface","long","native","new","package","private","protected","public","return","short","static","strictfp","super","switch","synchronized","this","throw","throws","transient","try","void","volatile","while","false","null","true"};

    private static HashSet<String> keywordSet = new HashSet<String>();

    static
    {
        for(String s: javaKeywords)
        {
            keywordSet.add(s);
        }
    }

    public static String toJavaIdentifierCamelCase(String tmp)
    {
        StringBuilder builder = new StringBuilder(tmp.length());
        boolean mustUpperCase = true;
        for(int i=0;i<tmp.length();i++)
        {
            char c = tmp.charAt(i);
            if (c == '_')
            {
                mustUpperCase = true;
            }
            else if (Character.isJavaIdentifierPart(c))
            {
                if (mustUpperCase)
                {
                    c = Character.toUpperCase(c);
                    mustUpperCase = false;
                }
                builder.append(c);
            }
            else
            {
                mustUpperCase = true;
            }
        }
        return builder.toString();
    }

    public static String toJavaVariableName(String tmp)
    {
        String result = firstLetterToLower(toJavaIdentifierCamelCase(tmp));
        if (keywordSet.contains(result))
        {
            result = result+"_";
        }
        return result;
    }

    public static String firstLetterToLower(String tmp)
    {
        return tmp.substring(0, 1).toLowerCase() + tmp.substring(1);
    }

    public static String firstLetterToUpper(String tmp)
    {
        return tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
    }

    public static String removeLastCharacter(String tmp)
    {
        return StringUtility.removeLastCharacter(tmp, 1);
    }

    public static String removeLastCharacter(String tmp, int numberOfCharacters)
    {
        if (tmp.length() < numberOfCharacters)
        {
            return "";
        }
        return tmp.substring(0, tmp.length() - numberOfCharacters);
    }

    public static String trimPackage(String className)
    {
        return className.substring(className.lastIndexOf('.') + 1, className.length());
    }

    public static String replaceStr(String source, String pattern, String with)
    {
        // checking this in order to avoid indefinite loop
        if (pattern.length() == 0)
        {
            return source;
        }
        int i = 0;
        StringBuffer sb = new StringBuffer(source);
        do
        {
            i = sb.toString().indexOf(pattern, i);
            if (i >= 0)
            {
                sb.replace(i, i + pattern.length(), with);
                i += with.length();
            }
        } while (i >= 0);
        return sb.toString();
    }

    public static boolean equalsIgnoreCaseFirstLetter(String aName, String bName)
    {
        if (aName.length() != bName.length()) return false;
        if (!aName.substring(0, 1).toUpperCase().equals(bName.substring(0, 1).toUpperCase())) return false;
        return (aName.substring(1).compareToIgnoreCase(bName.substring(1)) == 0);
    }

    public static boolean vectorContainsIgnoreCaseFirstLetter(Vector v, String value)
    {
        for (int i = 0; i < v.size(); i++)
        {
            String s = (String) v.elementAt(i);
            if (equalsIgnoreCaseFirstLetter(s, value)) return true;
        }

        return false;
    }

    public static boolean hashMapContainsKeyIgnoreCaseFirstLetter(HashMap hm, String value)
    {
        Set keys = hm.keySet();
        for (Iterator it = keys.iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            if (equalsIgnoreCaseFirstLetter(key, value)) return true;
        }

        return false;
    }

    public static boolean listContainsIgnoreCaseFirstLetter(List l, String value)
    {
        for (Iterator it = l.iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            if (equalsIgnoreCaseFirstLetter(key, value)) return true;
        }

        return false;
    }

    public static boolean arrayListContainsIgnoreCaseFirstLetter(ArrayList list, String value)
    {
        for (Iterator it = list.iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            if (equalsIgnoreCaseFirstLetter(key, value)) return true;
        }

        return false;
    }

    public static String toCamelCaseIgnoringLastChar(String string, String delimiter, boolean upperFirst)
    {
        String trimmed = string.trim();
        String[] delimited;

        if (delimiter.length() > 0)
        {
            delimited = trimmed.split(delimiter);
        }
        else
        {
            delimited = new String[1];
            delimited[0] = string;
        }

        String result = "";

        for (int i = 0; i < delimited.length; i++)
        {
            /* Break up the string for camelcasing */

            delimited[i] = toCamelCasePresevingExisting(delimited[i]);

            if ((i == 0) & !upperFirst)
            {
                delimited[i] = firstLetterToLower(delimited[i]);
            }
            else
            {
                delimited[i] = firstLetterToUpper(delimited[i]);
            }

            if ((i == delimited.length - 1) && (delimited[i].length() == 1))
            {
                break;
            }

            result += delimited[i];
        }

        return result;
    }

    public static String toCamelCasePresevingExisting(String s)
    {
        char[] result = new char[s.length()];
        for (int i = 0; i < s.length(); i++)
        {
            boolean uppercaseAllowed = (i == 0) || (Character.isLowerCase(s.charAt(i - 1)));
            result[i] = s.charAt(i);
            if (!uppercaseAllowed)
            {
                result[i] = Character.toLowerCase(result[i]);
            }
        }
        return new String(result);
    }

    public static String englishPluralize(String tmp)
    {
        char last = tmp.charAt(tmp.length() - 1);
        if (last == 's' || last == 'x')
        {
            return tmp + "es";
        }
        if (last == 'y' && tmp.length() > 1)
        {
            char secondLast = tmp.charAt(tmp.length() - 2);
            if (isConsonant(secondLast))
            {
                return tmp.substring(0, tmp.length() - 1)+"ies";
            }
        }
        if (last == 'o' && tmp.length() > 1)
        {
            char secondLast = tmp.charAt(tmp.length() - 2);
            if (isConsonant(secondLast))
            {
                return tmp + "es";
            }
        }
        return tmp + 's';
    }

    private static boolean isConsonant(char secondLast)
    {
        return secondLast != 'a' && secondLast != 'e' && secondLast != 'i' && secondLast != 'o' && secondLast != 'u';
    }

    public static String escapeJavaString(String str)
    {
        StringBuilder builder = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++)
        {
            char ch = str.charAt(i);

            // handle unicode
            if (ch > 0xfff)
            {
                builder.append("\\u" + Integer.toHexString(ch).toUpperCase(Locale.ENGLISH));
            }
            else if (ch > 0xff)
            {
                builder.append("\\u0" + Integer.toHexString(ch).toUpperCase(Locale.ENGLISH));
            }
            else if (ch > 0x7f)
            {
                builder.append("\\u00" + Integer.toHexString(ch).toUpperCase(Locale.ENGLISH));
            }
            else if (ch < 32)
            {
                switch (ch)
                {
                    case '\b':
                        builder.append('\\');
                        builder.append('b');
                        break;
                    case '\n':
                        builder.append('\\');
                        builder.append('n');
                        break;
                    case '\t':
                        builder.append('\\');
                        builder.append('t');
                        break;
                    case '\f':
                        builder.append('\\');
                        builder.append('f');
                        break;
                    case '\r':
                        builder.append('\\');
                        builder.append('r');
                        break;
                    default:
                        if (ch > 0xf)
                        {
                            builder.append("\\u00" + Integer.toHexString(ch).toUpperCase(Locale.ENGLISH));
                        }
                        else
                        {
                            builder.append("\\u000" + Integer.toHexString(ch).toUpperCase(Locale.ENGLISH));
                        }
                        break;
                }
            }
            else
            {
                switch (ch)
                {
                    case '"':
                        builder.append('\\');
                        builder.append('"');
                        break;
                    case '\\':
                        builder.append('\\');
                        builder.append('\\');
                        break;
                    default:
                        builder.append(ch);
                        break;
                }
            }
        }
        return builder.toString();
    }
}
