package com.antiSpam.spamBayes;


import weka.core.Instances;
import weka.classifiers.bayes.NaiveBayes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SpamBayes {
    public static void main(String argv[]) {
        Instances data = null;
        Instances unlabeled = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("data.arff"));
            BufferedReader readerUnlabeled = new BufferedReader(new FileReader("data_test_unlabeled.arff"));

            data = new Instances(reader);
            unlabeled = new Instances(readerUnlabeled);

            reader.close();
            readerUnlabeled.close();

        } catch(FileNotFoundException exception) {
            System.out.println("File not found");
        } catch(IOException exception) {
            System.out.println("IOException");
        }


        data.setClassIndex(0);
        unlabeled.setClassIndex(0);
        NaiveBayes bayes = new NaiveBayes();
        try {
            bayes.buildClassifier(data);
            for (int i = 0; i < unlabeled.numInstances(); i++) {
                double clsLabel = bayes.classifyInstance(unlabeled.instance(i));
                unlabeled.instance(i).setClassValue(clsLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
