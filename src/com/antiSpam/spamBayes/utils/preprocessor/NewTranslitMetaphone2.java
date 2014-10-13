package com.antiSpam.spamBayes.utils.preprocessor;

import java.util.Arrays;


public class NewTranslitMetaphone2 implements SpamTextPreprocessor {
    private final boolean leaveDigits = true;

    enum RawStringType {Numeric, Cyrilic, Latin}

    static final char[] VOWELS = "AOUIEАОУЫИЭЕЮЯЁ".toCharArray();
    static {
        Arrays.sort(VOWELS);
    }

    @Override
    public String convert(String text) {
        return encode(text);
    }

    public String encode(String rawString) {
        if (rawString == null || rawString.isEmpty()) {
            return rawString;
        }
        StringBuilder string = new StringBuilder(rawString.length());
        RawStringType type = prepare(rawString, string);

        if (string.length() == 0) {
            return "";
        } else if (type == RawStringType.Numeric) {
            return string.toString();
        }
        return encodeMetaphone(string, type);
    }

    protected RawStringType prepare(String rawString, StringBuilder string) {
        boolean cyrillic = false;
        boolean onlyDigits = true;
        char prev = '\0';
        for (int pos = 0; pos < rawString.length(); ++pos) {
            final char curr = Character.toUpperCase(rawString.charAt(pos));

            if ('0' <= curr && curr <= '9') {
                string.append(curr);
                prev = curr;
            } else if (curr == ' ') { //Space
                string.append(curr);
                prev = curr;
            } else if (curr == '~') { //All strange symbols
                string.append(curr);
                prev = curr;
            } else if (Character.isLetter(curr) && (curr != prev)) {
                string.append(curr);
                prev = curr;

                if (!cyrillic && isCyrillic(curr)) {
                    cyrillic = true;
                }
                if (onlyDigits) {
                    onlyDigits = false;
                }
            }
        }
        return onlyDigits ? RawStringType.Numeric
                : (cyrillic ? RawStringType.Cyrilic : RawStringType.Latin);
    }

