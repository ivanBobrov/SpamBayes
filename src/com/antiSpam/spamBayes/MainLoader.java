package com.antiSpam.spamBayes;

import com.antiSpam.spamBayes.bayesSpamFilter.BayesSpamFilter;
import com.antiSpam.spamBayes.bayesSpamFilter.BayesSpamFilterException;
import com.antiSpam.spamBayes.utils.Dictionary;
import com.antiSpam.spamBayes.utils.io.JSONFileReader;

import java.io.IOException;
import java.util.Iterator;


public class MainLoader {
    public static void main(String args[]) {
        try {
            SpamFilter spamFilter = new BayesSpamFilter("ham.json", "spam.json");

            try {
                System.out.print("Loading filter...");
                spamFilter.loadSpamFilter();
            } catch (IOException loadingException) {
                System.out.print("\rCan't load filter. Building...");

                Dictionary.getInstance().setPreprocessEnabled(true);
                spamFilter.build();
                /*try {
                    System.out.print("\rSaving filter...");
                    spamFilter.storeSpamFilter();
                } catch (IOException storeException) {
                    storeException.printStackTrace();
                    System.exit(1);
                }*/
            }

            System.out.println("\rClassifying");

            try {
                performTest(spamFilter, 100000, 200000);
            } catch (IOException exception) {
                exception.printStackTrace();
                System.exit(1);
            }

        } catch (BayesSpamFilterException exception) {
            exception.printStackTrace();
        }
    }

    private static void performTest(SpamFilter spamFilter, int fromMessage, int toMessage)
            throws IOException, BayesSpamFilterException {
        Iterator<String> fileReader = new JSONFileReader("spam.json", fromMessage, toMessage);

        double spam = 0, ham = 0;
        int postCount = 0;
        double spamPercent = 0, hamPercent = 100;
        double level = 0.5;
        double timeStamp, timeSpent = 0;

        while (fileReader.hasNext()) {
            timeStamp = System.currentTimeMillis();

            String text = fileReader.next();
            if (spamFilter.check(text)) {
                spam++;
            } else {
                ham++;
            }
            postCount++;

            timeStamp -= System.currentTimeMillis();
            timeSpent += -timeStamp;

            double percentDone = (double)postCount*100 / (double)(toMessage - fromMessage);
            spamPercent = spam * 100 / (double)postCount;
            hamPercent = ham * 100 / (double)postCount;
            if (postCount % 1000 == 0) {
                System.out.print("\rspam: " + spamPercent + "% | ham: " + hamPercent +
                        "% | done: " + percentDone + "%");
            }
        }

        String out = String.format("\rspam: %.1f %% | ham %.1f %% | level: %e | Messages in second: %f | messages: %d\n",
                spamPercent,
                hamPercent,
                level,
                postCount * 1000 / timeSpent,
                postCount);
        System.out.print(out);
    }

}
