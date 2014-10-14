package com.odkl.moderation.text.server.domain.bayes.utils.preprocessor;

public class NonAlphabeticSymbolsPreprocessor implements SpamTextPreprocessor {
        private static final String REGEX = "[\\.|,|\"|\\?|!|:|–|\\)|\\(|\\*|\\[|\\]|\\||/|;|»|«|#|-|\u2014]+";

        @Override
        public String convert(String text) {
                //TODO: implement
                StringBuffer single = encodeSingleSymbol(text);
                StringBuffer result = encodeSpecialCharacters(single.toString());
                return result.toString().toUpperCase();
        }

        private StringBuffer encodeSpecialCharacters(String string) {
                String newStr = string.replaceAll(REGEX, "~");
                return new StringBuffer(newStr);
        }

        private StringBuffer encodeSingleSymbol(String string) {
                String[] words = string.split("\\s+");
                StringBuffer stringBuffer = new StringBuffer(string.length());

                //TODO: how will be better: with String or char?
                for (String word : words) {
                        if (isCyrillic(word)) {
                                for (int i = 0; i < word.length(); i++) {
                                        Character symbol = word.charAt(i);
                                        String replaceTo = MetaphoneMap.cyrillicSingleMap.get(symbol.toString());

                                        if (replaceTo != null) {
                                                stringBuffer.append(replaceTo);
                                        } else {
                                                stringBuffer.append(symbol);
                                        }
                                }
                                stringBuffer.append(" ");
                        } else {
                                stringBuffer.append(word).append(" ");
                        }
                }

                StringBuffer extra = new StringBuffer();
                for (int i = 0; i < stringBuffer.length(); i++) {
                        char charAt = stringBuffer.charAt(i);
                        if (!isCyrillic(charAt)) {
                                extra.append(charAt);
                        }
                }

                return stringBuffer;
        }

        private String encodeThreeSymbol(String string) {
                for (int i = 0; i < string.length() - 3; i++) {
                        String base = string.substring(i, i + 3).toUpperCase();
                        //TODO: implement
                }

                return string;
        }

        //Если в слове попался хотя бы один кириллический символ, то считаем кириллицей
        private boolean isCyrillic(StringBuffer text) {
                boolean cyrillic = false;
                for (int i = 0; i < text.length(); i++) {
                        char symbol = text.charAt(i);
                        if (isCyrillic(symbol)) {
                                cyrillic = true;
                                break;
                        }
                }

                return cyrillic;
        }

        private boolean isCyrillic(String text) {
                boolean cyrillic = false;
                for (int i = 0; i < text.length(); i++) {
                        char symbol = text.charAt(i);
                        if (isCyrillic(symbol)) {
                                cyrillic = true;
                                break;
                        }
                }

                return cyrillic;
        }

        private boolean isCyrillic(char curr) {
                return 0x410 <= curr && curr <= 0x44F;
        }

}