    protected String encodeMetaphone(StringBuilder string, RawStringType rawStringType) {
        boolean cyrillic = rawStringType == RawStringType.Cyrilic;
        final StringBuilder result = new StringBuilder(string.length());
        for (int pos = 0; pos < string.length(); ++pos) {
            char currentChar = string.charAt(pos);
            switch (currentChar) {
                /* Русские */
                case 'K': if (isNext(string, pos, 'H')) { result.append('H'); ++pos; break;}
                    /**/case 'К': result.append('K'); break;
                case 'G':
                    /**/case 'Г':
                case 'Ґ':
                    result.append('G'); break;
                /**/case 'Н':
                case 'N': result.append('N'); break;
                case '6':
                    /**/case 'С': result.append('S'); break;
                /**/case 'Ш': result.append("SH"); break;
                /**/case 'Щ': result.append("SH"); break;
                case 'S': if (isNext(string, pos, 'C') && isNext(string, pos + 1, 'H')) {result.append("SH"); pos+=2; }
                else if (isNext(string, pos, 'C'))                                 {result.append("SC"); ++pos; }
                else if (isNext(string, pos, 'H'))                                 {result.append("SH"); ++pos; }
                else                                                               { result.append('S'); }
                    break;
                case 'C': if (cyrillic || string.length() == 1 ) { result.append('S'); }
                else if (isNext(string, pos, 'H')) { result.append("CH"); ++pos;}
                else /*if (pos + 1 < string.length() )*/                              { result.append('C'); }
                    break;
                /**/case 'Ч':
                case '4': result.append((!cyrillic || isNextDigit(string, pos)) ? '4' : "CH"); break;
                /**/case 'Х': result.append('H'); break;
                case 'F':
                    /**/case 'Ф': result.append('F'); break;
                case 'W':
                case 'V':
                    /**/case 'В': result.append('V'); break;
                /**/case 'П': result.append('P'); break;
                case 'R':
                    /**/case 'Р': result.append('R'); break;
                case 'L':
                    /**/case 'Л': result.append('L'); break;
                /**/case 'З': result.append('Z'); break;
                /**/case 'Ж': result.append('J'); break;
                /**/case 'Ц': result.append('C'); break;
                case 'M':
                    /**/case 'М': result.append('M'); break;
                case 'D':
                    /**/case 'Д': result.append('D'); break;
                /**/case 'Т': result.append('T'); break;
                case 'B':
                    /**/case 'Б': result.append('B'); break;
                case 'I':
                /**/case 'І': // украинская
                /**/case 'И': result.append('I');  break;
                case 'Ї':
                /**/case 'Ы': result.append('Y'); break;
                case 'E':
                case 'Є':
                    /**/case 'Э': result.append('E'); break;
                /**/case 'Й': if (isNext(string, pos, 'А')) {result.append("YA"); ++pos;}
                else if (isNext(string, pos, 'Е')) {result.append("E"); ++pos;}
                else if (isNext(string, pos, 'О')) {result.append("E"); ++pos;}
                else                               {result.append('Y');}
                    break;
                /**/case 'У':
                case 'U': result.append('U'); break;
                /**/case 'А':
                case 'A': result.append('A'); break;
                /**/case 'О':
                    if (prevNot(string, pos, 'Ь')) { result.append('O');  break;}
                    // украинское ьо == ё
                    result.append("E"); break;
                case '0':
                case 'O': result.append('O'); break;
                /**/case 'Я': result.append("YA"); break;
                /**/case 'Е':
                /**/case 'Ё': result.append("E"); break;
                /**/case 'Ю': result.append("YU"); break;
                case 'Y': if (isNext(string, pos, 'A')) {result.append("YA"); ++pos;}
                else if (isNext(string, pos, 'E')) {result.append("E"); ++pos;}
                else if (isNext(string, pos, 'O')) {result.append("E"); ++pos;}
                else                               {result.append('Y');}
                    break;
                case 'J': if (isNext(string, pos, 'E')) { result.append("JE"); ++pos; }
                else if (isNext(string, pos, 'A')) { result.append("JA"); ++pos; }
                else if (isNext(string, pos, 'U')) { result.append("JU"); ++pos; }
                else if (isNext(string, pos, 'O')) { result.append("JO"); ++pos; }
                else                               { result.append('J'); }
                    break;
                case 'Q': result.append("KU"); break;
                case 'T': if (isNext(string, pos, 'H')) { ++pos;  result.append('T'); break; }
                    if (isNext(string, pos, 'S')) {++pos; result.append('C'); break; }
                    result.append('T'); break;
                case 'P': if (isNext(string, pos, 'H')) { result.append('F'); ++pos; }
                else                               { result.append('P'); }
                    break;
                case 'H':
                    if (cyrillic) {
                        result.append('N');
                    } else if (prevNot(string, pos, 'S') && prevNot(string, pos, 'Z')) {
                        result.append('H');
                    }
                    break;
                case 'Z': if (isNext(string, pos, 'H')) { result.append('J'); ++pos; break; }
                    result.append('Z'); break;
                case 'X': result.append("KS"); break;

                default: if ((leaveDigits && isDigit(currentChar)) || currentChar == ' ' || currentChar == '~') { result.append(currentChar); }

            }
        }
        return result.toString();
    }

    protected boolean isVowel(CharSequence string, int pos) {
        if (pos < 0 || pos >= string.length()) return false;
        return Arrays.binarySearch(VOWELS, string.charAt(pos)) >= 0;
    }

    protected boolean prevNot(CharSequence string, int pos, char c0) {
        return pos == 0 || string.charAt(pos - 1) != c0;
    }

    protected boolean isNext(CharSequence string, int pos, char c0) {
        return (pos + 1) < string.length() && string.charAt(pos + 1) == c0;
    }

    protected boolean isNextDigit(CharSequence string, int pos) {
        return (pos + 1) < string.length() && isDigit(string.charAt(pos + 1));
    }

    //Character.isDigit() is too international for us,
    //for example indian digits also digits, but users use it as beautifier
    protected boolean isDigit(char chr) {
        return '0' <= chr && chr <= '9';
    }

    public static boolean isCyrillic(char curr) {
        return 0x400 <= curr && curr < 0x600;
    }

}
