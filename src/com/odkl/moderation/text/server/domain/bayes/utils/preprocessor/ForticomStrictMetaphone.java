package com.odkl.moderation.text.server.domain.bayes.utils.preprocessor;

import java.util.Arrays;

public class ForticomStrictMetaphone implements SpamTextPreprocessor {

    enum RawStringType {Numeric, Cyrilic, Latin}

    static final char[] VOWELS = "AOUIEАОУЫИЭЕЮЯЁ".toCharArray();

    static {
        Arrays.sort(VOWELS);
    }

    private final int maxKeyLength = 100;
    private final int maxVowel = 2;
    private final boolean leaveDigits = true;

    @Override
    public String convert(String text) {
        return encode(text);
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

    private String encodeMetaphone(StringBuilder string, RawStringType rawStringType) {
        boolean cyrillic = rawStringType == RawStringType.Cyrilic;
        final StringBuilder result = new StringBuilder(string.length());
        int pos = 0;
        int vowels = 1;

        char currentChar = string.charAt(pos);
        switch (currentChar) {
        case 'Y':
            if (isNext(string, pos, 'I')) {
                result.append('I');
                pos += 2;
                ++vowels;
            } else if (isNext(string, pos, 'E')) {
                result.append('J').append('I');
                pos += 2;
                ++vowels;
            } else if (isNext(string, pos, 'A')) {
                result.append('J').append('A');
                pos += 2;
                ++vowels;
            } else if (isNext(string, pos, 'U')) {
                result.append('J').append('U');
                pos += 2;
                ++vowels;
            }
            break;
        case 'Э':
            result.append('I');
            ++pos;
            break;
        case 'Я':
            result.append('J').append('A');
            ++pos;
            ++vowels;
            break;
        case 'Ю':
            result.append('J').append('U');
            ++pos;
            ++vowels;
            break;
        case 'Е':
            result.append('J').append('I');
            ++pos;
            ++vowels;
            break;
        case 'Ё':
            result.append('J').append('O');
            ++pos;
            ++vowels;
            break;
        case 'Й':
            result.append('J');
            ++pos;
            break;
        case 'J':
            if (isNext(string, pos, 'E')) {
                result.append('J').append('I');
                pos += 2;
                ++vowels;
            } else if (isNext(string, pos, 'A')) {
                result.append('J').append('A');
                pos += 2;
                ++vowels;
            } else if (isNext(string, pos, 'U')) {
                result.append('J').append('U');
                pos += 2;
                ++vowels;
            } else if (isNext(string, pos, 'O')) {
                result.append('J').append('O');
                pos += 2;
                ++vowels;
            }
            break;
        case 'Д':
        case 'D':
            result.append('D');
            ++pos;
            vowels = 0;
            break;
        case 'Г':
        case 'G':
            result.append('G');
            ++pos;
            vowels = 0;
            break;
        case 'В':
        case 'V':
            result.append('V');
            ++pos;
            vowels = 0;
            break;

        default:
            if (leaveDigits && Character.isDigit(currentChar)) {
                result.append(currentChar);
                ++pos;
            }
            vowels = 0;
        }

        for (; pos < string.length() && result.length() < maxKeyLength; ++pos) {
            currentChar = string.charAt(pos);
            switch (currentChar) {
                /* Русские */
            case 'К':
            case 'Г':
                result.append('K');
                break;
            case 'Н':
                result.append('N');
                break;
            case '6':
                if (leaveDigits) {
                    result.append('6');
                }
                break;
            case 'Ш':
            case 'Щ':
                result.append('S');
                break;
            case 'Х':
                result.append('H');
                break;
            case 'Ф':
            case 'В':
                result.append('F');
                break;
            case 'П':
                result.append('P');
                break;
            case 'Р':
                result.append('R');
                break;
            case 'Л':
                result.append('L');
                break;
            case 'З':
                result.append('Z');
                break;
            case 'Ж':
                result.append('Z').append('H');
                break;
            case '4':
                if (leaveDigits) {
                    result.append('4');
                }
                break;
            case 'Ц':
            case 'Ч':
                result.append('C');
                break;
            case 'С':
                result.append('S');
                break;
            case 'М':
                result.append('M');
                break;
            case 'Д':
            case 'Т':
                result.append('T');
                break;
            case 'Б':
                result.append('B');
                break;

                /* Латиница */
            case 'J':
                if (vowels < maxVowel) {
                    ++vowels;
                    if (isNext(string, pos, 'E')) {
                        result.append('E');
                        ++pos;
                    } else if (isNext(string, pos, 'A')) {
                        if (pos + 2 == string.length()) {
                            result.append("JA");
                            ++vowels;
                        } else {
                            result.append('O');
                        }
                        ++pos;
                    } else if (isNext(string, pos, 'U')) {
                        result.append('U');
                        ++pos;
                    } else if (isNext(string, pos, 'O')) {
                        result.append('E');
                        ++pos;
                    } else if (pos + 1 < string.length()) {
                        result.append('I');
                    }
                }
                break;
            case 'Q':
                result.append('K');
                break;
            case 'W':
                result.append('F');
                break;
            case 'R':
                result.append('R');
                break;
            case 'T':
                if (isNext(string, pos, 'H')) {
                    ++pos;
                }
                result.append('T');
                break;
            case 'P':
                if (isNext(string, pos, 'H')) {
                    result.append('F');
                    ++pos;
                } else {
                    result.append('P');
                }
                break;
            case 'S':
                result.append('S');
                if (isNext(string, pos, 'C') && isNext(string, pos + 1, 'H')) {
                    pos += 2;
                }
                break;
            case 'D':
                result.append('T');
                break;
            case 'F':
                result.append('F');
                break;
            case 'G':
                if (isNext(string, pos, 'H')) {
                    ++pos;
                }
                result.append('K');
                break;
            case 'H':
                if (cyrillic) {
                    result.append('N');
                } else if (prevNot(string, pos, 'S')) {
                    result.append('H');
                }
                break;
            case 'K':
                result.append('K');
                break;
            case 'L':
                result.append('L');
                break;
            case 'Z':
                result.append('Z');
                if (isNext(string, pos, 'H')) {
                    result.append('H');
                    ++pos;
                }
                break;
            case 'X':
                result.append("KS");
                break;
            case 'C':
                if (cyrillic) {
                    result.append('S');
                } else if (isNext(string, pos, 'H')) {
                    result.append('C');
                    ++pos;
                } else {
                    result.append('K');
                }
                break;
            case 'V':
                result.append('F');
                break;
            case 'B':
                result.append('B');
                break;
            case 'N':
                if (isNext(string, pos, 'H')) {
                    ++pos;
                }
                result.append('N');
                break;
            case 'M':
                result.append('M');
                break;
            default:
                if (leaveDigits && Character.isDigit(currentChar)) {
                    result.append(currentChar);

                } else if (vowels < maxVowel) {
                    switch (currentChar) {
                    /**/
                    case 'Ё':
                        result.append('E');
                        ++vowels;
                        break;
                    /**/
                    case 'Е':
                    /**/
                    case 'И':
                        if (isNext(string, pos, 'Е')) {
                            result.append("IE");
                            vowels += 2;
                            ++pos;
                            break;
                        }
                    case 'E':
                        if (isNext(string, pos, 'J')) {
                            result.append("II");
                            vowels += 2;
                            ++pos;
                            break;
                        }
                    /**/
                    case 'Й':
                        if (pos + 1 == string.length()) {
                            break;
                        }
                    /**/
                    case 'І':
                    case 'I':
                        result.append('I');
                        ++vowels;
                        break;
                    /**/
                    case 'У':
                    /**/
                    case 'Ю':
                    case 'U':
                        result.append('U');
                        ++vowels;
                        break;
                    /**/
                    case 'Я':
                        if (pos + 1 == string.length()) {
                            result.append("JA");
                            vowels += 2;
                            break;
                        }
                    /**/
                    case 'А':
                    case 'A':
                        result.append(pos + 1 == string.length() ? 'A' : 'O');
                        ++vowels;
                        break;
                    case '0':
                    /**/
                    case 'О':
                    case 'O':
                        result.append('O');
                        ++vowels;
                        break;
                    case 'Y':
                        if (isNext(string, pos, 'A')) {
                            result.append(pos + 2 == string.length() ? "JA" : "O");
                            ++vowels;
                            ++pos;
                        } else if (isNext(string, pos, 'E')) {
                            result.append('E');
                            ++vowels;
                            ++pos;
                        } else if (isNext(string, pos, 'O')) {
                            result.append('E');
                            ++vowels;
                        } else if (pos + 1 == string.length()) {
                            result.append('I');
                            ++vowels;
                        }
                        break;
                    }
                }
            }
        }
        return result.toString();
    }

    protected boolean isVowel(CharSequence string, int pos) {
        if (pos < 0 || pos >= string.length()) {
            return false;
        }
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
