package com.odkl.moderation.text.server.domain.bayes;

import com.odkl.moderation.text.server.domain.bayes.filter.BayesSpamFilterException;
import com.odkl.moderation.text.server.domain.bayes.io.JSONFileReader;

import java.io.IOException;
import java.util.Iterator;

public class MainLoader {

        public static final int TO_MESSAGE_FOR_TRAINING = 51200;

        public static void main(String args[]) {
                try {
                        SpamFilterFactory filterFactory = new SpamFilterFactory();
                        System.out.print("\rBuilding...");

                        Iterator<String> hamFileIterator = new JSONFileReader("ham.json", 0, TO_MESSAGE_FOR_TRAINING);
                        Iterator<String> spamFileIterator = new JSONFileReader("spam.json", 0, TO_MESSAGE_FOR_TRAINING);
                        SpamFilter spamFilter = filterFactory.createSpamFilter(hamFileIterator, spamFileIterator);

                        //String[] ham = {"Привет!", null, "До свидания!"};
                        //String[] spam = {"Купи слона!", "Не покупай слона", "Путин наш Бог"};
                        //SpamFilter spamFilter = filterFactory.createSpamFilter(ham, spam);

                        //boolean answer = spamFilter.check("Привет. А ты, купил слона??!");

                        //System.out.println(answer);

            /*try {
                JSONFileReader reader = new JSONFileReader("spam.json");
                while (reader.hasNext()) {
                    spamFilter.reportSpam(reader.next());
                }
            } catch(IOException exception) {
                exception.printStackTrace();
                System.exit(1);
            }*/

                        System.out.print("\rClassifying");

                        /*double[] levels = {1e-150, 1e-140, 1e-100, 1e-60, 1e-40, 1e-30, 1e-20, 1e-10, 1e-5, 1e-2, 0.1, 0.5,
                                1 - 1e-10, 1 - 1e-12, 1 - 1e-15, 1 - 1e-20};*/
                        //for (double level : levels) {
                        performTest(spamFilter, 100000, 200000, 0.5);
                        //}
                        //System.out.println("Dictionary size: " + Dictionary.getInstance().getDictionarySize());

                } catch (BayesSpamFilterException exception) {
                        exception.printStackTrace();
                } catch (IOException ioException) {
                        ioException.printStackTrace();
                }
        }

        private static void performTest(SpamFilter spamFilter, int fromMessage, int toMessage, double level)
                throws IOException, BayesSpamFilterException {
                Iterator<String> fileReader = new JSONFileReader("spam.json", fromMessage, toMessage);

                double spam = 0, ham = 0;
                int postCount = 0;
                double spamPercent = 0, hamPercent = 100;
                double timeStamp, timeSpent = 0;

                while (fileReader.hasNext()) {
                        timeStamp = System.currentTimeMillis();

                        String text = fileReader.next();
                        if (spamFilter.check(text, level)) {
                                spam++;
                        } else {
                                ham++;
                        }
                        postCount++;

                        timeStamp -= System.currentTimeMillis();
                        timeSpent += -timeStamp;

                        double percentDone = (double) postCount * 100 / (double) (toMessage - fromMessage);
                        spamPercent = spam * 100 / (double) postCount;
                        hamPercent = ham * 100 / (double) postCount;
                        if (postCount % 1000 == 0) {
                                System.out.print("\rspam: " + spamPercent + "% | ham: " + hamPercent +
                                        "% | done: " + percentDone + "%");
                        }
                }

                String out = String
                        .format("\rspam: %.1f %% | ham %.1f %% | level: %e | Messages in second: %f | messages: %d\n",
                                spamPercent,
                                hamPercent,
                                level,
                                postCount * 1000 / timeSpent,
                                postCount);
                System.out.print(out);
        }

}
